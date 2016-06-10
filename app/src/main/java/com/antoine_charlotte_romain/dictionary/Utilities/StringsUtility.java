package com.antoine_charlotte_romain.dictionary.Utilities;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Created by summer1 on 18/08/2015.
 */
public class StringsUtility {

    public static String removeAccents(String text) {
        return text == null ? null : Normalizer.normalize(text, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
