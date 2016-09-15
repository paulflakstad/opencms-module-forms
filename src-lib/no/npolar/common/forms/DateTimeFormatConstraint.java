package no.npolar.common.forms;

import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Constraint for form input field: datetime format.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class DateTimeFormatConstraint extends A_InputFormatConstraint {
    
    /** Date format. */
    private SimpleDateFormat sdf;
    
    //public static final String MYSQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** Constant for the legal datetime format, as set in the DHTML calendar used by this package. */
    public static final String CALENDAR_DATETIME_FORMAT = "dd.MM.yyyy HH:mm";
    
    /**
     * Creates a new constraint instance using the default date format, as 
     * defined by {@link #CALENDAR_DATETIME_FORMAT}.
     */
    public DateTimeFormatConstraint() {
        this.sdf = new SimpleDateFormat(CALENDAR_DATETIME_FORMAT);
    }
    
    /**
     * Creates a new constraint instance using the given date format.
     * 
     * @param sdf The date format to validate against.
     */
    public DateTimeFormatConstraint(SimpleDateFormat sdf) {
        this.sdf = sdf;
    }
    
    /**
     * @see I_InputFormatConstraint#validate(no.npolar.common.forms.I_FormInputElement) 
     */
    @Override
    public boolean validate(I_FormInputElement element) {
        this.sdf.setLenient(false);
        try {
            this.sdf.parse(element.getValue());//stringDate);
            this.error = null;
            return true;
        }
        catch (ParseException pe) {
            this.error = "Icorrect datetime format.";
            this.error = Messages.get().container(Messages.MSG_FORMAT_REQUIRED_DATETIME_0).key(element.getContainingForm().getLocale());
            return false;
        }
    }
}
