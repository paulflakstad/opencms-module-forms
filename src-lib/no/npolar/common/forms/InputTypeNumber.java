package no.npolar.common.forms;

/**
 * Represents a form input field of <code>type=number</code> that accepts 
 * numeric input only.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeNumber extends InputTypeText {
    
    public static final int MAX_LENGTH = 127;
    
    /**
     * Default constructor.
     */
    public InputTypeNumber() {
        super();
        this.type = I_FormInputElement.NUMBER;
        this.formatConstraint = new NumericConstraint();
    }
    
    /**
     * Creates a new instance with the given configuration settings.
     * 
     * @param name the name of the input element.
     * @param label the label for the input element.
     * @param required <code>true</code> if the input element should force the user to input a value, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeNumber(String name, String label, boolean required, Form form) {
        super(name, label, required, form);
        this.type = I_FormInputElement.NUMBER;
        this.formatConstraint = new NumericConstraint();
    }
    
    /**
     * @see I_FormInputElement#getTypeName
     */
    /*@Override
    public String getTypeName() {
        return "number";
    }*/
    
    /**
     * @see I_FormInputElement#getMaxLength()
     */
    @Override
    public int getMaxLength() {
        if (length > 0 && length < MAX_LENGTH) {
            return length;
        }
        return this.MAX_LENGTH;
    }
}
