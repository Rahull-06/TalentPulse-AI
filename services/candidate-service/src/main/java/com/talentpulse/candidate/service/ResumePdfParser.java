package com.talentpulse.candidate.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Extracts text from PDF resumes and detects skills mentioned in that text.
 */
@Component
@Slf4j
public class ResumePdfParser {

    /** Common tech skills used to populate candidate_skills from resume prose. */
    private static final List<String> SKILL_CATALOG = List.of(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "Kotlin", "Scala",
            "Python", "Django", "Flask", "FastAPI",
            "JavaScript", "TypeScript", "React", "Next.js", "Angular", "Vue", "Node.js", "Express",
            "C#", ".NET", "ASP.NET", "Go", "Rust", "C++", "C",
            "SQL", "PostgreSQL", "MySQL", "MongoDB", "Redis", "Elasticsearch", "Cassandra",
            "Kafka", "RabbitMQ", "Microservices", "Docker", "Kubernetes", "AWS", "Azure", "GCP",
            "CI/CD", "Jenkins", "Git", "GraphQL", "REST", "gRPC",
            "HTML", "CSS", "Tailwind", "Android", "iOS", "Swift", "Flutter",
            "Machine Learning", "TensorFlow", "PyTorch", "NLP",
            "Linux", "Terraform", "Ansible", "Prometheus", "Grafana"
    );

    public ParsedResume parsePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        boolean pdfByName = name.toLowerCase(Locale.ROOT).endsWith(".pdf");
        boolean pdfByType = contentType.equalsIgnoreCase("application/pdf")
                || contentType.equalsIgnoreCase("application/x-pdf");
        if (!pdfByName && !pdfByType) {
            throw new IllegalArgumentException("Only PDF resumes are accepted");
        }

        try {
            return parsePdfBytes(file.getBytes());
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid or unreadable PDF: " + ex.getMessage(), ex);
        }
    }

    public ParsedResume parsePdfBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Resume file is required");
        }
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF has no pages");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException(
                        "Could not read text from this PDF. Upload a text-based PDF (not a scanned image)."
                );
            }
            String cleaned = text.replace("\u0000", " ").trim();
            List<String> skills = detectSkills(cleaned);
            log.info("Parsed resume PDF: {} chars, {} skills detected", cleaned.length(), skills.size());
            return new ParsedResume(cleaned, skills);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Invalid or unreadable PDF: " + ex.getMessage(), ex);
        }
    }

    public List<String> detectSkills(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return List.of();
        }
        String haystack = resumeText.toLowerCase(Locale.ROOT);
        Set<String> found = new LinkedHashSet<>();
        // Longer names first so "Spring Boot" wins before "Spring"
        List<String> ordered = new ArrayList<>(SKILL_CATALOG);
        ordered.sort((a, b) -> Integer.compare(b.length(), a.length()));
        for (String skill : ordered) {
            if (containsSkill(haystack, skill.toLowerCase(Locale.ROOT))) {
                found.add(skill);
            }
        }
        return List.copyOf(found);
    }

    static boolean containsSkill(String resumeLower, String skillLower) {
        String escaped = Pattern.quote(skillLower);
        Pattern pattern = Pattern.compile("(?<![a-z0-9+#.])" + escaped + "(?![a-z0-9+#.])");
        return pattern.matcher(resumeLower).find();
    }

    public record ParsedResume(String text, List<String> skills) {
    }
}
