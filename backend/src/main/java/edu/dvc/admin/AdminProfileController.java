package edu.dvc.admin;

import edu.dvc.community.CommunityProfileDto;
import edu.dvc.community.CommunityProfileService;
import edu.dvc.community.ProfileSessionUpdater;
import edu.dvc.community.UpdateCommunityProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/me/profile")
public class AdminProfileController {
    private final CommunityProfileService profiles;
    private final ProfileSessionUpdater sessionUpdater;

    public AdminProfileController(CommunityProfileService profiles, ProfileSessionUpdater sessionUpdater) {
        this.profiles = profiles; this.sessionUpdater = sessionUpdater;
    }

    @GetMapping
    public CommunityProfileDto profile(Principal principal) { return profiles.getAccountProfile(principal.getName()); }

    @PutMapping
    public CommunityProfileDto update(Principal principal, Authentication authentication,
            HttpServletRequest request, HttpServletResponse response,
            @Valid @RequestBody UpdateCommunityProfileRequest input) {
        CommunityProfileDto updated = profiles.updateAccountProfile(principal.getName(), input);
        sessionUpdater.updateEmail(authentication, updated.email(), request, response);
        return updated;
    }
}
