package edu.dvc.volunteer;

import edu.dvc.domain.AccountRole;
import edu.dvc.domain.AccountStatus;
import edu.dvc.domain.AvailabilitySlot;
import edu.dvc.domain.AvailabilityStatus;
import edu.dvc.domain.Skill;
import edu.dvc.domain.UserAccount;
import edu.dvc.domain.VolunteerProfile;
import edu.dvc.domain.VolunteerSkill;
import edu.dvc.repository.AvailabilitySlotRepository;
import edu.dvc.repository.SkillRepository;
import edu.dvc.repository.UserAccountRepository;
import edu.dvc.repository.VolunteerProfileRepository;
import edu.dvc.repository.VolunteerSkillRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VolunteerManagementService {
    private final UserAccountRepository accounts;
    private final VolunteerProfileRepository profiles;
    private final VolunteerSkillRepository volunteerSkills;
    private final SkillRepository skills;
    private final AvailabilitySlotRepository slots;

    public VolunteerManagementService(UserAccountRepository accounts, VolunteerProfileRepository profiles,
                                      VolunteerSkillRepository volunteerSkills, SkillRepository skills,
                                      AvailabilitySlotRepository slots) {
        this.accounts = accounts;
        this.profiles = profiles;
        this.volunteerSkills = volunteerSkills;
        this.skills = skills;
        this.slots = slots;
    }

    @Transactional(readOnly = true)
    public VolunteerProfileDto getProfile(String email) {
        return toProfileDto(requireVolunteer(email));
    }

    @Transactional
    public VolunteerProfileDto updateProfile(String email, UpdateVolunteerProfileRequest request) {
        VolunteerProfile profile = requireVolunteer(email);
        UserAccount account = profile.getUser();
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone().trim());
        profile.setAddress(request.address() == null || request.address().isBlank() ? null : request.address().trim());
        return toProfileDto(profile);
    }

    @Transactional(readOnly = true)
    public List<VolunteerSkillDto> getSkills(String email) {
        VolunteerProfile profile = requireVolunteer(email);
        return volunteerSkills.findByVolunteerId(profile.getId()).stream().map(this::toSkillDto).toList();
    }

    @Transactional
    public VolunteerSkillDto addSkill(String email, SkillRequest request) {
        VolunteerProfile profile = requireVolunteer(email);
        Skill skill = findOrCreateSkill(request.name());
        if (volunteerSkills.existsByVolunteerIdAndSkillId(profile.getId(), skill.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This skill is already in your profile");
        }
        VolunteerSkill link = new VolunteerSkill();
        link.setVolunteer(profile);
        link.setSkill(skill);
        return toSkillDto(volunteerSkills.save(link));
    }

    @Transactional
    public VolunteerSkillDto renameSkill(String email, Long volunteerSkillId, SkillRequest request) {
        VolunteerProfile profile = requireVolunteer(email);
        VolunteerSkill link = requireOwnedSkill(profile, volunteerSkillId);
        Skill replacement = findOrCreateSkill(request.name());
        if (replacement.getId().equals(link.getSkill().getId())) return toSkillDto(link);
        if (volunteerSkills.existsByVolunteerIdAndSkillId(profile.getId(), replacement.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That skill is already in your profile");
        }
        link.setSkill(replacement);
        return toSkillDto(volunteerSkills.save(link));
    }

    @Transactional
    public void removeSkill(String email, Long volunteerSkillId) {
        VolunteerProfile profile = requireVolunteer(email);
        VolunteerSkill link = requireOwnedSkill(profile, volunteerSkillId);
        volunteerSkills.delete(link);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(String email) {
        VolunteerProfile profile = requireVolunteer(email);
        return toAvailabilityResponse(profile);
    }

    @Transactional
    public AvailabilityResponse updateAvailability(String email, AvailabilityStatus status) {
        VolunteerProfile profile = requireVolunteer(email);
        profile.setAvailability(status);
        return toAvailabilityResponse(profile);
    }

    @Transactional
    public AvailabilitySlotDto addAvailabilitySlot(String email, AvailabilitySlotRequest request) {
        VolunteerProfile profile = requireVolunteer(email);
        ensureNoOverlap(profile, request.startsAt(), request.endsAt(), null);
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setVolunteer(profile);
        slot.setStartsAt(request.startsAt());
        slot.setEndsAt(request.endsAt());
        return toSlotDto(slots.save(slot));
    }

    @Transactional
    public AvailabilitySlotDto updateAvailabilitySlot(String email, Long slotId, AvailabilitySlotRequest request) {
        VolunteerProfile profile = requireVolunteer(email);
        AvailabilitySlot slot = requireOwnedSlot(profile, slotId);
        ensureNoOverlap(profile, request.startsAt(), request.endsAt(), slotId);
        slot.setStartsAt(request.startsAt());
        slot.setEndsAt(request.endsAt());
        return toSlotDto(slots.save(slot));
    }

    @Transactional
    public void removeAvailabilitySlot(String email, Long slotId) {
        VolunteerProfile profile = requireVolunteer(email);
        slots.delete(requireOwnedSlot(profile, slotId));
    }

    @Transactional(readOnly = true)
    public List<VolunteerAvailabilityDto> listVolunteerAvailability() {
        return profiles.findAll(Sort.by("id")).stream()
            .filter(profile -> profile.getUser().getRole() == AccountRole.VOLUNTEER)
            .filter(profile -> profile.getUser().getAccountStatus() == AccountStatus.ACTIVE)
            .map(profile -> new VolunteerAvailabilityDto(profile.getId(), profile.getUser().getFullName(),
                profile.getUser().getEmail(), profile.getUser().getPhone(), profile.getAddress(),
                profile.getAvailability(), slots.findByVolunteerIdOrderByStartsAt(profile.getId()).stream()
                    .map(this::toSlotDto).toList()))
            .toList();
    }

    private VolunteerProfile requireVolunteer(String email) {
        UserAccount account = accounts.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer account not found"));
        if (account.getRole() != AccountRole.VOLUNTEER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Volunteer access is required");
        }
        return profiles.findByUserId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer profile not found"));
    }

    private Skill findOrCreateSkill(String rawName) {
        String name = rawName.trim();
        return skills.findByNameIgnoreCase(name).orElseGet(() -> {
            Skill skill = new Skill();
            skill.setName(name);
            return skills.save(skill);
        });
    }

    private VolunteerSkill requireOwnedSkill(VolunteerProfile profile, Long id) {
        VolunteerSkill link = volunteerSkills.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer skill not found"));
        if (!link.getVolunteer().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer skill not found");
        }
        return link;
    }

    private AvailabilitySlot requireOwnedSlot(VolunteerProfile profile, Long id) {
        AvailabilitySlot slot = slots.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability slot not found"));
        if (!slot.getVolunteer().getId().equals(profile.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability slot not found");
        }
        return slot;
    }

    private void ensureNoOverlap(VolunteerProfile profile, LocalDateTime startsAt, LocalDateTime endsAt, Long excludedId) {
        boolean overlap = excludedId == null
            ? slots.existsByVolunteerIdAndStartsAtLessThanAndEndsAtGreaterThan(profile.getId(), endsAt, startsAt)
            : slots.existsByVolunteerIdAndStartsAtLessThanAndEndsAtGreaterThanAndIdNot(profile.getId(), endsAt, startsAt, excludedId);
        if (overlap) throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability slots cannot overlap");
    }

    private VolunteerProfileDto toProfileDto(VolunteerProfile profile) {
        UserAccount account = profile.getUser();
        int completed = 0;
        if (hasText(account.getFullName())) completed++;
        if (hasText(account.getEmail())) completed++;
        if (hasText(account.getPhone())) completed++;
        if (hasText(profile.getAddress())) completed++;
        return new VolunteerProfileDto(profile.getId(), account.getFullName(), account.getEmail(), account.getPhone(),
            profile.getAddress(), profile.getAvailability(), completed * 25);
    }

    private AvailabilityResponse toAvailabilityResponse(VolunteerProfile profile) {
        return new AvailabilityResponse(profile.getAvailability(), slots.findByVolunteerIdOrderByStartsAt(profile.getId())
            .stream().map(this::toSlotDto).toList());
    }

    private VolunteerSkillDto toSkillDto(VolunteerSkill link) {
        return new VolunteerSkillDto(link.getId(), link.getSkill().getId(), link.getSkill().getName());
    }

    private AvailabilitySlotDto toSlotDto(AvailabilitySlot slot) {
        return new AvailabilitySlotDto(slot.getId(), slot.getStartsAt(), slot.getEndsAt());
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
