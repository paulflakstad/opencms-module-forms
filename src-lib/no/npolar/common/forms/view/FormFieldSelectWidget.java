package no.npolar.common.forms.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import no.npolar.common.forms.*;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsLoaderException;
//import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
//import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
//import org.opencms.widgets.CmsSelectWidget;
//import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
* Provides a widget to check the fields to show on the form report page, for use 
* on a widget dialog.<p>
*
* @author Paul-Inge Flakstad, Norwegian Polar Institute (based on Andreas 
*           Zahner's CmsReportCheckFieldWidget)
*/
public class FormFieldSelectWidget extends A_CmsWidget {

    /** Separator for fields used in XML content value. */
    public static final char SEPARATOR_FIELDS = '|';

    /**
    * Creates a new form report fields widget.
    */
    public FormFieldSelectWidget() {
        // empty constructor is required for class registration
        this("");
    }

    /**
    * Creates a new form report fields widget with the given configuration.
    *
    * @param configuration the configuration to use
    */
    public FormFieldSelectWidget(String configuration) {
        super(configuration);
    }

    /**
    * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
    */
    @Override
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        StringBuffer result = new StringBuffer(256);

        // cast param to I_CmsXmlContentValue
        I_CmsXmlContentValue contentValue = (I_CmsXmlContentValue)param;
        Locale locale = contentValue.getLocale();
        
        int RESOURCE_TYPE_ID_FORM = -1;
        try {
            RESOURCE_TYPE_ID_FORM = OpenCms.getResourceManager().getResourceType("np_formview").getTypeId();
        } catch (CmsLoaderException cle) {
            throw new IllegalArgumentException("Unable to resolve the ID for the 'np_formview' resource type.");
        }
        
        CmsResourceFilter formFilter = CmsResourceFilter.DEFAULT_FILES.addRequireType(RESOURCE_TYPE_ID_FORM);

        // on initial call, all fields should be checked
        boolean allChecked = CmsStringUtil.isEmptyOrWhitespaceOnly(param.getStringValue(cms));
        List<String> checkedFields = new ArrayList<String>();
        if (!allChecked) {
            checkedFields = CmsStringUtil.splitAsList(param.getStringValue(cms), SEPARATOR_FIELDS);
        }
        CmsXmlContent content = (CmsXmlContent)contentValue.getDocument();
        String uri = content.getStringValue(cms, "FormPath", locale);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(uri)) {
            // Form URI not set: show error message
            result.append("<td class=\"xmlTdError\">");
            result.append(widgetDialog.getMessages().key(Messages.ERR_REPORT_NO_FORM_URI_0));
            result.append("</td>");
        } 
        else {
            // URI is set, generate check boxes for input fields
            result.append("<td class=\"xmlTd\">");

            try {
                // get the web form file
                CmsFile file = cms.readFile(uri);
                content = CmsXmlContentFactory.unmarshal(cms, file);
                
                // We could use the Form class to get the input fields, but let's just do it the fastest way:
                //List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();

                List<I_CmsXmlContentValue> inputs = content.getValues("Input", locale);
                Iterator<I_CmsXmlContentValue> iInputs = inputs.iterator();

                while (iInputs.hasNext()) {
                    I_CmsXmlContentValue val = iInputs.next();
                    String name = content.getValue(val.getPath() + "/Name", locale).getStringValue(cms);
                    String label = content.getValue(val.getPath() + "/Label", locale).getStringValue(cms);
                    
                    result.append("<input type=\"checkbox\" name=\"");
                    result.append(param.getId());
                    result.append("\" value=\"").append(CmsEncoder.escapeXml(name)).append("\"");
                    if (allChecked || checkedFields.contains(name)) {
                        result.append(" checked=\"checked\"");
                    }
                    result.append("/>&nbsp;");
                    result.append(name);
                    if (!name.equals(label)) {
                        // show the label text behind the database label
                        result.append(" (").append(CmsEncoder.escapeXml(label)).append(")");
                    }
                    if (iInputs.hasNext()) {
                        result.append("<br/>\n");
                    }
                }
            } catch (Exception e) {
                // error reading form
                result.append(widgetDialog.getMessages().key(Messages.ERR_REPORT_NO_FORM_URI_0));
            }

            result.append("</td>");
        }

        return result.toString();
    }

    /**
    * @see org.opencms.widgets.I_CmsWidget#newInstance()
    */
    @Override
    public I_CmsWidget newInstance() {

        return new FormFieldSelectWidget(getConfiguration());
    }

    /**
    * @see org.opencms.widgets.I_CmsWidget#setEditorValue(org.opencms.file.CmsObject, java.util.Map, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
    */
    @Override
    public void setEditorValue(
            CmsObject cms,
            Map formParameters,
            I_CmsWidgetDialog widgetDialog,
            I_CmsWidgetParameter param) {

        String[] values = (String[])formParameters.get(param.getId());
        if ((values != null) && (values.length > 0)) {
            StringBuffer newValue = new StringBuffer(values.length * 8);
            // loop the found values
            for (int i = 0; i < values.length; i++) {
                newValue.append(values[i]);
                if (i < values.length - 1) {
                    newValue.append(SEPARATOR_FIELDS);
                }
            }
            // set the value
            param.setStringValue(cms, newValue.toString());
        } else {
            // set empty String as value
            param.setStringValue(cms, "");
        }
    }

}