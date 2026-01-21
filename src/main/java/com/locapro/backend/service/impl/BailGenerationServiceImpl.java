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
    private final ProprietaireBienRepository proprietaireBienRepository; // <--- NOUVEAU

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
        InputStream templateStream = new ClassPathResource("templates/Bail_residence_principale_mapped_sdt.docx").getInputStream();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateStream);

        JsonNode formNode = objectMapper.readTree(bail.getReponsesBail());

        // --- ETAPE 1 : NETTOYAGE DES BLOCS (AVANT VariablePrepare) ---
        // On supprime les blocs "Société" ou "Personne" tant que les balises existent encore.
        nettoyerBlocsConditionnels(wordMLPackage, formNode, proprio);

        // --- ETAPE 2 : PRÉPARATION DES VARIABLES ---
        // Maintenant que le doc est propre, on prépare le remplacement des ${...}
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
        // Priorité au JSON, sinon Fallback sur la BDD (ProprietaireBienEntity)
        String nomBailleurJson = text(form, "/parties/bailleurs/0/details/nom");

        if (nomBailleurJson.isEmpty() && proprio != null) {
            // Remplissage depuis la BDD si le JSON est vide
            vars.put("bailleur.nomComplet", (proprio.getProprietaireNom() != null ? proprio.getProprietaireNom() : "") + " " + (proprio.getProprietairePrenom() != null ? proprio.getProprietairePrenom() : ""));
            vars.put("bailleur.email", proprio.getProprietaireEmail() != null ? proprio.getProprietaireEmail() : "");

            // Si c'est une entreprise
            vars.put("bailleur.denomination", proprio.getProprietaireEntrepriseNom() != null ? proprio.getProprietaireEntrepriseNom() : "");

            // Champs vides par défaut s'ils ne sont pas dans ProprietaireBienEntity
            vars.put("bailleur.telephone", "");
            vars.put("bailleur.adresse", "");
            vars.put("bailleur.dateLieuNaissance", "");
            vars.put("bailleur.siegeSocial", "");
            vars.put("bailleur.bce", "");
            vars.put("bailleur.representant", "");
        } else {
            // Remplissage depuis le JSON
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

        // --- 2. LE PRENEUR (JSON) ---
        vars.put("preneur.nomComplet", text(form, "/parties/locataires/0/details/nom") + " " + text(form, "/parties/locataires/0/details/prenom"));
        vars.put("preneur.dateLieuNaissance", text(form, "/parties/locataires/0/details/dateNaissance") + " à " + text(form, "/parties/locataires/0/details/lieuNaissance"));
        vars.put("preneur.adresse", text(form, "/parties/locataires/0/details/adresse"));
        vars.put("preneur.email", text(form, "/parties/locataires/0/details/email"));
        vars.put("preneur.telephone", text(form, "/parties/locataires/0/details/telephone"));
        vars.put("preneur.denomination", text(form, "/parties/locataires/0/details/denomination"));
        vars.put("preneur.siegeSocial", text(form, "/parties/locataires/0/details/siegeSocial"));
        vars.put("preneur.bce", text(form, "/parties/locataires/0/details/bce"));
        vars.put("preneur.representant", text(form, "/parties/locataires/0/details/representant"));

        // --- 3. LE BIEN ---
        // On prend d'abord l'adresse du bien en BDD si dispo, sinon JSON
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

        // --- 4. DUREE ---
        vars.put("bail.destinationUsage", "Résidence Principale");
        vars.put("duree.dateDebut", bail.getDateDebut() != null ? bail.getDateDebut().format(fmt) : "");
        vars.put("duree.dateFin", bail.getDateFin() != null ? bail.getDateFin().format(fmt) : "");

        boolean is9ans = "CLASSIQUE_9ANS".equals(bail.getTypeContrat());
        vars.put("duree.check9Ans", is9ans ? CHECKED : UNCHECKED);
        vars.put("duree.checkCourt", !is9ans ? CHECKED : UNCHECKED);

        // --- 5. FINANCIER ---
        // On lit le JSON. Si vide, on met 0.00 car BailEntity n'a pas de loyerBase.
        double loyer = form.at("/financier/loyer/montantBase").asDouble(0.0);
        vars.put("financier.loyerMontant", String.format("%.2f", loyer));
        vars.put("financier.loyerLettres", "....................");
        vars.put("financier.compteBancaire", text(form, "/financier/loyer/compteBancaire"));

        vars.put("financier.indiceBase", text(form, "/financier/indexation/indiceBase"));
        String moisIndice = text(form, "/financier/indexation/moisBase");
        // Fallback sur le champ BDD de BailEntity s'il est null dans le JSON
        // Attention: Ton entité BailEntity n'a PAS de getMoisIndiceBase(). On utilise le JSON uniquement.
        vars.put("financier.moisIndiceBase", moisIndice);

        String loyerRef = text(form, "/loyerReference");
        if(loyerRef.isEmpty() && bail.getLoyerReference() != null) loyerRef = bail.getLoyerReference().toString();
        vars.put("financier.loyerReference", !loyerRef.isEmpty() ? loyerRef : "N/A");

        // --- 6. CHARGES ---
        vars.put("charges.checkProvision", "PROVISION".equals(text(form, "/financier/charges/type")) ? CHECKED : UNCHECKED);
        vars.put("charges.checkForfait", "FORFAIT".equals(text(form, "/financier/charges/type")) ? CHECKED : UNCHECKED);
        vars.put("charges.montant", text(form, "/financier/charges/montant"));
        vars.put("charges.description", text(form, "/financier/charges/details"));

        // --- 7. COMPTEURS ---
        JsonNode cpts = form.at("/bien/compteurs"); // Chemin corrigé pour matcher ton JSON précédent
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

        // --- 8. GARANTIE ---
        vars.put("garantie.montant", text(form, "/financier/garantie/montant"));
        vars.put("garantie.montantLettres", "....................");
        String typeGarantie = text(form, "/financier/garantie/type");
        vars.put("garantie.checkCompteBloque", "COMPTE_BLOQUE".equals(typeGarantie) ? CHECKED : UNCHECKED);
        vars.put("garantie.checkGarantieBancaire", "GARANTIE_BANCAIRE".equals(typeGarantie) ? CHECKED : UNCHECKED);
        vars.put("garantie.checkAutre", UNCHECKED);

        // --- 9. SIGNATURE ---
        vars.put("signature.ville", "Bruxelles");
        vars.put("signature.date", java.time.LocalDate.now().format(fmt));

        vars.replaceAll((k, v) -> v == null ? "" : v);
        return vars;
    }

    private void nettoyerBlocsConditionnels(WordprocessingMLPackage pkg, JsonNode formNode, ProprietaireBienEntity proprio) {
        // --- Logique Bailleur ---
        boolean isBailleurSociete = false;

        // 1. Check JSON
        String typeJson = formNode.at("/parties/bailleurs/0/type").asText();
        if (!typeJson.isEmpty()) {
            isBailleurSociete = "SOCIETE".equals(typeJson) || "PERSONNE_MORALE".equals(typeJson);
        }
        // 2. Fallback BDD (ProprietaireBienEntity)
        else if (proprio != null) {
            String typeDb = proprio.getProprietaireType(); // "PERSONNE" ou "ENTREPRISE"
            isBailleurSociete = "ENTREPRISE".equalsIgnoreCase(typeDb) || "SOCIETE".equalsIgnoreCase(typeDb);
        }

        if (isBailleurSociete) {
            DocxUtils.removeSdtByTag(pkg, "bailleur.personne");
        } else {
            DocxUtils.removeSdtByTag(pkg, "bailleur.societe");
        }

        // --- Logique Preneur (Reste sur JSON) ---
        boolean isPreneurSociete = formNode.at("/parties/locataires/0/details/denomination").isTextual()
                && !formNode.at("/parties/locataires/0/details/denomination").asText().isEmpty();

        if (isPreneurSociete) {
            DocxUtils.removeSdtByTag(pkg, "preneur.personne");
        } else {
            DocxUtils.removeSdtByTag(pkg, "preneur.societe");
        }
    }

    private String text(JsonNode node, String path) {
        JsonNode n = node.at(path);
        return n.isMissingNode() || n.isNull() ? "" : n.asText();
    }
}