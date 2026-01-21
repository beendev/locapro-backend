package com.locapro.backend.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Tag;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.util.ArrayList;
import java.util.List;

public class DocxUtils {

    /**
     * Supprime un Content Control (SdtBlock) identifié par son Tag.
     */
    public static void removeSdtByTag(WordprocessingMLPackage pkg, String tagVal) {
        List<Object> sdtElements = getAllElementFromObject(pkg.getMainDocumentPart(), SdtElement.class);

        for (Object obj : sdtElements) {
            SdtElement sdt = (SdtElement) obj;
            if (sdt.getSdtPr() != null && sdt.getSdtPr().getTag() != null) {
                Tag tag = sdt.getSdtPr().getTag();
                if (tagVal.equals(tag.getVal())) {
                    removeElementFromParent(sdt);
                }
            }
        }
    }

    private static void removeElementFromParent(Object child) {
        Object parent = null;
        if (child instanceof org.jvnet.jaxb2_commons.ppp.Child) {
            parent = ((org.jvnet.jaxb2_commons.ppp.Child) child).getParent();
        }

        if (parent instanceof ContentAccessor) {
            ((ContentAccessor) parent).getContent().remove(child);
        }
    }

    private static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

        if (toSearch.isInstance(obj)) {
            result.add(obj);
        }

        if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }
}