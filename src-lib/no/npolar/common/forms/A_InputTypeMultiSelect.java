package no.npolar.common.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base class for form input types that are enabled for multiple selection from 
 * a range of pre-defined values.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class A_InputTypeMultiSelect extends A_InputTypePreDefined {
    
    /** 
     * The value delimiter, or, the character used to separate individual 
     * values when storing multiple values as a single string.
     */
    public static final String VALUE_SEPARATOR = "|";
    
    /** A regex-safe representation of the value delimiter. */
    public static final String VALUE_SEPARATOR_REGEX_ESCAPED = "\\|";
    
    /**
     * Gets a list of this form element's selected options.
     * <p>
     * Each object in the returned list is of type {@link Option}. If no options 
     * are selected, an empty list is returned.
     * 
     * @return  The element's selected options, or an empty list if none.
     */
    public List getSelectedOptions() {
        ArrayList selectedOptions = new ArrayList();
        // Iterate over all options and get those that are "selected"
        Iterator itr = this.options.iterator();
        Option option = null;
        while (itr.hasNext()) {
            option = (Option)itr.next();
            if (option.isSelected()) {
                selectedOptions.add(option);
            }
        }
        return selectedOptions;
    }
    
    /**
     * @see I_FormInputElement#recreate(java.sql.ResultSet) 
     */
    @Override
    public void recreate(ResultSet rs) throws SQLException {
        if (rs.first()) {
            String val = rs.getString(this.name);
            if (val != null) {
                String[] values = null;
                if (val.indexOf(VALUE_SEPARATOR) != -1) { // Multiple values selected
                    values = val.split(VALUE_SEPARATOR_REGEX_ESCAPED);
                }
                else {
                    values = new String[] { val };
                }
                this.submit(values);
            }
        } else {
            throw new SQLException("No data to recreate form element '" + name + "' from.");
        }
    }
    
    /**
     * Determines whether or not the current submission for this input element 
     * contains multiple values.
     * <p>
     * Assumes that a form submission is currently available. If not, this 
     * method will always return <code>false</code>.
     * 
     * @return  <code>true</code> if the current submission contains multiple values, <code>false</code> if not.
     */
    public boolean hasMultipleValues() {
        try {
            if (this.getSubmission().length > 1) {
                return true;
            }
        } catch (Exception e) {
            // Unable to get the length of the submission array ==> No submission exists, so definitely no multiple values stored here
            return false;
        }
        return false;
    }
}
