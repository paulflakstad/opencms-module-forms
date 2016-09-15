package no.npolar.common.forms.view;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.npolar.common.forms.A_InputTypePreDefined;
import no.npolar.common.forms.Form;
import no.npolar.common.forms.FormSqlManager;
import no.npolar.common.forms.FormUtil;
import no.npolar.common.forms.I_FormInputElement;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Macro resolver for this forms library.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FormMacroResolver {
    
    /**
     * Creates a new, blank FormaMacroResolver instance.
     */
    public FormMacroResolver() {} // Default constructor
    
    /**
    * Resolves macros in a given text, based on a given result set.
    * 
    * @param text  The text to resolve macros for.
    * @param rs  The result set that contains the values to use when replacing the macros with actual values.
    * @param f
    * @param cmso
    * @return String 
    * @throws SQLException
    * @throws InstantiationException
    * @throws CmsException
    */
    public static String resolveMacros(String text, ResultSet rs, Form f, CmsObject cmso) throws SQLException, InstantiationException, CmsException {
        Iterator<String> itr = FormUtil.getMacros(text).iterator();
        while (itr.hasNext()) {
            String macro = itr.next();
            //out.println("<!-- Found macro: '" + macro + "'");
            String macroReplacement = null;

            if (macro != null) {
                if (macro.equals("entrydata")) { // ==> All data registered through the form (but not ID and time-last-modified)
                    Form form = new Form(f.getResourceUri(), cmso);
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

            if (macroReplacement == null) {
                throw new NullPointerException("The macro value for '" + macro + "' was NULL.");
            }
        }
        return text;
    }
}
