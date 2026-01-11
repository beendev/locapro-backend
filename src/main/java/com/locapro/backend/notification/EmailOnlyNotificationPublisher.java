package com.locapro.backend.notification;

import com.locapro.backend.entity.AgenceInvitationEntity;
import com.locapro.backend.service.mail.MailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailOnlyNotificationPublisher implements NotificationPublisher {

    private final MailSender mailSender;

    public EmailOnlyNotificationPublisher(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void publishAgenceInvitationCreated(AgenceInvitationEntity invitation) {
        String toEmail = invitation.getEmailInvite();
        String agenceNom = invitation.getAgence().getNom();
        String token = invitation.getToken().toString();

        // Pour l’instant, pas de message optionnel
        mailSender.sendAgenceInvitation(toEmail, agenceNom, token, null);
    }
}
