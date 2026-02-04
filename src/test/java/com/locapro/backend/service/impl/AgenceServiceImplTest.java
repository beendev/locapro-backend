package com.locapro.backend.service.impl;

import com.locapro.backend.dto.agence.AgenceRequest;
import com.locapro.backend.dto.agence.AgenceResponse;
import com.locapro.backend.entity.AgenceEntity;
import com.locapro.backend.entity.EntrepriseEntity;
import com.locapro.backend.entity.UtilisateurAgenceEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.ConflictException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.repository.AgenceRepository;
import com.locapro.backend.repository.EntrepriseRepository;
import com.locapro.backend.repository.UtilisateurAgenceRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 👈 C'est ça qui active Mockito sans charger Spring/BDD
class AgenceServiceImplTest {

    // On mocke toutes les dépendances (les Repositories)
    // Ce sont des coquilles vides qui obéiront à nos "when(...)"
    @Mock private AgenceRepository agenceRepo;
    @Mock private UtilisateurAgenceRepository userAgenceRepo;
    @Mock private UtilisateurRepository userRepo;
    @Mock private EntrepriseRepository entrepriseRepo;

    // On injecte les mocks dans le VRAI service qu'on veut tester
    @InjectMocks
    private AgenceServiceImpl agenceService;

    @Test
    void create_succes_quand_tout_est_valide() {
        // GIVEN (Le scénario)
        Long userId = 1L;
        AgenceRequest request = new AgenceRequest("Ma Super Agence");

        // 1. Simuler l'utilisateur existant
        UtilisateurEntity user = new UtilisateurEntity();
        user.setId(userId);
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        // 2. Simuler l'entreprise existante
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(10L);
        when(entrepriseRepo.findByProprietaire_IdAndEnabledTrue(userId))
                .thenReturn(Optional.of(entreprise));

        // 3. Vérifier qu'il n'y a PAS déjà d'agence (Optional.empty())
        when(agenceRepo.findByEntreprise_IdAndEnabledTrue(10L))
                .thenReturn(Optional.empty());

        // 4. Vérifier que l'utilisateur n'est PAS déjà dans une agence (false)
        when(userAgenceRepo.existsByUtilisateur_Id(userId))
                .thenReturn(false);

        // 5. Simuler la sauvegarde de l'agence (on renvoie l'objet avec un ID généré)
        when(agenceRepo.save(any(AgenceEntity.class))).thenAnswer(invocation -> {
            AgenceEntity agenceToSave = invocation.getArgument(0);
            agenceToSave.setId(999L); // On simule que la BDD a mis l'ID 999
            return agenceToSave;
        });

        // WHEN (L'action)
        AgenceResponse response = agenceService.create(userId, request);

        // THEN (Les vérifications)
        assertNotNull(response);
        assertEquals("Ma Super Agence", response.nom());
        assertEquals(999L, response.id());

        // On vérifie que le lien User <-> Agence a bien été créé et sauvegardé
        verify(userAgenceRepo).save(any(UtilisateurAgenceEntity.class));
    }

    @Test
    void create_echoue_si_entreprise_introuvable() {
        // GIVEN
        Long userId = 1L;
        AgenceRequest request = mock(AgenceRequest.class); // On peut aussi mocker le DTO si on veut pas le construire

        // Utilisateur OK
        when(userRepo.findById(userId)).thenReturn(Optional.of(new UtilisateurEntity()));

        // Entreprise KO (Optional.empty())
        when(entrepriseRepo.findByProprietaire_IdAndEnabledTrue(userId))
                .thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(NotFoundException.class, () -> agenceService.create(userId, request));

        // On vérifie qu'on n'a JAMAIS essayé de sauvegarder quoi que ce soit
        verify(agenceRepo, never()).save(any());
        verify(userAgenceRepo, never()).save(any());
    }

    @Test
    void create_echoue_si_utilisateur_deja_dans_une_agence() {
        // GIVEN
        Long userId = 1L;
        AgenceRequest request = new AgenceRequest("Agence Test");

        // Utilisateur OK
        when(userRepo.findById(userId)).thenReturn(Optional.of(new UtilisateurEntity()));

        // Entreprise OK
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(10L);
        when(entrepriseRepo.findByProprietaire_IdAndEnabledTrue(userId)).thenReturn(Optional.of(entreprise));

        // Pas d'agence existante pour l'entreprise
        when(agenceRepo.findByEntreprise_IdAndEnabledTrue(10L)).thenReturn(Optional.empty());

        // MAIS l'utilisateur est DÉJÀ lié à une autre agence !
        when(userAgenceRepo.existsByUtilisateur_Id(userId)).thenReturn(true);

        // WHEN + THEN
        ConflictException ex = assertThrows(ConflictException.class, () -> agenceService.create(userId, request));
        assertEquals("L’utilisateur appartient déjà à une agence", ex.getMessage());

        verify(agenceRepo, never()).save(any());
    }
}