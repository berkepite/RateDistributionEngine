package com.berkepite.RateDistributionEngine.email;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger LOGGER = LogManager.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final Map<String, List<String>> groupEmailMap = new HashMap<>();

    @Value("${app.email.group-file}")
    private String emailGroupFilePath;

    @Value("${app.email.enabled}")
    private boolean emailEnabled = false;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void loadEmailGroups() {
        if (!emailEnabled) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(Path.of(emailGroupFilePath).toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);

                if (parts.length == 2) {
                    String level = parts[0].trim().toLowerCase();

                    List<String> emails = Arrays.stream(parts[1].split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    groupEmailMap.put(level, emails);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read email group file: {}", e.getMessage());
        }
    }

    public void sendEmail(String subject, String message, String level) {
        if (!emailEnabled) return;

        List<String> recipients = getEmailsForLevel(level);

        if (recipients == null || recipients.isEmpty()) {
            LOGGER.warn("No recipients found for level: {}. Skipping email...", level);
            return;
        }

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipients.toArray(new String[0]));
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }

    private List<String> getEmailsForLevel(String level) {
        return groupEmailMap.getOrDefault(level.toLowerCase(), List.of());
    }
}
