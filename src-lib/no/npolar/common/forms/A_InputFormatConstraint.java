package no.npolar.common.forms;

/**
 * Base class for all format constraint classes.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class A_InputFormatConstraint implements I_InputFormatConstraint {
    
    /** Holds any error message caused by an illegal value for this constraint. */
    protected String error = null;
    
    /** The element using this constraint. */
    protected I_FormInputElement containingElement = null;
        
    /**
     * Gets the error message.
     * 
     * @return the error message, or <code>null</code> if none.
     */
    @Override
    public String getError() { return this.error; }
}
