package no.npolar.common.forms;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class for utilizing localized messages in this bundle.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Messages extends A_CmsMessageBundle {
    
    /** Message constant for  */
    public static final String MSG_FORMAT_REQUIRED_EMAIL_0 = "MSG_FORMAT_REQUIRED_EMAIL_0";
    public static final String MSG_FORMAT_REQUIRED_NUMERIC_0 = "MSG_FORMAT_REQUIRED_NUMERIC_0";
    public static final String MSG_FORMAT_REQUIRED_DATETIME_0 = "MSG_FORMAT_REQUIRED_DATETIME_0";
    public static final String MSG_FORM_EXPIRED_0 = "MSG_FORM_EXPIRED_0";
    public static final String ERR_CONSTRAINT_CLASS_NOT_FOUND_1 = "ERR_CONSTRAINT_CLASS_NOT_FOUND_1";
    public static final String ERR_TOO_MANY_VALUES_0 = "ERR_TOO_MANY_VALUES_0";
    public static final String ERR_REQUIRED_FIELD_MISSING_0 = "ERR_REQUIRED_FIELD_MISSING_0";
    public static final String ERR_DUPLICATE_VALUE_1 = "ERR_DUPLICATE_VALUE_1";
    public static final String ERR_NO_VALUE_0 = "ERR_NO_VALUE_0";
    public static final String ERR_VALUE_TOO_LONG_1 = "ERR_VALUE_TOO_LONG_1";
    public static final String ERR_SET_CONSTRAINT_1 = "ERR_SET_CONSTRAINT_1";
    public static final String ERR_REPORT_NO_FORM_URI_0 = "ERR_REPORT_NO_FORM_URI_0";
    public static final String ERR_REPORT_INVALID_FORM_URI_1 = "ERR_REPORT_INVALID_FORM_URI_1";
    public static final String DATA_COUNTRIES_0 = "DATA_COUNTRIES_0";
    
    public static final String LABEL_DEFAULT_BUTTON_PREVIEW = "label.default.button.preview";
    public static final String LABEL_DEFAULT_BUTTON_BACK = "label.default.button.back";
    public static final String LABEL_DEFAULT_BUTTON_CONFIRM = "label.default.button.confirm";
    
    
    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "no.npolar.common.forms.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {
        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {
        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {
        return BUNDLE_NAME;
    }
}
