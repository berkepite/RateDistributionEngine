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
import java.util.*;

/**
 * Service for sending emails based on predefined recipient groups
 * loaded from a configuration file.
 *
 * <p>Supports email level groups like "fatal", "warn", etc., and
 * sends emails only if enabled through configuration.
 */
@Service
public class EmailService {
    private static final Logger LOGGER = LogManager.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private Map<String, List<String>> groupEmailMap = new HashMap<>();

    @Value("${app.email.group-file}")
    private String emailGroupFilePath;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Constructs EmailService with injected JavaMailSender.
     *
     * @param mailSender the mail sender component
     */
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Loads email groups from the configured file into memory.
     * Each line in the file should be in the format:
     * <pre>
     * level:email1@example.com,email2@example.com
     * </pre>
     */
    @PostConstruct
    public void loadEmailGroups() {
        if (!emailEnabled) {
            LOGGER.info("Email sending is disabled. Skipping loading of email groups.");
            return;
        }

        if (emailGroupFilePath == null || emailGroupFilePath.isBlank()) {
            LOGGER.warn("Email group file path is empty or not set.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(Path.of(emailGroupFilePath).toFile()))) {
            Map<String, List<String>> tempMap = new HashMap<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);

                if (parts.length == 2) {
                    String level = parts[0].trim().toLowerCase();

                    List<String> emails = Arrays.stream(parts[1].split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();

                    if (!emails.isEmpty()) {
                        tempMap.put(level, emails);
                    }
                }
            }

            groupEmailMap = Collections.unmodifiableMap(tempMap);
            LOGGER.info("Loaded email groups from file '{}': {}", emailGroupFilePath, groupEmailMap.keySet());

        } catch (Exception e) {
            LOGGER.error("Failed to read email group file '{}'", emailGroupFilePath, e);
        }
    }

    /**
     * Sends an email with the specified subject and message to the recipients
     * associated with the given email level.
     *
     * @param subject the email subject
     * @param message the email body content
     * @param level   the recipient group level (e.g., "fatal", "warn")
     */
    public void sendEmail(String subject, String message, String level) {
        if (!emailEnabled) {
            LOGGER.debug("Email sending disabled; skipping email for level: {}", level);
            return;
        }

        List<String> recipients = getEmailsForLevel(level);

        if (recipients.isEmpty()) {
            LOGGER.warn("No recipients found for email level '{}'. Skipping sending email.", level);
            return;
        }

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipients.toArray(new String[0]));
        email.setSubject(subject);
        email.setText(message);

        try {
            mailSender.send(email);
            LOGGER.info("Sent email to {} recipients for level '{}'", recipients.size(), level);
        } catch (Exception e) {
            LOGGER.error("Failed to send email to recipients: {}", recipients, e);
        }
    }

    /**
     * Retrieves the list of email addresses for the specified group level.
     *
     * @param level the email group level
     * @return list of email addresses, or empty list if none found
     */
    public List<String> getEmailsForLevel(String level) {
        if (level == null) {
            return Collections.emptyList();
        }
        return groupEmailMap.getOrDefault(level.toLowerCase(), Collections.emptyList());
    }
}
