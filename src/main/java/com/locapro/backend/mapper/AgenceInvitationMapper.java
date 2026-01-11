package com.locapro.backend.mapper;

import com.locapro.backend.dto.agence.AgenceInvitationResponse;
import com.locapro.backend.entity.AgenceInvitationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgenceInvitationMapper {

    public AgenceInvitationResponse toResponse(AgenceInvitationEntity entity) {
        if (entity == null) return null;

        return new AgenceInvitationResponse(
                entity.getId(),
                entity.getAgence().getId(),
                entity.getAgence().getNom(),
                entity.getEmailInvite(),
                entity.getStatut(),
                entity.getCreatedAt()
        );
    }

    public List<AgenceInvitationResponse> toResponseList(List<AgenceInvitationEntity> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
