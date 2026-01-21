package com.locapro.backend.service;

import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.user.UpdateUserProfileRequest;
import com.locapro.backend.dto.user.ChangePasswordRequest;
import com.locapro.backend.dto.user.UserResponse;

public interface UserService {
    // Récupérer son propre profil
    UserResponse getCurrentUser(Long userId);

    // Mettre à jour les infos (Nom, Prénom, Adresse, IPI...)
    UserResponse updateProfile(Long userId, UpdateUserProfileRequest request);

    // Changer de mot de passe (quand on est déjà connecté)
    ApiMessageResponse changePassword(Long userId, ChangePasswordRequest request);

    ApiMessageResponse anonymiserUser(Long userId);


}