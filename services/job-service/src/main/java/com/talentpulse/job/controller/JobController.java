package com.talentpulse.job.controller;

import com.talentpulse.job.dto.CreateJobRequest;
import com.talentpulse.job.dto.JobResponse;
import com.talentpulse.job.dto.PageResponse;
import com.talentpulse.job.dto.UpdateJobRequest;
import com.talentpulse.job.enums.JobStatus;
import com.talentpulse.job.security.AuthPrincipal;
import com.talentpulse.job.service.JobService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request, principal));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID jobId,
            @Valid @RequestBody UpdateJobRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(jobService.updateJob(jobId, request, principal));
    }

    @PostMapping("/{jobId}/publish")
    public ResponseEntity<JobResponse> publishJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(jobService.publishJob(jobId, principal));
    }

    @PostMapping("/{jobId}/close")
    public ResponseEntity<JobResponse> closeJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(jobService.closeJob(jobId, principal));
    }

    /** Recruiter: jobs for my organization (declared before /{jobId}) */
    @GetMapping("/organization/me")
    public ResponseEntity<PageResponse<JobResponse>> listMyOrganizationJobs(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.listMyOrganizationJobs(principal, status, page, size));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(jobService.getJob(jobId, principal));
    }

    /** Public: search published jobs */
    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> searchPublished(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.searchPublished(q, location, page, size));
    }
}
