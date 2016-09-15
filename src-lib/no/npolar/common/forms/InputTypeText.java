package no.npolar.common.forms;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.opencms.file.CmsObject;

/**
 * Represents a form input field of <code>type="text</code>, that accepts 
 * regular text input.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeText extends A_InputTypeUserDefined {
    
    /** Holds the length of this text input field. */
    protected int length = -1;
    
    /**
     * Creates a new text input element.
     */
    public InputTypeText() { 
        this.type = I_FormInputElement.TEXT;
    }
    
    /**
     * Creates a new text input element.
     * 
     * @param name The name of the element.
     * @param label The label for the element.
     * @param required <code>true</code> if this element should be required, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeText(String name, String label, boolean required, Form form) {
        this.type = I_FormInputElement.TEXT;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
    }
    
    
    /**
     * Set the length of the input element.
     * 
     * @param length The length of the input element.
     */
    public void setLength(int length) { this.length = length; }
    
    
    
    // -----------------------------------------
    // Required interface method implementations
    // -----------------------------------------
    
    /**
     * @see I_FormInputElement#getHtml(boolean)
     */
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
        html += "<div class=\"input\">";
        */
        String html = beginElementHtml();
        html += "<input type=\"" + getTypeName() + "\"";
        html += getNameAndIdHtmlAttributes();
        /*if (this.formatConstraint != null && this.formatConstraint instanceof EmailAddressConstraint) {
            html += " type=\"email\"";
        }
        else if (this.formatConstraint != null && this.formatConstraint instanceof NumericConstraint) {
            html += " type=\"number\"";
        }
        else {
            html += " type=\"text\"";
        }
        
        html += " id=\"" + this.getName() + "\"";
        html += " name=\"" + this.getName() + "\"";
        */
        if (this.length != -1) {
            html += " size=\"" + length + "\"";
        }
        /*else 
            html += " size=\"50%\"";*/
        if (this.value != null) {
            html += " value=\"" + value + "\"";
        }
        if (this.readOnly) {
            html += " readonly=\"readonly\"";
        }
        if (xhtmlSyntax) {
            html += " /";
        }
        html += ">";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    
    /**
     * @see I_FormInputElement#getTypeName() 
     */
    @Override
    public String getTypeName() {
        if (this.formatConstraint != null && this.formatConstraint instanceof EmailAddressConstraint) {
            return "email";
        }
        else if (this.formatConstraint != null && this.formatConstraint instanceof NumericConstraint) {
            return "number";
        }
        return "text"; 
    }
    
    /**
     * Determines if the user has entered anything.
     * 
     * @return <code>false</code> if the user has entered something, <code>true</code> if not.
     */
    protected boolean valueIsNullOrEmptyString() {
        if (value == null)
            return true;
        else {
            if (value.trim().length() > 0)
                return false;
            return true;
        }
    }
    
    /**
     * @see I_FormInputElement#submit(java.lang.String[])
     */
    @Override
    public void submit(String[] values) {
        this.submission = null;
        this.value = null;
        //
        // ToDo: Add length validation as well
        //
        if (values.length != 1) {
            this.hasValidSubmit = false; // Ambiguous submit (too many values, should be only one)
            //this.error = "Too many values submitted.";
            this.error = Messages.get().container(Messages.ERR_TOO_MANY_VALUES_0).key(this.getContainingForm().getLocale());
        } else if (this.required && values[0].trim().length() == 0) {
            this.hasValidSubmit = false; // Field is required, but value is missing (or only whitespace)
            //this.error = "This is a required field, it cannot be empty.";
            this.error = Messages.get().container(Messages.ERR_REQUIRED_FIELD_MISSING_0).key(this.getContainingForm().getLocale());
        } else {
            // Assume the submission is OK
            this.submission = values;
            try {
                this.value = values[0].trim();
            } catch (NullPointerException npe) {
                this.value = "";
            }
            this.hasValidSubmit = true;
            this.error = null;
            
            // Then validate unique fields
            if (this.isUnique()) {
                //CmsObject cmso = null;
                try {
                    //cmso = OpenCms.initCmsObject("Export");
                    //if (this.form.hasExistingTable(cmso)) { // No need to process below lines if the table does not exist yet
                        
                    if (this.form.hasExistingTable()) { // No need to process below lines if the table does not exist yet
                        // Query the database: get any row that has a value identical to the one submitted now
                        //ResultSet rs = this.form.getSubmissionFromUnique(this.name, this.value, cmso);
                        ResultSet rs = this.form.getSubmissionFromUnique(this.name, this.value);
                        if (rs != null) {
                            if (rs.next()) {
                                if (this.form.isEditEnabled()) { // If an entry is being edited: Ensure that the unique value does not already exists in antoher entry.
                                    if (!rs.getString("id").equals(String.valueOf(this.form.getEditingEntryId()))) {
                                        // The ID returned was not the ID of the entry we're editing. This means that a duplicate value exists in another entry.
                                        this.hasValidSubmit = false;
                                        this.error = Messages.get().container(Messages.ERR_DUPLICATE_VALUE_1, values[0]).key(this.getContainingForm().getLocale());
                                    }
                                    
                                } else if (this.value.equals(rs.getString(this.name))) {
                                    // A row existed in the result set, meaning a duplicate value for this element exists in the database
                                    this.hasValidSubmit = false;
                                    //this.error = "\"" + values[0] + "\" already exists, and duplicates are not allowed.";
                                    this.error = Messages.get().container(Messages.ERR_DUPLICATE_VALUE_1, values[0]).key(this.getContainingForm().getLocale());
                                }
                            }
                            rs.close();
                        }
                    }
                /*} catch (CmsException cmse) {
                    throw new NullPointerException("Unable to create CmsObject: " + cmse.getMessage());
                } catch (InstantiationException inste) {
                    throw new NullPointerException("Unable to create an SQL agent: " + inste.getMessage());*/
                } catch (SQLException sqle) {
                    throw new NullPointerException("SQL exception when checking for duplicate unique value: " + sqle.getMessage());
                }
            }
            if (this.formatConstraint != null) {
                boolean ignoreConstraint = (!this.isRequired()) && this.value.trim().length() == 0; // Ignore when field is not required and has no value
                // Apply constraint only if there is a value
                if (!ignoreConstraint) {
                    if (!this.formatConstraint.validate(this)) {
                        this.hasValidSubmit = false;
                        this.error = this.formatConstraint.getError();
                    }
                }
            }
        }
    }
}
