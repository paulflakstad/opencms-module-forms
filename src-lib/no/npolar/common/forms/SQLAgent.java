package no.npolar.common.forms;

import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsFile;
import org.w3c.dom.*;

/**
 * Used for accessing and interacting with the database.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class SQLAgent {
    private String usr = null;
    private String pwd = null;
    private String url = null;
    private String db = null;
    private Connection conn = null;
    private Statement statement = null;
    private ResultSet rs = null;
    
    /**
     * The path to the configuration folder.
     * <p>
     * This folder contains a configuration file that the forms module needs in 
     * order to work properly. It also contains helper files and a readme file 
     * that has more details.
     */
    public static final String CONFIG_FOLDER_PATH = "/system/modules/no.npolar.common.forms/config/";
    
    /**
     * The name of the configuration file.
     * <p>
     * For details on this file, see the readme file in the configuration folder, 
     * {@link #CONFIG_FOLDER_PATH}.
     */
    public static final String CONFIG_FILE_NAME = "opencms-forms.xml";
    
    /**
     * The path to the configuration file.
     * <p>
     * For details on this file, see the readme file in the configuration folder, 
     * {@link #CONFIG_FOLDER_PATH}.
     */
    public static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH.concat(CONFIG_FILE_NAME);
    
    /*
    public SQLAgent() throws InstantiationException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            if (usr == null || pwd == null || url == null || db == null) {
                throw new NullPointerException("Forms module: Database connection information (hostname, port, database name, username, password) contained a null pointer.");
            }
            conn = DriverManager.getConnection(url.concat(db), usr, pwd);
            statement = conn.createStatement();
        }
        catch (ClassNotFoundException cnfe) {
            throw new NullPointerException("Class not found: com.mysql.jdbc.Driver");
        }
        catch (SQLException sqle) {
            throw new NullPointerException("SQLException caught: " + sqle.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
    
    /**
     * Creates a new SQLAgent instance, based on the details in the config file.
     * 
     * @param cmso Initialized CmsObject needed to access the CMS' virtual file system.
     * @throws InstantiationException
     * @see #CONFIG_FILE_PATH
     */
    public SQLAgent(CmsObject cmso) throws InstantiationException {
        //this.cms = cms;
        if (!readConfig(cmso)) {
            throw new InstantiationException("Forms module: Null pointer encountered while reading MySQL config file.");
        }
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            if (usr == null || pwd == null || url == null || db == null) {
                throw new NullPointerException("Forms module: Database connection information (hostname, port, database name, username, password) contained a null pointer.");
            }
            conn = DriverManager.getConnection(url.concat(db), usr, pwd);
            statement = conn.createStatement();
        }
        catch (ClassNotFoundException cnfe) {
            throw new NullPointerException("Class not found: com.mysql.jdbc.Driver");
        }
        catch (SQLException sqle) {
            throw new NullPointerException("SQLException caught: " + sqle.getMessage());
        }
        catch (Exception e) {
            throw new InstantiationException("Unable to construct SQLAgent instance.");
        }
    }
    
    /**
     * Creates a new SQLAgent instance, based on the given details.
     * 
     * @param host The host name.
     * @param port The port number.
     * @param user The user name.
     * @param pass The user's password.
     * @throws IllegalArgumentException 
     */
    public SQLAgent(String host, String port, String user, String pass) throws IllegalArgumentException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/", user, pass);
            if (conn == null) {
                throw new SQLException("Connection failed on jdbc:mysql//" + host + ":" + port + "/" + " using user '" + user + "' and password '" + pass + "'");
            }
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("Could not find any class 'com.mysql.jdbc.Driver': " + cnfe.getMessage());
        } catch (InstantiationException ie) {
            throw new IllegalArgumentException("Could not instanciate jdbc driver: " + ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Illegal access when attempting to instanciate jdbc driver: " + iae.getMessage());
        } catch (SQLException sqle) {
            throw new IllegalArgumentException("Could not establish a connection to the database at " + host + ":" + port + " with the supplied credentials: " + sqle.getMessage());
        }
    }
    
    /**
     * Executes a select query.
     * 
     * @param q The SQL query.
     * @return The result set produced by the query.
     * @throws SQLException 
     */
    public ResultSet doSelect(String q) throws SQLException {
        rs = statement.executeQuery(q);
        return rs;
    }
    
    /**
     * Executes an SQL statement that modifies the table, like an 
     * <code>ALTER</code> or <code>UPDATE</code>.
     * <p>
     * The return indicates the form of the first result. You must then use the 
     * methods {@link #getResultSet()} or {@link #getUpdateCount()} to retrieve 
     * the result. 
     * 
     * @param q the SQL statement to execute.
     * @return <code>true</code> if the first result is a ResultSet object; <code>false</code> if it is an update count or there are no results 
     * @throws SQLException if a database access error occurs.
     * @see java.sql.Statement#execute(java.lang.String) 
     */
    public boolean doManip(String q) throws SQLException {
        boolean result = statement.execute(q);
        rs = statement.getResultSet();
        return result;
    }
    
    /**
     * Gets the current result set for this instance, as set by the last call to
     * {@link #doSelect(java.lang.String)} or {@link #doManip(java.lang.String)}.
     * 
     * @return The current result set for this instance.
     */
    public ResultSet getResultSet() {
        return rs;
    }
    
    /**
     * Gets the update count for this instance's statement.
     * 
     * @return The update count for this instance's statement.
     * @throws SQLException 
     * @see Statement#getUpdateCount() 
     */
    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }
    
    /**
     * Gets the connection used by this instance.
     * 
     * @return The connection used by this instance.
     */
    public Connection getConnection() {
        return conn;
    }
    
    /**
     * Does nothing yet.
     */
    private void init() {
        // Read configuration from a VFS resource (config file)
    }
    
    /**
     * Gets the database name used by this instance.
     * 
     * @return The database name used by this instance.
     */
    public String getDatabaseName() { return this.db; }
    
    /**
     * Reads the configuration file.
     * 
     * @param cmso Initialized CmsObject, needed to access the CMS' virtual file system.
     * @return <code>true</code> if all went well, <code>false</code> if not.
     * @see #CONFIG_FILE_PATH
     */
    private boolean readConfig(CmsObject cmso) {
        Node n_dbHost, n_dbPort, n_dbName, n_usr, n_pwd;
        
        try {
            CmsFile configFile = cmso.readFile(cmso.readResource(CONFIG_FILE_PATH));
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(configFile.getContents()));

            // normalize text representation
            doc.getDocumentElement().normalize();
            // Get Nodes from the XML config file
            n_dbHost = doc.getElementsByTagName("host").item(0).getFirstChild();
            n_dbPort = doc.getElementsByTagName("port").item(0).getFirstChild();
            n_dbName = doc.getElementsByTagName("name").item(0).getFirstChild();
            n_usr = doc.getElementsByTagName("username").item(0).getFirstChild();
            n_pwd = doc.getElementsByTagName("password").item(0).getFirstChild();
        } catch (Exception e) { 
            return false; 
        }
            
        if (n_dbHost == null || n_dbPort == null || n_dbName == null || n_usr == null || n_pwd == null) {
            throw new NullPointerException("Configuration incomplete: Missing host, port, name, username, or password.");
        }

        this.db = n_dbName.getNodeValue();
        this.url = "jdbc:mysql://".concat(n_dbHost.getNodeValue()).concat(":").concat(n_dbPort.getNodeValue()).concat("/");
        this.usr = n_usr.getNodeValue();
        this.pwd = n_pwd.getNodeValue();
        return true;
    }
}