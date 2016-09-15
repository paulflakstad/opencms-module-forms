package no.npolar.common.forms;

import java.util.regex.*;

/**
 * Constraint for form input field: email address format.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class EmailAddressConstraint extends A_InputFormatConstraint {
    /** Email regex pattern string. */
    public static final String EMAIL_REGEX_PATTERN = 
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
    
    /** Pattern used by the regex. */
    private Pattern pattern = null;
    
    /** Matcher used by the regex. */
    private Matcher matcher = null;
    
    /**
     * Creates a new constraint instance, based on the email regex pattern 
     * defined in {@link #EMAIL_REGEX_PATTERN}.
     */
    public EmailAddressConstraint() {
        pattern = Pattern.compile(EMAIL_REGEX_PATTERN);
    }    
    
    /**
     * @see A_InputFormatConstraint#validate(no.npolar.common.forms.I_FormInputElement) 
     */
    @Override
    public boolean validate(I_FormInputElement element) {
        matcher = this.pattern.matcher(element.getValue());
        if (matcher.find()) {
            this.error = null;
            return true;
        }
        //this.error = "Icorrect email format.";
        this.error = Messages.get().container(Messages.MSG_FORMAT_REQUIRED_EMAIL_0).key(element.getContainingForm().getLocale());
        return false;
    }
}
