package no.npolar.common.forms;

import java.util.List;

/**
 * Base class for form input types where the user inputs the value.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public abstract class A_InputTypeUserDefined extends A_FormInputElement {
    
    /** Holds the value of the input. */
    protected String value = null;
    
    /** Flag indicating whether or not this input element is read-only or not. */
    protected boolean readOnly = false;
    
    /**
     * Always returns <code>null</code>, as this element by definition has no 
     * options.
     * 
     * @param optionValue Irrelevant.
     * @return Always <code>null</code>.
     */
    @Override
    public String getTextForOption(String optionValue) { return null; }
    
    /**
     * Set the value of the input element.
     * 
     * @param value  The value of the input element.
     */
    public void setValue(String value) { this.value = value; }
    
    /**
     * Determine whether or not this form element is read-only.
     * <p>
     * The default value is <code>false</code> (not read-only).
     * 
     * @return  <code>true</code> if this element is read-only, <code>false</code> if not.
     */
    public boolean isReadOnly() { return this.readOnly; }
    
    /**
     * Always returns <code>null</code>.
     * <p>
     * Instances of this class will not have any options, so this method will 
     * always return <code>null</code>.
     * 
     * @return  Always <code>null</code>.
     */
    @Override
    public List getOptions() { return null; }
    
    /**
     * Sets the "is editable" option (or, the read-only flag) for this element.
     * 
     * @param editable  Provide <code>true</code> to enable editing, or <code>false</code> to disable / set as read-only.
     */
    public void setEditable(boolean editable) {
        this.readOnly = !editable;
    }
    
    /**
     * @see I_FormInputElement#setUnique(boolean) 
     */
    @Override
    public void setUnique(boolean unique) { this.unique = unique; }
    
    /**
     * @return  Always returns <code>false</code>.
     * @see I_FormInputElement#hasOptions() 
     */
    @Override
    public boolean hasOptions() { return false; }
}
