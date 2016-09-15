package no.npolar.common.forms;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Country drop-down.
 * <p>
 * Specialized implementation of {@link InputTypeSelect}, where only a single 
 * item (country) can be selected.
 * <p>
 * Each country's stored value will be its 2-letter country code. The list of 
 * countries can be maintained in the .properties file of this package.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public final class InputTypeSelectSingleCountry extends InputTypeSelect {
    
    /** The countries base string, from which all country options are created. */
    protected final String COUNTRIES_BASE = 
            Messages.get().container(Messages.DATA_COUNTRIES_0).key(this.getContainingForm().getLocale());
    
    /**
     * Creates a new country select element, with standard options.
     */
    public InputTypeSelectSingleCountry() {
        super();
        this.type = I_FormInputElement.SELECT_SINGLE_COUNTRY;
        this.options = new ArrayList<Option>();
        this.options.addAll(this.getCountryOptions());
    }
    
    /**
     * Creates a new country selector input element, with standard options.
     * 
     * @param name The name of the select element.
     * @param label The label of the select element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeSelectSingleCountry(String name, String label, boolean required, Form form) {
        super(name, label, required, form);
        this.options = new ArrayList<Option>();
        this.options.addAll(this.getCountryOptions());
    }
    
    /**
     * Creates a new country select element, with the given options plus the 
     * standard options.
     * 
     * @param name The name of the select element.
     * @param label The label of the select element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param options List of options, as {@link Option} instances.
     */
    public InputTypeSelectSingleCountry(String name, String label, boolean required, List options, Form form) {
        super(name, label, required, options, form);
        this.type = I_FormInputElement.SELECT_SINGLE_COUNTRY;
        this.options = options != null ? options : new ArrayList<Option>();
        this.options.addAll(this.getCountryOptions());
    }
    
    /**
     * Creates an option for each country listed in {@link #COUNTRIES_BASE}.
     * 
     * The value for 
     * 
     * @return A list of country options.
     * @see #COUNTRIES_BASE
     */
    protected List<Option> getCountryOptions() {
        List countries = new ArrayList<Option>();
        String[] entries = COUNTRIES_BASE.split("\\|");
        String s = "";
        
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            String[] isoAndCountry = entry.split(":");
            try {
                String iso = isoAndCountry[0].toLowerCase();
                String country = isoAndCountry[1];
                countries.add(new Option(iso, country, false));
                
            } catch (IndexOutOfBoundsException e) {
                // Should _NEVER_ happen (would be a result of an error in the base string)
            }
        }
        Collections.sort(countries, sortAlphabeticallyByDisplayText());
        return countries;
    }
    
    /**
     * Returns the display name for a country, based on a two-letter ISO code.
     * <p>
     * (E.g. "no" will return "Norway", "us" will return "United States", and 
     * so on.
     * 
     * @param isoCode The two-letter ISO code.
     * @return The display name for the corresponding country.
     */
    public String getDisplayCountry(String isoCode) {
        String[] entries = COUNTRIES_BASE.split("\\|");
        
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            String[] isoAndCountry = entry.split(":");
            try {
                String iso = isoAndCountry[0].toLowerCase();
                String country = isoAndCountry[1];
                if (iso.equalsIgnoreCase(isoCode)) {
                    return country;
                }                
            } catch (IndexOutOfBoundsException e) {
                // Should _NEVER_ happen (would be a result of an error in the base string)
            }
        }
        return "";
    }
    
    /**
     * @see InputTypeSelectSingleCountry#getDisplayCountry(java.lang.String)
     */
    public static String getCountry(String isoCode) {
        // This is just a wrapper method, impossible (?) to make the "actual" method static.
        return new InputTypeSelectSingleCountry().getDisplayCountry(isoCode);
    }
    
    /*
    // Identical to InputTypeSelect#getHtml
    @Override
    public String getHtml(boolean xhtmlSyntax) {
        String html = beginElementHtml();
        html += "<select " + getNameAndIdHtmlAttributes() + ">";
        //html += "<select name=\"" + name + "\">";
        
        // Iterate over all options
        Iterator i = options.iterator();
        Option option = null;
        while (i.hasNext()) {
            option = (Option)i.next();
            html += "<option value=\"" + option.getValue() + "\"";
            if (option.isSelected())
                html += " selected=\"selected\"";
            html += ">" + option.getText() + "</option>";
        }
        
        html += "</select>";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    */
    
}
