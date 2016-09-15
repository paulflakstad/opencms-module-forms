<%-- 
    Document   : form-data-export 
    Description: Exports data collected via a form as XLS, CSV or XML.
    Created on : Aug 02, 2012, 9:20:38 PM
    Author     : Paul-Inge Flakstad, Norwegian Polar Institute
--%><%@page import="javax.swing.text.html.FormView"
%><%@page import="org.opencms.util.CmsHtmlExtractor"
%><%@page import="java.nio.charset.Charset"
%><%@page import="org.apache.poi.ss.usermodel.Workbook"
%><%@page import="org.apache.poi.ss.usermodel.CellStyle"
%><%@page import="org.apache.poi.ss.usermodel.Cell"
%><%@page import="org.apache.poi.xssf.usermodel.XSSFCell"
%><%@page import="org.apache.poi.xssf.usermodel.XSSFSheet"
%><%@page import="org.apache.poi.xssf.usermodel.XSSFWorkbook"
%><%@page import="org.apache.poi.xssf.usermodel.XSSFRow"
%><%@page import="org.apache.poi.hssf.usermodel.*"
%><%@page import="no.npolar.util.*"
%><%@page import="no.npolar.common.forms.*"
%><%@page import="no.npolar.common.forms.view.*"
%><%@page import="java.util.*"
%><%@page import="java.sql.*"
%><%@page import="java.io.*"
%><%@page import="org.opencms.main.*"
%><%@page import="org.opencms.security.CmsRoleManager"
%><%@page import="org.opencms.security.CmsRole"
%><%@page import="org.opencms.util.CmsUUID"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="javax.xml.parsers.*"
%><%@page import="javax.xml.transform.*"
%><%@page import="javax.xml.transform.dom.*"
%><%@page import="javax.xml.transform.stream.*"
%><%@page import="org.w3c.dom.*"
%><%@page import="org.opencms.jsp.*"
%><%@page import="org.opencms.file.*"
%><%@page import="org.opencms.file.types.*"
%><%@page import="org.opencms.util.CmsRequestUtil"
%><%@page import="org.opencms.xml.content.*"
%><%@page import="org.opencms.xml.types.*" session="true" pageEncoding="UTF-8"
%><%!
/**
* Converts a data row (represented as an array of strings) to a single string,
* formatted so to fit as a single line in a CSV file. The delimiter character is
* the semicolon (;). Two double quotes ("") are used for escaping (if neccesary).
* At the end of the line, a newline character (\n) is appended.
* 
* @param a  The array to convert to a CSV line
* @return String  The resulting CSV line
*/
public String arrayAsCsvLine(String[] a) {
    String s = "";
    for (int i = 0; i < a.length; i++) {
        // the {{n}} below indicates "new line" (used to separate multi-select option values)
        if (a[i] != null && (a[i].contains("\"") || a[i].contains(";") || a[i].contains("{{n}}"))) { 
            s += "\"" + a[i].replaceAll("\"", "\"\"") + "\"";
        } else {
            s += a[i];
        }
        if (i+1 < a.length)
            s += ";";
    }
    s += "\n";
    return s;
}

/**
 * Gets the "display friendly" version of a stored value, collected via a 
 * specific form field.
 * 
 * @param form The form.
 * @param fieldName The form field's name. This field must be present in the given form.
 * @param fieldValue The form field's value.
 * @param delimiter String to be used to separate multiple values (if needed).
 * @return String The "display friendly" version of the stored value, or an empty string.
 */
public static String getDisplayValue(
    Form form 
    ,String fieldName 
    ,String fieldValue 
    ,String delimiter) {

    if (delimiter == null || delimiter.isEmpty())
        delimiter = ", "; // Default delimiter
    
    String rowContent = "";
    
    if (fieldValue != null && !fieldValue.isEmpty()) {
        I_FormInputElement formElement = form.getElement(fieldName);
        if (formElement == null || (formElement != null && !formElement.isMultipleChoice())) { // Form does not contain this field (like ID or "time last modified")
            rowContent += fieldValue;
        }
        else {
            try {
                if (formElement.isMultipleChoice()) {
                    List fieldValues = Arrays.asList(fieldValue.split("\\|"));
                    Iterator iFieldValues = fieldValues.iterator();
                    while (iFieldValues.hasNext()) {
                        String optionValue = (String)iFieldValues.next();
                        rowContent += formElement.getTextForOption(optionValue);
                        if (iFieldValues.hasNext()) {
                            rowContent += delimiter;
                        }
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
    return rowContent;
}

/**
* Converts a ResultSet to an org.w3c.dom.Document.
* 
* @param rs  The ResultSet to convert to a Document
* @return Document The resulting Document
*/
/*
public static Document toDocument(ResultSet rs, Form form) 
            throws ParserConfigurationException, SQLException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder        = factory.newDocumentBuilder();
    Document doc                   = builder.newDocument();

    Element results = doc.createElement("results");

    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();

    if (rs.first()) {
        while (!rs.isAfterLast()) {
            Element row = doc.createElement("row");

            for (int i = 1; i <= colCount; i++) {
                String columnName = rsmd.getColumnName(i);
                //String value = (String)rs.getObject(i);
                String value = getDisplayValue(form, columnName, (String)rs.getObject(i), "|");

                Element node = doc.createElement(columnName);
                node.setAttribute("label", form.getLabelForElement(columnName));
                node.appendChild(doc.createTextNode(value.toString()));
                row.appendChild(node);
            }
            results.appendChild(row);
            rs.next();
        }
    }
    doc.appendChild(results);
    return doc;
}
//*/

/**
 * Converts a ResultSet to an org.w3c.dom.Document. (New version.)
 * 
 * Used in XML export.
 * 
* @param rs  The result set to convert to a document.
* @param form  The result set's corresponding form.
* @return Document The resulting document.
 */
public static Document toDocumentNew(ResultSet rs, Form form) 
            throws ParserConfigurationException, SQLException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder        = factory.newDocumentBuilder();
    Document doc                   = builder.newDocument();

    Element results = doc.createElement("results");

    ResultSetMetaData rsmd = rs.getMetaData();
    int colCount = rsmd.getColumnCount();

    if (rs.first()) {
        while (!rs.isAfterLast()) {
            Element row = doc.createElement("row");

            for (int i = 1; i <= colCount; i++) {
                String columnName = rsmd.getColumnName(i);
                String fieldLabel = columnName;
                Element node = null;
                I_FormInputElement element = form.getElement(columnName);
                if (element != null) {
                    fieldLabel = element.getLabel();
                    if (element.isMultipleChoice()) {
                        columnName = columnName + "s"; // Make the name plural
                    }
                }
                
                node = doc.createElement(columnName);
                node.setAttribute("label", fieldLabel);
                String value = "[unknown value]";
                try {
                    value = rs.getObject(i).toString();
                } catch (Exception e) {
                    //try {
                    //    value = rs.getTimestamp(i).toString();
                    //} catch (Exception ee) {}
                }
                try {
                    if (element.isMultipleChoice()) { // Possibly several values stored here: Create a parent element, and let each stored value be a child of that parent
                        List<String> values = Arrays.asList(value.split("\\|"));
                        Iterator<String> iValues = values.iterator();
                        while (iValues.hasNext()) {
                            Element optionValue = doc.createElement(element.getName()); // Create an XML element, use the name element's name (we appended the plural "s" to the parent element)
                            optionValue.appendChild(doc.createTextNode(element.getTextForOption(iValues.next()))); // Insert the value text, but replace the actual value with the option's label
                            node.appendChild(optionValue); // Add the node as a child of the parent
                        }
                    } else { // Always just a single value stored here
                        //value = getDisplayValue(form, columnName, value, null);
                        if (element.isPreDefinedValueInput()) // Could still be pre-defined input...
                            value = element.getTextForOption(value);
                        node.appendChild(doc.createTextNode(value));
                    }
                } catch (Exception e) {
                    // Assume element==null => probably timestamp or ID
                }
                row.appendChild(node);
            }
            results.appendChild(row);
            rs.next();
        }
    }
    doc.appendChild(results);
    return doc;
}

/**
* Prints an org.w3c.dom.Document to a given output stream.
* 
* @param doc  The document to convert.
* @param out  The stream to use when printing the document.
*/
public static void printDocument(Document doc, OutputStream out) 
            throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    //return transformer.toString();
    
    transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
}

%><%
CmsAgent cms = new CmsAgent(pageContext, request, response);
CmsObject cmso = cms.getCmsObject();

// Optional: ID parameter (if present, the user has checked one or more rows)
String[] checkedIdsArr = request.getParameterValues("id");
List<String> matchIds = new ArrayList<String>();
List<String> checkedIds = new ArrayList<String>();
try {
    checkedIds = Arrays.asList(checkedIdsArr);
} catch (Exception e) {
    // Ignore the ID parameter (either it's not present or it contains errors)
}
        

//String requestFileUri = cms.getRequestContext().getUri();

//boolean included = cmso.readResource(cms.getRequestContext().getUri()).getTypeId() != OpenCms.getResourceManager().getResourceType("jsp").getTypeId();

String table = request.getParameter("_table")== null ? "" : request.getParameter("_table");
String currentSort = request.getParameter("_sort") == null ? "" : request.getParameter("_sort");
boolean descending = request.getParameter("_order") == null ? false : (request.getParameter("_order").equals("desc") ? true : false);
boolean displayColumnNames = false; // Whether or not to display the column name under the column label in the table header

boolean userAllowedRead = false;
boolean userAllowedWrite = false;
/*
String formPath = request.getParameter("fpath");
String formViewPath = request.getParameter("fvpath");

if (formPath != null && formViewPath != null) {
    if (!cmso.existsResource(formPath))
        throw new IllegalArgumentException("The form '" + formPath + "' does not exist.");
    if (!cmso.existsResource(formViewPath))
        throw new IllegalArgumentException("The form view '" + formViewPath + "' does not exist.");
*/
String formViewPath = request.getParameter("fvpath");

if (formViewPath != null) {
    
    if (!cmso.existsResource(formViewPath)) {
        throw new IllegalArgumentException("The form view '" + formViewPath + "' does not exist.");
    } else {
        userAllowedRead = cmso.hasPermissions(cmso.readResource(formViewPath), org.opencms.security.CmsPermissionSet.ACCESS_READ);
        if (!userAllowedRead) {
            throw new IllegalArgumentException("Permission denied. You are not authorized to access these data.");
        }
        userAllowedWrite = cmso.hasPermissions(cmso.readResource(formViewPath), org.opencms.security.CmsPermissionSet.ACCESS_WRITE);
        //cms.getRequestContext().getCurrentUser().
    }
    
    
// These are the export formats currently supported
final String FILE_TYPE_CSV = "csv";
final String FILE_TYPE_XLS = "xls";
final String FILE_TYPE_XML = "xml";
final String DEFAULT_EXPORT_FORMAT = FILE_TYPE_CSV;
List<String> supportedFormats = Arrays.asList( new String[] { FILE_TYPE_CSV, FILE_TYPE_XLS, FILE_TYPE_XML } );

String exportFormat = request.getParameter("format");
if (exportFormat == null || exportFormat.isEmpty()) { // No format specified: fallback to default
    exportFormat = DEFAULT_EXPORT_FORMAT;
}
else if (!supportedFormats.contains(exportFormat)) { // Unsupported format
    throw new IllegalArgumentException("Requested to export data as '" + exportFormat + "', but this format is not supported.");
}
    
/*
Map pm = new HashMap(request.getParameterMap());
pm.remove("_table");
pm.remove("_sort");
pm.remove("_order");
*/

/*  
List<String> specialities = null;
try {
    specialities = Arrays.asList(request.getParameterValues("speciality"));
} catch (NullPointerException npe) {
    specialities = new ArrayList<String>(); // No such parameter, make an empty list
}
*/

CmsRoleManager roleManager  = OpenCms.getRoleManager();
boolean userIsVfsManager    = roleManager.hasRole(cms.getCmsObject(), CmsRole.VFS_MANAGER);

final String LABEL_UNABLE_TO_CREATE_FORM = "Unable to create Form instance";
final String LABEL_NO_DATA = "No data has been submitted through this form";
/*
final String DELETED_FORM = "DELETED FORM";
final String LABEL_FORM_DATA_FOR = "Form data for";
final String LABEL_TABLE = "Table";
final String LABEL_ROWS = "Rows total";
final String LABEL_RESTRICTED = "Access restricted";
final String LABEL_REQUIRE_VFS_MANAGER = "To view this file, you need to be logged in as VFS manager.";
final String LABEL_SUGGEST_LOGIN = "Please log in, or contact your system administrator for help.";
*/

final boolean DEBUG = false;

//if (userIsVfsManager) {
    

    //
    // Read form definition (structured content) file
    //
    String viewTitle = null; // The title for this view
    String formPath = null; // The path to the form VFS resource
    String defaultOrderField = null;
    String fieldNamesView = null; // Fields to display
    String fieldNamesFilter = null; // Fields to use as (public) filters
    String fieldNamesAdminFilter = null; // Fields to use as filters available only to admins
    
    I_CmsXmlContentContainer configuration = cms.contentload("singleFile", formViewPath, new Locale(cmso.readPropertyObject(formViewPath, "locale", true).getValue("en")), false);
    while (configuration.hasMoreContent()) {
        viewTitle = cms.contentshow(configuration, "Title");
        formPath = cms.contentshow(configuration, "FormPath");
        defaultOrderField = cms.contentshow(configuration, "OrderBy");
        fieldNamesView = cms.contentshow(configuration, "FieldsView");
        fieldNamesFilter = cms.contentshow(configuration, "FieldsFilter");
        fieldNamesAdminFilter = cms.contentshow(configuration, "FieldsAdminFilter");
    }
    
    CmsResource formResource = null;
    
    try {
    	formResource = cmso.readResource(formPath);
    } catch (Exception e) {
    	throw new IllegalArgumentException("Unable to continue, could not read the form '" + formPath + "'"
                + " (defined in '" + formViewPath + "', locale=" + cms.getRequestContext().getLocale() + ".");
    }
    //String formName = cmso.readPropertyObject(formResource, "Title", false).getValue("[no name]");

    //CmsUUID sid = formResource.getStructureId();
    //String structureIdString = sid.getStringValue();
    
    // The fields to list in this view. (The table columns.) E.g. "first_name", "last_name", "email"
    List listFields = Arrays.asList(fieldNamesView.split("\\|"));
    
    // The (multiple choice) field(s) to create public filters for. E.g. "speciality", "theme"
    List filterFields = new ArrayList(Arrays.asList( fieldNamesFilter.split("\\|")));
    
    // The (multiple choice) field(s) to create "admin" filters for.
    // These filters should not be visible to the public, only to logged-in, privileged users. E.g. "panel"
    List adminFilterFields = new ArrayList(Arrays.asList( fieldNamesAdminFilter.split("\\|") ));
    
    // For "admins", add the admin filter fields (if any) to the list of filters
    if (userIsVfsManager) {
        filterFields.addAll(adminFilterFields);
    }
    
    // Get a map containing only the filter parameters
    Map filterMap = FilterUtils.retainOnlyFilterKeys(request.getParameterMap(), filterFields);
    
    
    List<String> rows = new ArrayList<String>(); // List to hold the data to display (the rows of a table)
    //List<String> ids = new ArrayList<String>(); // List to hold the IDs of the entries to display
    
    // During HTML output, this array is used to ensure correct order
    //String[] tableHeaders = new String[listFields.size()];
    String[] tableRow = new String[listFields.size()];
    
    
    // Create the Form instance using the form file's path in the VFS
    Form f = null;
    try {
        f = new Form(formPath, cmso);
        table = f.getTableName();
    } catch (Exception e) {
        // We can't continue, the form instance is vital
        out.println("<p><em>" + LABEL_UNABLE_TO_CREATE_FORM + ".</em></p>");
    }
    
    
    // Compose query to get the field (column) names, as stored in the database
    String columnNamesQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.Columns WHERE TABLE_NAME='" + table + "'";
    
    // Compose query to get the values (rows) stored in the databases, for the specified fields
    String tableDataQuery = "SELECT ";
    tableDataQuery += "*";
    tableDataQuery += " FROM `" + table + "`";
    
    if (currentSort != null && !currentSort.isEmpty()) {
        tableDataQuery += " ORDER BY `" + currentSort + "`";
        if (descending) {
            tableDataQuery += " DESC";
        }
    } else if (CmsAgent.elementExists(defaultOrderField)) {
        tableDataQuery += " ORDER BY `" + defaultOrderField + "`";
    }
    
    // Query to get the total number of rows
    String idCountQuery = "SELECT COUNT(id) FROM `" + table + "`";
    
    //out.println("<h3>Form file</h3><pre>" + formPath + "</pre>");
    //out.println("<h3>Structure ID</h3><pre>" + cmso.readResource(formPath).getStructureId() + "</pre>");
    //out.println("<h3>Resource ID</h3><pre>" + cmso.readResource(formPath).getResourceId() + "</pre>");
    //out.println("<h3>Table data query</h3><pre>" + tableDataQuery + "</pre>");
    
    //*
    SQLAgent sqlAgent = new SQLAgent(cmso);

    ResultSet rs = null;
    /*
    rs = sqlAgent.doSelect(idCountQuery);
    if (rs.first()) {
        out.println("<p>" + LABEL_TABLE + ": <code>" + table + "</code><br />");
        out.println(LABEL_ROWS + ": <code>" + rs.getString(1) + "</code></p>");
        out.println("<p>" + rs.getString(1) + " scientists found:</p>");
    }
    //*/
    
    // Query to get the column names
    rs = sqlAgent.doSelect(columnNamesQuery);
    /*
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder        = factory.newDocumentBuilder();
    Document doc                   = builder.newDocument();
    Element results = doc.createElement("Results");
    doc.appendChild(results);
    */
    // Will hold ALL field (column) names (not just those in the configured view)
    ArrayList allColumnNames = new ArrayList();
    // Will hold only the column (field) names of the columns that should be displayed in the result list
    //ArrayList listColumnNames = new ArrayList(); 

    
    //--------------------------------------------------------------------------
    // Table headers
    //
    
    String th = "";
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            // Get the column name
            String columnName = rs.getString(1);
            // Keep the column name in the list of ALL column names
            allColumnNames.add(columnName);
            // Display this field?
            if (listFields.contains(columnName)) {                
                // Initially use the real table column name (the "techy" name)
                String rowContent = columnName;
                // Then see if the form has a "nice" name for this column
                I_FormInputElement element = f.getElement(columnName);
                // If getElement() retured null, the form has no such element. 
                // (Could be a db-only field like "ID" or "time last modified", 
                // or a field that once existed but has since been removed.)
                if (element != null) {
                    // A "nice" name for this column was found, use it instead
                    rowContent = f.getElement(columnName).getLabel();
                }
                // Store the column name at the proper index
                tableRow[listFields.indexOf(columnName)] = rowContent;
            }
            rs.next();
        }
        // All column names needed should now be present; convert to a CSV line
        th = arrayAsCsvLine(tableRow);
    }
    // Done with table headers
    
    
    
    //--------------------------------------------------------------------------
    // Table rows / content
    //
    
    
    
    rs = sqlAgent.doSelect(tableDataQuery);
    /*
    ResultSet xmlResults = sqlAgent.doSelect(tableDataQuery);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    */
    //*
    String fieldName = null;
    String fieldValue = null;
    
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            // The editor may check individual rows in the result list. If any 
            // rows were checked, we want only those rows; so determine whether 
            // the current row was checked or not
            boolean processThisRow = true;
            String id = rs.getString("id");
            if (!checkedIds.isEmpty()) {
                if (!checkedIds.contains(id))
                    processThisRow = false; // Not checked, make the flag false ("Don't add this row to result list")
            }
            //String html = "<tr>";
            //out.print("<tr>");
            
            if (processThisRow) {
                // Get a filter matcher object. We'll use this to match against all filters (if any)
                FilterMatcher filterMatcher = new FilterMatcher(filterMap);
                // XML element that will represent this row
                //Element row = doc.createElement("Row");;
                
                // We must iterate on ALL columns, not just the columns we're 
                // displaying, because there could be filter options for columns
                // that are not displayed.
                for (int i = 0; i < allColumnNames.size(); i++) {
                    fieldName = (String)allColumnNames.get(i);
                    fieldValue = rs.getString(fieldName);

                    // Check for filters (fetched from URL parameters - i.e. "speciality" filter: "specialty=glacier&speciality=frozen_ground")
                    if (filterMatcher.isMatcherFor(fieldName)) { // if (there is an active filter on this form field)                        
                        if (f.getElement(fieldName).isMultipleChoice()) { // Multiple filter values possible (need to evaluate each one separately)
                            // Get a list containing the current row's stored values for this field
                            if (fieldValue != null && !fieldValue.isEmpty()) { // Require that the current row has at least one stored value in this field
                                // Get the individual values by splitting on the "pipe" delimiter character
                                List fieldValues = Arrays.asList(fieldValue.split("\\|"));
                                // Check if any of the stored values matches any of the filter values
                                Iterator iFieldValues = fieldValues.iterator();
                                while (iFieldValues.hasNext()) {
                                    String singleFieldValue = (String)iFieldValues.next();
                                    //out.print("<h5>Checking if '" + singleFieldValue + "' is match on '" + fieldName + "' ... ");
                                    if (filterMatcher.match(fieldName, singleFieldValue)) {
                                        // Found a match on this filter, no need to continue
                                        break;
                                    }
                                }
                            }
                        } 
                        // Singular value, check if the stored value (if any) matches the given filter value
                        else {
                            //out.print("<h5>Checking if '" + fieldValue + "' is match on '" + fieldName + "' ... ");
                            filterMatcher.match(fieldName, fieldValue);
                        }
                    }
                    
                    // Create this "data cell". (We cannot at this point determine 
                    // if we've got a match or not - a match may be triggered on
                    // a cell that has not yet been visited.)
                    if (listFields.contains(fieldName)) {
                        String rowContent = "";
                        rowContent = getDisplayValue(f, fieldName, fieldValue, exportFormat.equalsIgnoreCase(FILE_TYPE_XLS) ? "{{n}}" : null); // {{n}} indicates new line
                        /*
                        if (fieldValue == null) {
                            rowContent += "";
                        }

                        else if (!fieldValue.isEmpty()) {
                            I_FormInputElement formElement = f.getElement(fieldName);
                            if (formElement == null || (formElement != null && !formElement.isMultipleChoice())) { // Form does not contain this field (like ID)
                                rowContent += fieldValue;
                            }
                            else {
                                try {
                                    if (formElement.isMultipleChoice()) {
                                        List fieldValues = Arrays.asList(fieldValue.split("\\|"));
                                        Iterator iFieldValues = fieldValues.iterator();
                                        while (iFieldValues.hasNext()) {
                                            String optionValue = (String)iFieldValues.next();
                                            rowContent += formElement.getTextForOption(optionValue);
                                            if (iFieldValues.hasNext()) {
                                                rowContent += ", ";
                                            }
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
                        */
                        tableRow[listFields.indexOf(fieldName)] = rowContent;
                        /*
                        // Now create the XML nodes
                        Element node = doc.createElement(fieldName);
                        String value = fieldValue;
                        node.appendChild(doc.createTextNode(value.toString()));
                        row.appendChild(node);
                        */
                    }
                }
                
                // Include this row, but only if it matches any given filter OR no filters are present
                if (filterMatcher.isMatch()) {               
                    rows.add(arrayAsCsvLine(tableRow));
                    matchIds.add(id); // add this ID to the list of matching rows
                    //results.appendChild(row);
                } 
                /*
                else {
                    xmlResults.deleteRow();
                }
                */
            }
            // Done with this row, continue to the next one
            rs.next();
            //xmlResults.next();
        } // while (we're not at the table's last row)
        
        //
        // The "rows" variable now contains all the rows from the table; each 
        // list item in "rows" is a single CSV line.
        //
        
        
        
        
               
        if (!rows.isEmpty()) {
            if (exportFormat.equals(FILE_TYPE_XML)) {
                //out.println("<pre>");
                
                // XML processing
                String xmlDataQuery = "SELECT ";
                Iterator<String> iListFields = listFields.iterator();
                while (iListFields.hasNext()) {
                    xmlDataQuery += iListFields.next();
                    if (iListFields.hasNext())
                        xmlDataQuery += ", ";
                }
                xmlDataQuery += " FROM `" + table + "`";
                xmlDataQuery += " WHERE id IN (";
                Iterator<String> iMatchIds = matchIds.iterator();
                while (iMatchIds.hasNext()) {
                    xmlDataQuery += iMatchIds.next();
                    if (iMatchIds.hasNext())
                        xmlDataQuery += ", ";
                }
                xmlDataQuery += ")";
                if (currentSort != null && !currentSort.isEmpty()) {
                    xmlDataQuery += " ORDER BY `" + currentSort + "`";
                    if (descending) {
                        xmlDataQuery += " DESC";
                    }
                } else if (CmsAgent.elementExists(defaultOrderField)) {
                    xmlDataQuery += " ORDER BY `" + defaultOrderField + "`";
                }
                //out.println("Query: " + xmlDataQuery);
                try {
                    ResultSet xmlResults = sqlAgent.doSelect(xmlDataQuery);
                    Document xml = toDocumentNew(xmlResults, f);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    printDocument(xml, bos);
                    //out.println(bos.toString("UTF-8"));
                    byte[] rawContent = bos.toByteArray();
                    
                    cms.setContentType("application/xml;charset=UTF-8");
                    response.setContentType("application/xml;charset=UTF-8");
                    response.setContentLength(rawContent.length);
                    response.setHeader("Content-Disposition","attachment; filename=\"" 
                            + CmsHtmlExtractor.extractText(viewTitle, "UTF-8").toLowerCase().replaceAll(" ", "-") 
                            + ".xml\"");
                    response.getOutputStream().write(rawContent, 0, rawContent.length);
                    response.getOutputStream().flush();
                } catch (Exception e) {
                    out.println("<h4>An error occured while generating the XML file.</h4>");
                    if (userIsVfsManager) {
                        out.println("Query: " + xmlDataQuery);
                        e.printStackTrace(response.getWriter());
                    }
                }
                //out.println("</pre>");
            } else {
                byte[] rawContent = null;
                // Create the CSV content
                String csvContent = "";
                // Add the column names line (the "table headers" row) to the CSV file
                csvContent += th;
                // Add all the data lines (the "table data" rows) to the CSV file
                Iterator<String> iRows = rows.iterator();
                while (iRows.hasNext()) {
                    csvContent += iRows.next();
                }
                    
                if (exportFormat.equals(FILE_TYPE_CSV)) {
                    /*
                    String csvContent = "";

                    //out.println("<table class=\"scientist-dir odd-even-table\" border=\"0\" cellpadding=\"0\" cellspacing=\"2\">\n<tr>");
                    //out.println("<tr>" + th + "</tr>");

                    // Add the column names line (the "table headers" row) to the CSV file
                    csvContent += th;
                    // Add all the data lines (the "table data" rows) to the CSV file
                    Iterator<String> iRows = rows.iterator();
                    while (iRows.hasNext()) {
                        csvContent += iRows.next();
                    }
                    //out.println("</table>");
                    //*/
                    
                    // Convert the CSV to a byte array, using UTF-8 encoding.
                    rawContent = csvContent.getBytes(Charset.forName("UTF-8"));

                    //response.setContentType("application/csv;charset=utf-8");
                    //response.setContentType("application/csv;charset=Unicode");
                    //response.setContentType("text/csv;charset=UTF-8");
                    //response.setContentType("application/unknown"); //this also works

                    //response.setContentType("application/x-msexcel;charset=UTF-8");
                    cms.setContentType("text/csv;charset=UTF-8");
                    response.setContentType("text/csv;charset=UTF-8");
                    /*
                    response.setContentLength(rawContent.length);

                    // set the file name to whatever required..
                    //response.setHeader("Content-Disposition","attachment; filename=\"data.csv\""); 
                    response.setHeader("Content-Disposition","attachment; filename=\"" 
                            + CmsHtmlExtractor.extractText(viewTitle, "UTF-8").toLowerCase().replaceAll(" ", "-") 
                            + ".csv\"");

                    //response.getOutputStream().write('\ufeff'); // (not working) byte-order marker (BOM) to identify the CSV file as a Unicode file - Needed for Excel to know this file is UTF-8-encoded

                    response.getOutputStream().write(rawContent, 0, rawContent.length);
                    response.getOutputStream().flush();
                    //*/
                } else if (exportFormat.equals(FILE_TYPE_XLS)) {
                    try {
                        // Create the Excel content, using the CSV content as base
                        HSSFWorkbook workbook = new HSSFWorkbook();
                        HSSFSheet sheet = workbook.createSheet("Data");
                        
                        String currentLine = null;
                        int rowNum = 0;
                        int columns = 0;
                        
                        BufferedReader br = new BufferedReader(new StringReader(csvContent));
                        while ((currentLine = br.readLine()) != null) {
                            rowNum++;
                            HSSFRow row = sheet.createRow(rowNum);

                            // Complex but necessary regex...
                            // See http://stackoverflow.com/questions/15738918/splitting-a-csv-file-with-quotes-as-text-delimiter-using-string-split
                            String csvParts[] = currentLine.split(";(?=([^\"]|\"[^\"]*\")*$)");;
                            for (int i = 0; i < csvParts.length; i++) {
                                HSSFCell cell = row.createCell(i);
                                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                // Wrap text in the cell
                                try {
                                    CellStyle style = workbook.createCellStyle();
                                    style.setWrapText(true);
                                    cell.setCellStyle(style);
                                    row.setRowStyle(style);
                                } catch (Exception ignore) {}
                                
                                String csvPart = csvParts[i];
                                if (csvPart.startsWith("\"") && csvPart.endsWith("\"")) {
                                    // Remove quotes (used for CSV escaping)
                                    csvPart = csvPart.substring(1, csvPart.length()-2);
                                }
                                try {
                                    cell.setCellValue(csvPart.replaceAll("\\{\\{n\\}\\}", "\n").replaceAll("\\\"\\\"", "\""));
                                } catch (Exception e) {
                                    cell.setCellValue("");
                                }
                                
                                if (i > columns) {
                                    columns = i;
                                }
                            }
                        }
                        
                        // Set automatic width on all columns
                        for (int i = 0; i < columns; i++) {
                            sheet.autoSizeColumn(i);
                        }

                        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
                        workbook.write(outByteStream);
                        rawContent = outByteStream.toByteArray();

                        // Write the response
                        HttpServletResponse tlResponse = org.opencms.flex.CmsFlexController.getController(request).getTopResponse();
                        tlResponse.setContentType("application/ms-excel;charset=UTF-8");
                        cms.setContentType("application/ms-excel;charset=UTF-8");
                        response.setContentType("application/ms-excel;charset=UTF-8");
                        /*
                        response.setContentLength(rawContent.length);
                        response.setHeader("Content-Disposition","attachment; filename=\"" 
                                + CmsHtmlExtractor.extractText(viewTitle, "UTF-8").toLowerCase().replaceAll(" ", "-") 
                                + ".xls\"");
                        response.getOutputStream().write(rawContent, 0, rawContent.length);
                        response.getOutputStream().flush();
                        //*/
                    } catch (Exception e) {
                        out.println("<p>Something went wrong during the file export: " + e.getMessage() + "</p>");
                    }
                }
                

                response.setContentLength(rawContent.length);

                //response.setHeader("Content-Disposition","attachment; filename=\"data.csv\""); // set the file name to whatever required..
                response.setHeader("Content-Disposition","attachment; filename=\"" 
                        + CmsHtmlExtractor.extractText(viewTitle, "UTF-8").toLowerCase().replaceAll(" ", "-") 
                        + "." + exportFormat + "\"");

                //response.getOutputStream().write('\ufeff'); // (not working) byte-order marker (BOM) to identify the CSV file as a Unicode file - Needed for Excel to know this file is UTF-8-encoded

                response.getOutputStream().write(rawContent, 0, rawContent.length);
                response.getOutputStream().flush();
            }
        } else {
            out.println("<p>Sorry, no matches found.</p>");
        }
    } // if (the table contained at least 1 row)
    else {
        out.print("<em>" + LABEL_NO_DATA + ".</em>");
    }
    //*/
}
%>