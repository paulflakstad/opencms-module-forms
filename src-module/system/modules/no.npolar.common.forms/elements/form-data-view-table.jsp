<%-- 
    Document   : form-data-view.jsp
    Created on : 19.jan.2009, 16:50:13
    Author     : Paul-Inge Flakstad, Norwegian Polar Institute
    Description: Provides a raw view of the data collected through a form.
--%><%@page import="no.npolar.util.*"
%><%@page import="no.npolar.common.forms.*"
%><%@page import="java.util.*"
%><%@page import="java.sql.*"
%><%@page import="org.opencms.main.*"
%><%@page import="org.opencms.security.CmsRoleManager"
%><%@page import="org.opencms.security.CmsRole"
%><%@page import="org.opencms.util.CmsUUID"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="org.opencms.jsp.*"
%><%@page import="org.opencms.file.*"
%><%@page import="org.opencms.file.types.*"
%><%@page import="org.opencms.xml.content.*"
%><%@page import="org.opencms.xml.types.*" session="true" 
%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Form data</title>
    </head>
    <body>
<%
CmsAgent cms = new CmsAgent(pageContext, request, response);
CmsObject cmso = cms.getCmsObject();

String table = request.getParameter("table")== null ? "" : request.getParameter("table");;
String currentSort = request.getParameter("sort") == null ? "" : request.getParameter("sort");
boolean descending = request.getParameter("order") == null ? false : (request.getParameter("order").equals("desc") ? true : false);

//CmsRoleManager roleManager  = OpenCms.getRoleManager();
final boolean USER_IS_VFS_MANAGER = OpenCms.getRoleManager().hasRole(cms.getCmsObject(), CmsRole.VFS_MANAGER);

final String DELETED_FORM = "DELETED FORM";
final String LABEL_FORM_DATA_FOR = "Form data for";
final String LABEL_UNABLE_TO_CREATE_FORM = "Unable to create Form instance";
final String LABEL_TABLE = "Table";
final String LABEL_ROWS = "Rows total";
final String LABEL_NO_DATA = "No data has been submitted through this form";
final String LABEL_RESTRICTED = "Access restricted";
final String LABEL_REQUIRE_VFS_MANAGER = "To view this file, you need to be logged in as VFS manager.";
final String LABEL_SUGGEST_LOGIN = "Please log in, or contact your system administrator for help.";

final boolean DEBUG = false;

if (USER_IS_VFS_MANAGER) {
    String columnNamesQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.Columns where TABLE_NAME='" + table + "'";
    String tableDataQuery = "SELECT * from `" + table + "`";
    String idCountQuery = "SELECT COUNT(id) from `" + table + "`";

    if (request.getParameter("sort") != null) {
        tableDataQuery += " ORDER BY `" + currentSort + "`";
        if (descending) {
            tableDataQuery += " DESC";
        }
    }

    String structureIdString = table.replaceAll("_", "-");
    CmsUUID sid = CmsUUID.valueOf(structureIdString);
    
    CmsResource formResource = null;
    String formName = null;
    
    try {
        formResource = cmso.readResource(sid);
        formName = cmso.readPropertyObject(formResource, "Title", false).getValue();
    } catch (CmsVfsResourceNotFoundException e) {
        formName = DELETED_FORM;
    }

    out.println("<h2>" + LABEL_FORM_DATA_FOR + " " + formName + "</h2>");
    
    
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
        out.println("<p>" + LABEL_TABLE + ": <code>" + table + "</code><br />");
        out.println(LABEL_ROWS + ": <code>" + rs.getString(1) + "</code></p>");
    }

    rs = sqlAgent.doSelect(columnNamesQuery);

    out.print("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\"><tr>");
    ArrayList columnNames = new ArrayList();
    String th = null;
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            String columnName = rs.getString(1);
            I_FormInputElement element = f.getElement(columnName);
            
            columnNames.add(columnName);
            // Create the table header. This will be a link to sort the data by this field.
            th = "<th><a href=\"form-data-view.jsp?" +
                    "table=" + table + "&amp;sort=" + columnName;
            // If the current view is sorted by this field, create a link that swaps the order (ascending / descending)
            if (columnName.compareTo(currentSort) == 0 && !descending) {
                th += "&amp;order=desc";
            }
            th += "\">";
            if (element != null) {
                th += f.getElement(columnName).getLabel() + "</a><br/>" +
                    "<span style=\"font-size:0.8em;\">" + columnName + "</span>";
            }
            else {
                th += columnName + "</a>";
            }
            th += "</th>";
            
            out.print(th);
            rs.next();
        }
    }
    out.print("</tr>");

    rs = sqlAgent.doSelect(tableDataQuery);
    String fieldVal = null;
    if (rs.first()) {
        while (!rs.isAfterLast()) {
            out.print("<tr>");
            for (int i = 0; i < columnNames.size(); i++) {
                fieldVal = rs.getString((String)columnNames.get(i));
                out.println("<td>" + (fieldVal != null ? fieldVal.replace("|", ", ") : "<i>NULL</i>") + "</td>");
            }
            rs.next();
            out.print("</tr>");
        }
        out.print("</table>");
    }
    else {
        out.print("<em>" + LABEL_NO_DATA + ".</em>");
    }
} else {
    out.print("<h2 class=\"error\">" + LABEL_RESTRICTED + "</h2>" +
            "<p>" + LABEL_REQUIRE_VFS_MANAGER + "</p>" + 
            "<p>" + LABEL_SUGGEST_LOGIN + "</p>");
}
%>
    </body>
</html>
