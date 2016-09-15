package no.npolar.common.forms;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a <code>checkbox</code> group (multiple choice) in a form.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeCheckbox extends A_InputTypeMultiSelect {
    
    public static final int MAX_LENGTH = 511;
    
    /**
     * Creates a new, blank <code>checkbox</code> input element.
     */
    public InputTypeCheckbox() { 
        this.type = I_FormInputElement.CHECKBOX;
        this.mc = true;
    }
    
    /**
     * Constructor, creates a new <code>checkbox</code> input element, without 
     * options.
     * 
     * @param name The name of the select element.
     * @param label The label of the select element.
     * @param form The containing form.
     * @param required  <code>true</code> if this form element is required, <code>false</code> if not.
     */
    public InputTypeCheckbox(String name, String label, boolean required, Form form) {
        this.type = I_FormInputElement.CHECKBOX;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.options = new ArrayList<Option>();
        this.mc = true;
    }
    
    /**
     * Creates a new <code>checkbox</code> input element, with options.
     * 
     * @param name  The name of the select element.
     * @param label  The label of the select element.
     * @param required  <code>true</code> if this form element is required, <code>false</code> if not.
     * @param options  List of options, as {@link Option} instances.
     * @param form The containing form.
     */
    public InputTypeCheckbox(String name, String label, boolean required, List options, Form form) {
        this.type = I_FormInputElement.CHECKBOX;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.options = options != null ? options : new ArrayList<Option>();
        this.mc = true;
    }
    
    /**
     * @see I_FormInputElement#getTypeName() 
     */
    /*@Override
    public String getTypeName() {
        return "checkbox";
    }*/
    
    /**
     * @see I_FormInputElement#getHtml(boolean) 
     */
    @Override
    public String getHtml(boolean xhtmlSyntax) {
        /*
        String html = "<div class=\"element\">";
        if (information != null)
            html += "<div class=\"information\">" + information + "</div>";
        html += "<div class=\"label\">" + (required ? "<span class=\"required\">*</span>" : "") + 
                (error != null ? ("<span class=\"error\">" + label + "</span>") : label) +
                (error != null ? ("<span class=\"errormsg\">" + error + "</span>") : "") +
                (helpText != null ? ("<span class=\"help-text\">" + helpText + "</span>") : "") +
                "</div>";
        html += "<div class=\"input\">\n<!-- " + options.size() + " checkboxes: -->\n";
        */
        String html = beginElementHtml() + "\n<!-- " + options.size() + " checkboxes: -->\n";
        // Iterate over all options
        Iterator i = options.iterator();
        Option option = null;
        while (i.hasNext()) {
            option = (Option)i.next();
            String optionId = name + "_" + option.getValue();
            html += "<input type=\"" + getTypeName() + "\""
                    + " name=\"" + getName() + "\""
                    + " value=\"" + option.getValue() + "\""
                    + " id=\"" + optionId + "\""
                    + (option.isSelected() ? " checked=\"checked\"" : "");
            //html += "<input type=\"checkbox\" name=\"" + name + "\" value=\"" + option.getValue() + "\" id=\"" + optionId + "\"";
            
            //if (option.isSelected()) 
            //    html += " checked=\"checked\"";
            
            html += (xhtmlSyntax ? " /" : "") + ">\n<label for=\"" + optionId + "\">" + option.getText() + "</label>";
            //html += (xhtmlSyntax ? " /" : "") + "> <label for=\"" + optionId + "\">" + option.getText() + "</label><br" + (xhtmlSyntax ? "/" : "") + ">";
        }
        //html += "\n<!-- done printing checkboxes -->\n";
        //html += "</div></div>";
        html += endElementHtml();
        
        return html;
    }
    
    /**
     * @see I_FormInputElement#submit(java.lang.String[]) 
     */
    @Override
    public void submit(String[] values) {
        submission = null;
        boolean missingValue = true;
        // Loop over all submitted values and check the length. If at least one has 
        // a length longer than zero, value(s) are not missing.
        for (int i = 0; i < values.length; i++) {
            if (values[i].trim().length() != 0) {
                missingValue = false;
                break;
            }
        }
        
        // If none of the submitted values had a real value
        if (missingValue) {
            // Update the "valid submit" variable
            if (this.required) {
                this.hasValidSubmit = false; 
                //error = "This is a required field. Please select minimum one option.";
                this.error = Messages.get().container(Messages.ERR_REQUIRED_FIELD_MISSING_0).key(this.getContainingForm().getLocale());
            } else { // 
                this.hasValidSubmit = true;
                this.error = null;
            }
            // Iterate over all options and update "selected"
            Iterator itr = options.iterator();
            Option option = null;
            while (itr.hasNext()) {
                option = (Option)itr.next();
                option.setSelected(false);
            }
            return;
        }
        else {
            this.submission = values;
            ArrayList vals = new ArrayList(Arrays.asList(values));
            // Iterate over all options and update "selected"
            Iterator itr = options.iterator();
            Option option = null;
            while (itr.hasNext()) {
                option = (Option)itr.next();
                if (vals.contains(option.getValue())) {
                    option.setSelected(true);
                } else {
                    option.setSelected(false);
                }
            }
            this.hasValidSubmit = true;
            this.error = null;
        }
    }
    
    /**
     * @see I_FormInputElement#getMaxLength()
     */
    @Override
    public int getMaxLength() {
        return MAX_LENGTH;
    }
}
