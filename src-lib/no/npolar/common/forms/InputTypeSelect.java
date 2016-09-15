package no.npolar.common.forms;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a <code>select</code> (single/multiple choice) input in a form.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeSelect extends A_InputTypeSingleSelect {
    public static final int MAX_LENGTH = 127;
    /**
     * Creates a new <code>select</code> input element.
     */
    public InputTypeSelect() { 
        this.type = I_FormInputElement.SELECT;
    }
    
    /**
     * Creates a new <code>select</code> input element, without options.
     * 
     * @param name The name of the <code>select</code> element.
     * @param label The label of the <code>select</code> element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeSelect(String name, String label, boolean required, Form form) {
        this.type = I_FormInputElement.SELECT;
        this.name = name;
        this.label = label;
        this.required = required;
        this.form = form;
        this.options = new ArrayList<Option>();
    }
    
    /**
     * Creates a new <code>select</code> input element, with options.
     * 
     * @param name The name of the <code>select</code> element.
     * @param label The label of the <code>select</code> element.
     * @param required <code>true</code> if this form element is required, <code>false</code> if not.
     * @param options List of options, as {@link Option} instances.
     */
    public InputTypeSelect(String name, String label, boolean required, List options, Form form) {
        this.type = I_FormInputElement.SELECT;
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
        String html = beginElementHtml();
        html += "<select " + getNameAndIdHtmlAttributes() + ">";
        //html += "<select name=\"" + name + "\">";
        
        // Iterate over all options
        Iterator i = options.iterator();
        Option option = null;
        while (i.hasNext()) {
            option = (Option)i.next();
            html += "<option value=\"" + option.getValue() + "\"";
            if (option.isSelected()) {
                html += " selected=\"selected\"";
            }
            html += ">" + option.getText() + "</option>";
        }
        
        html += "</select>";
        //html += "</div></div>";
        html += endElementHtml();
        return html;
    }
    
    /**
     * @see I_FormInputElement#getTypeName() 
     */
    /*public String getTypeName() {
        // Note: null is also a candidate, but this way it can be used as part 
        // of the class attribute
        return "select"; 
    }*/
    
    /**
     * @see I_FormInputElement#getMaxLength()
     */
    @Override
    public int getMaxLength() {
        return this.MAX_LENGTH;
    }
}
