<%-- 
    Document   : form.jsp
        Generates a HTML form by reading a user-created structured content file. 
        Handles form validation, preview and submission.
    Created on : 13 Jan 2009
    Author     : Paul-Inge Flakstad / Norwegian Polar Institute
--%><%@page import="no.npolar.common.forms.view.FormMacroResolver"
%><%@page import="no.npolar.util.*"
%><%@page import="no.npolar.common.forms.*"
%><%@page import="java.util.*"
%><%@page import="java.sql.*"
%><%@page import="java.io.IOException"
%><%@page import="java.lang.StackTraceElement"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="org.apache.commons.lang.StringEscapeUtils"
%><%@page import="org.opencms.main.*"
%><%@page import="org.opencms.mail.CmsSimpleMail"
%><%@page import="org.opencms.security.CmsRoleManager"
%><%@page import="org.opencms.security.CmsRole"
%><%@page import="org.opencms.jsp.*"
%><%@page import="org.opencms.file.*"
%><%@page import="org.opencms.file.types.*"
%><%@page import="org.opencms.xml.content.*"
%><%@page import="org.opencms.xml.types.*"
%><%@page import="org.jsoup.Jsoup"
%><%@page import="org.jsoup.safety.Whitelist" session="true" pageEncoding="UTF-8"
%><%!

public void printForm(JspWriter w, Form f, String msg, boolean requestFileIsForm) throws IOException {
    w.println("<h" + (requestFileIsForm ? "1" : "2") + ">" + f.getTitle() + "</h" + (requestFileIsForm ? "1" : "2") + ">");
    w.println("<div class=\"form_text form_text--before-form\">" + f.getInformation() + "</div>");
    if (msg != null) {
        w.print("<div class=\"formerror box message\">" + msg + "</div>");
    }
    w.print(f.getHtml(true));
}

%><%
CmsAgent cms                = new CmsAgent(pageContext, request, response);
CmsObject cmso              = cms.getCmsObject();
String resourceUri          = cms.getRequestContext().getUri();
Locale locale               = cms.getRequestContext().getLocale();
String loc                  = locale.toString();
HttpSession sess            = request.getSession(true);
boolean requestFileIsForm   = cmso.readResource(resourceUri).getTypeId() == OpenCms.getResourceManager().getResourceType("np_form").getTypeId();
CmsRoleManager roleManager  = OpenCms.getRoleManager();
final boolean SYS_ADM       = roleManager.hasRole(cms.getCmsObject(), CmsRole.VFS_MANAGER); //loggedInUser.hasFormDataAccess();
final boolean DEBUG         = false;    // Set to true to print out debugging data
final boolean NOT_SUBMITTED = request.getParameter("submit") == null 
                                && request.getParameter("confirm_submit") == null 
                                && request.getParameter("regret_submit") == null;
final boolean SUBMITTED     = request.getParameter("submit") != null;
final boolean APPROVED      = request.getParameter("confirm_submit") != null;
final boolean NOT_APPROVED  = request.getParameter("regret_submit") != null;
final String VIEWER_SCRIPT  = "/system/modules/no.npolar.common.forms/elements/form-data-view-table.jsp";


//
// Get the form object, either as a new instance, or fetched from session.
//
Form form = null;
String formUri = null;
// IMPORTANT:
// Check if the "resourceUri" (the file to apply this template on) is set as a parameter.
// If so, use this value as the file URI.
// (When the form is included as part of another "parent" web page, the include() call
// from that parent web page should set the parameter "resourceUri" pointing to the form 
// resource. This is because the parent web page has to include this JPS template, it 
// cannot include the form resource - an XML content file - directly.)
String paramUri = request.getParameter("resourceUri");
if (paramUri != null)
    resourceUri = cmso.existsResource(paramUri) ? paramUri : resourceUri;

if (NOT_SUBMITTED) {
    formUri = resourceUri; // Set the form URI
    form = new Form(formUri, cmso); // Create a new form instance
} else if (SUBMITTED || APPROVED || NOT_APPROVED) {
    form = (Form)sess.getAttribute("form"); // Get the form object from session
    // No Form found in the session. A form object in the session is a prerequisite, so the script cannot continue.
    if (form == null) {
        throw new ServletException(cms.label("ERR_MISSING_SESSION_FORM_0"));
    }
}

// Case: The form has only English content, and has a sibling in the "no" locale. 
// It is this sibling we're currently viewing, and it's showing us content from 
// the English language node. So, the RC's locale is "no", but the form's locale 
// is "en" (note the difference). We'll need to modify the RQ's locale before reading 
// the messages (cms.label(...)), so we get the correctly localized versions.
Locale requestLocale = locale; // Keep the original locale (we'll need it to do a reset soon...)
try {
    cms.getRequestContext().setLocale(form.getLocale()); // Modify the RC's locale if neccessary
} catch (Exception e) {
    // Whoops..!
}

//
// Localized text
//
final String MISSING_REQUIRED   = cms.label("MSG_FORM_REJECTED_0"); // "Form not accepted, check error messages in the form"
final String VIEW_DATA          = cms.label("MSG_FORM_VIEW_SUBMITTED_DATA_0"); // "View submitted data"
final String VIEW_DATA_INFO     = cms.label("MSG_FORM_USER_LOGGED_IN_0"); // "You are logged in"
// Default button labels
final String BUTTON_CONTINUE    = " " + cms.label("MSG_FORM_CONTINUE_0") + " "; // "Continue to preview"
final String BUTTON_BACK        = " " + cms.label("MSG_FORM_BACK_0") + " "; // "Back to form"
final String BUTTON_CONFIRM     = " " + cms.label("MSG_FORM_CONFIRM_0") + " "; // "OK"

// Default "dialog" texts
final String ACCEPTED           = cms.label("MSG_FORM_ACCEPTED_0"); // "Submission accepted"
final String REVIEW_HEADING     = cms.label("MSG_FORM_REVIEW_FORM_SUBMISSION_0"); // "Please review your details"
final String CONFIRM_INFO       = cms.label("MSG_FORM_REVIEW_FORM_SUBMISSION_NAVIGATION_0"); // 

try {
    cms.getRequestContext().setLocale(requestLocale); // Reset the request context's locale
} catch (Exception e) {
    // Whoops..!
}

if (cms.template("main")) {
    // If the requesting file is a form, include the html code to create a complete page
    if (requestFileIsForm) {
        cms.includeTemplateTop();
    }
    
    

    
    
    
    //
    // Handle form generation / submission
    //
    

    //
    // Form not submitted: create and present the form
    // 
    if (NOT_SUBMITTED) {
        if (!form.isExpired()) {
            //form.setAction(cms.link(cms.getRequestContext().getUri()));
            form.setAction(cms.link(formUri));
            // Add the form to the session
            sess.setAttribute("form", form);
            // Print the view link if the user has privileges to view form data
            if (SYS_ADM) {
                String viewLink = cms.link(VIEWER_SCRIPT + "?table=" + form.getTableName());
                out.println("<div class=\"box message\">");
                out.print(VIEW_DATA_INFO + ": <a href=\"" + viewLink + "\" target=\"_blank\">" + VIEW_DATA.toLowerCase() + "</a>.");
                out.println("</div>");
            }
            // Print the form
            printForm(out, form, null, requestFileIsForm);
        } else { // Expired, print the expired text
            out.println("<h1>" + form.getTitle() + "</h1>");
            out.println(form.getExpiredText());
        }
    }










    //
    // Form submitted: check validity, show preview where the user can confirm / regret submission OR show the form again if not valid
    //
    else if (SUBMITTED) {

        I_FormInputElement element  = null;
        String parameterName        = null;
        String[] parameterValues    = null;
        List options                = null;
        HashMap submission          = new HashMap(); //HashMap<String, String[]> = HashMap<elementName, submittedValues>

        int i                       = 0;

        Enumeration parameterNames = request.getParameterNames(); // Get all the parameter names
        
        if (DEBUG) {
            out.print("<table border=\"1\">");
            out.print("<tr>"
                    + "<th>Parameter name</th>"
                    + "<th>TypeID</th>"
                    + "<th>Required</th>"
                    + "<th>Label</th>"
                    + "<th>Name</th>"
                    + "<th>Submitted value(s)</th>"
                    + "</tr>");
        }
        
        // Loop over all parameters
        while (parameterNames.hasMoreElements()) {
            parameterName   = (String)parameterNames.nextElement(); // Get the name of the parameter
            element         = form.getElement(parameterName); // Get the form element that corresponds to the parameter
            parameterValues = request.getParameterValues(parameterName); // Get the submitted value(s) for this parameter
            
            if (element != null) {
                if (element.getType() == I_FormInputElement.TEXT || element.getType() == I_FormInputElement.TEXTAREA) {
                    parameterValues[0] = StringEscapeUtils.unescapeHtml(Jsoup.clean(parameterValues[0], Whitelist.basic()));
                }
            
                submission.put(parameterName, parameterValues); // Put the parameter name and the parameter value(s) in the submission map
            }
            
            if (DEBUG) {
                out.println("<tr><td>");
                out.println(parameterName);
                out.println("</td><td>");
                out.println(element == null ? "No value" : Integer.toString(element.getType()));
                out.println("</td><td>");
                out.println(element == null ? "No value" : element.isRequired() ? "Yes" : "No");
                out.println("</td><td>");
                out.println(element == null ? "No value" : element.getLabel());
                out.println("</td><td>");
                out.println(element == null ? "No value" : element.getName());
                out.println("</td><td><ul>");
                for (i = 0; i < parameterValues.length; i++)
                    out.println("<li>" + parameterValues[i] + "</li>");
                out.println("</ul></td></tr>");
            }
        }

        if (DEBUG) {
            out.print("</table>");
            out.print("<br /><h2>" + submission.size() + " parameters for submission.</h2>");
        }

        //
        // Submit the form data. 
        // NOTE: This operation will not perform any DB transactions, only the Form object is updated.
        //
        form.submit(submission); 
        if (DEBUG) {
            out.print("<h4>Submitted to Form \"" + form.getTitle() + "\":</h4>" +
                        "<ul>");
            Set keys = submission.keySet();
            Iterator kItr = keys.iterator();
            String key = null;
            while (kItr.hasNext()) {
                key = (String)kItr.next();
                out.println("<li>" + key + "<ul>");
                String[] submittedValues = (String[])submission.get(key);
                for (i = 0; i < submittedValues.length; i++) {
                    out.print("<li>" + submittedValues[i] + "</li>");
                }
                out.print("</ul></li>");
            }
            out.print("</ul>");
        }

        if (DEBUG) out.print("<h2>Valid submit: " + (form.hasValidSubmission() ? "yes" : "no") + "</h2>");

        // One or more required form fields were missing data - show the form again
        if (!form.hasValidSubmission()) {
            printForm(out, form, MISSING_REQUIRED, requestFileIsForm);
        }
        // All required form fields were submitted
        else {
            out.println("<h" + (requestFileIsForm ? "1" : "2") + ">" + form.getTitle() + "</h" + (requestFileIsForm ? "1" : "2") + ">");
            if (form.isEditEnabled()) {
                out.println("<h3>" + "About to save - check that everything is OK." + "</h3>");
            } 
            else {
                if (form.getConfirmText() == null || form.getConfirmText().isEmpty())
                    out.println("<h3>" + REVIEW_HEADING + "</h3><p><em>" + CONFIRM_INFO + "</em><p>"); // Default text
                else {
                    out.println("<div class=\"form_text form_text--before-form\">");
                    out.println(form.getConfirmText());
                    out.println("</div>");
                }
            }
            out.println(form.getPreview());
            
            String backLabel = form.isEditEnabled() ? BUTTON_BACK : form.getButtonTextBack();
            String confLabel = form.isEditEnabled() ? BUTTON_CONFIRM : form.getButtonTextConfirm();
            
            out.println("<div class=\"form\">");
            out.println("<form method=\"post\" action=\"" + cms.link(cms.getRequestContext().getUri()) + "\">");
            out.println("<div class=\"submit\">");
            
            out.println("<button"
                        + " class=\"button button--approve confirm-button\""
                        + " type=\"submit\""
                        + " name=\"confirm_submit\""
                        + " value=\"" + confLabel + "\""
                    + ">" + confLabel + "</button>");
            out.println("<button"
                        + " class=\"button button--reject back-button\""
                        + " type=\"submit\""
                        + " name=\"regret_submit\""
                        + " value=\"" + backLabel + "\""
                    + ">" + backLabel + "</button>");
            //out.println("<input class=\"confirm-button\" type=\"submit\" name=\"confirm_submit\" value=\"" + confLabel + "\" />");
            //out.println("<input class=\"back-button\" type=\"submit\" name=\"regret_submit\" value=\"" + backLabel + "\" />");
            
            out.println("</div>");
            out.println("</form>");
            out.println("</div>");
        }
    }




    //
    // Form submission confirmed: the user approved the submitted details
    //
    else if (APPROVED) {

        // Check the validity (should now be valid)
        if (form.hasValidSubmission()) {
            out.println("<h" + (requestFileIsForm ? "1" : "2") + ">" 
                    + form.getTitle() 
                    + "</h" + (requestFileIsForm ? "1" : "2") + ">");
            //
            // Insert form details into the database
            //
            cmso.getRequestContext().setCurrentProject(cmso.readProject("Offline"));
            
            int lastInsertedId = -1;
            
            // Create the table for the form (if it doesn't exist)
            try {
                form.update(FormSqlManager.SQL_CREATE_TABLE_STATEMENT);
            } catch (SQLException sqle1) {
                OpenCms.getLog(form).error("Could not create new table for form \"" + form.getName() + "\".", sqle1);
                //throw new JspException("An error occured while trying to create the new table for the form: " + sqle1.getMessage());
                out.println("<aside class=\"msg msg--error\">"
                                + "<h3 class=\"msg__heading\">Error</h3>"
                                + "<p>A critical system error occured.</p>"
                                + "<p>Please try again. We apologize for the inconvenience."
                        + "</aside>");
                if (requestFileIsForm) {
                    cms.includeTemplateBottom();
                }
                return;
            } catch (InstantiationException ie) {
                OpenCms.getLog(form).error("Failed to create new no.npolar.common.forms.SQLAgent instance. Please check the configuration file.");
                //throw new JspException("The SQLAgent object could not be initialized. Please check the configuration file.");
                out.println("<aside class=\"msg msg--error\">"
                                + "<h3 class=\"msg__heading\">Error</h3>"
                                + "<p>A critical system error occured.</p>"
                                + "<p>Please try again. We apologize for the inconvenience."
                        + "</aside>");
                if (requestFileIsForm) {
                    cms.includeTemplateBottom();
                }
                return;
            }
            
            // Insert the form data into the table
            try {
                if (form.isEditEnabled()) {
                    //out.println("<!-- Updating entry ... -->");
                    form.update(FormSqlManager.SQL_UPDATE_STATEMENT);
                } else {
                    //out.println("<!-- Inserting entry ... -->");
                    lastInsertedId = form.update(FormSqlManager.SQL_INSERT_STATEMENT);
                    //out.println("<!-- OK, last inserted ID: " + lastInsertedId + " -->");
                }
                
                //out.println("<h2>Last SQL query:</h2>");
                //out.println("<p>(form.isEditEnabled()=" + String.valueOf(form.isEditEnabled()) + ")</p>");
                //out.println(form.getSqlManager().getLastExecutedStatement().toString());
            } 
            catch (SQLException sqle2) {
                /* // There is no Form#getUniqueElement method since the update where every element is possibly unique
                if (sqle2.getMessage().indexOf("Duplicate entry") != -1)
                    throw new JspException("ERROR: A duplicate entry for the unique field '" + form.getUniqueElement().getLabel() + 
                            "' was found in the database.");
                else
                */ 
                    //throw new JspException("An error occured while trying to insert values into the form table: " + sqle2.getMessage());
                    OpenCms.getLog(form).error("Error inserting values into table for form \"" + form.getName() + "\": ", sqle2);
                    out.println("<aside class=\"msg msg--error\">"
                                + "<h3 class=\"msg__heading\">Error</h3>"
                                + "<p>A critical error occured while attempting to update our database records.</p>"
                                + "<p>Please try again. We apologize for the inconvenience."
                            + "</aside>");
                    if (requestFileIsForm) {
                        cms.includeTemplateBottom();
                    }
                    return;
            }
            
            if (form.isEditEnabled()) {
                out.println("<h3>" + "Changes saved." + "</h3>");
                out.println("<h4>You should now "
                            + "<a href=\"#\" onclick=\"window.close()\">close this window</a>"
                        + "</h4>");
            } 
            else {
                if (form.getSuccessText() == null || form.getSuccessText().isEmpty()) {
                    out.print("<h3>" + ACCEPTED + "</h3>"); // Default text
                } else {
                    // ToDo: Allow more macros...
                    out.println("<div class=\"form_text form_text--before-form\">");
                    out.println(form.getSuccessText().replaceAll("%\\(id\\)", String.valueOf(lastInsertedId)));
                    out.println("</div>");
                }
                
                
                // Send e-mails (if required)
                boolean notificationEmailOK = true;
                boolean confirmationEmailOK = true;
                
                // Resolve macros in the notification e-mail, and send it
                AutoEmail autoMail = form.getNotificationEmail();
                if (autoMail != null) {
                    try {
                        autoMail.resolveMacros(form.getResourceUri(), lastInsertedId, cmso);
                        autoMail.send();
                    } catch (Exception me) {
                        OpenCms.getLog(form).error("Sending notification e-mail for form \"" + form.getName() + "\" failed, trying fallback.");
                        try {
                            autoMail.getSimpleEmail().send();
                        } catch (Exception mee) {
                            notificationEmailOK = false;
                        }                         
                        //throw new JspException("Error sending notification e-mail: " + me.getMessage());
                    } finally {
                        if (!notificationEmailOK) {
                            OpenCms.getLog(form).error("Sending notification e-mail for form \"" + form.getName() + "\" failed.");
                        }
                    }
                }
                
                // Resolve macros in the confirmation e-mail, and send it
                autoMail = form.getConfirmationEmail();
                if (autoMail != null) {
                    try {
                        autoMail.resolveMacros(form.getResourceUri(), lastInsertedId, cmso);
                        autoMail.send();
                    } catch (Exception me) {
                        OpenCms.getLog(form).error("Sending confirmation e-mail for form \"" + form.getName() + "\" failed, trying fallback.");
                        try {
                            autoMail.getSimpleEmail().send();
                        } catch (Exception mee) {
                            confirmationEmailOK = false;
                        }                         
                        //throw new JspException("Error sending confirmation e-mail: " + me.getMessage());
                    } finally {
                        if (!confirmationEmailOK) {
                            OpenCms.getLog(form).error("Sending confirmation e-mail for form \"" + form.getName() + "\" failed.");
                        }
                    }
                }
                
                if (!confirmationEmailOK) {
                    out.println("<aside class=\"msg msg--alert\">"
                                    + "<h3 class=\"msg__heading\">Note: No confirmation email was sent</h3>"
                                    + "<p>For your convenience and reference, we would normally send you a confirmation email right now"
                                        + ", but our robots decided not to cooperate this time."
                                        + " We apologize for this inconvenience."
                                    + "</p>"
                                    + "<p>Everything else went well:"
                                        + " <strong>We have received your form, and registered all the details.</strong>"
                                    + "</p>"
                                    + "<p><strong>No further action is required</strong>."
                                        + " We are just letting you know about the unsent email."
                                    + "</p>"
                                    + "<p>PS: This is what we tried to send you:</p>");
                    out.println(autoMail.getMessage().replaceAll("(\\r\\n|\\n)", "<br>"));
                    out.println("</aside>");
                }
            }
            
            
            
            
            
            
            
            if (form.hasPostSubmitScript()) {
                // The form has a post-submit script: include it now
                cms.include( cmso.getSitePath(cmso.readResource(form.getPostSubmitScript())) );
            } else {
                // No post-submit script exists: delete the form from the session 
                // to prevent trouble caused by "back" navigation in the browser.
                sess.setAttribute("form", null);
            }
        }
        
        
        else {
            // Not valid submit, required form data was missing - show the form again
            printForm(out, form, MISSING_REQUIRED, requestFileIsForm);
        }
    }





    //
    // User did not approve: show last submitted form
    //
    else if (NOT_APPROVED) {
        printForm(out, form, null, requestFileIsForm);
    }



    // Include the neccessary HTML if needed
    if (requestFileIsForm) {
        cms.includeTemplateBottom();
    }
} // if (cms.template())
%>