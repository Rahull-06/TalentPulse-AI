package com.talentpulse.candidate.service;

import com.talentpulse.candidate.config.StorageProperties;
import com.talentpulse.candidate.dto.CandidateMapper;
import com.talentpulse.candidate.dto.CandidateProfileResponse;
import com.talentpulse.candidate.dto.ResumeResponse;
import com.talentpulse.candidate.dto.UpdateProfileRequest;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.CandidateSkill;
import com.talentpulse.candidate.entity.Resume;
import com.talentpulse.candidate.enums.ParseStatus;
import com.talentpulse.candidate.enums.Role;
import com.talentpulse.candidate.enums.SkillSource;
import com.talentpulse.candidate.exception.ForbiddenActionException;
import com.talentpulse.candidate.exception.ResourceNotFoundException;
import com.talentpulse.candidate.repository.CandidateProfileRepository;
import com.talentpulse.candidate.repository.CandidateSkillRepository;
import com.talentpulse.candidate.repository.ResumeRepository;
import com.talentpulse.candidate.security.AuthPrincipal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final StorageProperties storageProperties;
    private final ResumePdfParser resumePdfParser;
    private final ApplicationRescoringService applicationRescoringService;

    @Transactional
    public CandidateProfileResponse getOrCreateMyProfile(AuthPrincipal principal) {
        requireCandidate(principal);
        return candidateProfileRepository.findByUserId(principal.userId())
                .map(CandidateMapper::toProfileResponse)
                .orElseGet(() -> createProfileSafely(principal.userId()));
    }

    @Transactional
    public CandidateProfileResponse updateMyProfile(UpdateProfileRequest request, AuthPrincipal principal) {
        requireCandidate(principal);
        CandidateProfile profile = getProfileEntity(principal.userId());

        profile.setHeadline(trimToNull(request.getHeadline()));
        profile.setSummary(trimToNull(request.getSummary()));
        profile.setExperienceYears(request.getExperienceYears());
        profile.setLocation(trimToNull(request.getLocation()));
        profile.setLinkedinUrl(trimToNull(request.getLinkedinUrl()));
        profile.setGithubUrl(trimToNull(request.getGithubUrl()));
        profile.setPhone(trimToNull(request.getPhone()));

        return CandidateMapper.toProfileResponse(candidateProfileRepository.save(profile));
    }

    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, AuthPrincipal principal) {
        requireCandidate(principal);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        CandidateProfile profile = getProfileEntity(principal.userId());
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf";
        String extension = extractExtension(originalName);
        if (!"pdf".equals(extension)) {
            throw new IllegalArgumentException("Only PDF resumes are accepted");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read resume file: " + ex.getMessage(), ex);
        }
        ResumePdfParser.ParsedResume parsed = resumePdfParser.parsePdfBytes(bytes);

        Path baseDir = Paths.get(storageProperties.getResumeDir()).toAbsolutePath().normalize();
        Path dir = baseDir.resolve(profile.getId().toString());
        try {
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID() + ".pdf";
            Path target = dir.resolve(storedName).normalize();
            if (!target.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid resume storage path");
            }
            Files.write(target, bytes);

            boolean firstResume = resumeRepository
                    .findByCandidateProfileIdOrderByUploadedAtDesc(profile.getId())
                    .isEmpty();

            Resume resume = Resume.builder()
                    .candidateProfile(profile)
                    .fileName(originalName)
                    .fileUrl(target.toString())
                    .fileType("pdf")
                    .parsedText(parsed.text())
                    .parseStatus(ParseStatus.SUCCESS)
                    .primaryResume(firstResume)
                    .uploadedAt(Instant.now())
                    .build();

            Resume saved = resumeRepository.save(resume);
            replaceParsedSkills(profile, parsed.skills());
            if (!firstResume) {
                // Keep newest upload as primary so applications use the parsed PDF.
                resumeRepository.clearPrimaryFlag(profile.getId());
                saved.setPrimaryResume(true);
                saved = resumeRepository.save(saved);
            }

            applicationRescoringService.rescoreOpenApplications(profile, saved);
            return CandidateMapper.toResumeResponse(saved);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store resume file: " + ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> listMyResumes(AuthPrincipal principal) {
        requireCandidate(principal);
        CandidateProfile profile = getProfileEntity(principal.userId());
        return resumeRepository.findByCandidateProfileIdOrderByUploadedAtDesc(profile.getId())
                .stream()
                .map(CandidateMapper::toResumeResponse)
                .toList();
    }

    @Transactional
    public ResumeResponse setPrimaryResume(UUID resumeId, AuthPrincipal principal) {
        requireCandidate(principal);
        CandidateProfile profile = getProfileEntity(principal.userId());
        Resume resume = resumeRepository.findByIdAndCandidateProfileId(resumeId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        resumeRepository.clearPrimaryFlag(profile.getId());
        resume.setPrimaryResume(true);
        Resume saved = resumeRepository.save(resume);
        applicationRescoringService.rescoreOpenApplications(profile, saved);
        return CandidateMapper.toResumeResponse(saved);
    }

    @Transactional(readOnly = true)
    public CandidateProfile getProfileEntity(UUID userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found. Call GET /me first."));
    }

    private void replaceParsedSkills(CandidateProfile profile, List<String> skills) {
        candidateSkillRepository.deleteByCandidateProfileIdAndSource(profile.getId(), SkillSource.RESUME_PARSED);
        for (String skillName : skills) {
            candidateSkillRepository.save(CandidateSkill.builder()
                    .candidateProfile(profile)
                    .skillName(skillName)
                    .source(SkillSource.RESUME_PARSED)
                    .build());
        }
    }

    private CandidateProfileResponse createProfileSafely(UUID userId) {
        try {
            CandidateProfile created = candidateProfileRepository.save(
                    CandidateProfile.builder().userId(userId).build()
            );
            return CandidateMapper.toProfileResponse(created);
        } catch (DataIntegrityViolationException ex) {
            return candidateProfileRepository.findByUserId(userId)
                    .map(CandidateMapper::toProfileResponse)
                    .orElseThrow(() -> ex);
        }
    }

    private void requireCandidate(AuthPrincipal principal) {
        if (principal == null || principal.role() != Role.CANDIDATE) {
            throw new ForbiddenActionException("Only candidates can manage profile/resumes");
        }
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
