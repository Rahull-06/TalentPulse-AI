package com.talentpulse.candidate.controller;

import com.talentpulse.candidate.dto.CandidateProfileResponse;
import com.talentpulse.candidate.dto.ResumeResponse;
import com.talentpulse.candidate.dto.UpdateProfileRequest;
import com.talentpulse.candidate.security.AuthPrincipal;
import com.talentpulse.candidate.service.CandidateProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @GetMapping("/me")
    public ResponseEntity<CandidateProfileResponse> getMyProfile(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(candidateProfileService.getOrCreateMyProfile(principal));
    }

    @PutMapping("/me")
    public ResponseEntity<CandidateProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(candidateProfileService.updateMyProfile(request, principal));
    }

    @PostMapping(value = "/me/resumes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateProfileService.uploadResume(file, principal));
    }

    @GetMapping("/me/resumes")
    public ResponseEntity<List<ResumeResponse>> listMyResumes(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(candidateProfileService.listMyResumes(principal));
    }

    @PostMapping("/me/resumes/{resumeId}/primary")
    public ResponseEntity<ResumeResponse> setPrimaryResume(
            @PathVariable UUID resumeId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(candidateProfileService.setPrimaryResume(resumeId, principal));
    }
}
