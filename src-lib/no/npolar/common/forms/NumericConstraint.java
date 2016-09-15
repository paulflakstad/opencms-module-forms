package no.npolar.common.forms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Constraint for form input field: numeral value.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class NumericConstraint extends A_InputFormatConstraint {
    
    /** Pattern used by the regex. */
    private Pattern pattern = null;
    /** Matcher used by the regex. */
    private Matcher matcher = null;
    
    /** Constant for the numeric regex. */
    public static final String NUMERIC_REGEX_PATTERN = "^[0-9]*$";
    
    /**
     * Creates a new numeric constraint using {@link #NUMERIC_REGEX_PATTERN}.
     */
    public NumericConstraint() {
        this.pattern = Pattern.compile(NUMERIC_REGEX_PATTERN);
    }
    
    /**
     * @see I_InputFormatConstraint#validate(no.npolar.common.forms.I_FormInputElement) 
     */
    @Override
    public boolean validate(I_FormInputElement element) {
        this.matcher = this.pattern.matcher(element.getValue());
        if (this.matcher.find()) {
            this.error = null;
            return true;
        }
        this.error = "Not a number.";
        this.error = Messages.get().container(Messages.MSG_FORMAT_REQUIRED_NUMERIC_0).key(element.getContainingForm().getLocale());
        return false;
    }
}
