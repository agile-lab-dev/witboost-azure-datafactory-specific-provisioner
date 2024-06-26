package it.agilelab.witboost.datafactory.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String sanitize(String unsafe, int maxLength) {
        Pattern pattern = Pattern.compile("[^a-z0-9]");
        Matcher matcher = pattern.matcher(unsafe.toLowerCase());
        String cleanedInput = matcher.replaceAll("");
        if (cleanedInput.length() > maxLength) {
            cleanedInput = cleanedInput.substring(0, maxLength);
        }
        return cleanedInput;
    }
}
