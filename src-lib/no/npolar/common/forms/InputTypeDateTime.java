package no.npolar.common.forms;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Represents a form input field of <code>type=text</code> that accepts datetime 
 * input only.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeDateTime extends InputTypeText {
    
    /** Member variable that holds a Date representation of this datetime */
    private Date date = null;
    /** Member variable that holds the date format used by this datetime element */
    private SimpleDateFormat sdf;
    /** Constant string representation of the standard MySQL datetime format */
    private static final String MYSQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Default constructor.
     */
    public InputTypeDateTime() {
        super();
        this.type = I_FormInputElement.DATETIME;
        this.sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.formatConstraint = new DateTimeFormatConstraint();
    }
    
    /**
     * Creates a new instance with the given configuration settings.
     * 
     * @param name the name of the input element.
     * @param label the label for the input element.
     * @param required <code>true</code> if the input element should force the user to input a value, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeDateTime(String name, String label, boolean required, Form form) {
        super(name, label, required, form);
        this.type = I_FormInputElement.DATETIME;
        this.sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.formatConstraint = new DateTimeFormatConstraint();
    }
    
    /**
     * Sets the date format that should be used for this input element.
     * 
     * @param sdf The date format to use.
     */
    public void setDateFormat(SimpleDateFormat sdf) {
        this.formatConstraint = new DateTimeFormatConstraint(sdf);
        this.sdf = sdf;
    }
    
    /**
     * @see I_FormInputElement#submit(java.lang.String[])
     */
    @Override
    public void submit(String[] values) {
        super.submit(values);
        if (this.value != null) {
            try {
                this.date = this.sdf.parse(this.value);
            } catch (ParseException pe) {
                // Sustain null date
            }
        }
    }
    
    /**
     * Get the submitted datetime, as a date.
     * 
     * @return the submitted datetime, as a date, or <code>null</code> if nothing has been submitted.
     */
    public Date getDate() { return this.date; }
    
    /**
     * Returns the submitted datetime, as a number.
     * 
     * @return the submitted datetime, as a number.
     */
    public long getTime() { return this.date != null ? this.date.getTime() : new Long("-1"); }
    
    /**
     * @see I_FormInputElement#hasValidSubmit() 
     */
    @Override
    public boolean hasValidSubmit() {
        if (!super.hasValidSubmit())
            return false;
        else {
            // The input string is valid as a string, now validate it as a date
            // but handle the case "not required and no value entered" first:
            if (!isRequired() && valueIsNullOrEmptyString()) {
                return true;
            } else {
                return this.formatConstraint.validate(this);
            }
        }
    }    
    
    /**
     * @see I_FormInputElement#getHtml(boolean)
     */
    @Override
    public String getHtml(boolean xhtmlSyntax) {
        /*
        String html = "<div class=\"element\">";
        if (super.getInformation() != null)
            html += "<div class=\"information\">" + super.getInformation() + "</div>";
        html += "<div class=\"label\">" + (required ? "<span class=\"required\">*</span>" : "") + 
                (error != null ? ("<span class=\"error\">" + label + "</span>") : label) +
                (error != null ? ("<span class=\"errormsg\">" + error + "</span>") : "") +
                (helpText != null ? ("<span class=\"help-text\">" + helpText + "</span>") : "") +
                "</div>";
        html += "\n<div class=\"input\">";
        */
        String html = beginElementHtml();
        html += "<input type=\"" + getTypeName() + "\"" 
                + getNameAndIdHtmlAttributes() 
                + " readonly=\"readonly\"";
        //html += "<input type=\"text\" name=\"" + this.getName() + "\" id=\"" + this.getName() + "\" readonly=\"readonly\"";
        html += " value=\"" + (this.value == null ? "" : this.value) + "\"";
        html += " onfocus=\"return showCalendar('" + this.getName() + "', '%d.%m.%Y %H:%M', '24', true)\"";
        if (xhtmlSyntax) {
            html += " /";
        }
        html += "><input type=\"reset\" value=\"...\" onclick=\"return showCalendar('" + this.getName() + "', '%d.%m.%Y %H:%M', '24', true);\" />";
        html += "<input type=\"button\" value=\" C \" onclick=\"clearInputField('" + this.getName() + "');\" />";
        //html += error != null ? "<br/><span class=\"errormsg\">" + error + "</span>" : "";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    
    /**
     * @see I_FormInputElement#getTypeName
     */
    @Override
    public String getTypeName() {
        return "text";
    }
    
    /**
     * @see I_FormInputElement#recreate(java.sql.ResultSet) 
     */
    @Override
    public void recreate(ResultSet rs) throws SQLException {
        if (rs.first()) {
            Date d = rs.getDate(this.getName());
            this.submit(new String[] { sdf.format(d) });
        } else {
            throw new SQLException("No data to recreate form element '" + this.getName() + "' from.");
        }
    }
}
