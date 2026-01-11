package com.locapro.backend.domain.context;


public enum TravailContext {
        AGENCE,
        PERSONNEL;

        public static TravailContext fromHeader(String headerValue) {
            if (headerValue == null) {
                return PERSONNEL; // par défaut
            }
            return switch (headerValue.toUpperCase()) {
                case "AGENCE" -> AGENCE;
                case "PERSONNEL" -> PERSONNEL;
                default -> PERSONNEL;
            };
        }
}
