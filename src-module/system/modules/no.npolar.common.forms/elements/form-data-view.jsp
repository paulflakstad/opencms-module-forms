<%-- 
    Document   : form-data-view 
    Created on : May 22, 2012, 9:20:38 PM (loosely based on CliC's "/specialist-directory/specialist-directory.jsp")
    Author     : flakstad
--%><%@page import="no.npolar.util.*"
%><%@page import="no.npolar.common.menu.*"
%><%@page import="no.npolar.common.forms.*"
%><%@page import="no.npolar.common.forms.view.*"
%><%@page import="java.util.*"
%><%@page import="java.util.regex.*"
%><%@page import="java.sql.*"
%><%@page import="java.io.IOException"
%><%@page import="org.opencms.main.*"
%><%@page import="org.opencms.mail.*"
%><%@page import="org.opencms.security.CmsRoleManager"
%><%@page import="org.opencms.security.CmsRole"
%><%@page import="org.opencms.util.CmsUUID"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="org.opencms.jsp.*"
%><%@page import="org.opencms.file.*"
%><%@page import="org.opencms.file.types.*"
%><%@page import="org.opencms.util.CmsRequestUtil"
%><%@page import="org.opencms.util.CmsStringUtil"
%><%@page import="org.opencms.xml.content.*"
%><%@page import="org.opencms.xml.types.*" session="true" pageEncoding="UTF-8"
%><% 
CmsAgent cms = new CmsAgent(pageContext, request, response);
CmsObject cmso = cms.getCmsObject();

String requestFileUri = cms.getRequestContext().getUri();
String resourceUri = request.getParameter("resourceUri") == null ? requestFileUri : request.getParameter("resourceUri");

boolean included = cmso.readResource(cms.getRequestContext().getUri()).getTypeId() != OpenCms.getResourceManager().getResourceType("np_formview").getTypeId();

//String table = request.getParameter("_table")== null ? "" : request.getParameter("_table");
String currentSort = request.getParameter("_sort") == null ? "" : request.getParameter("_sort");
String action = request.getParameter("_action") == null ? "" : request.getParameter("_action");
boolean descending = request.getParameter("_order") == null ? false : (request.getParameter("_order").equals("desc") ? true : false);
boolean displayColumnNames = false; // Whether or not to display the column name under the column label in the table header

String singleId = request.getParameter("id"); // Used to determine if a single ID was given (if so, a page title must be constructed according to the definition in the config file)
String entryTitle = null;

CmsRoleManager roleManager  = OpenCms.getRoleManager();
boolean userIsVfsManager    = roleManager.hasRole(cms.getCmsObject(), CmsRole.VFS_MANAGER);
boolean userIsLoggedIn      = roleManager.hasRole(cms.getCmsObject(), CmsRole.WORKPLACE_USER);

final boolean USE_TABLE     = request.getParameter("vm") == null ? true : request.getParameter("vm").equalsIgnoreCase("table");

final String DELETED_FORM = "DELETED FORM";
final String LABEL_FORM_DATA_FOR = "Form data for";
final String LABEL_UNABLE_TO_CREATE_FORM = "Unable to create Form instance";
final String LABEL_TABLE = "Table";
final String LABEL_ROWS = "Rows total";
final String LABEL_NO_DATA = "No data has been submitted through this form";
final String LABEL_RESTRICTED = "Access restricted";
final String LABEL_REQUIRE_VFS_MANAGER = "The page or operation you requested is not publicly accessible.";
final String LABEL_SUGGEST_LOGIN = "If you have an account, please log in and try again.";
final String MSG_PARAMETER_ERROR = "<h2 class=\"error\">Missing or malformed information</h2>"
                                            + "<p>Sorry, the request for this page seems to be missing some vital information. Please try again.</p>";
final String LABEL_SHOW_FILTERS = cms.labelUnicode("MSG_SHOW_FILTERS_0");
final String LABEL_HIDE_FILTERS = cms.labelUnicode("MSG_HIDE_FILTERS_0");
final String LABEL_APPLY_FILTERS = cms.labelUnicode("MSG_APPLY_FILTERS_0");

final String LABEL_DELETING_ENTRY = cms.labelUnicode("MSG_DELETING_ENTRY_0");

final boolean DEBUG = false;







/////////////////////////////////////////////////////////
// Read config file
//
String viewTitle                = null; // The title for this view
String viewText                 = null; // The text for this view
String listText                 = null; // The text displayed immediately before the result list itself
String listEmptyText            = null; // The text displayed when the result list is empty
String formPath                 = null; // The path to the form VFS resource
String tableClass               = null; // Optional additional CSS class to apply on the result list table
String defaultOrderField        = null; // The field to order by (initially, when the page is viewed with default ordering)
String fieldNamesView           = null; // Fields to display (possibly multiple values, separated by pipe)
String fieldNamesFilter         = null; // Fields to use as (public) filters (possibly multiple values, separated by pipe)
String fieldNamesAdminFilter    = null; // Fields to use as filters available only to admins (possibly multiple values, separated by pipe)
String entryTitleModel          = null; // The model to use when constructing the title for an individual entry, defined using regular text and macros, e.g. '%(title) (%(year))' could produce 'My title (2012)'
String emailFromAddr            = null; // The mass e-mail sender e-mail address, e.g. 'no-reply@mysite.com'
String emailFromName            = null; // The mass e-mail sender name, e.g. 'My Company Name'
String emailField               = null; // The name of the table column that contains the e-mail addresses, e.g. 'email'
String emailAltField            = null; // The name of the table column that contains the alternative e-mail addresses, e.g. 'email_alt'
String emailToNameField         = null; // The model to use when constructing e-mail recipient names, defined using regular text and macros, e.g. '%(fname) %(lastname)' could produce 'James Bond'
String detailTemplate           = null; // Optional. Path to a JSP that will output individual entries

I_CmsXmlContentContainer configuration = cms.contentload("singleFile", resourceUri, true);
while (configuration.hasMoreContent()) {
    viewTitle = cms.contentshow(configuration, "Title");
    viewText = cms.contentshow(configuration, "Text");
    listText = cms.contentshow(configuration, "ListText");
    listEmptyText = cms.contentshow(configuration, "ListEmptyText");
    formPath = cms.contentshow(configuration, "FormPath");
    tableClass = cms.contentshow(configuration, "TableClass");
    defaultOrderField = cms.contentshow(configuration, "OrderBy");
    fieldNamesView = cms.contentshow(configuration, "FieldsView");
    fieldNamesFilter = cms.contentshow(configuration, "FieldsFilter");
    fieldNamesAdminFilter = cms.contentshow(configuration, "FieldsAdminFilter");
    entryTitleModel = cms.contentshow(configuration, "FieldsEntryTitle");
    detailTemplate = cms.contentshow(configuration, "EntryDetailTemplate");
    if (CmsAgent.elementExists(detailTemplate) && !cmso.existsResource(detailTemplate))
        throw new NullPointerException("The detail template '" + detailTemplate + "' does not exist.");
    
    I_CmsXmlContentContainer mailConfig = cms.contentloop(configuration, "MassEmail");
    while (mailConfig.hasMoreContent()) {
        emailFromAddr = cms.contentshow(mailConfig, "FromEmail");
        emailFromName = cms.contentshow(mailConfig, "FromName");
        emailField = cms.contentshow(mailConfig, "EmailField");
        emailAltField = cms.contentshow(mailConfig, "EmailAltField");
        emailToNameField = cms.contentshow(mailConfig, "ToNameField");
    }
}

//out.println("<!-- formPath = '" + formPath + "' -->");
//out.println("<!-- emailFromAddr = '" + emailFromAddr + "' -->");
//out.println("<!-- emailFromName = '" + emailFromName + "' -->");
//out.println("<!-- emailField = '" + emailField + "' -->");

String pageTitle = viewTitle; // Default page title

if (!cmso.existsResource(formPath))
    throw new IllegalArgumentException("The form '" + formPath + "' does not exist.");


// Find the fields (column names) for the entry titles
if (singleId != null) { // if (a single entry ID was given) --> Then we need to construct a page title according to the definitions in the config file
    if (!FormUtil.isValidFormatForId(singleId)) {
        cms.includeTemplateTop();
        out.println("<h1>Error</h1>");
        out.println(MSG_PARAMETER_ERROR);
        cms.includeTemplateBottom();
        return;
    }
        
    Form singleEntry = new Form(formPath, cmso); // Create the form ...
    try {
        singleEntry.recreateFromId(singleId, cmso); // ... and reconstruct the entry
    } catch (Exception e) {
        cms.includeTemplateTop();
        out.println("<h1>Error</h1>");
        out.println(MSG_PARAMETER_ERROR);
        cms.includeTemplateBottom();
        return;
    }
    
    entryTitle = entryTitleModel; // Will eventually hold the entry title
    List macrosPresent = FormUtil.getMacros(entryTitleModel);
    Iterator<String> itr = macrosPresent.iterator();
    while (itr.hasNext()) {
        String macro = itr.next();
        String macroReplacement = singleEntry.getElement(macro).getValue();
        Matcher m = Pattern.compile("%\\(" + macro + "\\)").matcher(entryTitle);
        entryTitle = m.replaceAll(macroReplacement);
    }
    
    pageTitle = entryTitle;
    request.setAttribute("Title", pageTitle);
}










if (action.isEmpty()) {

//if (userIsVfsManager) {

    // Read the form resource
    CmsResource formResource = cmso.readResource(formPath);
    // Get the form title and structure ID, and the name of the associated table
    String formTitle = cmso.readPropertyObject(formResource, "Title", false).getValue("[no name]");
    CmsUUID sid = formResource.getStructureId();
    String table = sid.getStringValue().replaceAll("-", "_");
    
    List listFields = Arrays.asList(fieldNamesView.split("\\|")); // List to hold the names of the form fields (= table columns) to display in this view
    List filterFields = new ArrayList(Arrays.asList( fieldNamesFilter.split("\\|"))); // The (multiple choice) field(s) to create public filters for
    List adminFilterFields = new ArrayList(Arrays.asList( fieldNamesAdminFilter.split("\\|") )); // The (multiple choice) field(s) to create admin filters for
    //List listFields = Arrays.asList(new String[] {"lastname", "firstname", "speciality", "theme"}); // List to hold the names of the fields to list
    //List filterFields = new ArrayList(Arrays.asList( new String[] {"speciality", "theme", "region", "gender", "country"} )); // The (multiple choice) field(s) to create filters for (for everyone)
    // List adminFilterFields = new ArrayList(Arrays.asList( new String[] {"panel"} )); // The (multiple choice) field(s) to create filters for (for "admins")
    
    // For "admins", add the admin filter fields (if any) to the list of filters
    if (userIsLoggedIn) {
        filterFields.addAll(adminFilterFields);
    }
    
    List<String> rows = new ArrayList<String>(); // List to hold the dataset entries (= table rows) to display
    List<String> ids = new ArrayList<String>(); // List to hold the IDs of the entries to display
    String[] tableRow = new String[listFields.size()]; // Helper array, used during HTML output to achieve correct order
    
    Map<String, List> filterLinks = new HashMap<String, List>(); // Map to hold filter links (the user's "triggers") for multiple choice elements
    List<String> activeFilters = new ArrayList<String>(); // List of currently active filters
    List<I_FormInputElement> filterElements = new ArrayList<I_FormInputElement>(); // List of form elements that are also filters
    
    // Get a map containing ONLY the filter parameters, no other parameters
    Map filterMap = FilterUtils.retainOnlyFilterKeys(request.getParameterMap(), filterFields);
    
    
    // Query to get the column (field) names, as stored in the database
    String columnNamesQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.Columns WHERE TABLE_NAME='" + table + "'";
    
    
    
    // Query to get the values stored in the databases, for the specified fields
    String tableDataQuery = "SELECT ";
    
    tableDataQuery += "*";
    
    tableDataQuery += " FROM `" + table + "`";
    
    String idCountQuery = "SELECT COUNT(id) FROM `" + table + "`";

    if (currentSort != null && !currentSort.isEmpty()) {
        tableDataQuery += " ORDER BY `" + currentSort + "`";
        if (descending) {
            tableDataQuery += " DESC";
        }
    } else if (CmsAgent.elementExists(defaultOrderField)) {
        tableDataQuery += " ORDER BY `" + defaultOrderField + "`";
    }
    
    
    
    
    
    
    
    
    
    

    //out.println("<h2>" + LABEL_FORM_DATA_FOR + " \"" + formName + "\"</h2>");
    if (!included) {
        cms.includeTemplateTop();
        out.println("<h1>" + pageTitle + "</h1>"); // Use the individual entry title if this is 
        if (action.isEmpty()) {
            if (CmsAgent.elementExists(viewText))
                out.println("<div class=\"ingress\">" + viewText + "</div>");
        }
    }
    
    
    Form f = null;
    try {
        f = new Form(cmso.getSitePath(formResource), cmso);
    } catch (Exception e) {
        // Just means we won't be getting the nice column names
        out.println("<p><em>" + LABEL_UNABLE_TO_CREATE_FORM + ".</em></p>");
    }

    if (DEBUG) {
        out.println("<p>Column names query: " + columnNamesQuery + "<br/>");
        out.println("Table data query: " + tableDataQuery + "</p>");
    }

    SQLAgent sqlAgent = new SQLAgent(cmso);

    ResultSet rs = sqlAgent.doSelect(idCountQuery);
    if (rs.first()) {
        //out.println("<p>" + LABEL_TABLE + ": <code>" + table + "</code><br />");
        //out.println(LABEL_ROWS + ": <code>" + rs.getString(1) + "</code></p>");
        //out.println("<p>" + rs.getString(1) + " scientists found:</p>");
    }

    rs = sqlAgent.doSelect(columnNamesQuery);
    
    //ArrayList listColumnNames = new ArrayList(); // Will hold the column (field) names of those that should be displayed in the result list
    ArrayList allColumnNames = new ArrayList(); // Will hold ALL column (field) names

    String th = "";
    // Add select all rows checkbox (for editors)
    if (userIsLoggedIn && CmsAgent.elementExists(emailField)) {
        if (USE_TABLE) {
            th += "<th scope=\"col\" style=\"text-align:center;\"><input id=\"select-all-rows\" type=\"checkbox\" name=\"selected-all-rows\" value=\"all\" /></th>";
        } else {
            th += "<div class=\"th\" style=\"display:table-cell\"><input id=\"select-all-rows\" type=\"checkbox\" name=\"selected-all-rows\" value=\"all\" /></div>";
        }
    }
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            String columnName = rs.getString(1);
            I_FormInputElement element = f.getElement(columnName);
            
            allColumnNames.add(columnName);
            
            // Create a filter for this column?
            if (element != null && filterFields.contains(columnName)) { // If the element exists and is set to appear as a filter ...
                if (element.isPreDefinedValueInput()) { // ... and this form element has pre-defined values (select, checkbox or radio)
                    
                    
                    
                    List filterLinksHtml = new ArrayList();
                    List elementOptions = element.getOptions();
                    Iterator iElementOptions = elementOptions.iterator();
                    while (iElementOptions.hasNext()) {
                        Option o = (Option)iElementOptions.next();
                        //filterLinksHtml.add("<a href=\"?" + element.getName() + "=" + o.getValue() + "\">" + o.getText() + "</a>");
                        FilterLink fl = FilterUtils.addOrRemoveFilter(element.getName(), o.getValue(), request.getParameterMap());
                        String filterLinkHtml = "<a"
                                                + " href=\"?" + fl.getParam()
                                                + (fl.isActive() ? "\" class=\"form-view-onfilter\" style=\"font-weight:bold;" : "")
                                                + "\">" + o.getText() + "</a>";
                        filterLinksHtml.add(filterLinkHtml);
                        
                        // Filter with a form
                        
                        if (fl.isActive()) {
                            activeFilters.add(filterLinkHtml);
                            o.setSelected(true);
                        }
                    }
                    //filterFormFields.put(element.getName(), element.getOptions());
                    filterElements.add(element);
                    if (!filterLinksHtml.isEmpty()) {
                        filterLinks.put(element.getName(), filterLinksHtml);
                    }
                }
            }
            
            // Display this field?
            if (listFields.contains(columnName)) {
                //out.println("<!-- columnName=" + columnName + ", currentSort=" + currentSort + " -->");
                //listColumnNames.add(columnName);
                String rowContent = USE_TABLE ? "<th scope=\"col\">" : "<div class=\"th\" style=\"display:table-cell;\">";
                
                // Create the table header. This will be a link to sort the data by this field.
                //rowContent += "<h4>";
                rowContent += "<a href=\"?" +
                        "_sort=" + columnName;
                // If the current view is sorted by this field, create a link that swaps the order (ascending / descending)
                if (columnName.compareTo(currentSort) == 0 && !descending)
                    rowContent += "&amp;_order=desc";
                if (!filterFields.isEmpty()) {
                    rowContent += "&amp;" + FilterUtils.createParameterString(filterMap);
                }
                rowContent += "\"";
                if (columnName.compareTo(currentSort) == 0)
                    rowContent += " class=\"view-sort" + (descending ? "-descending" : "") + "\"";
                rowContent += ">";
                
                if (element != null) {
                    //rowContent += f.getElement(columnName).getLabel().replaceAll(" ","&nbsp;") + "</a>" +
                    rowContent += f.getElement(columnName).getLabel() + "</a>" +
                        (displayColumnNames ? ("<br /><span class=\"view-column-name\" style=\"font-size:0.8em;\">" + columnName + "</span>") : "") + "";
                }
                else {
                    rowContent += columnName + "</a>";
                }
                //rowContent += "</h4>";
                
                rowContent += USE_TABLE ? "</th>" : "</div>";
                
                tableRow[listFields.indexOf(columnName)] = rowContent;
                //out.print(th);
            }
            rs.next();
        }  
        th += FilterUtils.arrayAsString(tableRow);
    }
    //out.println("</tr>");
    
    
    
    //
    // Done with table headers, handle table content
    //
    
    
    
    rs = sqlAgent.doSelect(tableDataQuery);
    //*
    String fieldName = null;
    String fieldValue = null;
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            String id = rs.getString("id");
            //String html = "<tr>";
            //out.print("<tr>");
            //boolean filterMatch = filterMap.isEmpty() ? true : false; // Initialize as true or false depending on filter existence
            //List<Boolean> filterMatches = new ArrayList<Boolean>(filterMap.size());
            
            // Get a filter matcher object. We'll use this to match against all filters (if any)
            FilterMatcher filterMatcher = new FilterMatcher(filterMap);
            
            for (int i = 0; i < allColumnNames.size(); i++) {
                fieldName = (String)allColumnNames.get(i);
                fieldValue = rs.getString(fieldName);
                
                //
                // Match against filters 
                // (Filters are defined in URL parameters; i.e. a "speciality" filter may look like this: "specialty=glacier&speciality=frozen_ground")
                //
                if (filterMatcher.isMatcherFor(fieldName)) { // If true, then a filter exists for this field
                    //out.println("<h5>filter found for " + fieldName + "</h5>");
                    // Get the filter (the stuff to match against)
                    //List fieldFilter = filterMatcher.getMatches(fieldName);
                    
                    // Handle multiple values separate from singular values
                    if (f.getElement(fieldName).isMultipleChoice()) { // Multiple values
                        if (fieldValue != null && !fieldValue.isEmpty()) {
                            // Get a list containing the stored values for this field
                            List fieldValues = FormUtil.getMultipleValues(fieldValue); // Arrays.asList(fieldValue.split("\\|"));
                            // See if any of the stored values matches any of the filter values
                            Iterator iFieldValues = fieldValues.iterator();
                            while (iFieldValues.hasNext()) {
                                String singleFieldValue = (String)iFieldValues.next();
                                //out.print("<h5>Checking if '" + singleFieldValue + "' is match on '" + fieldName + "' ... ");
                                if (filterMatcher.match(fieldName, singleFieldValue)) {
                                    //out.println("MATCH</h5>");
                                    // Found a match on this filter, no need to continue (we've got a match on at least one value, so this entry is considered a match)
                                    break;
                                }
                            }
                        }
                    } 
                    else {
                        filterMatcher.match(fieldName, fieldValue);
                    }
                }
                
                if (listFields.contains(fieldName)) {
                    String rowContent = "";
                    if (USE_TABLE) {
                        rowContent += "<td"
                                //+ " onclick=\"javascript:window.location = '" + "view.html?id=" + id +  "'\""
                                //+ " onclick=\"javascript:window.location = '" + cms.link(detailTemplate) + "?id=" + id +  "'\""
                                + " onclick=\"javascript:window.location = '" + requestFileUri + "?_action=view&amp;id=" + id +  "'\""
                                + ">";
                    } else {
                        rowContent += "<div class=\"td\" style=\"display:table-cell;\">";
                    }
                    
                    if (fieldValue == null) {
                        rowContent += "<i>NULL</i>";
                    } 
                    else if (!fieldValue.isEmpty()) {
                        I_FormInputElement formElement = f.getElement(fieldName);
                        if (formElement == null || (formElement != null && !formElement.isMultipleChoice())) { // Form does not contain this field (like ID, which is added automatically)
                            rowContent += fieldValue;
                        } 
                        else {
                            try {
                                if (formElement.isMultipleChoice()) {
                                    List fieldValues = FormUtil.getMultipleValues(fieldValue);// Arrays.asList(fieldValue.split("\\|"));
                                    Iterator iFieldValues = fieldValues.iterator();
                                    while (iFieldValues.hasNext()) {
                                        String optionValue = (String)iFieldValues.next();
                                        rowContent += formElement.getTextForOption(optionValue);
                                        if (iFieldValues.hasNext())
                                            rowContent += ", ";
                                    }
                                }
                                else {
                                    rowContent += fieldValue;
                                }
                            } catch (Exception e) {
                                rowContent += " &ndash; ";
                            }
                        }
                    }
                    rowContent += USE_TABLE ? "</td>" : "</div>";
                    tableRow[listFields.indexOf(fieldName)] = rowContent;
                }
            }
            //html += "</tr>";
            // Print this row, but only if: 
            //      the row matches the applied filter(s)
            //      OR 
            //      no filters are present
            if (filterMatcher.isMatch()) {
                if (USE_TABLE) {
                    rows.add("<tr"
                            //+ " onmouseover=\"this.style.paddingTop='2px';this.style.background='red';this.style.cursor='pointer'\""
                            //+ " onmouseover=\"this.style.cursor='pointer'\""
                            //+ " onclick=\"javascript:window.location='" + "view.html?id=" + id +  "'\"" // Click listener on the table row will trigger navigation to detail page when clicking the "select row" checkbox...
                            + ">" 
                            // Add "select row" checkbox (for editors)
                            + (userIsLoggedIn && CmsAgent.elementExists(emailField) ? ("<td><input class=\"select-row\" type=\"checkbox\" name=\"id\" value=\"" + id + "\" /></td>") : "") 
                            +  FilterUtils.arrayAsString(tableRow) + "</tr>"
                    );
                } else {
                    rows.add("<a href=\"" + cms.link(requestFileUri + "?_action=view&amp;id=" + id) + "\" class=\"tr\" style=\"display:table-row;\">"
                            + (userIsLoggedIn && CmsAgent.elementExists(emailField) ? ("<div class=\"td\" display=\"table-cell;\"><input class=\"select-row\" type=\"checkbox\" name=\"id\" value=\"" + id + "\" /></div>") : "") 
                            +  FilterUtils.arrayAsString(tableRow) + "</a>"
                    );
                }
            }
            
            rs.next();
        }
        
        //
        // Filters
        //
        // BEGIN FILTER LINKS
        /*if (!activeFilters.isEmpty()) {
            // Print the active filters
            String activeFiltersHtml = ""; // Holds the HTML for the active filters (if any)
            Iterator<String> iActiveFilters = activeFilters.iterator();
            while (iActiveFilters.hasNext()) {
                activeFiltersHtml += iActiveFilters.next().replaceFirst(">", " title=\"Click to remove this filter\">") + (iActiveFilters.hasNext() ? ", " : "");
            }
            out.println("<h4 style=\"margin-bottom:0.2em;\">Active filters (click to remove):</h4><p style=\"margin-top:0;\">" + activeFiltersHtml + "</p>");
        }*/
        if (!filterLinks.isEmpty()) {
            /*String filtersHtml = ""; // Holds the HTML for the full list of filters
            
            Set keys = filterLinks.keySet();
            Iterator iKeys = keys.iterator();

            filtersHtml += "<a id=\"form-view-filters-toggle\">Show all filters" + "</a>";
            filtersHtml += "<div id=\"form-view-filters\">";
            while (iKeys.hasNext()) {
                filtersHtml += "<div class=\"form-view-filter\" style=\"width:" + ((100.0 / filterLinks.size()) - 2) + "%;\">";
                String key = (String)iKeys.next();
                List availableFilters = filterLinks.get(key);
                Iterator iAvailableFilters = availableFilters.iterator();
                if (iAvailableFilters.hasNext()) {
                    filtersHtml += "<h4>" + f.getElement(key).getLabel() + "</h4>";
                    filtersHtml += "<ul>";
                    while (iAvailableFilters.hasNext()) {
                        filtersHtml += "<li>" + iAvailableFilters.next() + "</li>";
                    }
                    filtersHtml += "</ul>";
                }
                filtersHtml += "</div><!-- .form-view-filter -->";
            }
            filtersHtml += "</div><!-- .form-view-filters -->";
            // Print the full list of filters
            out.println(filtersHtml);
            // END FILTER LINKS
            */

            
            // BEGIN FILTER FORM
            out.println("<a id=\"form-view-filters-toggle\">" + LABEL_SHOW_FILTERS + "</a>");
            out.println("<div id=\"form-view-filters\">");
            //out.println("<div style=\"padding:1em 0;\">");
            out.println("<form class=\"form\" name=\"\" action=\"" + requestFileUri + "\" method=\"get\">");
            Iterator<I_FormInputElement> iFilterElements = filterElements.iterator();
            while (iFilterElements.hasNext()) {
                I_FormInputElement filterElement = iFilterElements.next();
                out.println(filterElement.getHtml(true));
            }
            out.println("<input class=\"button default\" type=\"submit\" value=\" " + LABEL_APPLY_FILTERS + " \" />");
            out.println("</form>");
            //out.println("</div>");
            out.println("</div>");
            // END FILTER FORM
            
            
            out.println("<script type=\"text/javascript\">"
                            + "\n$(document).ready(function() {" 
                            + "\n\t$(\"#form-view-filters\").hide();"
                            + "\n\t$(\"#form-view-filters-toggle\").addClass('slide-down');"
                            + "\n});"
                            + "\n$(\"#form-view-filters-toggle\").click(function() {"
                                //+ "\n\t$(\"#form-view-filters\").slideToggle(500);"
                                + "\n\tif ($(this).html() == '" + LABEL_SHOW_FILTERS + "') {"
                                + "\n\t\t$(this).html('" + LABEL_HIDE_FILTERS + "').removeClass('slide-down').addClass('slide-up');"
                                + "\n\t\t$(\"#form-view-filters\").fadeTo('fast', 1.0).slideDown(300);"
                                + "\n\t} else {"
                                + "\n\t\t$(this).html('" + LABEL_SHOW_FILTERS + "').removeClass('slide-up').addClass('slide-down');"
                                + "\n\t\t$(\"#form-view-filters\").fadeTo('fast', 0.2).slideUp(300);"
                                + "\n\t}});"
                        + "\n</script>");
           
            
            
            
        }
        
        
        
        // Display e-mail form
        if (userIsLoggedIn) {
            out.println("<form action=\"" + requestFileUri + "\" method=\"get\">");
        }
        
        if (!rows.isEmpty()) {
            
            // Export options
            if (userIsLoggedIn) {
                out.println("<p class=\"box message\">Download as: ");
                out.println(
                        "<a"
                        //+ " class=\"button default\""
                        + " href=\"" 
                                + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                                + "?format=xls&amp;fvpath=" + resourceUri 
                                + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                            + "\""
                        + " id=\"form-export-xls\""
                        + " target=\"_blank\""
                        + ">Excel (.xls)</a>"
                        );
                out.println(
                        " | <a"
                        //+ " class=\"button default\""
                        + " href=\"" 
                                + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                                + "?format=csv&amp;fvpath=" + resourceUri 
                                + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                            + "\""
                        + " id=\"form-export-cvs\""
                        + " target=\"_blank\""
                        + ">Comma-separated (.csv)</a>"
                        );
                out.println(
                        " | <a"
                        //+ " class=\"button default\""
                        + " href=\"" 
                                + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                                + "?format=xml&amp;fvpath=" + resourceUri 
                                + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                            + "\""
                        + " id=\"form-export-cvs\""
                        + " target=\"_blank\""
                        + ">XML</a>"
                        );
                out.println("</p>");
            }
            
            out.println(listText.replaceAll("%\\(list\\.count\\)", String.valueOf(rows.size())));
            //out.println("<table class=\"scientist-dir odd-even-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"2\">\n<tr>");
            
            if (USE_TABLE) {
                // option a) table
                out.println("<table class=\"form-view-table" + (CmsAgent.elementExists(tableClass) ? " ".concat(tableClass) : "") + "\">");
                out.println("<thead><tr>" + th + "</tr></thead><tbody>");
            } else {
                // option b) divs
                out.println("<div class=\"table form-view-table" + (CmsAgent.elementExists(tableClass) ? " ".concat(tableClass) : "") + "\" style=\"display:table;\">");
                out.println("<div class=\"tr\" style=\"display:table-row;\">" + th + "</div>");
            }
            Iterator<String> iRows = rows.iterator();
            while (iRows.hasNext()) {
                out.println(iRows.next());
            }
            
            out.println(USE_TABLE ? "</tbody></table>" : "</div>");
            
            %>
            <script type="text/javascript">
                $(document).ready(function() {
                <% if (USE_TABLE) { %>
                    /*
                    $(".form-view-table tr").mouseenter(function() {
                        $(this).find("td").addClass("row-hover");
                    });
                    $(".form-view-table tr").mouseleave(function() {
                        $(this).find("td").removeClass("row-hover");
                    });
                    */
                <% } %>
                    $("#select-all-rows").click(function() {
                        $(".select-row").attr('checked', $("#select-all-rows").is(':checked'));
                    });
                });
            </script>
	    <%
        } 
        else {
            //out.println("<p>Sorry, no matches found.</p>");
            out.println(listEmptyText);
        }
    }
    else {
        out.print("<em>" + LABEL_NO_DATA + ".</em>");
    }
    
    // Display e-mail form
    if (userIsLoggedIn) {
        out.println("<input type=\"hidden\" name=\"_action\" value=\"email\" />");
        if (CmsAgent.elementExists(emailField)) {
            out.println("<input class=\"button default\" type=\"submit\" label=\" E-mail selected \"  value=\" E-mail selected \" />");
        }
                
        out.println(
                "<input type=\"button\""                
                + " onclick=\"javascript:window.location='" 
                        + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                        + "?format=xls&amp;fvpath=" + resourceUri 
                        + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                    + "'\""
                + " id=\"form-export-xls\""
                + " class=\"button default\""
                + " value=\"Excel export\">"
                );
        out.println(
                "<input type=\"button\""                
                + " onclick=\"javascript:window.location='" 
                        + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                        + "?format=csv&amp;fvpath=" + resourceUri 
                        + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                    + "'\""
                + " id=\"form-export-cvs\""
                + " class=\"button default\""
                + " value=\"CSV export\">"
                );
        out.println(
                "<input type=\"button\""
                + " onclick=\"javascript:window.location='" 
                        + cms.link("/system/modules/no.npolar.common.forms/elements/form-data-export.jsp") 
                        + "?format=xml&amp;fvpath=" + resourceUri 
                        + "&amp;" + FilterUtils.createParameterString(request.getParameterMap()) // Parameters
                    + "'\""
                + " id=\"form-export-cvs\""
                + " class=\"button default\""
                + " value=\"XML export\">"
                );
        out.println("</form>");
    }
}






////////////////////////////////////////////////////////////////////////////////
// Delete single entry
//
else if (action.equals("delete")) {
    //request.setAttribute("id", request.getParameter("id"));
    // Get the ID of the entry to delete.
    String entryId = request.getParameter("id");
    pageTitle = "Deleting entry";//LABEL_DELETING_ENTRY;// + pageTitle;
    request.setAttribute("Title", pageTitle);
    // Adjust breadcrumb menu (customization for www.npolar.no, requires module no.npolar.common.menu)
    request.setAttribute("addCrumb", new MenuItem(pageTitle, cms.getRequestContext().getUri()));
    cms.includeTemplateTop();

    out.println("<h1>" + pageTitle + "</h1>");

    if (userIsVfsManager) {
        if (!FormUtil.isValidFormatForId(entryId)) {
            out.println(MSG_PARAMETER_ERROR);
            cms.includeTemplateBottom();
            return;
        }
        
        
        Form form = new Form(formPath, cmso);
        form.setAction(formPath);
        try {
            form.recreateFromId(entryId, cmso);
        } catch(Exception e) {
            out.println("<h2 class=\"error\">Nothing to delete</h2><p>The entry that was requested deleted does not exist (entry ID " + entryId + ").</p>");
            cms.includeTemplateBottom();
            return;
        }
        try {
            form.deleteData(Integer.valueOf(entryId).intValue());
            out.println("<p><strong>" + entryTitle + "</strong> (entry ID " + entryId + ") successfully deleted.</p>");
            out.println("<h4><a href=\"" + cms.link(requestFileUri) + "\">To the overview</a></h4>");
        } catch (Exception e) {
            out.println("<h2 class=\"error\">Error</h2><p>The delete process failed:</p><div class=\"trace\">");
            e.printStackTrace(response.getWriter());
            out.println("</div>");
            cms.includeTemplateBottom();
            return;
        }
    } 
    else {
        out.print("<h2 class=\"error\">" + LABEL_RESTRICTED + "</h2>"
                + "<p>" + LABEL_REQUIRE_VFS_MANAGER + "</p>"
                + "<p>" + LABEL_SUGGEST_LOGIN + "</p>");
    }
}

////////////////////////////////////////////////////////////////////////////////
// Edit single entry
//
else if (action.equals("edit")) {
    //request.setAttribute("id", request.getParameter("id"));
    // Get the ID of the entry to edit.
    String entryId = request.getParameter("id");
    pageTitle = "Editing " + pageTitle;
    request.setAttribute("Title", pageTitle);
    // Adjust breadcrumb menu (customization for www.npolar.no, requires module no.npolar.common.menu)
    request.setAttribute("addCrumb", new MenuItem(pageTitle, cms.getRequestContext().getUri()));
    cms.includeTemplateTop();
    
    out.println("<h1>" + pageTitle + "</h1>");

    // Check that the ID is in fact present as a parameter and of correct format.
    if (!FormUtil.isValidFormatForId(entryId)) {
        out.println(MSG_PARAMETER_ERROR);
        cms.includeTemplateBottom();
        return;
    }
    
    if (userIsVfsManager) {
        Form form = new Form(formPath, cmso);
        form.setAction(formPath);
        try {
            form.recreateFromId(entryId, cmso);
        } catch(Exception e) {
            out.println("<h2 class=\"error\">Nothing to edit</h2>"
                        + "<p>The entry that was requested edited does not exist (entry ID " + entryId + ").</p>");
            cms.includeTemplateBottom();
            return;
        }
        // Let the form know we're editing an existing entry, so it will use "UPDATE" (not "INSERT") in the SQL.
        form.setEditEnabled(true, Integer.valueOf(entryId).intValue());
        form.setButtonTextPreview("Continue to preview");
        out.println(form.getHtml(true));

        session.setAttribute("form", form);
    }
    else {
        out.print("<h2 class=\"error\">" + LABEL_RESTRICTED + "</h2>" +
                "<p>" + LABEL_REQUIRE_VFS_MANAGER + "</p>" + 
                "<p>" + LABEL_SUGGEST_LOGIN + "</p>");
    }
}
    



//////////////////////////////////////////////////////////////////////////////// 
// Viewing single entry
//
else if (action.equals("view")) {
    request.setAttribute("id", request.getParameter("id"));
    // Adjust breadcrumb menu (customization for www.npolar.no, requires module no.npolar.common.menu)
    request.setAttribute("addCrumb", new MenuItem(pageTitle, cms.getRequestContext().getUri()));
    request.setAttribute("Title", pageTitle);
    
    Form form = new Form(formPath, cmso);
    
    
    cms.includeTemplateTop();
    
    // Get and validate the entry (row) ID
    String entryId = request.getParameter("id");
    if (!FormUtil.isValidFormatForId(entryId)) {
        out.println(MSG_PARAMETER_ERROR);
        cms.includeTemplateBottom();
        return;
    }
    // Recreate the form (or crash gracefully)
    try {
        form.recreateFromId(entryId, cmso);
    } catch (Exception e) {
        out.println("<h1>Error</h1>");
        out.println(MSG_PARAMETER_ERROR);
        cms.includeTemplateBottom();
        return;
    }
    
    // Edit / delete tools for logged in VFS managers
    if (userIsVfsManager) {
        String editButton = "<a"
                            + " class=\"button default\""
                            + " href=\"" + cms.link(requestFileUri) + "?_action=edit&amp;id=" + entryId + "\""
                            + " target=\"_blank\">Edit</a>";
        String deleteButton = "<a"
                            + " class=\"button default\""
                            + " style=\""
                                + "background:#c00;"
                            + "\""
                            + " href=\"" + cms.link(requestFileUri) + "?_action=delete&amp;id=" + entryId + "\""
                            + " onclick=\"return confirm('About to DELETE entry #" + entryId + ": " 
                                + CmsStringUtil.escapeHtml(pageTitle).replaceAll("'", "\\\\'") + ". Proceed?')\""
                            //+ " target=\"_blank\""
                            + ">Delete</a>";
        out.println("<div"
                        + " class=\"admin-tools\"" 
                        + " style=\""
                            + "border:1px solid #d00;"
                            + " text-align:right;"
                            + " border-radius:3px;"
                            + " background:#fee;"
                            + " margin-bottom:1em;"
                        + "\">"
                            + editButton 
                            + deleteButton 
                    + "</div>");
    }
    out.println("<h1>" + pageTitle + "</h1>");
    
    // If a custom detail template was defined, include it now
    if (CmsAgent.elementExists(detailTemplate)) {
        cms.include(detailTemplate);
    }
    // If not, use the default detail template
    else {
        out.println(form.getPreview());
    }
}







////////////////////////////////////////////////////////////////////////////////
// Mass e-mail
//
else if (action.equals("email")) {
    // Get the ID of the e-mail recipients.
    String[] id = request.getParameterValues("id");
    String msg = request.getParameter("msg");
    String subject = request.getParameter("subject");
    
    // Adjust breadcrumb menu (customization for www.npolar.no, requires module no.npolar.common.menu)
    request.setAttribute("addCrumb", new MenuItem("Mass email", cms.getRequestContext().getUri()));
    request.setAttribute("Title", "Mass email");
    
    cms.includeTemplateTop();

    boolean sendMail = false;
    try {
        if (request.getParameter("mail_action").equals("Send")) {
            sendMail = true;
        }
    } catch (Exception e) {
        sendMail = false;
    }

    if (sendMail) {
        if (msg == null || msg.isEmpty()) {
            out.println("<h1 class=\"error\">Missing information</h1>"
                        + "<p>Sorry, couldn't see that you provided any message to send. Please try again.</p>");
            cms.includeTemplateBottom();
            return;
        }
    }

    // Check that the ID is in fact present as a parameter and of correct format.
    if (id == null
            || id.length < 1) {
        out.println("<h1 class=\"error\">Missing information</h1>"
                    + "<p>Sorry, couldn't see that you provided any email recipients. Please try again.</p>");
        cms.includeTemplateBottom();
        return;
    }

    out.println("<h1>" + "Send mass email" + "</h1>");

    //List<String> emailRecipients = new ArrayList<String>();
    Form form = new Form(formPath, cmso);
    SQLAgent sqla = new SQLAgent(cmso);
    //String query = "SELECT email, email_alt, firstname, lastname FROM " + form.getTableName() + " WHERE id IN (";
    String query = "SELECT * FROM " + form.getTableName() + " WHERE id IN (";
    for (int i = 0; i < id.length; i++) {
        if (FormUtil.isValidFormatForId(id[i])) { // Prevent mysql injection
            query += id[i];
            if (i+1 < id.length) {
                query += ",";
            }
        }
    }
    query += ");";
    //out.println("<pre>" + query + "</pre>");
    //*
    ResultSet rs = sqla.doSelect(query);
    if (rs.first()) {
        out.println("<h3>" + (sendMail ? "Sent" : "About to send") +" email to:</h3>");
        out.println("<ul style=\"border:1px solid #ccc; max-height:200px; overflow:auto;\">");
        while (!rs.isAfterLast()) {
            try {
                // Create the mail here for simplicity (but don't send it unless "Send" was clicked by the user - see check below)
                CmsSimpleMail mail = new CmsSimpleMail();
                mail.setCharset("utf-8");
                //mail.setHostName("anton.npolar.no");
                //mail.setFrom("no-reply@npolar.no", "CliC Project Office");
                mail.setFrom(emailFromAddr, emailFromName);

                //String name = rs.getString("firstname") + " " + rs.getString("lastname");
                String name = FormMacroResolver.resolveMacros(emailToNameField, rs, form, cmso);
                //String email = rs.getString("email").toLowerCase();
                String email = rs.getString(emailField).toLowerCase();
                
                mail.addTo(email);
                
                if (CmsAgent.elementExists(emailAltField)) {
                    //String emailAlt = rs.getString("email_alt").toLowerCase();
                    String emailAlt = rs.getString(emailAltField).toLowerCase();
                    if (!emailAlt.isEmpty()) {
                        email += ";" + emailAlt;
                        mail.addTo(emailAlt);
                    }
                }
                
                
                out.println("<li><a href=\"mailto:" + email + "\">" + name + " (" + email + ")</a></li>");

                if (sendMail) {
                    // Send the email:
                    try {            
                        boolean noErrors = true;
                        try {
                            mail.setSubject(FormMacroResolver.resolveMacros(subject, rs, form, cmso));
                            mail.setMsg(FormMacroResolver.resolveMacros(msg, rs, form, cmso));
                        } catch (SQLException sqle) {
                            out.println("<h2 class=\"error\">Mail was not sent</h2>");
                            out.println("<p>Something went wrong while resolving macros in the message.");
                            out.println("<p>The error was: <i>" + sqle.getMessage() + "</i></p>");
                            noErrors = false;
                        }
                        if (noErrors)
                            mail.send(); // Send the mail only there were no errors when resolving macros.
                    } catch (Exception e) {
                        out.println("<h2 class=\"error\">Error</h2>");
                        out.println("<p>Something went wrong while sending email to " + name + " (" + email + ").</p>");
                        out.println("<p>The error was: <i>" + e.getMessage() + "</i></p>");
                    }
                    // Done sending e-mail
                }

            } catch (Exception e ) {
                out.println("<li style=\"color:#900;\">Error fetching email address (" + e.getMessage() + ")</li>");
            }
            rs.next();
        }
        out.println("</ul>");
    }
    
    String ccStr = request.getParameter("cc");
    if (ccStr != null) {
        List ccRecipients = new ArrayList(Arrays.asList(ccStr.split(";")));
        
        // First, run through the list and check that the addresses are in fact valid e-mail addresses
        Iterator<String> check = ccRecipients.iterator();
        boolean noErrors = true;
        while (check.hasNext()) {
            String addr = check.next().trim();
            try {
                if (addr.isEmpty()) {
                    check.remove();
                } else {
                    CmsAgent.getJavascriptEmail(addr, false, null);
                }
            } catch (Exception e) {
                //check.remove();
                out.println("<h2 class=\"error\">Error</h2>");
                out.println("<p>This is not a valid email address: '" + addr + " .</p>");
                noErrors = false;
            }
        }
        if (noErrors && !ccRecipients.isEmpty()) {
            out.println("<h3>And to external recipient" + (ccRecipients.size() > 1 ? "s" : "") + ":</h3>");
            out.println("<ul style=\"border:1px solid #ccc; max-height:200px; overflow:auto;\">");
            Iterator<String> iCc = ccRecipients.iterator();
            while (iCc.hasNext()) {
                String cc = iCc.next().trim();
                if (!cc.isEmpty()) {
                    // Create the mail:
                    CmsSimpleMail mail = new CmsSimpleMail();
                    mail.setCharset("utf-8");
                    mail.setFrom(emailFromAddr, emailFromName);
                    mail.setSubject(subject);
                    mail.setMsg(msg);
                    mail.addTo(cc);

                    out.println("<li><a href=\"mailto:" + cc + "\">" + cc + "</a></li>");

                    if (sendMail) {
                        // Send the email:
                        try {
                            mail.send();
                        } catch (Exception e) {
                            out.println("<h2 class=\"error\">Error</h2>");
                            out.println("<p>Something went wrong while sending email to " + cc + ".</p>");
                            out.println("<p>The error was: <i>" + e.getMessage() + "</i></p>");
                        }
                        // Done sending e-mail
                    }
                }
            }
            out.println("</ul>");
        }
    }
    
    if (sendMail) {
        out.println("<p><a class=\"button default\" id=\"new-mail\" href=\"#\">Send more email to these recipients</a></p>");
    }
    
    out.println("<form action=\"#\" method=\"post\" id=\"mass-mail\">");

    out.println("<h4 style=\"margin:1em 0 0 0;\">Subject</h4>");
    out.println("<div style=\"border:1px solid #ccc;\">");
    out.println("<input type=\"text\" value=\"\" name=\"subject\" style=\"width:100%; border:none;\" />");
    out.println("</div>");

    out.println("<h4 style=\"margin:1em 0 0 0;\">External recipient(s)</h4>");
    out.println("<div style=\"border:1px solid #ccc;\">");
    out.println("<input type=\"text\" value=\"" + (ccStr == null ? "" : ccStr) + "\" name=\"cc\" style=\"width:100%; border:none;\" />");
    out.println("<div style=\"font-size:0.8em; font-style:italic; background:#C75000; color:#fff; padding:0.25em 0.5em;\">"
                + "When adding external recipients, avoid using any macros in the subject/message.</div>");
    out.println("</div>");


    out.println("<h4 style=\"margin:1em 0 0 0;\">Message</h4>");
    out.println("<div style=\"border:1px solid #ccc;\">");
    out.println("<textarea name=\"msg\" style=\"width:100%; height:300px; border:none;\"></textarea>");
    out.println("</div>");
    out.println("<input class=\"button default\" type=\"submit\" value=\"Send\" name=\"mail_action\" />");
    out.println("</form>");
    
    if (sendMail) {
        out.println("<script type=\"text/javascript\">"
                        + "$(document).ready(function() {"
                            + "$(\"#new-mail\").click(function () { "
                                + "$(\"#mass-mail\").slideToggle();"
                            + "});"
                            + "$(\"#mass-mail\").hide();"
                        + "});"
                        + "</script>");
        out.println("<div style=\"background:#eee; padding:0.5em;\">");
        out.println("<h3 style=\"margin:0.2em 0; border-bottom:1px solid #ccc;\">This is what you sent:</h3>");
        out.println("<pre>" + request.getParameter("msg") + "</pre>");
        out.println("</div>");
    }
}





    
    //*/
/*
} else {
    out.print("<h2 class=\"error\">" + LABEL_RESTRICTED + "</h2>" +
            "<p>" + LABEL_REQUIRE_VFS_MANAGER + "</p>" + 
            "<p>" + LABEL_SUGGEST_LOGIN + "</p>");
}
*/
if (!included) {
    cms.includeTemplateBottom();
} 
%>