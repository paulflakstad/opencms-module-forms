package no.npolar.common.forms;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsFile;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.*;
import org.opencms.xml.types.*;

/**
 * Handles operations within the OpenCms VFS.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FormVfsManager {
    public static final String DEFAULT_LOCALE_NAME = "en";
    
    /**
     * Creates a new blank instance.
     */
    public FormVfsManager() {}
    
    /**
     * Builds a form instance based on its definition file in the OpenCms VFS.
     * 
     * @param form  The form instance.
     * @param cmso  Initialized CmsObject, needed to access the OpenCms VFS.
     * @throws org.opencms.main.CmsException  If something goes wrong.
     * @see Form#getStructureId() 
     */
    protected void build(Form form, CmsObject cmso) throws CmsException {
        CmsResource formResource    = cmso.readResource(form.getStructureId());
        CmsFile formFile            = cmso.readFile(formResource);
        CmsXmlContent content       = CmsXmlContentFactory.unmarshal(cmso, formFile);
        
        String loc = cmso.readPropertyObject(formResource, "locale", true).getValue();
        if (loc == null) {
            loc = DEFAULT_LOCALE_NAME; // Set to default (english) locale, if no locale is set
        }
        Locale locale = new Locale(loc);
        if (!content.hasLocale(locale)) {
            locale = new Locale(DEFAULT_LOCALE_NAME); // Try to construct the form using the default locale (English)
            form.setLocale(locale);
        }
        
        // Containers for XML node content values:
        I_CmsXmlContentValue formTitleCV            = content.getValue("Title", locale);
        I_CmsXmlContentValue formInformationCV      = content.getValue("Information", locale);
        I_CmsXmlContentValue formExpiresCV          = content.getValue("Expires", locale);
        I_CmsXmlContentValue formConfirmCV          = content.getValue("Confirm", locale);
        I_CmsXmlContentValue formSuccessCV          = content.getValue("Success", locale);
        I_CmsXmlContentValue formNameCV             = content.getValue("FormName", locale);
        I_CmsXmlContentValue postSubmitScriptCV     = content.getValue("FormScript", locale);
        I_CmsXmlContentValue previewTextCV          = content.getValue("PreviewText", locale);
        I_CmsXmlContentValue backTextCV             = content.getValue("BackText", locale);
        I_CmsXmlContentValue confirmTextCV          = content.getValue("ConfirmText", locale);
        I_CmsXmlContentValue expiredTextCV          = content.getValue("ExpiredText", locale);
        
        I_CmsXmlContentValue emailToAddrCV          = null;
        I_CmsXmlContentValue emailFromNameCV        = null;
        I_CmsXmlContentValue emailFromAddrCV        = null;
        I_CmsXmlContentValue emailSubjectCV         = null;
        I_CmsXmlContentValue emailTextCV            = null;
        //I_CmsXmlContentValue idCV                   = content.getValue("Identifier", locale); // This will be a unique field in the table
        I_CmsXmlContentValue inputTypeCV            = null;
        I_CmsXmlContentValue inputInfoCV            = null;
        I_CmsXmlContentValue inputHelpTextCV        = null;
        I_CmsXmlContentValue inputNameCV            = null;
        I_CmsXmlContentValue inputLabelCV           = null;
        I_CmsXmlContentValue inputRequiredCV        = null;
        I_CmsXmlContentValue inputUniqueContentCV   = null;
        I_CmsXmlContentValue inputConstraintCV      = null;
        I_CmsXmlContentValue inputLengthCV          = null;
        I_CmsXmlContentValue inputValueCV           = null;
        I_CmsXmlContentValue inputOptionsCV         = null;
        I_CmsXmlContentValue inputOptionsTextCV     = null;
        I_CmsXmlContentValue inputOptionsSelectedCV = null;
        
        if (formNameCV != null) {
            form.setName(formNameCV.getStringValue(cmso));
        }
        if (formTitleCV != null) {
            form.setTitle(formTitleCV.getStringValue(cmso));
        }
        if (formInformationCV != null) {
            form.setInformation(formInformationCV.getStringValue(cmso));
        }
        if (formExpiresCV != null) {
            form.setExpiryDatetime(formExpiresCV.getStringValue(cmso));
        }
        if (formConfirmCV != null) {
            form.setConfirmText(formConfirmCV.getStringValue(cmso));
        }
        if (formSuccessCV != null) {
            form.setSuccessText(formSuccessCV.getStringValue(cmso));
        }
        if (postSubmitScriptCV != null) {
            form.setPostSubmitScript(postSubmitScriptCV.getStringValue(cmso));
        }
        if (previewTextCV != null) {
            form.setButtonTextPreview(previewTextCV.getStringValue(cmso));
            if (form.getButtonTextPreview().isEmpty())
                form.setButtonTextPreview(Messages.get().getBundle(locale).key(Messages.LABEL_DEFAULT_BUTTON_PREVIEW));
        }
        if (backTextCV != null) {
            form.setButtonTextBack(backTextCV.getStringValue(cmso));
            if (form.getButtonTextBack().isEmpty())
                form.setButtonTextBack(Messages.get().getBundle(locale).key(Messages.LABEL_DEFAULT_BUTTON_BACK));
        }
        if (confirmTextCV != null) {
            form.setButtonTextConfirm(confirmTextCV.getStringValue(cmso));
            if (form.getButtonTextConfirm().isEmpty())
                form.setButtonTextConfirm(Messages.get().getBundle(locale).key(Messages.LABEL_DEFAULT_BUTTON_CONFIRM));
        }
        if (expiredTextCV != null) {
            form.setExpiredText(expiredTextCV.getStringValue(cmso));
        } else {
            form.setExpiredText(Messages.get().getBundle(locale).key(Messages.MSG_FORM_EXPIRED_0));
        }
        
        String[] autoMails = new String[] { "NotificationEmail", "ConfirmationEmail" };
        
        for (int autoMailIndex = 0; autoMailIndex < autoMails.length; autoMailIndex++) {
            
            String autoMailName = autoMails[autoMailIndex];
            
            // Confirmation / notification e-mails
            try {
                emailToAddrCV = content.getValue(autoMailName + "/ToAddr", locale);
                emailFromAddrCV = content.getValue(autoMailName + "/FromAddr", locale);
                emailFromNameCV = content.getValue(autoMailName + "/FromName", locale);
                emailSubjectCV = content.getValue(autoMailName + "/Subject", locale);
                emailTextCV = content.getValue(autoMailName + "/Text", locale);

                // Require all of the above fields to be non-empty
                if (emailToAddrCV != null 
                        && emailFromAddrCV != null 
                        && emailFromNameCV != null 
                        && emailSubjectCV != null 
                        && emailTextCV != null) {
                    
                    // The AutoMail class is just a convenience class
                    AutoEmail mail = new AutoEmail();
                    
                    mail.setToAddr(emailToAddrCV.getStringValue(cmso));
                    mail.setFromAddr(emailFromAddrCV.getStringValue(cmso));
                    mail.setFromName(emailFromNameCV.getStringValue(cmso));
                    mail.setSubject(emailSubjectCV.getStringValue(cmso));
                    mail.setMessage(emailTextCV.getStringValue(cmso)); // Note: Macros are not resolved here!!!
                    
                    // Make the form aware of the email(s)
                    if (autoMailName.equals("NotificationEmail")) {
                        form.setNotificationEmail(mail);
                    } else if (autoMailName.equals("ConfirmationEmail")) {
                        form.setConfirmationEmail(mail);
                    }
                }
            } catch (Exception e) {
                // No (valid) e-mail present
            }
        }
        
        //if (idCV != null)
        //    this.uniqueField = idCV.getStringValue(cmso);
        
        ArrayList options = null; // Will hold an element's options
        
        // Create a list of form input types. We'll need this to know which 
        // type of data we can expect for each form element.
        List inputTypes = new ArrayList(6); 
        /*
        inputTypes.add("Text");
        inputTypes.add("Text-area");
        inputTypes.add("Drop-down");
        inputTypes.add("Checkbox");
        inputTypes.add("Radiobutton");
        inputTypes.add("Date-time");
        */
        inputTypes = Arrays.asList(Form.FORM_INPUT_TYPES);
        
        int i = 0;
        int j = 1;
        
        do {
            j = 1;
            i++;
            options = new ArrayList(); // Instanciate / reset the options list
            
            // Get the values
            inputTypeCV             = content.getValue("Input["+i+"]/Type["+j+"]", locale);
            inputNameCV             = content.getValue("Input["+i+"]/Name["+j+"]", locale);
            inputInfoCV             = content.getValue("Input["+i+"]/Information["+j+"]", locale);
            inputHelpTextCV         = content.getValue("Input["+i+"]/HelpText["+j+"]", locale);
            inputLabelCV            = content.getValue("Input["+i+"]/Label["+j+"]", locale);
            inputRequiredCV         = content.getValue("Input["+i+"]/Required["+j+"]", locale);
            inputUniqueContentCV    = content.getValue("Input["+i+"]/Unique["+j+"]", locale);
            inputConstraintCV       = content.getValue("Input["+i+"]/Constraint["+j+"]", locale);
            inputLengthCV           = content.getValue("Input["+i+"]/Length["+j+"]", locale);
            inputValueCV            = content.getValue("Input["+i+"]/Value["+j+"]", locale);
            inputOptionsCV          = content.getValue("Input["+i+"]/Options["+j+"]/Value[1]", locale);
            inputOptionsTextCV      = content.getValue("Input["+i+"]/Options["+j+"]/Text[1]", locale);
            inputOptionsSelectedCV  = content.getValue("Input["+i+"]/Options["+j+"]/Selected[1]", locale);

            // If options exist, get them
            if (inputOptionsCV != null) { // != null -> options present
                Option opt = null;
                // Get option details
                while (inputOptionsCV != null) {
                    inputOptionsCV = content.getValue("Input["+i+"]/Options["+j+"]/Value[1]", locale);
                    inputOptionsTextCV = content.getValue("Input["+i+"]/Options["+j+"]/Text[1]", locale);
                    inputOptionsSelectedCV = content.getValue("Input["+i+"]/Options["+j+"]/Selected[1]", locale);
                    if (inputOptionsCV != null) {
                        opt = new Option(inputOptionsCV.getStringValue(cmso), 
                                         inputOptionsTextCV.getStringValue(cmso), 
                                         Boolean.parseBoolean(inputOptionsSelectedCV.getStringValue(cmso)));
                        options.add(opt);
                    }
                    j++;
                }
            }

            // If the type cannot be defined, break out
            if (inputTypeCV == null) {
                break;
            }

            // Get the type of the form element
            int inputType = inputTypes.indexOf(inputTypeCV.getStringValue(cmso));

            // Don't want to add options to form elements that should not have options, so set options to NULL
            if (inputType == I_FormInputElement.TEXT || 
                    inputType == I_FormInputElement.TEXTAREA || 
                    inputType == I_FormInputElement.DATETIME) {
                options = null;
            }

            // Add the element to the form
            I_FormInputElement addedElement = form.addElement(inputType, 
                                                    inputInfoCV != null ? inputInfoCV.getStringValue(cmso) : null,
                                                    inputHelpTextCV != null ? inputHelpTextCV.getStringValue(cmso) : null,
                                                    inputNameCV.getStringValue(cmso), 
                                                    inputLabelCV.getStringValue(cmso), 
                                                    Boolean.parseBoolean(inputRequiredCV.getStringValue(cmso)), 
                                                    options);
            
            // Set the form element unique or not
            if (Boolean.parseBoolean(inputUniqueContentCV.getStringValue(cmso))) {
                form.validateUniqueField(addedElement.getName());
                addedElement.setUnique(Boolean.parseBoolean(inputUniqueContentCV.getStringValue(cmso)));
            }
            
            
            // Set constraint, but only for InputTypeText
            if (inputConstraintCV != null) {
                if (inputType == I_FormInputElement.TEXT) {
                    switch (inputType) {
                        case I_FormInputElement.TEXT:
                            boolean constraintAddedOk = ((InputTypeText)addedElement).setConstraint(inputConstraintCV.getStringValue(cmso));
                            if (!constraintAddedOk) {
                                throw new NullPointerException(Messages.get().container(Messages.ERR_SET_CONSTRAINT_1, addedElement.getName()).key());
                                //throw new NullPointerException("Could not add constraint to the field \"" + addedElement.getName() + "\".");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            
            // Add the "length" attribute, if the element is of type TEXT
            if (inputLengthCV != null) {
                if (addedElement.getType() == I_FormInputElement.TEXT) {
                    ((InputTypeText)addedElement).setLength(Integer.parseInt(inputLengthCV.getStringValue(cmso)));
                }
            }
        } while(true); // Will break out inside the do{} code
        
        // Validate the identifier (must be done here, after all form elements have been added)
        //this.validateUniqueField();
    }
    
    /**
     * Builds a form instance based on its definition file in the OpenCms VFS.
     * <p>
     * The {@link CmsObject} needed to access the OpenCms VFS, is initialized 
     * using the standard export user.
     * 
     * @param structureId The form definition file's structure ID.
     * @return The form instance, built from the form definition file identified by the given ID.
     * @throws org.opencms.main.CmsException If something goes wrong.
     */
    public Form buildFromXml(CmsUUID structureId) throws CmsException {
        CmsObject cmso = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        
        Form form = new Form(structureId.getStringValue());
        
        CmsResource formResource = cmso.readResource(structureId);
        CmsFile formFile = cmso.readFile(formResource);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmso, formFile);
        
        String loc = cmso.readPropertyObject(formResource, "locale", true).getValue();
        if (loc == null) {
            loc = DEFAULT_LOCALE_NAME; // Set to default (english) locale, if no locale is set
        }
        Locale locale   = new Locale(loc);
        
        // Containers for XML node content values:
        I_CmsXmlContentValue formTitleCV            = content.getValue("Title", locale);
        I_CmsXmlContentValue formInformationCV      = content.getValue("Information", locale);
        I_CmsXmlContentValue formNameCV             = content.getValue("FormName", locale);
        I_CmsXmlContentValue postSubmitScriptCV     = content.getValue("FormScript", locale);
        //I_CmsXmlContentValue idCV                   = content.getValue("Identifier", locale); // This will be a unique field in the table
        I_CmsXmlContentValue inputTypeCV            = null;
        I_CmsXmlContentValue inputInfoCV            = null;
        I_CmsXmlContentValue inputHelpTextCV        = null;
        I_CmsXmlContentValue inputNameCV            = null;
        I_CmsXmlContentValue inputLabelCV           = null;
        I_CmsXmlContentValue inputRequiredCV        = null;
        I_CmsXmlContentValue inputUniqueContentCV   = null;
        I_CmsXmlContentValue inputConstraintCV      = null;
        I_CmsXmlContentValue inputLengthCV          = null;
        I_CmsXmlContentValue inputValueCV           = null;
        I_CmsXmlContentValue inputOptionsCV         = null;
        I_CmsXmlContentValue inputOptionsTextCV     = null;
        I_CmsXmlContentValue inputOptionsSelectedCV = null;
        
        if (formNameCV != null) {
            form.setName(formNameCV.getStringValue(cmso));
        } 
        if (formTitleCV != null) {
            form.setTitle(formTitleCV.getStringValue(cmso));
        } 
        if (formInformationCV != null) {
            form.setInformation(formInformationCV.getStringValue(cmso));
        } 
        if (postSubmitScriptCV != null) {
            form.setPostSubmitScript(postSubmitScriptCV.getStringValue(cmso));
        } 
        //if (idCV != null)
        //    this.uniqueField = idCV.getStringValue(cmso);
        
        ArrayList options = null; // Will hold an element's options
        
        // Create a list of form input types. We'll need this to know which 
        // type of data we can expect for each form element.
        List inputTypes = new ArrayList(6);
        inputTypes = Arrays.asList(Form.FORM_INPUT_TYPES);
        
        int i = 0;
        int j = 1;
        
        do {
            j = 1;
            i++;
            options = new ArrayList(); // Instanciate / reset the options list
            
            // Get the values
            inputTypeCV             = content.getValue("Input["+i+"]/Type["+j+"]", locale);
            inputNameCV             = content.getValue("Input["+i+"]/Name["+j+"]", locale);
            inputInfoCV             = content.getValue("Input["+i+"]/Information["+j+"]", locale);
            inputHelpTextCV         = content.getValue("Input["+i+"]/HelpText["+j+"]", locale);
            inputLabelCV            = content.getValue("Input["+i+"]/Label["+j+"]", locale);
            inputRequiredCV         = content.getValue("Input["+i+"]/Required["+j+"]", locale);
            inputUniqueContentCV    = content.getValue("Input["+i+"]/Unique["+j+"]", locale);
            inputConstraintCV       = content.getValue("Input["+i+"]/Constraint["+j+"]", locale);
            inputLengthCV           = content.getValue("Input["+i+"]/Length["+j+"]", locale);
            inputValueCV            = content.getValue("Input["+i+"]/Value["+j+"]", locale);
            inputOptionsCV          = content.getValue("Input["+i+"]/Options["+j+"]/Value[1]", locale);
            inputOptionsTextCV      = content.getValue("Input["+i+"]/Options["+j+"]/Text[1]", locale);
            inputOptionsSelectedCV  = content.getValue("Input["+i+"]/Options["+j+"]/Selected[1]", locale);

            // If options exist, get them
            if (inputOptionsCV != null) { // != null -> options present
                Option opt = null;
                // Get option details
                while (inputOptionsCV != null) {
                    inputOptionsCV = content.getValue("Input["+i+"]/Options["+j+"]/Value[1]", locale);
                    inputOptionsTextCV = content.getValue("Input["+i+"]/Options["+j+"]/Text[1]", locale);
                    inputOptionsSelectedCV = content.getValue("Input["+i+"]/Options["+j+"]/Selected[1]", locale);
                    if (inputOptionsCV != null) {
                        opt = new Option(inputOptionsCV.getStringValue(cmso), 
                                         inputOptionsTextCV.getStringValue(cmso), 
                                         Boolean.parseBoolean(inputOptionsSelectedCV.getStringValue(cmso)));
                        options.add(opt);
                    }
                    j++;
                }
            }

            // If the type cannot be defined, break out
            if (inputTypeCV == null) {
                break;
            }

            // Get the type of the form element
            int inputType = inputTypes.indexOf(inputTypeCV.getStringValue(cmso));

            // Don't want to add options to form elements that should not have options, so set options to NULL
            if (inputType == I_FormInputElement.TEXT || 
                    inputType == I_FormInputElement.TEXTAREA || 
                    inputType == I_FormInputElement.DATETIME) {
                options = null;
            }

            // Add the element to the form
            I_FormInputElement addedElement = form.addElement(inputType, 
                                                    inputInfoCV != null ? inputInfoCV.getStringValue(cmso) : null,
                                                    inputHelpTextCV != null ? inputHelpTextCV.getStringValue(cmso) : null,
                                                    inputNameCV.getStringValue(cmso), 
                                                    inputLabelCV.getStringValue(cmso), 
                                                    Boolean.parseBoolean(inputRequiredCV.getStringValue(cmso)), 
                                                    options);
            
            // Set the form element unique or not
            if (Boolean.parseBoolean(inputUniqueContentCV.getStringValue(cmso))) {
                form.validateUniqueField(addedElement.getName());
                addedElement.setUnique(Boolean.parseBoolean(inputUniqueContentCV.getStringValue(cmso)));
            }
            
            
            // Set constraint, but only for InputTypeText
            if (inputConstraintCV != null) {
                if (inputType == I_FormInputElement.TEXT) {
                    switch (inputType) {
                        case I_FormInputElement.TEXT:
                            boolean constraintAddedOk = ((InputTypeText)addedElement).setConstraint(inputConstraintCV.getStringValue(cmso));
                            if (!constraintAddedOk) {
                                throw new NullPointerException(Messages.get().container(Messages.ERR_SET_CONSTRAINT_1, addedElement.getName()).key());
                                //throw new NullPointerException("Could not add constraint to the field \"" + addedElement.getName() + "\".");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            
            
            
            // Add the "length" attribute, if the element is of type TEXT
            if (inputLengthCV != null) {
                if (addedElement.getType() == I_FormInputElement.TEXT) {
                    ((InputTypeText)addedElement).setLength(Integer.parseInt(inputLengthCV.getStringValue(cmso)));
                }
            }
        } while(true); // Will break out inside the do{} code
        
        return form;
        // Validate the identifier (must be done here, after all form elements have been added)
        //this.validateUniqueField();
    }
}
