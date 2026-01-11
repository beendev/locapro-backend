package com.locapro.backend.service.mail;

public interface MailSender {
    void sendEmailVerification(String toEmail, String token);
    void sendPasswordReset(String toEmail, String resetLink);
    void sendAgenceInvitation(String toEmail, String agenceNom, String token, String messageOptionnel);
}
