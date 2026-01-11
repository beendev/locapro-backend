package com.locapro.backend.service.impl;

import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;
import com.locapro.backend.entity.AgenceEntity;
import com.locapro.backend.entity.PortefeuilleEntity;
import com.locapro.backend.entity.PortefeuilleMembreEntity;
import com.locapro.backend.entity.UtilisateurAgenceEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.mapper.PortefeuilleMapper;
import com.locapro.backend.mapper.PortefeuilleMembreMapper;
import com.locapro.backend.repository.PortefeuilleMembreRepository;
import com.locapro.backend.repository.PortefeuilleRepository;
import com.locapro.backend.repository.UtilisateurAgenceRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.PortefeuilleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortefeuilleServiceImplTest {

    @Mock
    private UtilisateurAgenceRepository utilisateurAgenceRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PortefeuilleRepository portefeuilleRepository;

    @Mock
    private PortefeuilleMembreRepository portefeuilleMembreRepository;

    @Mock
    private PortefeuilleMapper portefeuilleMapper;

    @Mock
    private PortefeuilleMembreMapper portefeuilleMembreMapper;

    @InjectMocks
    private PortefeuilleServiceImpl service;


    @Test
    void creerPortefeuilleAgence_ok_quand_user_est_admin_agence() {
        // GIVEN (= mise en place du contexte du test)

        // Id de l'utilisateur "courant" (celui qui appelle le service)
        Long currentUserId = 1L;

        // On mock la request, car dans le service on ne lis pas encore ses champs
        // -> pas besoin de remplir des valeurs, un simple mock suffit
        PortefeuilleCreateRequest request = mock(PortefeuilleCreateRequest.class);

        // On crée / mock les objets que la méthode va manipuler en interne
        // (mais on ne va pas les charger depuis la BD, on les fournit nous-mêmes)
        UtilisateurAgenceEntity lien = mock(UtilisateurAgenceEntity.class); // lien user <-> agence
        AgenceEntity agence = new AgenceEntity();                           // agence de l'utilisateur
        UtilisateurEntity user = new UtilisateurEntity();                   // entité utilisateur
        PortefeuilleEntity portefeuille = new PortefeuilleEntity();         // portefeuille créé
        PortefeuilleMembreEntity membre = new PortefeuilleMembreEntity();   // membre gestionnaire ajouté
        PortefeuilleResponse responseAttendue = mock(PortefeuilleResponse.class); // DTO de réponse

        // On "programme" le comportement des mocks avec Mockito :
        // quand le service va appeler ces méthodes, on contrôle la réponse.

        // 1) Lien user ↔ agence
        // Quand le service fait:
        //   utilisateurAgenceRepository.findFirstByUtilisateurIdAndEnabledTrue(currentUserId)
        // on veut qu'il reçoive Optional.of(lien)
        when(utilisateurAgenceRepository.findFirstByUtilisateurIdAndEnabledTrue(currentUserId))
                .thenReturn(Optional.of(lien));

        // Quand le service appelle lien.getRoleDansAgence(), on répond "ADMIN_AGENCE"
        // -> ça simule que l'utilisateur est admin de l'agence
        when(lien.getRoleDansAgence()).thenReturn("ADMIN_AGENCE");

        // Quand le service appelle lien.getAgence(), on donne l'objet agence
        when(lien.getAgence()).thenReturn(agence);

        // 2) Utilisateur courant
        // Quand le service appelle utilisateurRepository.findById(currentUserId),
        // on renvoie un Optional contenant notre "user"
        when(utilisateurRepository.findById(currentUserId))
                .thenReturn(Optional.of(user));

        // 3) Mapper portefeuille
        // Quand le service appelle portefeuilleMapper.fromAgenceCreateRequest(...)
        // avec la request, l'agence, l'user et n'importe quel OffsetDateTime (any()),
        // on veut qu'il obtienne notre objet "portefeuille"
        when(portefeuilleMapper.fromAgenceCreateRequest(
                eq(request),   // on veut exactement cette request
                eq(agence),    // exactement cette agence
                eq(user),      // exactement cet utilisateur
                any()          // on s'en fiche de la valeur précise de la date
        )).thenReturn(portefeuille);

        // 4) Sauvegarde du portefeuille en BD
        // Quand le service fait portefeuilleRepository.save(portefeuille),
        // on renvoie le même objet (comportement classique d'un save JPA)
        when(portefeuilleRepository.save(portefeuille))
                .thenReturn(portefeuille);

        // 5) Mapper membre gestionnaire
        // Quand le service appelle portefeuilleMembreMapper.asGestionnaire(portefeuille, user, now),
        // on veut renvoyer notre objet "membre"
        when(portefeuilleMembreMapper.asGestionnaire(
                eq(portefeuille),
                eq(user),
                any()          // pareil, la date exacte ne nous intéresse pas
        )).thenReturn(membre);

        // 6) Mapper réponse
        // Quand le service appelle portefeuilleMapper.toResponse(portefeuille),
        // on renvoie notre "responseAttendue" (mock de PortefeuilleResponse)
        when(portefeuilleMapper.toResponse(portefeuille))
                .thenReturn(responseAttendue);

        // WHEN (= action qu'on teste)
        // On appelle la vraie méthode de la classe PortefeuilleServiceImpl
        PortefeuilleResponse resultat =
                service.creerPortefeuilleAgence(currentUserId, request);

        // THEN (= ce qu'on vérifie)

        // 1) La réponse retournée par le service doit être exactement
        //    la même instance que responseAttendue (même objet)
        assertSame(responseAttendue, resultat);

        // 2) On vérifie que le membre a bien été sauvegardé en BD :
        //    portefeuilleMembreRepository.save(membre) doit avoir été appelé une fois.
        verify(portefeuilleMembreRepository).save(membre);
    }

    @Test
    void creerPortefeuilleAgence_interdit_si_pas_admin_ni_responsable() {
        // GIVEN
        Long currentUserId = 2L;

        UtilisateurAgenceEntity lien = new UtilisateurAgenceEntity();
        lien.setRoleDansAgence("COLLABORATEUR"); // pas ADMIN_AGENCE ni RESPONSABLE

        when(utilisateurAgenceRepository.findFirstByUtilisateurIdAndEnabledTrue(currentUserId))
                .thenReturn(Optional.of(lien));

        PortefeuilleCreateRequest request = mock(PortefeuilleCreateRequest.class);

        // WHEN + THEN
        assertThrows(
                ForbiddenException.class,
                () -> service.creerPortefeuilleAgence(currentUserId, request),
                "Un utilisateur sans rôle ADMIN_AGENCE/RESPONSABLE ne doit pas pouvoir créer un portefeuille."
        );

        // On vérifie qu’aucun portefeuille n’a été créé
        verify(portefeuilleRepository, never()).save(any());
    }

    @Test
    void quitterPortefeuille_quand_membre_actif() {
        // GIVEN
        Long portefeuilleId = 100L;
        Long currentUserId = 3L;

        PortefeuilleMembreEntity membre = new PortefeuilleMembreEntity();
        membre.setEnabled(true);

        when(portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId))
                .thenReturn(Optional.of(membre));

        // WHEN
        ApiMessageResponse response = service.quitterPortefeuille(portefeuilleId, currentUserId);

        // THEN
        assertEquals("Vous avez quitté ce portefeuille.", response.message());

        // On vérifie que le membre a été désactivé et sauvegardé
        assertFalse(membre.isEnabled(), "Le membre doit être désactivé (enabled = false).");
        verify(portefeuilleMembreRepository).save(membre);
    }

    @Test
    void quitterPortefeuille_quand_pas_membre() {
        // GIVEN
        Long portefeuilleId = 200L;
        Long currentUserId = 4L;

        when(portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId))
                .thenReturn(Optional.empty());

        // WHEN
        ApiMessageResponse response = service.quitterPortefeuille(portefeuilleId, currentUserId);

        // THEN
        assertEquals("Vous n'êtes pas membre de ce portefeuille.", response.message());

        // On ne sauvegarde rien
        verify(portefeuilleMembreRepository, never()).save(any());
    }
}
