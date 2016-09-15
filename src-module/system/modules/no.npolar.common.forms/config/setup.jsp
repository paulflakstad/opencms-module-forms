<%@ page import="org.opencms.security.CmsRole"%>
<%@ page import="org.opencms.main.OpenCms"%>
<%@ page import="org.opencms.file.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="no.npolar.util.*" %>
<%@ page import="no.npolar.common.forms.*" %>
<%@ page import="javax.xml.parsers.*" %>
<%@ page import="javax.xml.transform.dom.*" %>
<%@ page import="javax.xml.transform.stream.*" %>
<%@ page import="javax.xml.transform.Result" %>
<%@ page import="javax.xml.transform.Transformer" %>
<%@ page import="javax.xml.transform.TransformerFactory" %>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="org.xml.sax.*" %>
<%@ page import="org.opencms.main.CmsException" %>
<%@ page import="org.opencms.xml.content.*" %>
<%@ page import="org.opencms.xml.page.*" %>
<%@ page import="org.opencms.xml.types.*" %>
<%@ page import="org.opencms.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Form module setup</title>
    </head>
    <body style="background-color:#aaa;">
        <%
        CmsAgent cms = new CmsAgent(pageContext, request, response);
        CmsObject cmso = cms.getCmsObject();
        
        // Permission check
        if (!OpenCms.getRoleManager().hasRole(cmso, CmsRole.ADMINISTRATOR)) {
            out.println("<h1>Access denied</h1>");
            out.println("<p>Sorry, only administrators are permitted to access this file.</p>");
            out.println("</body>");
            out.println("</html>");
            return;
        }
        
        CmsFile configFile = null;
        final String CONFIG_FILE_PATH = SQLAgent.CONFIG_FILE_PATH;// "/system/modules/no.npolar.common.forms/config/opencms-forms.xml";
        
        // Show form (no submitted value)
        if (request.getParameter("submit") == null) {
            %>
                <div style="border:1px solid orange; background-color:#eee; color:#333; width:600px; padding:1em 5em 5em 5em; margin:2em auto;">
                    <h2 style="border-bottom:1px dotted #ccc;">Forms module setup</h2>
                    <p style="font-size:1.1em; font-weight:bold;">
                        Please take a minute to provide the necessary MySQL database details. In order for the forms module to work, a configuration 
                        file must be created. Submitting this form will generate a configuration file for you.
                    </p>
                    <p>
                        The root username and password details are needed only this one time, to complete the initial setup process, 
                        <strong>this information will not be stored</strong>.
                    </p>
                    <p>
                        If you prefer, the MySQL database and user that the forms module should use can be manually created. 
                        In this case, the forms module configuration file must also be edited manually.
                    </p>
                    <p>
                        The configuration file is found in the forms module folder, <code><%= SQLAgent.CONFIG_FOLDER_PATH %></code>. 
                        It is named <code><%= SQLAgent.CONFIG_FILE_NAME %></code> and <strong>it should always be flagged as "internal"</strong>.
                    </p>
                    <div style="border:1px solid grey; background-color:#fff; padding:1em;">
                        <form name="form-setup" method="post" action="setup.jsp">
                            <table border="0" cellpadding="4" cellspacing="0">
                                <tr><td>MySQL server host</td><td><input type="text" name="host" value="localhost" /></td></tr>
                                <tr><td>MySQL server port number (default is 3306)</td><td><input type="text" name="port" value="3306" /></td></tr>
                                <tr><td>MySQL root username</td><td><input type="text" name="rootuser" value="root" /></td></tr>
                                <tr><td>MySQL root password</td><td><input type="text" name="rootpass"/></td></tr>
                                <tr><td>Name to use for forms database and user</td><td><input type="text" name="preferredname" value="forms_opencms" /></td></tr>
                                <tr><td>Password to use for database user</td><td><input type="text" name="preferredpassword" value="" /></td></tr>
                                <tr><td><input type="submit" name="submit" value="Submit"/></td><td></td></tr>
                            </table>
                        </form>
                    </div>
                </div>
            <%
        }
        // Handle submitted form
        else {   
            String host = request.getParameter("host");
            String port = request.getParameter("port");
            String rootuser = request.getParameter("rootuser");
            String rootpass = request.getParameter("rootpass");
            String preferredname = request.getParameter("preferredname");
            String preferredpassword = request.getParameter("preferredpassword");
            if (host == null || port == null || rootuser == null || rootpass == null || preferredname == null || preferredpassword == null) {
                throw new NullPointerException("A form field was missing. Press return and complete the form.");
            }
            
            SQLAgent sqlAgent = null;
            
            try {
                sqlAgent = new SQLAgent(host, port, rootuser, rootpass);
            } catch (Exception e) {
                throw new Exception("Setup Failed! Could not establish a connection to the database using the provided information.");
            }
            
            if (sqlAgent != null) {
                //try {
                    ArrayList statements = new ArrayList(4);
                    
                    statements.add("CREATE USER '" + preferredname + "'@'" + host + "' IDENTIFIED BY '" + preferredpassword + "';");
                    //statements.add("GRANT USAGE ON *.* TO '" + preferredname + "'@'" + host + "' IDENTIFIED BY '" + preferredpassword + "';");
                                    //"WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0; ";
                    statements.add("CREATE DATABASE IF NOT EXISTS `" + preferredname + "`;");
                    statements.add("GRANT ALL PRIVILEGES ON `" + preferredname + "`.* TO '" + preferredname + "'@'" + host + "';");
                    
                    Iterator itr = statements.iterator();
                    java.sql.PreparedStatement ps = null;
                    
                    while (itr.hasNext()) {
                        ps = sqlAgent.getConnection().prepareStatement((String)itr.next());
                        ps.execute();
                    }
                    
                /*} catch (Exception e) {
                    throw new Exception("<h2>Setup Failed</h2><p>The neccessary SQL statements could not be executed.</p>");
                }*/
                
                Document doc = null;
                byte[] editedContent = null;
                cmso.getRequestContext().setCurrentProject(cmso.readProject("Offline"));                
                configFile = cmso.readFile(cmso.readResource(CONFIG_FILE_PATH));

                if (configFile != null) {
                    try {
                        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                        doc = docBuilder.parse(new ByteArrayInputStream(configFile.getContents()));
                        // normalize text representation
                        doc.getDocumentElement().normalize();
                    } catch (Exception e) {
                        throw new Exception("Setup Failed! Unable to parse the config file.");
                    }
                    
                    try {
                        // Get Nodes from the XML config file
                        doc.getElementsByTagName("host").item(0).getFirstChild().setNodeValue(host);
                        doc.getElementsByTagName("port").item(0).getFirstChild().setNodeValue(port);
                        doc.getElementsByTagName("name").item(0).getFirstChild().setNodeValue(preferredname);
                        doc.getElementsByTagName("username").item(0).getFirstChild().setNodeValue(preferredname);
                        doc.getElementsByTagName("password").item(0).getFirstChild().setNodeValue(preferredpassword);
                        // Get a byte[] of the updated XML file, and write it to the VFS
                        DOMSource dom = new DOMSource(doc.getDocumentElement());
                        StringWriter stringWriter = new StringWriter();
                        Result result = new StreamResult(stringWriter);
                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();
                        transformer.transform(dom, result);
                        editedContent = stringWriter.getBuffer().toString().getBytes();
                    } catch (Exception e) {
                        throw new Exception("Setup failed! Unable to edit the XML content of the config file.");
                    }
                    
                    try {
                        cmso.lockResource(CONFIG_FILE_PATH);
                        configFile.setContents(editedContent);
                        cmso.writeFile(configFile);
                        // ToDo: Should flag config file as internal at this point
                        cmso.unlockResource(CONFIG_FILE_PATH);
                        //org.opencms.main.OpenCms.getPublishManager().publishResource(cmso, CONFIG_FILE_PATH);//cms.getRequestContext().removeSiteRoot(configFile.getRootPath()));

                        out.println("<h2>Setup complete!</h2>" + 
                                "<p>The database is now ready for use.</p>"
                                + "<p style=\"font-weight:bold;\">Please make sure that the config file is flagged as internal immediately.</p>");
                    } catch (Exception e) {
                        throw new Exception("Setup failed! A possible reason may be that the config file is locked.");
                    }
                }
                else { // No config-file found (must have been deleted)
                    //out.println("<h2>Setup failed!</h2>Could not locate the config file.<p>Please check your module import, try again.</p>");
                    throw new Exception("Setup failed! Could not locate the config file. Please check your module import, try again.");
                }
            }
        }
        %>
    </body>
</html>
