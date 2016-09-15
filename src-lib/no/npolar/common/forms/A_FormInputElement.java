package no.npolar.common.forms;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

/**
 * Base class for all form input elements.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
//public interface I_FormInputElement {
public abstract class A_FormInputElement implements I_FormInputElement {
    
    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(A_FormInputElement.class);
 
    /** Holds the element's name. */
    protected String name = null;
    
    /** Holds the element's label. */
    protected String label = null;
    
    /** Holds the element's information text. */
    protected String information = null;
    
    /** Flag indicating whether or not the element requires an input value. */
    protected boolean required = false;
    
    /** Holds the element's options (if multiple choice). */
    protected List options;
    
    /** Flag indicating the validity of the submitted value. */
    protected boolean hasValidSubmit = false;
    
    /** Flag indicating whether or not the element is multiple choice.  */
    protected boolean mc = false;
    
    /** Holds the element's submitted value(s). */
    protected String[] submission = null;
    
    /** Holds the element's error message. */
    protected String error = null;
    
    /** Holds the element's help text. */
    protected String helpText = null;
    
    /** Holds a reference to the Form in which the element appears. */
    protected Form form = null;
    
    /** 
     * Flag indicating whether or not the form element's submitted value must 
     * be unique within the all submissions for this element. 
     */
    protected boolean unique = false;
    
    /** 
     * Type definition for the form input element, as defined in 
     * {@link I_FormInputElement}.
     */
    protected int type = -1;
    
    /** Holds the form input element's constraint. */
    protected I_InputFormatConstraint formatConstraint = null;
    
    /** 
     * Standard string prepended to IDs by default, that is, when using 
     * {@link #getNameAndIdHtmlAttributes()}. 
     */
    public static final String PREFIX_FOR_ID = "form-input-element-";
    
    /**
     * @see I_FormInputElement#getName() 
     */
    @Override
    public String getName() { return this.name; }
    
    /**
     * @see I_FormInputElement#getType() 
     */
    @Override
    public int getType() { return this.type; }
    
    /**
     * @see I_FormInputElement#getTypeName()
     */
    @Override
    public String getTypeName() { 
        return Form.FORM_INPUT_TYPE_NAMES[this.getType()]; 
    }
    
    /**
     * @see I_FormInputElement#getLabel() 
     */
    @Override
    public String getLabel() { return this.label; }
    
    /**
     * @see I_FormInputElement#isRequired()
     */
    @Override
    public boolean isRequired() { return this.required; }
    
    /**
     * @see I_FormInputElement#isMultipleChoice() 
     */
    @Override
    public boolean isMultipleChoice() { return this.mc; }
    
    /**
     * @see I_FormInputElement#recreate(java.sql.ResultSet) 
     * @throws SQLException 
     */
    @Override
    public void recreate(ResultSet rs) throws SQLException {
        if (rs.first()) {
            this.submit(new String[] { rs.getString(this.name) });
            
        } else {
            throw new SQLException("No data to recreate form element '" + name + "' from.");
        }
    }
    
    /**
     * @see I_FormInputElement#hasValidSubmit() 
     */
    @Override
    public boolean hasValidSubmit() {
        return this.hasValidSubmit;
    }
    
    /**
     * @see I_FormInputElement#hasValidLength() 
     */
    public boolean hasValidLength() {
        try {
            return this.getValue().length() <= this.getMaxLength();
        } catch (NullPointerException npe) {
            return true;
        }
    }
    
    /**
     * @see I_FormInputElement#hasInformationText() 
     */
    @Override
    public boolean hasInformationText() { 
        return this.information == null ? false : (this.information.trim().length() > 0); 
    }
    
    /**
     * @see I_FormInputElement#hasHelpText() 
     */
    @Override
    public boolean hasHelpText() { 
        return this.helpText == null ? false : (this.helpText.trim().length() > 0); 
    }
    
    /**
     * @see I_FormInputElement#beginElementHtml() 
     */
    @Override
    public String beginElementHtml() {
        String html = "<" + (this.isFieldsetGroup() ? "fieldset" : "div") 
                + " class=\"element form-element form-element--" + getTypeName() 
                + (error != null ? " has-error form-element--has-error" : "") 
                + "\">\n";
        
        if (information != null) {
            html += "<div class=\"information\">" + information + "</div>";
        }
        
        html += "<" + (this.isFieldsetGroup()? "legend" : "label") 
                + " class=\"label\"" 
                + (this.isFieldsetGroup() ? "" : " for=\"" + PREFIX_FOR_ID + this.getName() + "\"") 
                + ">";
        
        if (required || error != null) {
            if (required) {
                html += "<span class=\"required-symbol\">*</span>";
            }
            
            html += "<span class=\"";
            
            if (error != null) {
                html += "error";
            }
            
            if (required) {
                html += (html.endsWith("error") ? " " : "") + "required";
            }
            
            html += "\">" + label + "</span>";
        } else {
            html += label;
        }
        html += "</" + (this.isFieldsetGroup()? "legend" : "label") + ">";
            
        if (error != null) {
            html += "<span class=\"errormsg\">" + error + "</span>";
        }

        if (hasHelpText() && this.getType() != I_FormInputElement.TEXTAREA) {
            html += "<span class=\"help-text\">" + getHelpText() + "</span>";
        }
        
        //html += "</" + (this.isFieldsetGroup()? "legend" : "label") + ">";
        
        String typeName = Form.FORM_INPUT_TYPES[this.getType()].toLowerCase();
        html += "<div class=\"input " + typeName + "\">";
        
        if (hasHelpText() && this.getType() == I_FormInputElement.TEXTAREA) {
            html += "<span class=\"help-text\">" + getHelpText() + "</span>";
        }
        
        return html;
    }
    
    /**
     * @see I_FormInputElement#endElementHtml() 
     */
    @Override
    public String endElementHtml() {
        return "</div>\n</" + (this.isFieldsetGroup() ? "fieldset" : "div") + ">"
                + "<!-- end of form element '" + this.getName() + "' -->";
    }
    
    /**
     * @see I_FormInputElement#getNameAndIdHtmlAttributes() 
     */
    @Override
    public String getNameAndIdHtmlAttributes() {
        return " name=\"" + this.getName() + "\""
                + " id=\"" + PREFIX_FOR_ID + this.getName() + "\"";
    }
        
    /**
     * @see I_FormInputElement#getSubmission() 
     */
    @Override
    public String[] getSubmission() { return this.submission; }
    
    /**
     * @see I_FormInputElement#getError() 
     */
    @Override
    public String getError() { return this.error; }
    
    /**
     * @see I_FormInputElement#getHelpText() 
     */
    @Override
    public String getHelpText() { return this.helpText; }
    
    /**
     * @see I_FormInputElement#getInformation() 
     */
    @Override
    public String getInformation() { return this.information; }
    
    /**
     * @see I_FormInputElement#setHelpText(java.lang.String) 
     */
    @Override
    public void setHelpText(String helpText) { this.helpText = helpText; }
    
    /**
     * @see I_FormInputElement#setInformation(java.lang.String) 
     */
    @Override
    public void setInformation(String information) { this.information = information; }
    
    /**
     * @see I_FormInputElement#isUnique()
     */
    @Override
    public boolean isUnique() { return this.unique; }
    
    /**
     * @see I_FormInputElement#isPreDefinedValueInput() 
     */
    @Override
    public boolean isPreDefinedValueInput() { return false; }
    
    /**
     * Sets a constraint on this form input element.
     * <p>
     * The constraint class must implement {@link I_InputFormatConstraint}.
     * 
     * @param constraintClassName the constraint class' name. It is assumed that the class resides in this package if the class name is not fully qualified.
     * @return <code>true</code> if the constraint is added OK, <code>false</code> if something goes wrong.
     */
    @Override
    public boolean setConstraint(String constraintClassName) {
        String qualifiedConstraintClassName = constraintClassName;
        if (!constraintClassName.contains(".")) {
            qualifiedConstraintClassName = I_FormInputElement.PACKAGE_NAME.concat(".").concat(constraintClassName);
        }
        try {
            Class constraintClass = Class.forName(qualifiedConstraintClassName);
            this.formatConstraint = (I_InputFormatConstraint)constraintClass.newInstance();
            return true;
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().container(Messages.ERR_CONSTRAINT_CLASS_NOT_FOUND_1, qualifiedConstraintClassName), e);
                /*throw new NullPointerException("Exception: could not add the constraint \"" + 
                        qualifiedConstraintClassName + "\" to \"" + this.name + "\": " + e.getMessage());*/
            }
            return false;
        }
    }
    
    /**
     * @see I_FormInputElement#getContainingForm()
     */
    @Override
    public Form getContainingForm() { return this.form; }
    
    /**
     * Gets the submitted value(s) as a single String.
     * <p>
     * If multiple values were submitted (i.e. using a group of checkboxes), 
     * then {@link A_InputTypeMultiSelect#VALUE_SEPARATOR} is used to separate 
     * the values.
     * 
     * @return the submitted value(s) as a single String, or <code>null</code> if there is no submitted value.
     */
    @Override
    public String getValue() {
        // Null submission
        if (this.submission == null) {
            return null;
        }
        
        // Empty submission
        if (this.submission.length == 0) {
            return null;
        }
        
        //
        // Code reaches this point = submitted value(s) exist.
        //
        
        
        if (this.submission.length == 1) {
            // Single value:
            try {
                return this.submission[0].trim();
            } catch (NullPointerException npe) {
                return "";
            }
        } else {
            // Multiple values:
            String value = "";
            for (int i = 0; i < this.submission.length; i++) {
                value += this.submission[i];
                value += i < this.submission.length - 1 ? A_InputTypeMultiSelect.VALUE_SEPARATOR : "";
            }
            return value;
        }
    }
    
    /**
     * Determines whether or not this input element should be presented using
     * a field set.
     * <p>
     * This is determined by looking at the input type and number of options:
     * Only if the input uses <code>checkbox</code> or <code>radio</code> 
     * options will this method return true.
     * 
     * @return  <code>true</code> if this input element should use field set, <code>false</code> if not.
     */
    public boolean isFieldsetGroup() {
        if (this.type == CHECKBOX || getType() == RADIO) {
            return true;
            /*try {
                return this.getOptions().size() > 1;
            } catch (Exception ignore) {}*/
        }
        return false;
    }
}