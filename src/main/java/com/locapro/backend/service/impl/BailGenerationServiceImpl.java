package com.locapro.backend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locapro.backend.entity.BailEntity;
import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.ProprietaireBienEntity;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.repository.BailRepository;
import com.locapro.backend.repository.BienRepository;
import com.locapro.backend.repository.ProprietaireBienRepository;
import com.locapro.backend.service.BailGenerationService;
import com.locapro.backend.utils.DocxUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BailGenerationServiceImpl implements BailGenerationService {

    private final ObjectMapper objectMapper;
    private final BailRepository bailRepository;
    private final BienRepository bienRepository;
    private final ProprietaireBienRepository proprietaireBienRepository;

    private static final String CHECKED = "☒";
    private static final String UNCHECKED = "☐";

    public BailGenerationServiceImpl(ObjectMapper objectMapper,
                                     BailRepository bailRepository,
                                     BienRepository bienRepository,
                                     ProprietaireBienRepository proprietaireBienRepository) {
        this.objectMapper = objectMapper;
        this.bailRepository = bailRepository;
        this.bienRepository = bienRepository;
        this.proprietaireBienRepository = proprietaireBienRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererBailPourId(Long bailId) throws Exception {
        // 1. Récupérer le Bail
        BailEntity bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new NotFoundException("Bail introuvable ID : " + bailId));

        // 2. Récupérer le Bien (Manuellement car pas de @ManyToOne)
        BienEntity bien = bienRepository.findById(bail.getBienId())
                .orElse(null);

        // 3. Récupérer le Propriétaire (Manuellement via le bienId)
        ProprietaireBienEntity proprietaire = null;
        if (bien != null) {
            proprietaire = proprietaireBienRepository.findFirstByBienIdAndEnabledTrue(bien.getId())
                    .orElse(null);
        }

        return genererFichierInterne(bail, bien, proprietaire);
    }

    private byte[] genererFichierInterne(BailEntity bail, BienEntity bien, ProprietaireBienEntity proprio) throws Exception {
        // ATTENTION : Mise à jour du nom de fichier vers le nouveau template
        InputStream templateStream = new ClassPathResource("templates/Bail_residence_principale_TEMPLATE_docx4j.docx").getInputStream();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateStream);

        JsonNode formNode = objectMapper.readTree(bail.getReponsesBail());

        // --- ETAPE 1 : NETTOYAGE DES BLOCS (AVANT VariablePrepare) ---
        // On supprime les blocs "Société"/"Personne" et "9 ans"/"Court terme"
        // On passe l'entité 'bail' pour connaître le type de contrat
        nettoyerBlocsConditionnels(wordMLPackage, formNode, proprio, bail);

        // --- ETAPE 2 : PRÉPARATION DES VARIABLES ---
        VariablePrepare.prepare(wordMLPackage);

        // --- ETAPE 3 : REMPLISSAGE ---
        Map<String, String> variables = mapperDonneesBail(bail, formNode, bien, proprio);
        wordMLPackage.getMainDocumentPart().variableReplace(variables);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        wordMLPackage.save(outputStream);
        return outputStream.toByteArray();
    }

    private Map<String, String> mapperDonneesBail(BailEntity bail, JsonNode form, BienEntity bien, ProprietaireBienEntity proprio) {
        Map<String, String> vars = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // --- 1. LE BAILLEUR ---
        String nomBailleurJson = text(form, "/parties/bailleurs/0/details/nom");
        if (nomBailleurJson.isEmpty() && proprio != null) {
            vars.put("bailleur.nomComplet", (proprio.getProprietaireNom() != null ? proprio.getProprietaireNom() : "") + " " + (proprio.getProprietairePrenom() != null ? proprio.getProprietairePrenom() : ""));
            vars.put("bailleur.email", proprio.getProprietaireEmail() != null ? proprio.getProprietaireEmail() : "");
            vars.put("bailleur.denomination", proprio.getProprietaireEntrepriseNom() != null ? proprio.getProprietaireEntrepriseNom() : "");
            vars.put("bailleur.telephone", "");
            vars.put("bailleur.adresse", "");
            vars.put("bailleur.dateLieuNaissance", "");
            vars.put("bailleur.siegeSocial", "");
            vars.put("bailleur.bce", "");
            vars.put("bailleur.representant", "");
        } else {
            vars.put("bailleur.nomComplet", text(form, "/parties/bailleurs/0/details/nom") + " " + text(form, "/parties/bailleurs/0/details/prenom"));
            vars.put("bailleur.dateLieuNaissance", text(form, "/parties/bailleurs/0/details/dateNaissance") + " à " + text(form, "/parties/bailleurs/0/details/lieuNaissance"));
            vars.put("bailleur.adresse", text(form, "/parties/bailleurs/0/details/adresse"));
            vars.put("bailleur.email", text(form, "/parties/bailleurs/0/details/email"));
            vars.put("bailleur.telephone", text(form, "/parties/bailleurs/0/details/telephone"));
            vars.put("bailleur.denomination", text(form, "/parties/bailleurs/0/details/denomination"));
            vars.put("bailleur.siegeSocial", text(form, "/parties/bailleurs/0/details/siegeSocial"));
            vars.put("bailleur.bce", text(form, "/parties/bailleurs/0/details/bce"));
            vars.put("bailleur.representant", text(form, "/parties/bailleurs/0/details/representant"));
        }

        // --- 2. LE PRENEUR ---
        vars.put("preneur.nomComplet", text(form, "/parties/locataires/0/details/nom") + " " + text(form, "/parties/locataires/0/details/prenom"));
        vars.put("preneur.dateLieuNaissance", text(form, "/parties/locataires/0/details/dateNaissance") + " à " + text(form, "/parties/locataires/0/details/lieuNaissance"));
        vars.put("preneur.adresse", text(form, "/parties/locataires/0/details/adresse"));
        vars.put("preneur.email", text(form, "/parties/locataires/0/details/email"));
        vars.put("preneur.telephone", text(form, "/parties/locataires/0/details/telephone"));
        vars.put("preneur.denomination", text(form, "/parties/locataires/0/details/denomination"));
        vars.put("preneur.siegeSocial", text(form, "/parties/locataires/0/details/siegeSocial"));
        vars.put("preneur.bce", text(form, "/parties/locataires/0/details/bce"));
        vars.put("preneur.representant", text(form, "/parties/locataires/0/details/representant"));

        // --- 3. OCCUPANT (Nouveau) ---
        // Par défaut vide ou on reprend le preneur
        vars.put("occupant.nomComplet", "");
        vars.put("occupant.nomComplet2", "");
        vars.put("occupant.adresse", "");
        vars.put("occupant.email", "");
        vars.put("occupant.telephone", "");
        vars.put("occupant.dateLieuNaissance", "");

        // --- 4. LE BIEN ---
        String rue = (bien != null && bien.getRue() != null) ? bien.getRue() : text(form, "/bien/adresse/rue");
        String numero = (bien != null && bien.getNumero() != null) ? bien.getNumero() : text(form, "/bien/adresse/numero");
        String cp = (bien != null && bien.getCodePostal() != null) ? bien.getCodePostal() : text(form, "/bien/adresse/codePostal");
        String ville = (bien != null && bien.getVille() != null) ? bien.getVille() : text(form, "/bien/adresse/ville");
        vars.put("bien.adresseComplete", rue + " " + numero + ", " + cp + " " + ville);

        String desc = "Type : " + text(form, "/bien/description/typeBien") +
                " | Étage : " + text(form, "/bien/description/etage") +
                " | Surface : " + text(form, "/bien/description/surface") + "m²";
        vars.put("bien.descriptionBloc", desc);
        vars.put("bien.phraseMeuble", form.at("/bien/description/meuble").asBoolean() ? "Le bien est loué MEUBLÉ." : "Le bien est loué NON-MEUBLÉ.");
        vars.put("bien.pebLettre", text(form, "/bien/peb/classe"));
        vars.put("bien.pebNumero", text(form, "/bien/peb/numero"));

        // --- 5. DUREE (Mapping intelligent) ---
        vars.put("bail.destinationUsage", "Résidence Principale");
        String dateDebut = bail.getDateDebut() != null ? bail.getDateDebut().format(fmt) : "";
        String dateFin = bail.getDateFin() != null ? bail.getDateFin().format(fmt) : "";

        // On remplit TOUTES les variantes de dates, comme ça le bloc survivant aura la bonne date
        vars.put("duree.dateDebut", dateDebut);
        vars.put("duree.dateFin", dateFin);
        vars.put("duree.dateDebutLong", dateDebut);
        vars.put("duree.dateFinLong", dateFin);
        vars.put("duree.dateDebutMoins6Mois", dateDebut);
        vars.put("duree.dateFinMoins6Mois", dateFin);

        // Termes (Valeurs par défaut ou calculées à terme)
        vars.put("duree.termeLong", "9 ans");
        vars.put("duree.termeCourt", "3 ans"); // ou calcul auto
        vars.put("duree.termeMoins6Mois", "6 mois");

        boolean is9ans = "CLASSIQUE_9ANS".equals(bail.getTypeContrat());
        vars.put("duree.check9Ans", is9ans ? CHECKED : UNCHECKED);
        vars.put("duree.checkCourt", !is9ans ? CHECKED : UNCHECKED);

        // --- 6. BAIL (Pourcentages) ---
        vars.put("bail.partImmeublePct", text(form, "/bail/partImmeublePct"));
        vars.put("bail.partMeublesPct", text(form, "/bail/partMeublesPct"));
        vars.put("bail.partProChargesPct", text(form, "/bail/partProChargesPct"));
        vars.put("bail.partProLoyerPct", text(form, "/bail/partProLoyerPct"));

        // --- 7. FINANCIER & PAIEMENT ---
        double loyer = form.at("/financier/loyer/montantBase").asDouble(0.0);
        vars.put("financier.loyerMontant", String.format("%.2f", loyer));
        vars.put("financier.loyerLettres", "....................");
        vars.put("financier.compteBancaire", text(form, "/financier/loyer/compteBancaire"));
        vars.put("financier.indiceBase", text(form, "/financier/indexation/indiceBase"));
        vars.put("financier.moisIndiceBase", text(form, "/financier/indexation/moisBase"));

        String loyerRef = text(form, "/loyerReference");
        if(loyerRef.isEmpty() && bail.getLoyerReference() != null) loyerRef = bail.getLoyerReference().toString();
        vars.put("financier.loyerReference", !loyerRef.isEmpty() ? loyerRef : "N/A");
        vars.put("financier.loyerReferenceComplement", ""); // Nouveau

        vars.put("paiement.joursAvantDebut", text(form, "/financier/paiement/joursAvantDebut"));
        vars.put("paiement.joursApresDebut", text(form, "/financier/paiement/joursApresDebut"));
        vars.put("paiement.periodeMois", "1"); // Mensuel par défaut
        vars.put("paiement.modaliteAutre", "");

        // --- 8. CHARGES & REPARTITION ---
        vars.put("charges.checkProvision", "PROVISION".equals(text(form, "/financier/charges/type")) ? CHECKED : UNCHECKED);
        vars.put("charges.checkForfait", "FORFAIT".equals(text(form, "/financier/charges/type")) ? CHECKED : UNCHECKED);
        vars.put("charges.montant", text(form, "/financier/charges/montant"));
        vars.put("charges.description", text(form, "/financier/charges/details"));
        vars.put("charges.descriptionSuite", "");
        vars.put("charges.frequencePaiement", "Mensuelle");

        vars.put("repartition.superficieLogement", text(form, "/bien/description/surface"));
        vars.put("repartition.quotitesNombre", "");
        vars.put("repartition.eauVille.montant", "");
        vars.put("repartition.eauChaude.montant", "");
        vars.put("repartition.gaz.montant", "");
        vars.put("repartition.electricite.montant", "");
        vars.put("repartition.chauffage.montant", "");
        vars.put("repartition.autre.montant", "");
        vars.put("repartition.autre.libelle", "");
        vars.put("repartition.autrePrecision", "");

        vars.put("repartitionCommune.superficieLogement", "");
        vars.put("repartitionCommune.quotitesNombre", "");
        vars.put("repartitionCommune.autrePrecision", "");

        // Liste charges communes (vide par défaut)
        vars.put("chargesCommunes.ligne1Libelle", ""); vars.put("chargesCommunes.ligne1Montant", "");
        vars.put("chargesCommunes.ligne2Libelle", ""); vars.put("chargesCommunes.ligne2Montant", "");
        vars.put("chargesCommunes.ligne3Libelle", ""); vars.put("chargesCommunes.ligne3Montant", "");
        vars.put("chargesCommunes.ligne4Libelle", ""); vars.put("chargesCommunes.ligne4Montant", "");
        vars.put("chargesCommunes.ligne5Libelle", ""); vars.put("chargesCommunes.ligne5Montant", "");

        // --- 9. COMPTEURS ---
        JsonNode cpts = form.at("/bien/compteurs");
        vars.put("compteurs.eauChaudeNum", text(cpts, "/eauChaude/numero"));
        vars.put("compteurs.eauChaudeEAN", text(cpts, "/eauChaude/code"));
        vars.put("compteurs.eauFroideNum", text(cpts, "/eauFroide/numero"));
        vars.put("compteurs.eauFroideEAN", text(cpts, "/eauFroide/code"));
        vars.put("compteurs.gazNum", text(cpts, "/gaz/numero"));
        vars.put("compteurs.gazEAN", text(cpts, "/gaz/code"));
        vars.put("compteurs.elecJourNum", text(cpts, "/electriciteJour/numero"));
        vars.put("compteurs.elecJourEAN", text(cpts, "/electriciteJour/code"));
        vars.put("compteurs.elecNuitNum", text(cpts, "/electriciteNuit/numero"));
        vars.put("compteurs.elecNuitEAN", text(cpts, "/electriciteNuit/code"));
        vars.put("compteurs.autreNum", "");
        vars.put("compteurs.autreEAN", "");

        // --- 10. GARANTIE ---
        vars.put("garantie.montant", text(form, "/financier/garantie/montant"));
        vars.put("garantie.montantLettres", "....................");
        String typeGarantie = text(form, "/financier/garantie/type");
        vars.put("garantie.checkCompteBloque", "COMPTE_BLOQUE".equals(typeGarantie) ? CHECKED : UNCHECKED);
        vars.put("garantie.checkGarantieBancaire", "GARANTIE_BANCAIRE".equals(typeGarantie) ? CHECKED : UNCHECKED);
        vars.put("garantie.checkAutre", UNCHECKED);

        // --- 11. DIVERS (Entretien, Expert, Assurances, etc.) ---
        vars.put("interets.delaiApresEcheance", "");
        vars.put("interets.delaiApresMiseEnDemeure", "");

        vars.put("expert.nomComplet", text(form, "/edl/expert/nom")); // Si dispo dans form
        vars.put("entretien.chauffage.frequence", "Annuelle");
        vars.put("entretien.chauffeEau.frequence", "Annuelle");
        vars.put("entretien.autre.element", "");
        vars.put("entretien.autre.frequence", "");

        vars.put("travaux.indemniteReference", "");
        vars.put("travaux.limiteLigne1", "");
        vars.put("travaux.limiteLigne2", "");
        vars.put("travaux.limiteLigne3", "");

        vars.put("renovation.periode", "");
        vars.put("renovation.reductionLoyer", "");
        vars.put("renovation.remiseMontant", "");
        vars.put("renovation.renonciationRevisionPeriode", "");
        vars.put("renovation.travauxDescriptionLigne1", "");
        vars.put("renovation.travauxDescriptionSuite", "");

        vars.put("visites.affichageMoisAvant", "3");
        vars.put("visites.heuresConsecutivesParJour", "2");
        vars.put("visites.joursParSemaine", "3");
        vars.put("visites.preavisJours", "3");

        vars.put("assurance.autreDescriptionLigne1", "");
        vars.put("assurance.autreDescriptionLigne2", "");

        vars.put("notaires.noms", text(form, "/notaires/noms"));

        vars.put("conditionsParticulieres.ligne1", text(form, "/conditionsParticulieres/ligne1"));
        vars.put("conditionsParticulieres.ligne2", text(form, "/conditionsParticulieres/ligne2"));
        vars.put("conditionsParticulieres.ligne3", text(form, "/conditionsParticulieres/ligne3"));

        // --- 12. SIGNATURE ---
        vars.put("signature.ville", "Bruxelles");
        vars.put("signature.date", java.time.LocalDate.now().format(fmt));

        vars.replaceAll((k, v) -> v == null ? "" : v);
        return vars;
    }

    private void nettoyerBlocsConditionnels(WordprocessingMLPackage pkg, JsonNode formNode,
                                            ProprietaireBienEntity proprio, BailEntity bail) {
        // --- 1. PROPRIÉTAIRE (Société vs Physique) ---
        boolean isBailleurSociete = false;
        String typeJson = formNode.at("/parties/bailleurs/0/type").asText();
        if (!typeJson.isEmpty()) {
            isBailleurSociete = "SOCIETE".equals(typeJson) || "PERSONNE_MORALE".equals(typeJson);
        } else if (proprio != null) {
            String typeDb = proprio.getProprietaireType();
            isBailleurSociete = "ENTREPRISE".equalsIgnoreCase(typeDb) || "SOCIETE".equalsIgnoreCase(typeDb);
        }

        if (isBailleurSociete) {
            DocxUtils.removeSdtByTag(pkg, "bailleur.personne");
        } else {
            DocxUtils.removeSdtByTag(pkg, "bailleur.societe");
        }

        // --- 2. PRENEUR (Société vs Physique) ---
        boolean isPreneurSociete = formNode.at("/parties/locataires/0/details/denomination").isTextual()
                && !formNode.at("/parties/locataires/0/details/denomination").asText().isEmpty();

        if (isPreneurSociete) {
            DocxUtils.removeSdtByTag(pkg, "preneur.personne");
        } else {
            DocxUtils.removeSdtByTag(pkg, "preneur.societe");
        }

        // --- 3. DUREE (9 ans vs Court Terme) ---
        // On vérifie le type de contrat pour supprimer le mauvais bloc
        boolean is9ans = "CLASSIQUE_9ANS".equals(bail.getTypeContrat());
        if (is9ans) {
            DocxUtils.removeSdtByTag(pkg, "clause.duree.court"); // On enlève le court terme
        } else {
            DocxUtils.removeSdtByTag(pkg, "clause.duree.9ans"); // On enlève le 9 ans
        }
    }

    private String text(JsonNode node, String path) {
        JsonNode n = node.at(path);
        return n.isMissingNode() || n.isNull() ? "" : n.asText();
    }
}