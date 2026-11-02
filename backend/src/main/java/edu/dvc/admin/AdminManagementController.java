package edu.dvc.admin;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminManagementController {
    private final AdminManagementService service;
    public AdminManagementController(AdminManagementService service) { this.service = service; }
    @GetMapping("/reports/summary") public DashboardReportDto report() { return service.report(); }
    @GetMapping("/users") public List<AdminUserDto> users() { return service.users(); }
    @PutMapping("/users/{id}/access") public AdminUserDto updateAccess(Principal principal, @PathVariable Long id, @Valid @RequestBody UpdateUserAccessRequest request) {
        return service.updateAccess(id, principal.getName(), request);
    }
}
