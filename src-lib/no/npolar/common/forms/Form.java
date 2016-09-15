package no.npolar.common.forms;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opencms.util.CmsUUID;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Main class for interacting with the forms created via this module.
 * <p>
 * Created Form instances are store in session variables, so that when a form is 
 * submitted, any post-processing script has easy access to it.
 * <p>
 * The following HTML form elements are supported:
 * <ul>
 * <li>text</li>
 * <li>textarea</li>
 * <li>select</li>
 * <li>checkbox</li>
 * <li>radio</li>
 * <li>password</li>
 * </ul>
 * <p>
 * In addition, a special version of the text element, <code>datetime</code>, 
 * has been implemented specially for date/time input.
 * <p>
 * File upload and CAPTCHA are among the obvious elements not supported yet.
 * <p>
 * Support for updating an existing form submission to be added at a later time.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class Form implements Comparable {
    
    /** Constant for the HTTP POST method. */
    public static final String METHOD_POST = "post";
    
    /** Constant for the HTTP GET method. */
    public static final String METHOD_GET = "get";
    
    /** Holds the form's title. */
    private String title = null;
    
    /** Holds the form's information text. */
    private String information = null;
    
    /** Holds the form's information text. */
    private Date expiryDatetime = null;
    
    /** Holds the form's name. */
    private String name = null;
    
    /** Holds the URI to the structured content VFS file representing the form. */
    private String resourceUri = null;
    
    /** Holds the structure ID to the structured content VFS file representing the form. */
    private CmsUUID structureId = null;
    
    /** Holds the database table name for the form. */
    private String tableName = null;
    
    /** Holds the list of input elements in the form. */
    private LinkedHashMap elements;
    
    /** Holds all hidden elements in the form. */
    private LinkedHashMap hiddenElements;
    
    /** Holds the method of the form. The default is HTTP POST. */
    private String method = METHOD_POST;
    
    /** Holds the form action. */
    private String action = "";
    
    /** Holds the text for the "continue to preview" button. The default is "Submit". */
    private String buttonTextPreview = " Submit ";
    
    /** Holds the text for the "back to form" button. The default is "Back". */
    private String buttonTextBack = " Back ";
    
    /** Holds the text for the "confirm form submission" button. The default is "Confirm". */
    private String buttonTextConfirm = " Confirm ";
    
    /** Holds text to display to the user during submission review. */
    private String confirmText = null;
    
    /** Holds text to display to the user upon successful submission. */
    private String successText = null;
    
    /** Holds text to display to the user when (s)he attempts to access the form after it has expired. */
    private String expiredText = null;
    
    /** Holds the path to the script that will be called upon valid form submission (if any).  */
    private String onHandle = null;
    
    /** SQL handler instance, performs database transactions for the class. */
    private FormSqlManager sqlManager = null;
    
    /** VFS handler instance, performs operations against the OpenCms virtual file system (VFS). */
    private FormVfsManager formFileManager = null;
    
    /** The current locale for this form, read from the request context upon form creation. */
    private Locale locale = null;
    
    /** Flag indicating whether or not editing is enabled. */
    private boolean editEnabled = false;
    
    /** Holds the id of the entry being edited. */
    private int editingEntryId = -1;
    
    /** Holds the notification email (if any). */
    private AutoEmail notificationEmail = null;
    
    /** Holds the confirmation email. */
    private AutoEmail confirmationEmail = null;
    
    /** The supported input field types, indexed to match the integer values returned by {@link I_FormInputElement#getType()}. */
    public static final String[] FORM_INPUT_TYPES = { "Text",
                                                    "Text-area",
                                                    "Drop-down",
                                                    "Checkbox",
                                                    "Radiobutton",
                                                    "Date-time",
                                                    "Country",
                                                    "Password" };
    
    /**
     * Constructor - creates a new form by reading the form resource.
     * <p>
     * This constructor calls the 
     * {@link FormVfsManager#build(no.npolar.common.forms.Form, org.opencms.file.CmsObject)} 
     * method after initial setup of class variables.
     * <p>
     * The {@link FormVfsManager#build(no.npolar.common.forms.Form, org.opencms.file.CmsObject)} 
     * method reads the OpenCms resource at <code>resourceUri</code> and 
     * populates the form with elements. The form is normally ready to use after 
     * this constructor is done.
     * <p>
     * Default settings:
     * <ul>
     * <li>The form will have <code>method="post"</code> and <code>action=""</code>.</li>
     * <li>The <code>submit</code> button will have the label "Submit".</li>
     * </ul>
     * 
     * @param resourceUri The URI of the form resource in the OpenCms VFS.
     * @param cmso An initialized CmsObject that can be used to access the OpenCms VFS.
     * @see org.opencms.file.CmsObject
     * @throws CmsException
     */
    public Form(String resourceUri, CmsObject cmso) throws CmsException {
        this.resourceUri    = resourceUri;
        this.structureId    = cmso.readResource(resourceUri).getStructureId();
        this.locale         = cmso.getRequestContext().getLocale();
        this.tableName      = structureId.getStringValue().replaceAll("-", "_");
        this.elements       = new LinkedHashMap<String, I_FormInputElement>();
        this.hiddenElements = new LinkedHashMap<String, I_FormInputElement>();
        this.sqlManager     = new FormSqlManager(this, cmso);
        this.formFileManager= new FormVfsManager();
        
        this.formFileManager.build(this, cmso);
    }
    
    /**
     * Constructor - creates an empty form (no elements) with the specified name.
     * <p>
     * Default settings:
     * <ul>
     * <li>The form will have <code>method="post"</code> and <code>action=""</code>.</li>
     * <li>The <code>submit</code> button will have the label "Submit".</li>
     * </ul>
     * 
     * @param name The name of the form.
     * @param resourceUri The URI of the form resource in the OpenCms VFS.
     * @param cmso An initialized CmsObject that can be used to access the OpenCms VFS.
     * @see org.opencms.file.CmsObject
     * @throws CmsException
     * @deprecated Use the {@link Form#Form(String, CmsObject)} constructor instead.
     */
    public Form(String name, String resourceUri, CmsObject cmso) throws CmsException {
        this.name           = name;
        this.resourceUri    = resourceUri;
        this.structureId    = cmso.readResource(resourceUri).getStructureId();
        this.locale         = cmso.getRequestContext().getLocale();
        this.tableName      = structureId.getStringValue().replaceAll("-", "_");
        this.elements       = new LinkedHashMap<String, I_FormInputElement>();
        this.sqlManager       = new FormSqlManager(this, cmso);
        this.formFileManager= new FormVfsManager();
        this.formFileManager.build(this, cmso);
    }
    
    /**
     * Creates an empty form with the specified name.
     * 
     * @param name The name of the form.
     */
    public Form(String name) {
        this.name = name;
        this.elements = new LinkedHashMap<String, I_FormInputElement>();
    }
    
    /**
     * Copy constructor.
     * 
     * @param otherForm the form to copy from.
     */
    public Form(Form otherForm) {
        this.action             = otherForm.action;
        this.elements           = otherForm.elements;
        this.formFileManager    = otherForm.formFileManager;
        this.hiddenElements     = otherForm.hiddenElements;
        this.information        = otherForm.information;
        this.expiryDatetime     = otherForm.expiryDatetime;
        this.locale             = otherForm.locale;
        this.method             = otherForm.method;
        this.name               = otherForm.name;
        this.onHandle           = otherForm.onHandle;
        this.resourceUri        = otherForm.resourceUri;
        this.sqlManager         = otherForm.sqlManager;
        //this.submitLabel        = otherForm.submitLabel;
        this.tableName          = otherForm.tableName;
        this.title              = otherForm.title;
        this.buttonTextBack     = otherForm.buttonTextBack;
        this.buttonTextConfirm  = otherForm.buttonTextConfirm;
        this.buttonTextPreview  = otherForm.buttonTextPreview;
        this.confirmText        = otherForm.confirmText;
        this.successText        = otherForm.successText;
        this.expiredText        = otherForm.expiredText;
    }
    
    /**
     * Compare implementation.
     * <p>
     * <strong>ToDo: Avoid NPE on equals(Object) calls.</strong>
     * 
     * @param otherForm another form to compare this one with.
     * @return <code>true</code> if the forms are identical, <code>false</code> if not.
     */
    @Override
    public int compareTo(Object otherForm) {
        if (!this.getClass().isInstance(otherForm))
            return -1;
        
        if (this.action.equals(((Form)otherForm).action)
                && this.elements.equals(((Form)otherForm).elements)
                && this.formFileManager.equals(((Form)otherForm).formFileManager)
                && this.hiddenElements.equals(((Form)otherForm).hiddenElements)
                && this.information.equals(((Form)otherForm).information)
                && this.expiryDatetime.equals(((Form)otherForm).expiryDatetime)
                && this.locale.equals(((Form)otherForm).locale)
                && this.method.equals(((Form)otherForm).method)
                && this.name.equals(((Form)otherForm).name)
                && this.onHandle.equals(((Form)otherForm).onHandle)
                && this.resourceUri.equals(((Form)otherForm).resourceUri)
                && this.sqlManager.equals(((Form)otherForm).sqlManager)
                //&& this.submitLabel.equals(((Form)otherForm).submitLabel)
                && this.tableName.equals(((Form)otherForm).tableName)
                && this.title.equals(((Form)otherForm).title)
                && this.buttonTextBack.equals(((Form)otherForm).buttonTextBack)
                && this.buttonTextConfirm.equals(((Form)otherForm).buttonTextConfirm)
                && this.buttonTextPreview.equals(((Form)otherForm).buttonTextPreview)
                && this.confirmText.equals(((Form)otherForm).confirmText)
                && this.successText.equals(((Form)otherForm).successText)
                && this.expiredText.equals(((Form)otherForm).expiredText)
                ) {
            return 0;
        }
        
        return -1;
    }
    
    /**
     * Adds a new form element with no information or help text, and no options.
     * 
     * @param type The type of the form element (see I_FormInputElement for the integer constants).
     * @param name The name of the form element. Each element in the form must have a unique name.
     * @param label The label of the form element.
     * @param required Set to <code>true</code> if this element should be a required element, <code>false</code> if not.
     * @return The form input element that was added to the form, or <code>null</code> if none.
     * @see #addElement(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.util.List) 
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     */
    public I_FormInputElement addElement(
            int type
            , String name
            , String label
            , boolean required
            ) {
        return addElement(type, null, null, name, label, required, null);
    }
    
    /**
     * Adds a new form element with no information or help text.
     * 
     * @param type The type of the form element (see I_FormInputElement for the integer constants).
     * @param name The name of the form element. Each element in the form must have a unique name.
     * @param label The label of the form element.
     * @param required Set to <code>true</code> if this element should be a required element, <code>false</code> if not.
     * @param options List of options, each option must be an Option instance. Must be <code>null</code> for the types <code>text</code> and <code>textarea</code>, and [not null] for the other types.
     * @return The form input element that was added to the form, or <code>null</code> if none.
     * @see #addElement(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.util.List) 
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     */
    public I_FormInputElement addElement(
            int type
            , String name
            , String label
            , boolean required
            , List options
            ) {
        return addElement(type, null, null, name, label, required, options);
    }
    
    /**
     * Adds a new form element with no help text.
     * 
     * @param type The type of the form element (see I_FormInputElement for the integer constants).
     * @param information The optional information text for the form element.
     * @param name The name of the form element. Each element in the form must have a unique name.
     * @param label The label of the form element.
     * @param required Set to <code>true</code> if this element should be a required element, <code>false</code> if not.
     * @param options List of options, each option must be an Option instance. Must be <code>null</code> for the types text and textarea, and [not null] for the other types.
     * @return The I_FormInputElement that was added to the form, or <code>null</code> if none.
     * @see #addElement(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.util.List) 
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     */
    public I_FormInputElement addElement(
            int type
            , String information
            , String name
            , String label
            , boolean required
            , List options
            ) {
        return addElement(type, information, null, name, label, required, options);
        /*
        // Check if the name exists in the form. Names need to be unique.
        if (elements != null) {
            if (elements.size() > 0) {
                if (elements.keySet().contains(name))
                    throw new IllegalArgumentException("Duplicate entry for form element name '" + name + "'. Form element names must be unique.");
            }
        }
            
        I_FormInputElement element = null;
        switch (type) {
            case I_FormInputElement.TEXT:
                if (options != null)
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Text'.");
                element = new InputTypeText(name, label, required, this);
                break;
            case I_FormInputElement.TEXTAREA:
                if (options != null)
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Textarea'.");
                element = new InputTypeTextarea(name, label, required, this);
                break;
            case I_FormInputElement.DATETIME:
                if (options != null)
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Date-time'.");
                element = new InputTypeDateTime(name, label, required, this);
                break;
            case I_FormInputElement.SELECT:
                if (options == null)
                    throw new IllegalArgumentException("Trying to create a form element of type 'Select' without options.");
                element = new InputTypeSelect(name, label, required, options, this);
                break;
            case I_FormInputElement.CHECKBOX:
                if (options == null)
                    throw new IllegalArgumentException("Trying to create a form element of type 'Checkbox' without options.");
                element = new InputTypeCheckbox(name, label, required, options, this);
                break;
            case I_FormInputElement.RADIO:
                if (options == null)
                    throw new IllegalArgumentException("Trying to create a form element of type 'Radio' without options.");
                element = new InputTypeRadio(name, label, required, options, this);
                break;
            case I_FormInputElement.SELECT_SINGLE_COUNTRY:
                element = new InputTypeSelectSingleCountry(name, label, required, options, this);
                break;
            case I_FormInputElement.PASSWORD:
                element = new InputTypePassword(name, label, required, this);
                break;
            default:
                break;
        }
        if (element != null) {
            element.setInformation(information);
            elements.put(element.getName(), element);
        }
        return element;
        */
    }
    
    /**
     * Adds an element to the form.
     * <p>
     * The element can be one of the standard HTML input types:
     * <ul>
     * <li>text</li>
     * <li>textarea</li>
     * <li>select</li>
     * <li>checkbox</li>
     * <li>radio</li>
     * <p>
     * Options are options for the <code>select</code> type, for 
     * <code>checkbox</code> and <code>radio</code> types, the options represent
     * the checkboxes/radiobuttons, and the element itself is merely a grouping 
     * for the checkboxes/radiobuttons.
     * <p>
     * The types <code>text</code> and <code>textarea</code> can not have
     * options, and hence an exception is thrown if an attempt is made to add 
     * options onto these element types. Likewise, the other types <em>must</em> 
     * have options, and an exception is thrown if an attempt is made to add 
     * such an element without options.
     * 
     * @param type The type of the form element (see I_FormInputElement for the integer constants).
     * @param information The optional information text for the form element.
     * @param helpText The optional help text for the form element.
     * @param name The name of the form element. Each element in the form must have a unique name.
     * @param label The label of the form element.
     * @param required Set to <code>true</code> if this element should be a required element, <code>false</code> if not.
     * @param options List of options, each option must be an Option instance. Must be <code>null</code> for the types <code>text</code> and <code>textarea</code>, and [not null] for the other types.
     * @return The form input element that was added to the form, or <code>null</code> if none.
     * @see I_FormInputElement
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     */
    public I_FormInputElement addElement(
            int type
            , String information
            , String helpText
            , String name
            , String label
            , boolean required
            , List options
            ) {
        // Check if the name exists in the form. Names need to be unique.
        if (elements != null) {
            if (elements.size() > 0) {
                if (elements.keySet().contains(name)) {
                    throw new IllegalArgumentException("Duplicate entry for form element name '" + name + "'. Form element names must be unique.");
                }
            }
        }
            
        I_FormInputElement element = null;
        switch (type) {
            case I_FormInputElement.TEXT:
                if (options != null) {
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Text'.");
                }
                element = new InputTypeText(name, label, required, this);
                break;
            case I_FormInputElement.TEXTAREA:
                if (options != null) {
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Textarea'.");
                }
                element = new InputTypeTextarea(name, label, required, this);
                break;
            case I_FormInputElement.DATETIME:
                if (options != null) {
                    throw new IllegalArgumentException("Options can not be added as part of a form input field of type 'Date-time'.");
                }
                element = new InputTypeDateTime(name, label, required, this);
                break;
            case I_FormInputElement.SELECT:
                if (options == null) {
                    throw new IllegalArgumentException("Trying to create a form element of type 'Select' without options.");
                }
                element = new InputTypeSelect(name, label, required, options, this);
                break;
            case I_FormInputElement.CHECKBOX:
                if (options == null) {
                    throw new IllegalArgumentException("Trying to create a form element of type 'Checkbox' without options.");
                }
                element = new InputTypeCheckbox(name, label, required, options, this);
                break;
            case I_FormInputElement.RADIO:
                if (options == null) {
                    throw new IllegalArgumentException("Trying to create a form element of type 'Radio' without options.");
                }
                element = new InputTypeRadio(name, label, required, options, this);
                break;
            case I_FormInputElement.SELECT_SINGLE_COUNTRY:
                element = new InputTypeSelectSingleCountry(name, label, required, options, this);
                break;
            case I_FormInputElement.PASSWORD:
                element = new InputTypePassword(name, label, required, this);
                break;
            default:
                break;
        }
        if (element != null) {
            element.setInformation(information);
            element.setHelpText(helpText);
            elements.put(element.getName(), element);
        }
        return element;
    }
    
    /**
     * Adds an already created element to the form.
     * 
     * @param element The element to add to the form.
     * @see I_FormInputElement
     */
    public void addElement(I_FormInputElement element) {
        elements.put(element.getName(), element);
    }
    
    /**
     * This method updates the form and its elements with a submission map.
     * <p>
     * The submission map should have the form element names as keys, each key
     * mapping to that element's submitted value(s) contained in a String array.
     * <br/>
     * <strong>NOTE:</strong> This method performs no database transactions. To
     * update the database, invoke the {@link #update(int) update} method.
     * <br/>
     * Usage:
     * 
     * <ul>
     * <li>The easy approach is to pass
     * <code>javax.servlet.http.HttpServletRequest.getParameterMap()</code> as the
     * argument to this method.</li>
     * <li>To accomplish the same thing manually:<br/>
     * Using a parameterName (form element name) as a Map key, the method 
     * <code>javax.servlet.http.HttpRequest.getParameterValues(parameterName)</code>
     * will return a String array that can be put directly as the key's 
     * corresponding value.</li>
     * </ul>
     * 
     * @param submission The submitted values as a <code>Map&lt;String parameterName, String[] parameterValues&gt;</code>.
     * @see "javax.servlet.http.HttpServletRequest"
     */
    public void submit(Map submission) {
        Set keys = elements.keySet();
        String key = null;
        String[] values = null;
        
        I_FormInputElement element = null;
        Iterator i = keys.iterator();
        
        while (i.hasNext()) {
            key = (String)i.next();
            values = (String[])submission.get(key);
            element = (I_FormInputElement)elements.get(key);
            if (element != null) {
                if (values == null)
                    values = new String[0];
                element.submit(values);
            }
        }
    }
    
    /**
     * Checks if the form has valid submissions for all its elements.
     * <p>
     * If one or more elements has a non-valid submission, this method will 
     * return <code>false</code>.
     * 
     * @return <code>true</code> if the form has a valid submission, <code>false</code> if not.
     * @see I_FormInputElement#hasValidSubmit() 
     */
    public boolean hasValidSubmission() {
        Set keys = elements.keySet();
        String key = null;
        I_FormInputElement element = null;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            key = (String)i.next();
            element = (I_FormInputElement)elements.get(key);
            if (!element.hasValidSubmit())
                return false;
        }
        return true;
    }
    
    /**
     * Checks if the form has an existing table in the database, using the given 
     * CmsObject.
     * 
     * @param cmso Initialized CmsObject.
     * @return <code>true</code> if the form has an existing table in the database, <code>false</code> if not.
     * @throws SQLException
     * @throws InstantiationException 
     * @see Form#hasExistingTable() 
     * @see FormSqlManager#hasExistingTable(org.opencms.file.CmsObject) 
     */
    public boolean hasExistingTable(CmsObject cmso) throws SQLException, InstantiationException { 
        return this.sqlManager.hasExistingTable(cmso); 
    }
    
    /**
     * Checks if the form has an existing table in the database, using a new 
     * CmsObject initialized with the export user.
     * 
     * @return <code>true</code> if the form has an existing table in the database, </code>false</code> if not.
     * @throws SQLException 
     * @see Form#hasExistingTable(org.opencms.file.CmsObject) 
     * @see FormSqlManager#hasExistingTable(org.opencms.file.CmsObject) 
     */
    public boolean hasExistingTable() throws SQLException {
        try {
            CmsObject cmso = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            return this.sqlManager.hasExistingTable(cmso); 
        } catch (Exception e) {
            throw new SQLException("Error checking table existence for form: " + e.getMessage());
        }
    }
    
    /**
     * Gets the result set for a single submission, identified by its unique 
     * value for a given field.
     * 
     * @param name The name of the unique field/column.
     * @param value The unique value that identifies the submission to fetch.
     * @param cmso An initialized CmsObject.
     * @return A result set of 1 or 0 rows.
     * @throws InstantiationException If a connection to the database cannot be established.
     * @throws SQLException If the SQL query fails.
     */
    public ResultSet getSubmissionFromUnique(String name, String value, CmsObject cmso) throws SQLException, InstantiationException {
        return this.sqlManager.getSubmissionFromUnique(name, value, cmso);
    }
    
    /**
     * Gets the result set for a single submission, identified by its unique 
     * value for a given field.
     * <p>
     * Uses a new CmsObject initialized with the export user.
     * 
     * @param name The name of the unique field/column.
     * @param value The unique value that identifies the submission to fetch.
     * @return A result set of 1 or 0 rows.
     * @throws SQLException If anything goes wrong.
     */
    public ResultSet getSubmissionFromUnique(String name, String value) throws SQLException {
        try {
            CmsObject cmso = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            return this.sqlManager.getSubmissionFromUnique(name, value, cmso);
        } catch (Exception e) {
            throw new SQLException("Error getting submission from unique value. The error was: " + e.getMessage());
        }
    }
    
    /**
     * Gets the result set for a single submission, identified by its ID.
     * 
     * @param id The ID of the submission (the row ID).
     * @param cmso An initialized CmsObject.
     * @return A result set of 1 or 0 rows.
     * @throws InstantiationException If a connection to the database cannot be established.
     * @throws SQLException If the SQL query fails.
     */
    public ResultSet getSubmissionById(int id, CmsObject cmso) throws InstantiationException, SQLException {
        return this.sqlManager.getSubmissionFromId(id, cmso);
    }
        
    /**
     * Gets <em>N</em> rows from the form's table, using the given field value.
     * 
     * @param field The field name to use.
     * @param value The field value to use.
     * @param cmso Instantiated CmsObject.
     * @return N rows, as a result set.
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     */
    protected ResultSet getFormEntryByFieldValue(String field, String value, CmsObject cmso) throws SQLException, InstantiationException {
        return this.sqlManager.getEntryByFieldValue(field, value);
    }
    
    
    /**
     * Recreates a form by fetching its data from the form's table in the 
     * database, using the primary key.
     * 
     * @param id The primary key value.
     * @param cmso Initialized CmsObject.
     * @throws java.sql.SQLException If a MySQL operation fails.
     * @throws java.lang.InstantiationException  If the MySQL connector object SQLAgent cannot be instantiated, or if the query result set is <code>null</code>.
     */
    public void recreateFromId(String id, CmsObject cmso) throws SQLException, InstantiationException {
        ResultSet rs = this.sqlManager.getSubmissionFromId(Integer.parseInt(id), cmso);  
        if (rs == null) {
            throw new InstantiationException("No data to recreate form from.");
        }
        
        Set keys = elements.keySet();
        Iterator i = keys.iterator();
        I_FormInputElement e = null;
        
        // Update the form elements with the submitted values
        while (i.hasNext()) {
            e = (I_FormInputElement)elements.get(i.next());
            e.recreate(rs);
        }
    }
    
    /**
     * Recreates a form by fetching its data from the form's table in the 
     * database, using a unique field.
     * <p>
     * NOTE: THE UNIQUE FIELD MUST BE A STRING VALUE (fix later?)
     * <p>
     * ToDo: NEEDS FIX TO UPDATE ACCORDING TO THE "MULTIPLE UNIQUE" CHANGE
     * 
     * @param uniqueName The unique field name.
     * @param uniqueValue The unique field value.
     * @param cmso Instantiated CmsObject.
     * @throws java.sql.SQLException  If a MySQL operation fails.
     * @throws java.lang.InstantiationException  If MySQL connector object SQLAgent cannot be instantiated, or if the query result set is <code>null</code>.
     */
    public void recreateFromUniqueField(String uniqueName, String uniqueValue, CmsObject cmso) throws SQLException, InstantiationException {
        //ResultSet rs = this.getSubmissionFromUnique(uniqueName, uniqueValue, cmso);  
        ResultSet rs = this.sqlManager.getSubmissionFromUnique(uniqueName, uniqueValue, cmso);  
        if (rs == null)
            throw new InstantiationException("No data to recreate form from.");
            //return "ERROR: No data to recreate from.";
        
        Set keys = elements.keySet();
        Iterator i = keys.iterator();
        I_FormInputElement e = null;
        
        // Update the form elements with the submitted values
        while (i.hasNext()) {
            e = (I_FormInputElement)elements.get(i.next());
            e.recreate(rs);
        }
    }
    
    /**
     * Gets the HTML code for displaying the form on a web page.
     * <p>
     * Choose between HTML and XHTML syntax. This method calls each form 
     * element's {@link I_FormInputElement#getHtml(boolean)} method (in the 
     * added order) to construct the HTML code.
     * 
     * @param xhtmlSyntax Provide <code>true</code> for XHTML syntax, or <code>false</code> for HTML syntax.
     * @return The ready-to-use HTML code for displaying the form on a web page.
     * @see I_FormInputElement#getHtml(boolean)
     * @see <a href="http://www.w3.org/TR/html401/interact/forms.html" target="_blank">HTML Forms</a>
     */
    public String getHtml(boolean xhtmlSyntax) {
        String html = "<div class=\"form\">";
        html += "<form";
        if (name != null) {
            html += " name=\"" + name + "\"";
        }
        if (action != null) {
            html += " action=\"" + action + "\"";
        }
        if (method != null) {
            html += " method=\"" + method + "\"";
        }
        html += ">";
        
        
        Set keys = elements.keySet();
        String key = null;
        I_FormInputElement element = null;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            key = (String)i.next();
            element = (I_FormInputElement)elements.get(key);
            if (element != null) {
                if (!this.hiddenElements.containsKey(element.getName())) {
                    html += element.getHtml(xhtmlSyntax);
                }
            }
        }
        html += "<div class=\"submit\">"
                + "<button"
                    + " class=\"button button--continue submit-button\""
                    + " type=\"submit\""
                    + " name=\"submit\""
                    + " value=\"" + this.getButtonTextPreview() + "\""
                + ">" 
                + getButtonTextPreview() 
                + "</button>"
                + "</div>";
        //html += "<div class=\"submit\"><input class=\"submit-button\" type=\"submit\" name=\"submit\" value=\"" + this.getButtonTextPreview() + "\" " + (xhtmlSyntax ? "/" : "") + "></div>";
        //html += "<div class=\"submit\"><input type=\"button\" name=\"dataview\" value=\"Data\"></div>";
        html += "</form></div>";
        return html;
    }
    
    /**
     * Returns HTML code that can be used to show a preview of the user's 
     * submission.
     * 
     * @return The HTML code for a submit preview.
     */
    public String getPreview() {
        String preview = "";
        Map formElements = this.getElements();
        Set keys = formElements.keySet();
        Iterator itr = keys.iterator();
        Iterator itr_opt = null;
        String[] submittedValues = null;
        List options = null;
        I_FormInputElement element = null;
        
        //
        // Print out the form submission
        //
        preview += "<div class=\"form submit-preview\">";
        
        while (itr.hasNext()) { // Loop over all submitted parameters
            options = null;
            element = (I_FormInputElement)formElements.get(itr.next()); // Get the form element from the Form object
            preview += "<div class=\"element\">" +
                        "<div class=\"label\">" + element.getLabel() + "</div>" +
                            "<div class=\"input\">"; 
            if (element.getType() == I_FormInputElement.TEXT || 
                    element.getType() == I_FormInputElement.TEXTAREA ||
                    element.getType() == I_FormInputElement.DATETIME) { // Procedure for non-option elements
                submittedValues = element.getSubmission(); // Get the submitted values (for these element types, there should be only one value)
                if (submittedValues != null) {
                    for (int i = 0; i < submittedValues.length; i++) {
                        preview += submittedValues[i]; // Print out the submitted values (again, should be only a single value)
                        if (submittedValues.length > i+1) {
                            preview += ", ";
                        }
                    }
                }
                else {
                    preview += "&ndash;"; // Print a "-" if no value was submitted
                }
            }
            else { // Procedure for option elements
                options = element.getOptions(); // Get element options
                Option opt = null;
                String optString = "";
                itr_opt = options.iterator();

                while (itr_opt.hasNext()) { // Loop over all options
                    opt = (Option)itr_opt.next();
                    if (opt.isSelected()) { // If the option is selected....
                        optString += opt.getText() + ", "; // ...print it out, and add a comma at the end
                    }
                }
                try {
                    preview += optString.substring(0, optString.length()-2); // Remove the last comma
                } catch (StringIndexOutOfBoundsException sioobe) {
                    // Means that optString is empty, so there is no last comma to remove
                }
            }
            preview += "</div></div>";
        }
        preview += "</div>";
        return preview;
    }
    
    /**
     * Checks the validity of the "unique" option.
     * <p>
     * If, for some reason, the given form element's values should not be unique 
     * in the database table, an exception will be thrown to describe the 
     * problem. (E.g. that there is no such element, or it is a multiple choice 
     * element.)
     * 
     * @param elementName The name of the form element to validate as "unique".
     * @throws IllegalArgumentException If the given form element's values should not be unique.
     */
    public void validateUniqueField(String elementName) {
        if (elementName != null) {
            I_FormInputElement unique = this.getElement(elementName);
            if (unique == null) {
                throw new IllegalArgumentException("The form element '" + elementName + "'"
                        + " cannot be unique, the form contains no such element.");
            }
            else if (unique.isMultipleChoice()) {
                throw new IllegalArgumentException("The form element '" + elementName + "'"
                        + " should not be unique, as it is a multiple choice element.");
            }
            else if (!unique.isRequired()) {
                throw new IllegalArgumentException("The form element '" + elementName + "'"
                        + " cannot be unique, this element is not required (set as 'required' to fix).");
            }
        }
    }
    
    /**
     * Flags an element as hidden, meaning it will not be included in the HTML
     * code generated by {@link Form#getHtml(boolean)}.
     * 
     * @param elementName The name of the element to hide.
     * @return <code>true</code> if the form contains an element with the given name, <code>false</code> if not.
     */
    public boolean hideElement(String elementName) {
        if (this.elements.containsKey(elementName)) {
            this.hiddenElements.put(elementName, this.getElement(elementName));
            return true;
        }
        return false;
    }
    
    /**
     * Flags an element as "open" (not hidden), meaning it will be included in 
     * the HTML code generated by {@link Form#getHtml(boolean)}.
     * 
     * @param elementName The name of the element to show.
     * @return <code>true</code> if the form contains an element with the given name, <code>false</code> if not.
     */
    public boolean showElement(String elementName) {
        if (this.elements.containsKey(elementName)) {
            if (hiddenElements.containsKey(elementName)) {
                hiddenElements.remove(elementName);
                return true;
            }
            return false;
        }
        return false;
    }
    
    /**
     * Sets the path to a script that will be called after the form has been
     * submitted.
     * <p>
     * The post-submit script is typically applied to perform actions that are
     * related to the form submission, but not part of the form itself.
     * <p>
     * For example, the script can be used to send a confirmation e-mail to 
     * the person who submitted the form, update a file or database with some 
     * data submitted via the form, create a user account / password, etc.
     * 
     * @param script The path to the script to be called upon valid form submission.
     */
    public void setPostSubmitScript(String script) { this.onHandle = script; }
    
    /**
     * Sets the form title.
     * 
     * @param title The form title.
     */
    public void setTitle(String title) { this.title = title; }
    
    /**
     * Sets the "continue to preview" button text.
     * 
     * @param text The button text.
     */
    public void setButtonTextPreview(String text) { this.buttonTextPreview = text; }
    
    /**
     * Sets the "back to form" button text.
     * 
     * @param text The button text.
     */
    public void setButtonTextBack(String text) { this.buttonTextBack = text; }
    
    /**
     * Sets the "confirm form submission" button text.
     * 
     * @param text The button text.
     */
    public void setButtonTextConfirm(String text) { this.buttonTextConfirm = text; }
    
    /**
     * Gets the "continue to preview" button text.
     * 
     * @return String The button text.
     */
    public String getButtonTextPreview() { return this.buttonTextPreview; }
    
    /**
     * Gets the "back to form" button text.
     * 
     * @return String The button text.
     */
    public String getButtonTextBack() { return this.buttonTextBack; }
    
    /**
     * Gets the "confirm form submission" button text.
     * 
     * @return String The button text.
     */
    public String getButtonTextConfirm() { return this.buttonTextConfirm; }
    
    /**
     * Gets the notification email, if any.
     * <p>
     * <strong>NOTE:</strong> Macros might need to be resolved on the returned 
     * object.
     * 
     * @return The notification email, or <code>null</code> if none.
     */
    public AutoEmail getNotificationEmail() { return this.notificationEmail; }
    
    /**
     * Gets the confirmation email, if any. 
     * <p>
     * <strong>NOTE:</strong> Macros might need to be resolved on the returned 
     * object.
     * 
     * @return The confirmation email, or <code>null</code> if none.
     */
    public AutoEmail getConfirmationEmail() { return this.confirmationEmail; }
    
    /**
     * Sets the form information text.
     * 
     * @param information  The form information text.
     */
    public void setInformation(String information) { this.information = information; }
    
    /**
     * Sets the form expiry datetime, based on an OpenCms datetime string 
     * (numeric, as fetched from property).
     * 
     * @param dateTimeStr The form expiry datetime string.
     */
    public void setExpiryDatetime(String dateTimeStr) { 
        try { 
            this.expiryDatetime = new Date(Long.valueOf(dateTimeStr)); 
        } catch (Exception e) { 
            this.expiryDatetime = new Date(Long.MAX_VALUE);
        } 
    }
    
    /**
     * Sets the text to display to the user during form submission review.
     * 
     * @param text The text to display to the user during form submission review.
     */
    public void setConfirmText(String text) { this.confirmText = text; }
    
    /**
     * Sets the text to display to the user upon successful form submission.
     * 
     * @param text The text to display to the user upon successful form submission.
     */
    public void setSuccessText(String text) { this.successText = text; }
    
    /**
     * Sets the text to display to the user when (s)he attempts to access this 
     * form after it has expired.
     * 
     * @param text The text to display to the user when (s)he attempts to access this form after it has expired.
     */
    public void setExpiredText(String text) { this.expiredText = text; }
    
    /**
     * Sets the notification email.
     * 
     * @param mail The notification email.
     */
    public void setNotificationEmail(AutoEmail mail) { this.notificationEmail = mail; }
    
    /**
     * Sets the confirmation email.
     * 
     * @param mail The confirmation email.
     */
    public void setConfirmationEmail(AutoEmail mail) { this.confirmationEmail = mail; }
    
    /**
     * Sets the method the form should implement; <code>post</code> or 
     * <code>get</code>.
     * 
     * @param method The method name.
     */
    public void setMethod(String method) { this.method = method; }
    
    /**
     * Sets the action the form should implement (which file it should call
     * upon submit).
     * 
     * @param action The (often relative) path to the file that the form should call when submitted.
     */
    public void setAction(String action) { this.action = action != null ? action : ""; }
    
    /**
     * Sets the label (visible text) for the submit button.
     * 
     * @param submitLabel The label for the submit button.
     * @deprecated Button texts are now defined in the form file.
     */
    public void setSubmitLabel(String submitLabel) { setButtonTextPreview(submitLabel); }
    
    /**
     * Sets the name for this form.
     * 
     * @param name The form's name.
     */
    protected void setName(String name) { this.name = name; }
    
    /**
     * Sets the locale for this Form.
     * 
     * @param locale The locale.
     */
    protected void setLocale(Locale locale) { this.locale = locale; }
    
    /**
     * Sets the edit mode for the form.
     * <p>
     * If the form is used to edit an existing entry, the form should be edit 
     * enabled. In other circumstances, the form should not be edit enabled.
     * 
     * @param editEnabled Provide <code>true</code> to enable edit mode, or <code>false</code> to disable edit mode.
     * @param entryId The ID for an existing entry. (Applies only when <code>editEnabled</code> is <code>true</code>.)
     */
    public void setEditEnabled(boolean editEnabled, int entryId) { 
        this.editEnabled = editEnabled;
        if (editEnabled) {
            this.editingEntryId = entryId;
        } else {
            this.editingEntryId = -1;
        }
    }
    
    /**
     * Used to determine if the form is edit enabled, that is: if the form is 
     * currently used to update an existing entry.
     * 
     * @return <code>true</code> if the form is edit enabled, <code>false</code> if not.
     */
    public boolean isEditEnabled() { return this.editEnabled; }
    
    /**
     * Used to determine if the form is configured to automatically send a 
     * notification email, a confirmation email, or both, upon a successful 
     * form submission.
     * 
     * @return <code>true</code> if the form is configured to automatically send any email(s), <code>false</code> if not.
     * @see #hasNotificationEmail()
     * @see #hasConfirmationEmail()
     */
    public boolean isAutoEmailEnabled() { return this.hasNotificationEmail() || this.hasConfirmationEmail(); }
    
    /**
     * Tests if the form is expired.
     * <p>
     * Note that this method will always return <code>false</code> if no expiry 
     * time has been set.
     * 
     * @return <code>true</code> if the form is expired, <code>false</code> if not.
     */
    public boolean isExpired() { 
        if (hasExpiryDate()) {
            return this.getExpiryDatetime().before(new Date()); // Test if the expiry datetime is before now
        }
        return false;
    }
    
    /**
     * Determines if this form has been assigned a script that will be called
     * upon valid form submission.
     * 
     * @return <code>true</code> if the form has a script, <code>false</code> if not.
     */
    public boolean hasPostSubmitScript() { return this.onHandle != null; }
    
    /**
     * Determines if this form has a configured notification email.
     * 
     * @return <code>true</code> if the form has a configured notification email, <code>false</code> if not.
     * @see Form#isAutoEmailEnabled() 
     */
    public boolean hasNotificationEmail() { return this.notificationEmail != null; }
    
    /**
     * Determines if this form has a configured confirmation email.
     * 
     * @return <code>true</code> if the form has a configured confirmation email, <code>false</code> if not.
     * @see Form#isAutoEmailEnabled()
     */
    public boolean hasConfirmationEmail() { return this.confirmationEmail != null; }
    
    /**
     * Determines if this form has been assigned an expiry datetime.
     * 
     * @return <code>true</code> if the form has been assigned an expiry datetime, <code>false</code> if not.
     */
    public boolean hasExpiryDate() { return this.expiryDatetime != null && !new Date(Long.MAX_VALUE).equals(this.expiryDatetime); }
    
    /**
     * Gets the path to the script that is called after a valid form submission.
     * 
     * @return The path to the post-submit script, or <code>null</code> if none.
     */
    public String getPostSubmitScript() { return this.onHandle; }
    
    /**
     * Gets the ID of the entry that's currently being edited, if any.
     * 
     * @return The ID of the entry that's currently being edited, or -1 if no entry is being edited.
     */
    public int getEditingEntryId() { return this.editingEntryId; }
    
    /**
     * Gets the label for the form element with the given name.
     * 
     * @param elementName The name of the element, as it appears in the form.
     * @return The label for the element.
     */
    public String getLabelForElement(String elementName) { return this.getElement(elementName).getLabel(); }
    
    /**
     * Gets a form element with the given name.
     * 
     * @param name The name of the form element.
     * @return The element with the given name, or <code>null</code> if no such element exists.
     * @see I_FormInputElement
     */
    public I_FormInputElement getElement(String name) { return (I_FormInputElement)elements.get(name); }
    
    /**
     * Gets all the elements in this form, as a map where the keys are the names
     * of the elements, and the map values are the elements themselves: 
     * <code>Map&lt;String, I_FormInputElement&gt;</code>.
     * 
     * @return All the elements in this form.
     */
    public Map getElements() { return this.elements; }
    
    /**
     * Gets all the <em>hidden</em> elements in this form, as a map where the 
     * keys are the names of the hidden elements, and the values are the hidden 
     * elements themselves: 
     * <code>Map&lt;String, I_FormInputElement&gt;</code>.
     * <p>
     * Hidden elements are not included in the {@link #getHtml(boolean)} output.
     * 
     * @return All hidden elements of this form.
     */
    public Map getHiddenElements() { return this.hiddenElements; }
    
    /**
     * Gets the label for the submit button.
     * 
     * @return The label of the submit button.
     * @deprecated Button texts are now defined in the form file.
     */
    public String getSubmitLabel() { return this.getButtonTextPreview(); }   
    
    /**
     * Gets the HTML code for the submit button, wrapped in a <code>span</code> 
     * element.
     * 
     * @return The HTML code for the submit button.
     * @deprecated Button texts are now defined in the form file.
     */
    public String getSubmitButtonHtml() { 
        return "<span class=\"submit\">"
                    + "<input type=\"submit\""
                        + " name=\"submit\""
                        + " value=\"" + getButtonTextPreview() + "\""
                    + ">"
                + "</span>"; 
    }
    
    /**
     * Gets the form's action path (the path that is invoked upon submit).
     * 
     * @return The form's action path.
     */
    public String getAction() { return this.action; }
    
    /**
     * Gets the form's method (<code>post</code> or </code>get</code>).
     * 
     * @return The form's method.
     */
    public String getMethod() { return this.method; } 
    
    /**
     * Gets the name of the form.
     * 
     * @return The name of the form.
     */
    public String getName() { return this.name; }
    
    /**
     * Gets the form page title.
     * 
     * @return The form page title.
     */
    public String getTitle() { return this.title; }
    
    /**
     * Gets the form page information text.
     * 
     * @return The form page information text.
     */
    public String getInformation() { return this.information; }
    
    /**
     * Gets the expiry datetime, if any.
     * 
     * @return The expiry datetime, or <code>null</code> if no expiry date is set.
     */
    public Date getExpiryDatetime() { return this.expiryDatetime; }
    
    /**
     * Gets the text to display to the user during form submission review.
     * 
     * @return The text to display to the user during form submission review.
     */
    public String getConfirmText() { return this.confirmText; }
    
    /**
     * Gets the text to display to the user upon successful form submission.
     * 
     * @return The text to display to the user upon successful form submission.
     */
    public String getSuccessText() { return this.successText; }
    
    /**
     * Gets the text to display to the user when (s)he attempts to access this 
     * form after it has expired.
     * 
     * @return The text to display to the user when (s)he attempts to access this form after it has expired.
     */
    public String getExpiredText() { return this.expiredText; }
    
    /**
     * Gets the path to the form file, that is, the URI of OpenCms VFS resource 
     * that represents the form.
     * 
     * @return The path to the form file.
     */
    public String getResourceUri() { return this.resourceUri; }
    
    /**
     * Gets the structure ID of the form file, that is, the structure ID of the 
     * OpenCms VFS resource that defines this form.
     * <p>
     * The structure ID can be used to read the resource, in order to access the 
     * form resource as a {@link org.opencms.file.CmsResource} or 
     * {@link org.opencms.file.CmsFile} instance.
     * 
     * @return The structure ID of the form resource.
     * @see org.opencms.util.CmsUUID
     */
    public CmsUUID getStructureId() { return this.structureId; }
    
    /**
     * Gets the name of the table in the MySQL database that holds the submitted 
     * data for this form.
     * <p>
     * The name of this table is constructed from the structure ID of the form 
     * resource, with all hyphens ("-") replaced by underscores ("_"). (Table 
     * names cannot contain hyphens.)
     * 
     * @return The name of the MySQL table for this form.
     */
    public String getTableName() { return this.tableName; }
    
    /**
     * Gets the form's configured locale.
     * <p>
     * This is the locale that was read from the request context when creating 
     * this form instance.
     * 
     * @return The form's configured locale.
     */
    public Locale getLocale() { return this.locale; }
    
    /**
     * Gets the SQL handler instance.
     * 
     * @return The SQL handler instance.
     */
    public FormSqlManager getSqlManager() { return this.sqlManager; }
    
    /**
     * Gets the VFS handler instance.
     * 
     * @return The VFS handler instance.
     */
    public FormVfsManager getVfsManager() { return this.formFileManager; }
    
    /**
     * Gets the last SQL statement ({@link PreparedStatement#toString()}) 
     * that was executed by this instance.
     * 
     * @return The last executed {@link PreparedStatement}, as a string.
     */
    public String getLastExecutedStatement() { return this.sqlManager.getLastExecutedStatement().toString(); }
    
    /**
     * Checks if the form has one or more unique element(s).
     * 
     * @return <code>true</code> if the form has at least one unique element, <code>false</code> if not.
     */
    public boolean hasUnique() { 
        Set keys = elements.keySet();
        I_FormInputElement element = null;
        String key = null;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            key = (String)i.next();
            element = (I_FormInputElement)elements.get(key);
            if (element.isUnique()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a list of all unique elements in this form.
     * <p>
     * If there are no unique elements in this form, an empty list is returned.
     * 
     * @return A list of {@link I_FormInputElement} objects that are unique elements in the form.
     */
    public List getUniqueElements() {
        ArrayList<I_FormInputElement> uniqueElements = new ArrayList<I_FormInputElement>();
        Set keys = elements.keySet();
        I_FormInputElement element = null;
        String key = null;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            key = (String)i.next();
            element = (I_FormInputElement)elements.get(key);
            if (element.isUnique()) {
                uniqueElements.add(element);
            }
        }
        return uniqueElements;
    }
    
    /**
     * Deletes <strong>all</strong> submitted form data from the database by 
     * dropping the form's corresponding table.
     * <p>
     * <strong>Use very carefully!</strong>
     * 
     * @return <code>true</code> if the data is deleted, <code>false</code> if something goes wrong.
     * @throws SQLException
     * @throws InstantiationException
     * @see FormSqlManager#dropTable() 
     */
    public boolean deleteData() throws SQLException, InstantiationException {
        return sqlManager.dropTable();
    }
    
    /**
     * Deletes a single submitted form entry from the database, that is, deletes 
     * that submission's corresponding row from the table.
     * <p>
     * <strong>Use with care!</strong>
     * 
     * @param entryId The ID of the entry (table row) to delete.
     * @return boolean <code>true</code> if the entry was deleted successfully, <code>false</code> if not.
     * @throws InstantiationException If a connection to the database cannot be established.
     * @throws SQLException If the SQL query fails.
     * @see FormSqlManager#deleteEntry(int) 
     */
    public boolean deleteData(int entryId) throws InstantiationException, SQLException {
        return sqlManager.deleteEntry(entryId);
    }
    
    /**
     * Resolves any macros present in the given text.
     * <p>
     * The actual values are fetched from the database table using the given 
     * entry (row) ID.
     * 
     * @param text The text that may or may not contain macros.
     * @param entryId The ID that identifies a single form submission.
     * @param cmso An initialized CmsObject.
     * @return The given text, with any macros resolved. If no macros are present, the given text is returned unmodified.
     * @see no.npolar.common.forms.view.FormMacroResolver#resolveMacros(java.lang.String, java.sql.ResultSet, no.npolar.common.forms.Form, org.opencms.file.CmsObject) 
     * @throws SQLException
     * @throws InstantiationException
     * @throws CmsException 
     */
    public String resolveMacros(String text, int entryId, CmsObject cmso) 
            throws SQLException, InstantiationException, CmsException {
        
        Iterator<String> itr = FormUtil.getMacros(text).iterator();
        while (itr.hasNext()) {
            String macro = itr.next();
            //out.println("<!-- Found macro: '" + macro + "'");
            String macroReplacement = null;

            if (macro != null) {
                ResultSet rs = this.getSubmissionById(entryId, cmso);
                if (rs.first()) {
                    if (macro.equals("entrydata")) { // ==> All data registered through the form (but not ID and time-last-modified)
                        Form form = new Form(this.getResourceUri(), cmso);
                        form.recreateFromId(rs.getString(FormSqlManager.TABLE_COL_NAME_ID), cmso);
                        Map dataMap = form.getElements();
                        Iterator<String> keys = dataMap.keySet().iterator();
                        macroReplacement = "";
                        while (keys.hasNext()) {
                            String key = keys.next(); // The form field's name
                            I_FormInputElement element = form.getElement(key); // The form field
                            String label = element.getLabel(); // The form field's label

                            String val = null; // The submitted value
                            if (element.isPreDefinedValueInput()) {
                                val = ((A_InputTypePreDefined)element).getSubmittedOptionsAsLabels(false);
                            } else {
                                val = element.getSubmission()[0];
                            }
                            //macroReplacement += label + ":\n" + (val.isEmpty() ? "(blank)" : val) + (keys.hasNext() ? "\n\n" : "");
                            if (!val.isEmpty()) {
                                macroReplacement += label + ":\n" + val + (keys.hasNext() ? "\n\n" : "");
                            }
                        }
                        if (!macroReplacement.isEmpty()) {
                            macroReplacement = "------\n" + macroReplacement + "\n------";
                        }
                    }
                    else if (macro.equals("url")) { // ==> The URL to the individual entry
                        macroReplacement = OpenCms.getLinkManager().getOnlineLink(cmso, cmso.getRequestContext().getUri() + "?_action=view&id=" + rs.getString("id"));
                    }
                    else {
                        macroReplacement = rs.getString(macro);
                        //out.println("<!-- Found macro replacement for '" + macro + "': '" + macroReplacement + "' -->");
                    }

                    Matcher m = Pattern.compile("%\\(" + macro + "\\)").matcher(text);
                    text = m.replaceAll(macroReplacement);
                }
                else {
                    throw new NullPointerException("No entry existed that matched the given ID.");
                }
            }

            if (macroReplacement == null) {
                throw new NullPointerException("The macro value for '" + macro + "' was NULL.");
            }
        }
        return text;
    }
    
    /**
     * Updates the database by executing SQL statements.
     * <p>
     * The <code>type</code> parameter is used to indicate which type of of 
     * statement (or operation) this update invocation is supposed to execute, 
     * and the given value should be one of the <code>SQL_XXXX_STATEMENT</code> 
     * constants of {@link FormSqlManager}.
     * 
     * @param type The type of database update to perform, one of the <code>SQL_XXXX_STATEMENT</code> constants of {@link FormSqlManager}.
     * @return  The number of affected rows. A return value of -1 indicates an error that did not cause an exception to be thrown.
     * @throws java.sql.SQLException If something goes wrong.
     * @throws java.lang.InstantiationException If the {@link SQLAgent} instance fails to instantiate (typically because reading the configuration file failed).
     * @see SQLAgent#readConfig(org.opencms.file.CmsObject)
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     * @see FormSqlManager#update(int) 
     */
    public int update(int type) throws SQLException, InstantiationException {
        return sqlManager.update(type);
    }
}
