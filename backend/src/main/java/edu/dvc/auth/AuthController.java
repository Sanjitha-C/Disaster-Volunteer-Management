package edu.dvc.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        token.getToken();
        return Map.of("headerName", token.getHeaderName(), "cookieName", "XSRF-TOKEN");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterVolunteerRequest request) {
        AuthUser user = authService.registerVolunteer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse("Volunteer account created", user));
    }

    @PostMapping("/register/requester")
    public ResponseEntity<AuthResponse> registerRequester(@Valid @RequestBody RegisterRequesterRequest request) {
        AuthUser user = authService.registerRequester(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse("Requester account created", user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(request.email().trim(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        AuthUser user = authService.findAuthUserByEmail(authentication.getName());
        return new AuthResponse("Login successful", user);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(Authentication authentication, HttpServletRequest request,
                                      HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal UserDetails principal) {
        return new AuthResponse("Authenticated", authService.findAuthUserByEmail(principal.getUsername()));
    }

    public record LoginRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email,
                               @jakarta.validation.constraints.NotBlank String password) { }
}
