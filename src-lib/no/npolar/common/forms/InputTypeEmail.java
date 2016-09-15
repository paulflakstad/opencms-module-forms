package no.npolar.common.forms;

/**
 * Represents a form input field of <code>type=email</code> that accepts 
 * only an email address as input.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class InputTypeEmail extends InputTypeText {
    
    public static final int MAX_LENGTH = 127;
    
    /**
     * Default constructor.
     */
    public InputTypeEmail() {
        super();
        this.type = I_FormInputElement.EMAIL;
        this.formatConstraint = new EmailAddressConstraint();
    }
    
    /**
     * Creates a new instance with the given configuration settings.
     * 
     * @param name the name of the input element.
     * @param label the label for the input element.
     * @param required <code>true</code> if the input element should force the user to input a value, <code>false</code> if not.
     * @param form The containing form.
     */
    public InputTypeEmail(String name, String label, boolean required, Form form) {
        super(name, label, required, form);
        this.type = I_FormInputElement.EMAIL;
        this.formatConstraint = new EmailAddressConstraint();
    }
    
    /**
     * @see I_FormInputElement#getTypeName
     */
    /*@Override
    public String getTypeName() {
        return "email";
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