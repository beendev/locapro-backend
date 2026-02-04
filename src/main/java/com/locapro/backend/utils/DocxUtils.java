package com.locapro.backend.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tag;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire technique pour la manipulation bas niveau des documents Word (OpenXML).
 * Permet la gestion dynamique du contenu (suppression conditionnelle de clauses).
 */
public class DocxUtils {

    /**
     * Point d'entrée principal : Supprime un bloc de contenu (SdtBlock) identifié par son Tag.
     * <p>
     * Cette méthode est utilisée pour la logique conditionnelle du bail :
     * si une condition n'est pas remplie (ex: pas de meubles), on supprime le bloc correspondant.
     *
     * @param pkg    Le document Word chargé en mémoire.
     * @param tagVal La valeur de l'étiquette (Tag) à chercher et supprimer (ex: "clause_meubles").
     */
    public static void removeSdtByTag(WordprocessingMLPackage pkg, String tagVal) {
        // 1. On lance une recherche récursive pour trouver tous les Contrôles de Contenu (SDT)
        // peu importe où ils sont cachés (tableaux, corps du texte, etc.).
        List<Object> sdtElements = getAllElementFromObject(pkg.getMainDocumentPart(), SdtElement.class);

        // 2. On parcourt les éléments trouvés
        for (Object obj : sdtElements) {
            SdtElement sdt = (SdtElement) obj;

            // 3. On vérifie si l'élément possède un Tag et si ce Tag correspond à celui demandé
            if (sdt.getSdtPr() != null && sdt.getSdtPr().getTag() != null) {
                Tag tag = sdt.getSdtPr().getTag();

                if (tagVal.equals(tag.getVal())) {
                    // 4. Si correspondance trouvée, on procède à la suppression physique du nœud XML
                    removeElementFromParent(sdt);
                }
            }
        }
    }

    /**
     * Méthode "chirurgicale" qui détache un élément enfant de son parent dans l'arbre XML.
     *
     * @param child L'objet (nœud) à supprimer.
     */
    private static void removeElementFromParent(Object child) {
        Object parent = null;

        // 1. On tente d'identifier le parent de l'élément grâce à l'interface JAXB
        if (child instanceof org.jvnet.jaxb2_commons.ppp.Child) {
            parent = ((org.jvnet.jaxb2_commons.ppp.Child) child).getParent();
        }

        // 2. Si le parent est un conteneur de contenu (ContentAccessor), on retire l'enfant de sa liste
        if (parent instanceof ContentAccessor) {
            ((ContentAccessor) parent).getContent().remove(child);
        }
    }

    /**
     * Algorithme de Recherche Récursive (Deep Search).
     * <p>
     * Traverse l'intégralité de l'arbre d'objets du document pour trouver tous les éléments
     * d'un type spécifique, quelle que soit leur profondeur.
     *
     * @param obj      L'objet racine où commence la recherche (ex: le document entier).
     * @param toSearch La classe de l'objet qu'on cherche (ex: SdtElement.class).
     * @return Une liste de tous les objets trouvés correspondants au type.
     */
    private static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();

        // Gestion des éléments enveloppés dans un JAXBElement (spécifique au XML)
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

        // 1. Condition d'arrêt / Succès : Si l'objet actuel est ce qu'on cherche, on l'ajoute
        if (toSearch.isInstance(obj)) {
            result.add(obj);
        }

        // 2. Récursivité : Si l'objet est un conteneur (ex: Tableau, Paragraphe), on fouille ses enfants
        if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                // Appel récursif : la fonction s'appelle elle-même pour descendre dans l'arbre
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }
}