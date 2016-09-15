package no.npolar.common.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities and helpers for this package.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FormUtil {
    
    /**
     * Creates a new blank instance.
     */
    public FormUtil() {}
    
    /**
     * Converts a multiple value string into a list.
     * <p>
     * This method expects the separator character to be 
     * {@link A_InputTypeMultiSelect#VALUE_SEPARATOR}.
     * <p>
     * If the value is not a multiple value string, but in fact just a singular 
     * value, a list with 1 item (the given string itself) is returned.
     * 
     * @param valueString The string to convert.
     * @return A list which separates all individual values in the given string.
     */
    public static List<String> getMultipleValues(String valueString) {
        return new ArrayList<String>( Arrays.asList(valueString.split(A_InputTypeMultiSelect.VALUE_SEPARATOR_REGEX_ESCAPED)) );
    }
    
    /**
     * Determines if a given string contains just a singular value or multiple 
     * values.
     * <p>
     * This method expects the separator character to be 
     * {@link A_InputTypeMultiSelect#VALUE_SEPARATOR}.
     * 
     * @param valueString The string to check.
     * @return <code>true</code> if the given string contains multiple values, <code>false</code> if not.
     */
    public static boolean isMultipleValueString(String valueString) {
        return valueString.indexOf(A_InputTypeMultiSelect.VALUE_SEPARATOR) != -1 ? true : false;
    }
    
    /**
     * @see InputTypeSelectSingleCountry#getCountry(java.lang.String)
     */
    public static String getDisplayCountry(String isoCode) {
        // Just a shortcut method...
        return InputTypeSelectSingleCountry.getCountry(isoCode);
    }
    
    /**
     * Determines if a string can be parsed as an integer.
     * 
     * @param s The string to check.
     * @return <code>true</code> if the given string can be parsed as an integer, <code>false</code> if not.
     */
    public static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Determines if a string is of the format required for entry IDs, that is,
     * if it is not <code>null</code>, not empty, and numeric.
     * 
     * @param id The string to check.
     * @return <code>false</code> if the given ID is <code>null</code>, empty, or non-numeric. <code>true</code> otherwise.
     */
    public static boolean isValidFormatForId(String id) {
        if (id == null
                || id.isEmpty()
                || !isNumeric(id)) {
            return false;
        }
        return true;
    }
    
    /**
     * Find all macros present in a given text.
     * <p>
     * Example macros: %(email_addr) %(name) %(StreetAddress).
     * 
     * @param text The text to check - may or may not contain macros.
     * @return A list of the macros present in the given text, or an empty list if none.
     */
    public static List<String> getMacros(String text) {
        String regex = "%\\((\\w)++\\)"; // Note: \w is equal to [a-zA-Z_0-9]
        List<String> macrosPresent = new ArrayList<String>();

        Matcher m = Pattern.compile(regex).matcher(text);

        while (m.find()) {
            String match = text.substring(m.start(), m.end());
            String matchStrVal = match.substring(match.indexOf("(") + 1, match.length() - 1); // Turn "%(macro_name)" into "macro_name"
            if (!macrosPresent.contains(matchStrVal)) {
                macrosPresent.add(matchStrVal); // Add "macro_name" to the list of macros
            }
        }

        return macrosPresent;
    }
}
