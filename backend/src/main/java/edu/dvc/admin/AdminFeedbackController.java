package edu.dvc.admin;

import edu.dvc.operations.FeedbackDto;
import edu.dvc.operations.OperationsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {
    private final OperationsService service;
    public AdminFeedbackController(OperationsService service) { this.service = service; }
    @GetMapping public List<FeedbackDto> all() { return service.allFeedback(); }
}
