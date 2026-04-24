package com.devbrief.api;

import com.devbrief.briefing.BriefingService;
import com.devbrief.ingest.IngestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final IngestionService ingestionService;
    private final BriefingService briefingService;
    private final String adminToken;

    public AdminController(IngestionService ingestionService,
                           BriefingService briefingService,
                           @Value("${devbrief.admin-token:${DEVBRIEF_ADMIN_TOKEN:}}") String adminToken) {
        this.ingestionService = ingestionService;
        this.briefingService = briefingService;
        this.adminToken = adminToken == null ? "" : adminToken.trim();
    }

    @PostMapping("/ingest/run")
    public ResponseEntity<?> runIngestion(@RequestHeader(value = "X-Admin-Token", required = false) String providedToken) {
        if (!authorized(providedToken)) {
            return unauthorized();
        }
        return ResponseEntity.ok(ingestionService.run());
    }

    @PostMapping("/briefings/generate")
    public ResponseEntity<?> generateBriefings(@RequestHeader(value = "X-Admin-Token", required = false) String providedToken) {
        if (!authorized(providedToken)) {
            return unauthorized();
        }
        return ResponseEntity.ok(briefingService.generate());
    }

    private boolean authorized(String providedToken) {
        return adminToken.isBlank() || adminToken.equals(providedToken);
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("관리 토큰이 필요합니다.");
    }
}
