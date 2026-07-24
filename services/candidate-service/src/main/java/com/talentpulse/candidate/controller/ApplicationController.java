package com.talentpulse.candidate.controller;

import com.talentpulse.candidate.dto.ApplicationResponse;
import com.talentpulse.candidate.dto.ApplyToJobRequest;
import com.talentpulse.candidate.dto.PageResponse;
import com.talentpulse.candidate.dto.RejectApplicationRequest;
import com.talentpulse.candidate.dto.StatusChangeRequest;
import com.talentpulse.candidate.enums.ApplicationStatus;
import com.talentpulse.candidate.security.AuthPrincipal;
import com.talentpulse.candidate.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/api/v1/applications")
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplyToJobRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(request, principal));
    }

    @GetMapping("/api/v1/applications/me")
    public ResponseEntity<PageResponse<ApplicationResponse>> myApplications(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(applicationService.myApplications(principal, page, size));
    }

    @GetMapping("/api/v1/applications/{applicationId}")
    public ResponseEntity<ApplicationResponse> getMyApplication(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.getMyApplication(applicationId, principal));
    }

    @GetMapping("/api/v1/jobs/{jobId}/applications")
    public ResponseEntity<PageResponse<ApplicationResponse>> listForJob(
            @PathVariable UUID jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(applicationService.listForJob(jobId, status, principal, page, size));
    }

    @GetMapping("/api/v1/applications/{applicationId}/recruiter")
    public ResponseEntity<ApplicationResponse> getForRecruiter(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.getForRecruiter(applicationId, principal));
    }

    @PostMapping("/api/v1/applications/{applicationId}/shortlist")
    public ResponseEntity<ApplicationResponse> shortlist(
            @PathVariable UUID applicationId,
            @Valid @RequestBody(required = false) StatusChangeRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.shortlist(
                applicationId,
                request != null ? request : new StatusChangeRequest(),
                principal
        ));
    }

    @PostMapping("/api/v1/applications/{applicationId}/interview")
    public ResponseEntity<ApplicationResponse> interview(
            @PathVariable UUID applicationId,
            @Valid @RequestBody(required = false) StatusChangeRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.moveToInterview(
                applicationId,
                request != null ? request : new StatusChangeRequest(),
                principal
        ));
    }

    @PostMapping("/api/v1/applications/{applicationId}/select")
    public ResponseEntity<ApplicationResponse> select(
            @PathVariable UUID applicationId,
            @Valid @RequestBody(required = false) StatusChangeRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.select(
                applicationId,
                request != null ? request : new StatusChangeRequest(),
                principal
        ));
    }

    @PostMapping("/api/v1/applications/{applicationId}/reject")
    public ResponseEntity<ApplicationResponse> reject(
            @PathVariable UUID applicationId,
            @Valid @RequestBody(required = false) RejectApplicationRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(applicationService.reject(
                applicationId,
                request != null ? request : new RejectApplicationRequest(),
                principal
        ));
    }
}
