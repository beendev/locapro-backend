package com.locapro.backend.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSender.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    // Base publique (front) – ex: https://app.locapro.example
    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public SmtpMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailVerification(String toEmail, String token) {
        String verifyLink = publicBaseUrl + "/auth/verify-email?token=" + token;
        String subject = "Confirmez votre adresse e-mail";
        String html = buildActionEmailHtml(
                "Confirmez votre adresse e-mail",
                "Merci de votre inscription. Pour activer votre compte, cliquez sur le bouton ci-dessous :",
                "Confirmer mon e-mail",
                verifyLink,
                "Cet e-mail expirera dans 24 heures."
        );
        sendHtml(toEmail, subject, html);
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetLink) {
        String subject = "Réinitialisation de votre mot de passe";
        String html = buildActionEmailHtml(
                "Réinitialisation de votre mot de passe",
                "Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous :",
                "Réinitialiser mon mot de passe",
                resetLink,
                "Ce lien expirera dans un délai limité. Si vous n’êtes pas à l’origine de cette demande, ignorez cet e-mail."
        );
        sendHtml(toEmail, subject, html);
    }

    @Override
    public void sendAgenceInvitation(String toEmail,
                                     String agenceNom,
                                     String token,
                                     String messageOptionnel) {

        // Exemple de route front (à adapter à ton vrai front)
        String invitationLink = publicBaseUrl + "/agences/invitations?token=" + token;

        String subject = "Invitation à rejoindre l'agence " + agenceNom;

        String intro = "Vous avez été invité(e) à rejoindre l'agence " + agenceNom + " sur LocaPro.";
        if (messageOptionnel != null && !messageOptionnel.isBlank()) {
            intro += "<br/><br/>Message de l'administrateur :<br/><em>"
                    + messageOptionnel
                    + "</em>";
        }

        String html = buildActionEmailHtml(
                "Invitation à rejoindre l'agence " + agenceNom,
                intro,
                "Accepter l'invitation",
                invitationLink,
                "Si vous ne reconnaissez pas cette invitation, ignorez simplement cet e-mail."
        );

        sendHtml(toEmail, subject, html);
    }

    /* ===================== utils ===================== */

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // HTML
            mailSender.send(msg);
        } catch (MailException | MessagingException e) {
            log.error("Échec d’envoi d’e-mail à {} ({}): {}", to, subject, e.getMessage(), e);
            // À toi de voir : relancer une RuntimeException pour faire échouer la requête,
            // ou juste logguer (ici on loggue seulement).
        }
    }

    private String buildActionEmailHtml(String title, String intro, String ctaLabel, String link, String footerNote) {
        return """
            <div style="font-family:Inter,Arial,sans-serif;max-width:520px;margin:auto;padding:24px;background:#ffffff;border:1px solid #eee;border-radius:12px">
              <div style="text-align:center;margin-bottom:18px;font-weight:700;font-size:18px;">LocaPro</div>
              <h1 style="font-size:20px;margin:0 0 12px 0;">%s</h1>
              <p style="color:#444;line-height:1.5;margin:0 0 16px 0;">%s</p>
              <p style="text-align:center;margin:24px 0">
                <a href="%s" style="display:inline-block;padding:12px 18px;border-radius:8px;background:#111;color:#fff;text-decoration:none;font-weight:600">
                  %s
                </a>
              </p>
              <p style="color:#666;font-size:12px;line-height:1.5;margin-top:16px">
                Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br/>
                <a href="%s" style="color:#111;">%s</a>
              </p>
              <hr style="border:none;border-top:1px solid #eee;margin:20px 0"/>
              <p style="color:#999;font-size:12px;margin:0">%s</p>
            </div>
            """.formatted(title, intro, link, ctaLabel, link, link, footerNote);
    }
}
