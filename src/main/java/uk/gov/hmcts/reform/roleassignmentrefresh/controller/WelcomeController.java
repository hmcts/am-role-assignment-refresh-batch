package uk.gov.hmcts.reform.roleassignmentrefresh.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignmentrefresh.domain.model.service.RoleAssignmentService;

@RestController
public class WelcomeController {

    private RoleAssignmentService ras;

    public WelcomeController(RoleAssignmentService ras) {
        this.ras = ras;
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        ras.getServiceStatus();
        return "Welcome to the limit";
    }
}
