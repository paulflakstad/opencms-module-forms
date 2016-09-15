package no.npolar.common.forms;

import java.util.Iterator;

/**
 * Base class for form input types that are enabled for only single selection 
 * from a range of pre-defined values.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class A_InputTypeSingleSelect extends A_InputTypePreDefined {
    
    /**
     * @see I_FormInputElement#submit(java.lang.String[]) 
     */
    @Override
    public void submit(String[] values) {
        this.submission = null;
        if (values.length > 1) {
            this.hasValidSubmit = false; // Ambiguous submit (too many values, should be only one).
            //this.error = "Too many values submitted.";
            this.error = Messages.get().container(Messages.ERR_TOO_MANY_VALUES_0).key(this.getContainingForm().getLocale());
        } 
        
        else if (values.length != 1) {
            this.hasValidSubmit = false; // No value
            //this.error = "No value submitted.";
            this.error = Messages.get().container(Messages.ERR_NO_VALUE_0).key(this.getContainingForm().getLocale());
        } 
        
        else if (this.required && values[0].trim().length() == 0) {
            this.hasValidSubmit = false; // Field is required, but value is missing (or only whitespace)
            //this.error = "This is a required field. Please select one of the options.";
            this.error = Messages.get().container(Messages.ERR_REQUIRED_FIELD_MISSING_0).key(this.getContainingForm().getLocale());
        } 
        
        else if (values[0].trim().length() > getMaxLength()) {
            this.hasValidSubmit = false;
            this.error = Messages.get().container(Messages.ERR_VALUE_TOO_LONG_1, String.valueOf(getMaxLength())).key(this.getContainingForm().getLocale());
        }
        
        else {
            this.submission = values;
            // Iterate over all options and update "selected"
            Iterator i = options.iterator();
            Option option = null;
            while (i.hasNext()) {
                option = (Option)i.next();
                if (option.getValue().equals(values[0])) {
                    option.setSelected(true);
                } else {
                    option.setSelected(false);
                }
            }
            this.hasValidSubmit = true;
            this.error = null;
        }
    }
}
