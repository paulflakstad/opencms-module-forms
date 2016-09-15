package no.npolar.common.forms;

/**
 * Interface used for pluggable constraints on form input field.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public interface I_InputFormatConstraint {
    
    /**
     * Validates the input value against the given format constraint.
     * 
     * @param element the element that the constraint is added to.
     * @return <code>true</code> if the submitted input value was valid according to the given constraint, <code>false</code> if not.
     */
    public boolean validate(I_FormInputElement element);
    
    /**
     * Gets the error message that was set during the validation routine.
     * 
     * @return the error message, or <code>null</code> if the validation was successful.
     */
    public String getError();
}
