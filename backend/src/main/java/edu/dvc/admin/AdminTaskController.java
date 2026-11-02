package edu.dvc.admin;

import edu.dvc.coordination.AssignmentDto;
import edu.dvc.domain.TaskStatus;
import edu.dvc.operations.OperationsService;
import edu.dvc.operations.TaskStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/assignments")
public class AdminTaskController {
    private final OperationsService service;
    public AdminTaskController(OperationsService service) { this.service = service; }
    @PutMapping("/{id}/status")
    public AssignmentDto updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusRequest request) {
        if (request.status() != TaskStatus.CANCELLED) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin can cancel an active assignment using this endpoint");
        return service.cancelTaskAsAdmin(id);
    }
}
