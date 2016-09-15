package no.npolar.common.forms;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for form input types that have pre-defined values.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class A_InputTypePreDefined extends A_FormInputElement {
    
    /**
     * Adds a new option to this html select element.
     * 
     * @param value  The option's value.
     * @param text  The option's displayable text.
     * @param selected  True if this option is to be the selected one, false if not.
     */
    public void addOption(String value, String text, boolean selected) {
        this.options.add(new Option(value, text, selected));
    }
    
    /**
     * Gets the options of this input element.
     * <p>
     * The list returned contains all options as {@link Option} objects.
     * 
     * @return  The options of this form element, or <code>null</code> if none.
     * @see Option
     */
    @Override
    public List getOptions() { return this.options; }
    
    /**
     * Gets the text for an option, given the option's value.
     * <p>
     * If more than one {@link Option} has the same value, the first encountered 
     * option with the given value is matched.
     * 
     * @param optionValue  The value of the option to get the text for.
     * @return  The text for the option with the given value.
     * @see Option
     */
    @Override
    public String getTextForOption(String optionValue) {
        Option option = null;
        Iterator itr = this.options.iterator();
        while (itr.hasNext()) {
            option = (Option)itr.next();
            if (option.getValue().equals(optionValue)) {
                return option.getText();
            }
        }
        return null;
    }
    
    /**
     * Get the submitted values in human readable format, either formatted 
     * as a list or comma-separated text.
     * 
     * @param list  Provide <code>true</code> to generate an unordered list, or <code>false</code> to generate a comma-separated text.
     * @return  The submitted values in human readable format.
     */
    public String getSubmittedOptionsAsLabels(boolean list) {
        String html = list ? "<ul><li>" : "";
        String[] submittedOptions = this.getSubmission();
        
        if (submittedOptions == null || submittedOptions.length < 1) {
            return "";
        }
        
        for (int i = 0; i < submittedOptions.length; i++) {
            html += this.getTextForOption(submittedOptions[i]);
            html += list ? "</li>" : "";
            if (i+1 < submittedOptions.length) {
                html += list ? "<li>" : ", ";
            }
        }
        html += list ? "</li></ul>" : "";
        return html;
    }
        
    /**
     * This method does nothing, since this input type has pre-defined values.
     * 
     * @param unique Irrelevant.
     */
    @Override
    public void setUnique(boolean unique) { 
        // Do nothing, multiple choice values should never be unique.
    }
    
    /**
     * Will always return <code>false</code>.
     * 
     * @return Always <code>false</code>, as this input type that has pre-defined values.
     */
    @Override
    public boolean isUnique() { return false; }
    
    /**
     * @see I_FormInputElement#isPreDefinedValueInput() 
     * @return Always returns <code>true</code>.
     */
    @Override
    public boolean isPreDefinedValueInput() { return true; }
    
    /**
     * Comparator used to sort options by their display text (label) 
     * alphabetically.
     * 
     * @return A comparator that can be used to sort options by their display text (label) alphabetically.
     */
    public Comparator<Option> sortAlphabeticallyByDisplayText() {
        return new Comparator<Option>() {
            @Override
            public int compare(Option one, Option another) {
                return one.getText().compareTo(another.getText());
            }
        };
    }
    
    /**
     * Comparator used to sort options by their display text (label) 
     * alphabetically, descending.
     * 
     * @return A comparator that can be used to sort options by their display text (label) alphabetically, descending.
     */
    public Comparator<Option> sortAlphabeticallyByDisplayTextDesc() {
        return new Comparator<Option>() {
            @Override
            public int compare(Option one, Option another) {
                return another.getText().compareTo(one.getText());
            }
        };
    }
}
