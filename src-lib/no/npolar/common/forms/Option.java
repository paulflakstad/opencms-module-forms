package no.npolar.common.forms;

/**
 * Represents an option in a single or multiple choice form input field 
 * (<em>checkbox</em>, <em>radio</em>, <em>select</em>).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Option {
    
    /** Flag indicating whether or not this option is selected. */
    private boolean selected = false;
    /** Holds the option's value. */
    private String value = null;
    /** Holds the option's displayable text. */
    private String text = null;
    
    /**
     * Creates a new option with the specified values.
     * 
     * @param value The value of the option.
     * @param text The displayable text of the option.
     * @param selected <code>true</code> if this option should be selected / checked, <code>false</code> if not.
     */
    public Option(String value, String text, boolean selected) {
        this.value = value;
        this.text = text;
        this.selected = selected;
    }
    
    /**
     * Gets the value of the option.
     * 
     * @return The value of the option.
     */
    public String getValue() { return this.value; }
    
    /**
     * Gets the displayable text of the option.
     * 
     * @return The displayable text of the option.
     */
    public String getText() { return this.text; }
    
    /**
     * Determines whether or not this option is selected.
     * 
     * @return <code>true</code> if selected, <code>false</code> if not.
     */
    public boolean isSelected() { return this.selected; }
    
    /**
     * Set the "selected" state for the option.
     * 
     * @param selected <code>true</code> if selected, <code>false</code> if not.
     */
    public void setSelected(boolean selected) { this.selected = selected; }
}
