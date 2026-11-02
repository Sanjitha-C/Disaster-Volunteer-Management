package edu.dvc.community;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/me/profile")
public class CommunityProfileController {
    private final CommunityProfileService service;
    private final ProfileSessionUpdater sessionUpdater;

    public CommunityProfileController(CommunityProfileService service, ProfileSessionUpdater sessionUpdater) {
        this.service = service; this.sessionUpdater = sessionUpdater;
    }

    @GetMapping
    public CommunityProfileDto profile(Principal principal) { return service.getProfile(principal.getName()); }

    @PutMapping
    public CommunityProfileDto update(Principal principal, Authentication authentication,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            @Valid @RequestBody UpdateCommunityProfileRequest request) {
        CommunityProfileDto updated = service.updateProfile(principal.getName(), request);
        sessionUpdater.updateEmail(authentication, updated.email(), httpRequest, httpResponse);
        return updated;
    }
}
