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

    // 👇 HARDCODÉ ICI COMME DEMANDÉ
    private final String frontendUrl = "http://localhost:3000";

    public SmtpMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailVerification(String toEmail, String token) {
        // Lien : http://localhost:3000/verify-email?token=...
        String verifyLink = frontendUrl + "/verify-email?token=" + token;

        String subject = "Confirmez votre adresse e-mail";
        String html = buildActionEmailHtml(
                "Bienvenue chez LocaPro ! 🏠",
                "Merci de votre inscription. Pour activer votre compte, cliquez sur le bouton ci-dessous :",
                "Confirmer mon e-mail",
                verifyLink,
                "Cet e-mail expirera dans 24 heures."
        );
        sendHtml(toEmail, subject, html);
    }

    @Override
    public void sendPasswordReset(String toEmail, String token) {
        // Lien : http://localhost:3000/reset-password?token=...
        // Note: Je suppose que le 2ème paramètre est le token.
        // Si ton interface envoie déjà un lien complet, change 'token' par 'resetLink'
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String subject = "Réinitialisation de votre mot de passe";
        String html = buildActionEmailHtml(
                "Mot de passe oublié ?",
                "Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le bouton ci-dessous :",
                "Réinitialiser mon mot de passe",
                resetLink,
                "Ce lien expirera dans un délai limité."
        );
        sendHtml(toEmail, subject, html);
    }

    @Override
    public void sendAgenceInvitation(String toEmail,
                                     String agenceNom,
                                     String token,
                                     String messageOptionnel) {


        String invitationLink = frontendUrl + "/accept-invitation?token=" + token;

        String subject = "Invitation à rejoindre l'agence " + agenceNom;

        String intro = "Vous avez été invité(e) à rejoindre l'agence <strong>" + agenceNom + "</strong> sur LocaPro.";
        if (messageOptionnel != null && !messageOptionnel.isBlank()) {
            intro += "<br/><br/>Message de l'administrateur :<br/><em>\""
                    + messageOptionnel
                    + "\"</em>";
        }

        String html = buildActionEmailHtml(
                "Invitation à rejoindre une agence",
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
            helper.setText(html, true); // HTML activé
            mailSender.send(msg);
            log.info("✅ Email envoyé à {} | Sujet: {}", to, subject);
        } catch (MailException | MessagingException e) {
            log.error("❌ Échec d’envoi d’e-mail à {} : {}", to, e.getMessage());
        }
    }

    private String buildActionEmailHtml(String title, String intro, String ctaLabel, String link, String footerNote) {
        return """
            <div style="font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;max-width:520px;margin:auto;padding:30px;background:#ffffff;border:1px solid #e5e7eb;border-radius:16px;">
              <div style="text-align:center;margin-bottom:24px;">
                <span style="font-weight:900;font-size:22px;color:#111;">LocaPro</span>
              </div>
              
              <h1 style="font-size:20px;font-weight:700;margin:0 0 16px 0;color:#111;text-align:center;">%s</h1>
              
              <p style="color:#4b5563;font-size:16px;line-height:1.6;margin:0 0 24px 0;text-align:center;">%s</p>
              
              <div style="text-align:center;margin:32px 0;">
                <a href="%s" style="display:inline-block;padding:14px 28px;border-radius:12px;background:#000000;color:#ffffff;text-decoration:none;font-weight:600;font-size:15px;box-shadow:0 4px 6px rgba(0,0,0,0.1);">
                  %s
                </a>
              </div>
              
              <p style="color:#6b7280;font-size:13px;line-height:1.5;margin-top:32px;text-align:center;border-top:1px solid #f3f4f6;padding-top:20px;">
                Si le bouton ne fonctionne pas, copiez ce lien :<br/>
                <a href="%s" style="color:#4f46e5;word-break:break-all;">%s</a>
              </p>
              
              <p style="color:#9ca3af;font-size:12px;margin:20px 0 0 0;text-align:center;">%s</p>
            </div>
            """.formatted(title, intro, link, ctaLabel, link, link, footerNote);
    }
}