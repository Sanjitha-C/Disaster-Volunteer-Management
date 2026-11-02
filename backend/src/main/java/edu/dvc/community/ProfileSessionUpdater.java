package edu.dvc.community;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class ProfileSessionUpdater {
    private final SecurityContextRepository repository;

    public ProfileSessionUpdater(SecurityContextRepository repository) { this.repository = repository; }

    public void updateEmail(Authentication current, String email, HttpServletRequest request, HttpServletResponse response) {
        if (current.getName().equalsIgnoreCase(email)) return;
        UsernamePasswordAuthenticationToken updated = UsernamePasswordAuthenticationToken.authenticated(
            email, null, current.getAuthorities());
        updated.setDetails(current.getDetails());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(updated);
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
    }
}
