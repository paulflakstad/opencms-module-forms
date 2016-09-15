package no.npolar.common.forms;

import org.opencms.main.OpenCms;
import org.opencms.main.CmsException;
import org.opencms.file.CmsObject;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a form input field of type <em>textarea</em> (multiple line text 
 * input).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeTextarea extends A_InputTypeUserDefined {
    
    /** The number of columns in the <code>textarea</code> element. */
    private int cols = 60;
    /** The number of rows in the <code>textarea</code>. element */
    private int rows = 4;
    
    /**
     * Creates a new text area input element.
     */
    public InputTypeTextarea() { 
        this.type = I_FormInputElement.TEXTAREA;
    }
    
    /**
     * Creates a new text area input element, with the given configuration.
     * 
     * @param name The name of the element
     * @param label The label for the element
     * @param required <code>true</code> if this element should disallow empty submissions, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeTextarea(String name, String label, boolean required, Form form) {
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.type = I_FormInputElement.TEXTAREA;
    }
    
    /**
     * Sets the number of columns for the <code>textarea</code> element.
     * <p>
     * Used when outputting HTML in {@link #getHtml(boolean)}.
     * 
     * @param cols The number of columns for the <code>textarea</code>.
     */
    public void setCols(int cols) { this.cols = cols; }
    
    /**
     * Sets the number of rows for the <code>textarea</code> element.
     * <p>
     * Used when outputting HTML in {@link #getHtml(boolean)}.
     * 
     * @param rows The number of rows for the <code>textarea</code>.
     */
    public void setRows(int rows) { this.rows = rows; }
    
    /**
     * @see I_FormInputElement#getTypeName() 
     */
    public String getTypeName() {
        return null; // or should we return "textarea"..?
    }
    
    /**
     * Gets the HTML code for this form element.
     * 
     * @param xhtmlSyntax <code>true</code> if XHTML syntax should be applied, <code>false</code> if not.
     * @return The HTML code for this form element.
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
        html += "<textarea " 
                + getNameAndIdHtmlAttributes() 
                + " cols=\"" + cols + "\""
                + " rows=\"" + rows + "\">";
        //html += "<textarea name=\"" + name + "\"";
        //html += " cols=\"" + cols + "\" rows=\"" + rows + "\">";
        
        if (this.value != null) {
            html += value;
        }
            //html += CmsStringUtil.escapeHtml(value);
        html += "</textarea>";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    
    
    /**
     * Handle the form submission for this element.
     * 
     * @param values This element's submitted value(s).
     */
    @Override
    public void submit(String[] values) {
        this.submission = null;
        this.value = null;
        
        if (values.length > 1) {
            this.hasValidSubmit = false; // Ambiguous submit (too many values, should be only one)
            //this.error = "Too many values submitted.";
            this.error = Messages.get().container(Messages.ERR_TOO_MANY_VALUES_0).key();
        }
        
        if (values.length != 1) {
            this.hasValidSubmit = false; // No value
            //this.error = "No value submitted.";
            this.error = Messages.get().container(Messages.ERR_NO_VALUE_0).key();
        } else if (this.required && values[0].trim().length() == 0) {
            this.hasValidSubmit = false; // Field is required, but value is missing (or only whitespace)
            //this.error = "This is a required field, it cannot be empty.";
            this.error = Messages.get().container(Messages.ERR_REQUIRED_FIELD_MISSING_0).key();
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
            
            // Then validate if this is a unique field in the database
            if (this.isUnique()) {
                CmsObject cmso = null;
                try {
                    cmso = OpenCms.initCmsObject("Export");

                    if (this.form.hasExistingTable(cmso)) { // No need to process below lines if the table does not exist yet
                        // Query the database: get any row that has a value identical to the one submitted now
                        ResultSet rs = this.form.getSubmissionFromUnique(this.name, this.value, cmso);
                        if (rs != null) {
                            if (rs.next()) {
                                if (this.value.equals(rs.getString(this.name))) {
                                    // A row existed in the result set, meaning a duplicate value for this element exists in the database
                                    this.hasValidSubmit = false;
                                    //this.error = "\"" + values[0] + "\" already exists, and duplicates are not allowed.";
                                    this.error = Messages.get().container(Messages.ERR_DUPLICATE_VALUE_1, values[0]).key();
                                }
                            }
                            rs.close();
                        }
                    }
                } catch (CmsException cmse) {
                    throw new NullPointerException("Unable to create CmsObject: " + cmse.getMessage());
                } catch (InstantiationException inste) {
                    throw new NullPointerException("Unable to create an SQL agent: " + inste.getMessage());
                } catch (SQLException sqle) {
                    throw new NullPointerException("SQL exception when checking for duplicate unique value: " + sqle.getMessage());
                }
            }
        }
    }
}
