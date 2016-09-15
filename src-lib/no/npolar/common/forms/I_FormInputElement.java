package no.npolar.common.forms;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for standard HTML form elements, as well as some commonly used 
 * special form elements (e.g. <em>datetime</em>).
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute.
 */
public interface I_FormInputElement {
    
    /** Type ID for input type "text". 
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int TEXT = 0;
    /** 
     * Type ID for input type "text area". 
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int TEXTAREA = 1;
    /** 
     * Type ID for input type "select".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int SELECT = 2;
    /** 
     * Type ID for input type "checkbox". 
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int CHECKBOX = 3;
    /** 
     * Type ID for input type "radio".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int RADIO = 4;
    /**
     * Type ID for input type "datetime".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int DATETIME = 5;
    /**
     * Type ID for input type "select single country" (special case of a normal drop-down).
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int SELECT_SINGLE_COUNTRY = 6;
    /**
     * Type ID for input type "password".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int PASSWORD = 7;
    /**
     * Type ID for input type "email".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int EMAIL = 8;
    /**
     * Type ID for input type "number".
     * @see {@link FormSqlManager#SQL_DEFINITIONS_FOR_INPUT_TYPES}. 
     */
    static final int NUMBER = 9;
    
    
    /** Constant for the package name */
    public static final String PACKAGE_NAME = "no.npolar.common.forms";
    
    /**
     * Gets the name of this form element.
     * 
     * @return  The name of this form element
     */
    public String getName();
    
    /**
     * Gets the type identifier for this form element.
     * 
     * @return  The type for this form element.
     * @see Form#FORM_INPUT_TYPES
     */
    public int getType();
    
    /**
     * Gets the type name for this form element, like "text" or "email", ready 
     * to use as the value of the <code>type</code> attribute in the HTML.
     * 
     * @return  The type name for this form element.
     */
    public String getTypeName();
    
    /**
     * Gets the label for this form element.
     * 
     * @return  The label for this form element.
     */
    public String getLabel();
    
    /**
     * Determine whether this form element is required.
     * <p>
     * I.e. if a submit of the form is accepted only when this element has a 
     * value.
     * 
     * @return  <code>true</code> if this form element is required, <code>false</code> if not.
     */
    public boolean isRequired();
    
    /**
     * Determine whether this form element is a multiple choice selection 
     * element.
     * 
     * @return  <code>true</code> if this is a multiple choice element, <code>false</code> if not.
     */
    public boolean isMultipleChoice();
    
    /**
     * Recreates the element from a previous submission.
     * 
     * @param rs  A result set that holds one table row.
     * @throws SQLException  If anything goes wrong during interaction with the database.
     */
    public void recreate(ResultSet rs) throws SQLException;
    
    /**
     * Form elements have a lot in common - label, information text, help text,
     * error message, etc.
     * <p>
     * This method returns this common part, beginning the element's HTML code.
     * 
     * @return The start of the HTML code for this element.
     */
    public String beginElementHtml();
    
    /**
     * @see I_FormInputElement#beginElementHtml() 
     * @return The end of the HTML code for this element.
     */
    public String endElementHtml();
    
    /**
     * Gets the HTML code for this form element.
     * 
     * @param xhtmlSyntax  <code>true</code> if XHTML syntax should be applied, <code>false</code> if not.
     * @return  The HTML code for this form element.
     */
    public String getHtml(boolean xhtmlSyntax);
    
    /**
     * Gets the <code>name</code> and <code>id</code> attributes for this
     * input element.
     * 
     * @return  The <code>name</code> and <code>type</code> attributes for this input element.
     */
    public String getNameAndIdHtmlAttributes();
    
    /**
     * Determines whether this form element has a valid submitted value.
     * <p>
     * I.e. if this element is required, this method will return 
     * <code>false</code> if the user did not submit a value for this element.
     * 
     * @return  <code>true</code> if this element has a valid submitted value, <code>false</code> if not.
     */
    public boolean hasValidSubmit();
    
    /**
     * Determines whether this form element has a valid value length.
     * <p>
     * This is to ensure we don't try to squeeze in a too-long value in a 
     * too-small table cell in the database.
     * 
     * @return  <code>true</code> if this element has a valid value length, <code>false</code> if not.
     */
    public boolean hasValidLength();
    
    /**
     * Gets the maximum allowed length for the submitted value.
     * 
     * @return  the maximum allowed length for the submitted value.
     */
    public int getMaxLength();
    
    /**
     * Determines whether this form element has an information text or not.
     * 
     * @return  <code>true</code> if this element has an information text, <code>false</code> if not.
     */
    public boolean hasInformationText();
    
    /**
     * Determines whether this form element has options or not.
     * 
     * @return  <code>true</code> if this element has options, <code>false</code> if not.
     */
    public boolean hasOptions();
    
    /**
     * Determines whether this form element has a help text or not.
     * 
     * @return  <code>true</code> if this element has a help text, <code>false</code> if not.
     */
    public boolean hasHelpText();
    
    /**
     * Handle the submission for this element of the form.
     * 
     * @param values  This element's submitted value(s).
     */
    public void submit(String[] values);
    
    /**
     * Get the submitted values, unchanged, as they were upon submit.
     * 
     * @return  The submitted values.
     */
    public String[] getSubmission();
    
    /**
     * Gets the submitted value as a single string.
     * 
     * @return The submitted value.
     */
    public String getValue();
    
    /**
     * Gets the {@link Form} that contains this element.
     * 
     * @return the form that contains this element.
     */
    public Form getContainingForm();
    
    /**
     * Gets the current error message for this form element.
     * 
     * @return  The current error message.
     */
    public String getError();
    
    /**
     * Gets the help text for this form element.
     * 
     * @return  The help text for this form element.
     */
    public String getHelpText();
    
    /**
     * Gets the options for this input element.
     * <p>
     * The list returned contains all options, as {@link Option} objects.
     * 
     * @return  The options of this form element, or <code>null</code> if none.
     * @see Option
     */
    public List getOptions();
    
    /**
     * Gets the information text for this form element.
     * 
     * @return  The information text for this form element, or <code>null</code> if none
     */
    public String getInformation();
    
    /**
     * Gets the text for an option, given the option's value.
     * <p>
     * If more than one option has the same value, the first encountered option 
     * with the given value is matched.
     * 
     * @param optionValue  The value of the option to get the text for.
     * @return  The text for the option with the given value.
     * @see Option
     */
    public String getTextForOption(String optionValue);
    
    /**
     * Sets the help text for this form element.
     * 
     * @param helpText  The help text for this form element, or <code>null</code> if none.
     */
    public void setHelpText(String helpText);
    
    /**
     * Sets the information text for this form element.
     * 
     * @param information  The information text to display for this form element.
     */
    public void setInformation(String information);
    
    /**
     * Sets a constraint class for this form element.
     * <p>
     * The constraint class, identified by the given class name, must implement 
     * {@link I_InputFormatConstraint}, and should extend 
     * {@link A_InputFormatConstraint}.
     * 
     * @param constraintClassName  The constraint class name, must implement {@link I_InputFormatConstraint}.
     * @return <code>true</code> if the constraint is added OK, <code>false</code> if something goes wrong.
     * @see I_InputFormatConstraint
     * @see A_InputFormatConstraint
     */
    public boolean setConstraint(String constraintClassName);
    
    /**
     * Sets the form element's uniqueness.
     * 
     * @param unique <code>true</code> if this form element is unique, <code>false</code> if not.
     */
    public void setUnique(boolean unique);
    
    /**
     * Checks if this form element is unique or not.
     * 
     * @return <code>true</code> if the form element is unique, <code>false</code> if not.
     */
    public boolean isUnique();
    
    /**
     * Checks if this form element has pre-defined input values or not.
     * 
     * @return <code>true</code> of the form element has pre-defined input values, <code>false</code> if not.
     */
    public boolean isPreDefinedValueInput();
}
