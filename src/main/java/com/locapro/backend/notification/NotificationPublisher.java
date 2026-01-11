// package à adapter selon ton organisation
package com.locapro.backend.notification;

import com.locapro.backend.entity.AgenceInvitationEntity;

public interface NotificationPublisher {

    /**
     * Événement métier : une invitation d'agence vient d'être créée.
     * V1 : envoi d'un e-mail.
     * V2 : on pourra ajouter WebSocket, notifications internes, etc.
     */
    void publishAgenceInvitationCreated(AgenceInvitationEntity invitation);
}
