package no.npolar.common.forms;

/**
 * Represents an form input field of <code>type="password"</code>.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypePassword extends InputTypeText {
    
    /**
     * Default constructor.
     */
    public InputTypePassword() {
        super();
        this.type = I_FormInputElement.PASSWORD;
    }
    
    /**
     * Creates a new <code>password</code> input element.
     * 
     * @param name  The name of the element.
     * @param label  The label for the element.
     * @param form The containing form.
     * @param required  <code>true</code> if this element should be required, <code>false</code> if not.
     */
    public InputTypePassword(String name, String label, boolean required, Form form) {
        super(name, label, required, form);
        this.type = I_FormInputElement.PASSWORD;
    }
    /**
     * Gets the HTML for this password field.
     * 
     * @param xhtmlSyntax If true, XHTML syntax will be used.
     * @return The HTML for this password field
     * @see InputTypeText#getHtml(boolean) 
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
        html += "<div class=\"input\">";
        */
        String html = beginElementHtml();
        html += "<input type=\"" + getTypeName() + "\"" 
                + getNameAndIdHtmlAttributes();
        //html += "<input type=\"password\" name=\"" + name + "\"";
        if (length != -1) {
            html += " size=\"" + length + "\"";
        }
        /*else 
            html += " size=\"50%\"";*/
        if (value != null) {
            html += " value=\"" + value + "\"";
        }
        if (readOnly) {
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
    /*@Override
    public String getTypeName() {
        return "password";
    }*/
}
