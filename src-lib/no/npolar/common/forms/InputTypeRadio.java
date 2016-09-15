package no.npolar.common.forms;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a group of <code>radio</code> buttons (single choice) in a form.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeRadio extends A_InputTypeSingleSelect {
    
    /**
     * Creates a new, blank <code>radio</code> input element.
     */
    public InputTypeRadio() { 
        this.type = I_FormInputElement.RADIO;
        this.options = new ArrayList<Option>(); 
    }
    
    /**
     * Creates a new <code>radio</code> input element, without options.
     * 
     * @param name The name of the select element.
     * @param label The label of the select element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeRadio(String name, String label, boolean required, Form form) {
        this.type = I_FormInputElement.RADIO;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.options = new ArrayList<Option>();
    }
    
    /**
     * Creates a new <code>radio</code> input element, with options.
     * 
     * @param name The name of the select element.
     * @param label The label of the select element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param options List of options, as {@link Option} instances.
     */
    public InputTypeRadio(String name, String label, boolean required, List options, Form form) {
        this.type = I_FormInputElement.RADIO;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.options = options != null ? options : new ArrayList<Option>();
    }
    
    /**
     * @see I_FormInputElement#getHtml(boolean) 
     */
    @Override
    public String getHtml(boolean xhtmlSyntax) {
        /*
        String html = "<div class=\"element\">";
        if (information != null)
        html += "<div class=\"label\">" + (required ? "<span class=\"required\">*</span>" : "") + 
                (error != null ? ("<span class=\"error\">" + label + "</span>") : label) +
                (error != null ? ("<span class=\"errormsg\">" + error + "</span>") : "") +
                (helpText != null ? ("<span class=\"help-text\">" + helpText + "</span>") : "") +
                "</div>";
        html += "<div class=\"input\">";
        */
        String html = beginElementHtml();
        html += "\n<!-- " + options.size() + " radio buttons: -->\n";
        // Iterate over all options
        Iterator i = options.iterator();
        Option option = null;
        while (i.hasNext()) {
            option = (Option)i.next();
            String optionId = name + "_" + option.getValue();
            html += "<input type=\"" + getTypeName() + "\""
                    + " name=\"" + getName() + "\""
                    + " id=\"" + optionId + "\""
                    + " value=\"" + option.getValue() + "\""
                    + (option.isSelected() ? " checked=\"checked\"" : "");
            html += (xhtmlSyntax ? " /" : "") + ">\n<label for=\"" + optionId + "\">" + option.getText() + "</label>";
            //html += "<input type=\"radio\" name=\"" + name + "\" value=\"" + option.getValue() + "\" id=\"" + optionId + "\"";
            //if (option.isSelected())
            //    html += " checked=\"checked\"";
            //html += (xhtmlSyntax ? " /" : "") + "> <label for=\"" + optionId + "\">" + option.getText() + "</label><br" + (xhtmlSyntax ? "/" : "") + ">";
        }
        //html += "\n<!-- done printing radio buttons -->\n";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    
    /**
     * @see I_FormInputElement#getTypeName() 
     */
    public String getTypeName() {
        return "radio";
    }
}
