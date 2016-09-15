package no.npolar.common.forms;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.opencms.file.CmsObject;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Form database handler.
 * <p>
 * This class, along with {@link SQLAgent}, handles all database interactions; 
 * creating, updating forms, and so on.
 * 
 * @author Paul-Inge Flakstad, Norwegian Polar Institute
 */
public class FormSqlManager {
    
    /** Holds a reference to a form instance. */
    private Form form = null;
    
    /** Holds a CmsObject instance. */
    private CmsObject cmso = null;
    
    /** Holds the last statement executed. */
    private PreparedStatement lastStatement = null;
    
    /** The column name for the (automatically inserted) ID. */
    public static final String TABLE_COL_NAME_ID = "id";
    
    /** The column name for the (automatically inserted) "last modified" timestamp. */
    public static final String TABLE_COL_NAME_LAST_MODIFIED = "last_modified";
    
    /** The name of the column used as primary key. */
    public static final String TABLE_COL_NAME_PRIMARY_KEY = TABLE_COL_NAME_ID;
    
    /** The name of the engine used in "create table" statements. */
    public static final String DB_ENGINE = "InnoDB";
    
    /** Constant for the "create table" option. */
    public static final int SQL_CREATE_TABLE_STATEMENT = 0;
    
    /** Constant for the "insert into table" option. */
    public static final int SQL_INSERT_STATEMENT = 1;
    
    /** Constant for the "update table" option. */
    public static final int SQL_UPDATE_STATEMENT = 2;
    
    /** 
     * The default database field definitions for the different input types, 
     * indexed equal to I_FormInputElement's integer values for input types.
     */
    public static final String[] SQL_DEFINITIONS_FOR_INPUT_TYPES = { 
        "VARCHAR(512)",     // text
        "VARCHAR(2048)",    // textarea
        "VARCHAR(128)",     // select
        "VARCHAR(512)",     // checkbox
        "VARCHAR(128)",     // radio
        "DATETIME",         // datetime
        "VARCHAR(512)",     // country
        "VARCHAR(512)"      // password
    };
    
    /**
     * Creates a new manager instance for the given form, and using the given 
     * CMS object.
     * 
     * @param form The form.
     * @param cmso The initialized CMS object.
     */
    public FormSqlManager(Form form, CmsObject cmso) {
        this.form = form;
        this.cmso = cmso;
    }
    
    /**
     * Gets <em>N</em> rows from the form table, using a given field value.
     * 
     * @param field The field name to use
     * @param value The field value to use
     * @return N rows, as a result set
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     */
    protected ResultSet getEntryByFieldValue(String field, String value) throws SQLException, InstantiationException {
        SQLAgent sqlAgent = new SQLAgent(cmso);
        ResultSet rs = sqlAgent.doSelect("SELECT * FROM `" + form.getName() + "` " +
                "WHERE " + field + " LIKE '" + value + "' LIMIT 0,1;");
        return rs;
    }
    
    /**
     * Gets zero on one row from the form table, using the row ID.
     * 
     * @param id The row ID
     * @param cmso Initialized CmsObject
     * @return Zero or one row, as a result set
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     */
    protected ResultSet getSubmissionFromId(int id, CmsObject cmso) throws SQLException, InstantiationException {
        SQLAgent sqlAgent = new SQLAgent(cmso);
        ResultSet rs = sqlAgent.doSelect(
                "SELECT * FROM `" + this.form.getTableName() + "`" 
                        + " WHERE " + TABLE_COL_NAME_ID + "=" + id + ""
                        + " LIMIT 0,1" 
                        + ";"
        );
        
        return rs;
    }
    
    /**
     * Gets zero or one row from the form table, using a unique value.
     * 
     * @param uniqueName The unique field name
     * @param uniqueValue The unique value
     * @param cmso Initialized CmsObject
     * @return Zero or one row, as a result set
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     */
    protected ResultSet getSubmissionFromUnique(String uniqueName, String uniqueValue, CmsObject cmso) throws SQLException, InstantiationException {
        SQLAgent sqlAgent = new SQLAgent(cmso);
        ResultSet rs = sqlAgent.doSelect(
                "SELECT * FROM `" + this.form.getTableName() + "`"
                        + " WHERE " + uniqueName + " LIKE '" + uniqueValue + "'"
                        + " LIMIT 0,1"
                        + ";"
        );
        
        return rs;
    }
    
    /**
     * Gets all rows from the form table.
     * 
     * @param cmso Instantiated CmsObject
     * @return All rows (possibly zero) from the form table, as a result set
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     */
    protected ResultSet getSubmissions(CmsObject cmso) throws SQLException, InstantiationException {
        SQLAgent sqlAgent = new SQLAgent(cmso);
        ResultSet rs = sqlAgent.doSelect("SELECT * FROM `" + this.form.getTableName() + "`;");
        
        return rs;
    }
    
    /**
     * Checks if the form has an existing table in the database.
     * 
     * @param cmso Initialized CmsObject.
     * @return <code>true</code> if the form has an existing table, <code>false</code> if not.
     * @throws java.sql.SQLException If something goes wrong when querying the database
     * @throws java.lang.InstantiationException If the database connection agent cannot be created
     */
    protected boolean hasExistingTable(CmsObject cmso) throws SQLException, InstantiationException {
        SQLAgent sqlAgent = new SQLAgent(cmso);
        String statement = "SELECT COUNT(*) FROM information_schema.tables" 
                + " WHERE table_schema = '" + sqlAgent.getDatabaseName() + "'"
                + " AND table_name = '" + this.form.getTableName() + "'"
                + ";";
        ResultSet rs = sqlAgent.doSelect(statement);
        if (rs.first()) {
            if (rs.getInt(1) > 0) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    /**
     * Updates the database by executing SQL statements.
     * <p>
     * The <code>type</code> parameter is used to indicate which type of of 
     * statement (or operation) this update invocation is supposed to execute, 
     * and the given value should be one of the <code>SQL_XXXX_STATEMENT</code> 
     * constants of this class.
     * 
     * @param type The type of database update to perform, one of the <code>SQL_XXXX_STATEMENT</code> constants in {@link FormSqlManager this class}.
     * @return  The number of affected rows. A return value of -1 indicates an error that did not cause an exception to be thrown.
     * @throws java.sql.SQLException If something goes wrong.
     * @throws java.lang.InstantiationException If the {@link SQLAgent} instance fails to instantiate (typically because reading the configuration file failed).
     * @see SQLAgent#readConfig(org.opencms.file.CmsObject)
     * @see <a href="../../../../constant-values.html">Constant field values</a>
     */
    public synchronized int update(int type) throws SQLException, InstantiationException {
        SQLAgent sqlAgent               = null;
        try {
            sqlAgent                    = new SQLAgent(cmso);
        } catch (Exception e) {
            throw new InstantiationException("Could not create the SQLAgent object, no database transactions can be performed. Please check the configuration file.");
        }
        String statement                = null;
        PreparedStatement ps            = null;
        String tableName                = form.getTableName();
        //String value                    = null;
        Map formElements                = form.getElements();
        Set formElementsKeys            = formElements.keySet();
        Iterator iFormElementsKeys      = formElementsKeys.iterator();
        I_FormInputElement formElement  = null;
        int result                      = -1;
        int index                       = 0;
        //String[] submittedValues        = null;
        
        switch (type) {
            case SQL_CREATE_TABLE_STATEMENT:
                // Construct the "create table if not exists" statement
                statement = "CREATE TABLE IF NOT EXISTS `" + sqlAgent.getDatabaseName() + "`.`" + form.getTableName() + "` "; // Table
                statement += "(`" + TABLE_COL_NAME_ID + "` INT(64) NOT NULL AUTO_INCREMENT,"; // First field, the auto-incremented ID (will be the primary key for each row if no identifier is set)
                statement += " `" + TABLE_COL_NAME_LAST_MODIFIED + "` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"; // Second field, the time of the last update
                
                String colTypeDef = null; // Type definition for column
                while (iFormElementsKeys.hasNext()) {
                    // Use the form element to determine details about this column
                    formElement = (I_FormInputElement)(formElements.get(iFormElementsKeys.next()));
                    colTypeDef = String.valueOf(SQL_DEFINITIONS_FOR_INPUT_TYPES[formElement.getType()]);
                    // Create the statement part for this column / form element
                    // E.g. `last_name` VARCHAR(512) NOT NULL
                    statement += " `" + formElement.getName() + "` " + colTypeDef + "" 
                            + (formElement.isRequired() ? " NOT NULL" : "") 
                            + ", "; // Note: We always add this comma, even on the last iteration, because we'll continue building the statement will below
                }
                
                statement += "PRIMARY KEY (`" + TABLE_COL_NAME_PRIMARY_KEY + "`)"; 
                
                // Add unique field(s) if neccessary
                /*
                if (this.uniqueField != null)
                    statement += ", UNIQUE (`" + this.uniqueField + "`)";
                */
                if (form.hasUnique()) {
                    List ue = form.getUniqueElements();
                    Iterator ueItr = ue.iterator();
                    if (ue.size() > 1) { // Several unique elements
                        statement += ", CONSTRAINT uc_id UNIQUE ("; // ToDo!
                        while (ueItr.hasNext()) {
                            I_FormInputElement element = (I_FormInputElement)ueItr.next();
                            statement += "`" + element.getName() + "`";
                            if (ue.indexOf(element) != ue.size()-1) {
                                statement += ",";
                            }
                        }
                        statement += ")";
                    }
                    else { // Single unique element
                        statement += ", UNIQUE (`" + ((I_FormInputElement)ue.get(0)).getName() + "`)";
                    }
                }
                statement += ") ENGINE=" + DB_ENGINE;
                
                // If the form has a title, make it the comment for this table
                if (form.getTitle() != null && !form.getTitle().isEmpty()) {
                    String trimmedTitle = form.getTitle().trim();
                    if (trimmedTitle.length() > 0) {
                        statement += " COMMENT=?;";
                        ps = sqlAgent.getConnection().prepareStatement(statement);  // Prepare the statement
                        if (trimmedTitle.length() > 31)
                            ps.setString(1, trimmedTitle.substring(0, 32)); // Make sure that we don't set a table comment that is too long
                        else
                            ps.setString(1, trimmedTitle);
                    }
                } else {
                    statement += ";";
                    ps = sqlAgent.getConnection().prepareStatement(statement);  // Prepare the statement
                }
                result = ps.execute() == false ? 0 : -1; // Execute the statement - the execute() call should return false
                break;
                
                
            case SQL_INSERT_STATEMENT:
                // Right:
                //INSERT INTO `form_table` (`id`, `foo`) VALUES (NULL, 'bar') 
                // Wrong:
                //INSERT INTO `webforms`.`form_table` (`id`, `foo`) VALUES (NULL, 'bar');
                
                // Construct the "insert" statement, that we'll prepare later
                
                // Start with the default fields not part of the form itself
                statement = "INSERT INTO `" + tableName + "` (`" + TABLE_COL_NAME_ID + "`, `" + TABLE_COL_NAME_LAST_MODIFIED + "`, "; 
                
                // Then all form elements
                String valuePlaceholders = ""; // The prepared statement's value placeholders (question marks), one for each form element
                while (iFormElementsKeys.hasNext()) { 
                    statement += "`" + (String)iFormElementsKeys.next() + "`" + (iFormElementsKeys.hasNext() ? ", " : "");
                    valuePlaceholders += "?" + (iFormElementsKeys.hasNext() ? ", " : "");
                }
                statement += ")";
                
                // Then the values (or, actually placeholders)
                statement += " VALUES (?, ?, " + valuePlaceholders + ");"; 
                /*
                for (int i = 0; i < formElements.size(); i++) {
                    statement += "?";
                    if (i+1 < formElements.size())
                        statement += ", ";
                }
                statement += ");";
                //*/ 
                
                // The statement should now be complete. Prepare it and assign values.
                
                ps = sqlAgent.getConnection().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS); 
                
                // 1 "maps to" the first "?" in the statment, 2 to the second "?", etc.
                index = 0;
                
                // Set the ID (auto-increments => should always have NULL inserted)
                ps.setNull(++index, Types.INTEGER);  // Note: ++ on the left => 1 is passed (while ++ on the right => 0 is passed)
                // Set the "last updated" timestamp
                ps.setTimestamp(++index, new Timestamp(new Date().getTime()));
                
                // Set all the rest of the values in the prepared statement
                formElement = null;
                iFormElementsKeys = formElementsKeys.iterator();
                //submittedValues = null;
                while (iFormElementsKeys.hasNext()) {
                    index++;
                    formElement = (I_FormInputElement)(formElements.get(iFormElementsKeys.next()));
                    // Begin duplicate code
                    ps = setParameterFromElement(formElement, ps, index);
                    /*
                    // Get the submitted values (for text/textarea there is only one value) for the form element
                    submittedValues = formElement.getSubmission(); 
                    if (submittedValues == null) {
                        // No submitted value => set null value
                        ps.setNull(index, Types.VARCHAR);
                    } else { 
                        // Submitted value found => Determine how best to set it.
                        if (formElement.getType() == I_FormInputElement.DATETIME) {
                            Date submittedTimestamp = ((InputTypeDateTime)formElement).getDate();
                            if (submittedTimestamp == null) {
                                ps.setNull(index, Types.TIMESTAMP);
                            } else {
                                ps.setTimestamp(index, new Timestamp(submittedTimestamp.getTime()));
                            }
                        } else { 
                            // If the element type is not DATETIME, then we're inserting into a VARCHAR field
                            value = "";
                            // Construct the value string
                            for (int i = 0; i < submittedValues.length; i++) {
                                value += (i == 0 ? "" : A_InputTypeMultiSelect.VALUE_SEPARATOR) 
                                        + submittedValues[i];
                            }
                            ps.setString(index, value);
                        }
                    }
                    //*/
                }
                
                result = -1;
                
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Unable to insert this entry.");
                } else {
                    ResultSet genKeys = ps.getGeneratedKeys();
                    try {
                        if (genKeys.next()) {
                            result = Integer.valueOf(Long.toString(genKeys.getLong(1)));
                        }
                    } catch (Exception e) {
                        throw new SQLException("Unable to retrieve the created entry's ID.");
                    } finally {
                        if (genKeys != null) {
                            try { genKeys.close(); } catch (Exception e) { }
                        }
                    }
                    
                }
                break;
                
                
            case SQL_UPDATE_STATEMENT:
                if (!form.isEditEnabled()) {
                    throw new IllegalArgumentException("Unable to update this entry, because the form is not in edit mode. Enable edit mode to allow entry update.");
                }
                
                ResultSet rs = sqlAgent.doSelect("SELECT * FROM `" + tableName + "`"
                        + " WHERE `" + TABLE_COL_NAME_ID + "` LIKE " + form.getEditingEntryId() 
                        + ";");
                rs.next(); // There should be only one
                List<I_FormInputElement> modifiedElements = new ArrayList<I_FormInputElement>();
                // Wrong:
                //INSERT INTO `form_table` (`id`, `foo`) VALUES (NULL, 'bar') 
                // Right:
                //INSERT INTO `webforms`.`form_table` (`id`, `foo`) VALUES (NULL, 'bar');
                
                // Construct the insert statement (we'll prepare it later)
                statement = "UPDATE `" + tableName + "` SET " 
                        + "`" + TABLE_COL_NAME_LAST_MODIFIED + "`=?"; // Start with the default field(s) not part of the form data itself
                while (iFormElementsKeys.hasNext()) { // Loop over all form elements
                    String formElementName = (String)iFormElementsKeys.next();
                    I_FormInputElement element = form.getElement(formElementName);
                    String oldValue = rs.getString(formElementName);
                    String newValue = element.getValue();

                    if (oldValue != null && !oldValue.equals(newValue)) {
                        statement += ", `" + formElementName + "`=?"; // Add the "[form element name]=?" part for each form element
                        modifiedElements.add(element);
                    }
                    
                }
                statement += " WHERE `" + TABLE_COL_NAME_ID + "` LIKE " + form.getEditingEntryId(); // Identify the entry we're updating.
                statement += ";"; // The statement should now be complete.
                
                // Next, prepare the statement and assign values.
                
                ps = sqlAgent.getConnection().prepareStatement(statement);  // Prepare the statement
                
                index = 1; // The number 1 will "map to" the first "?" in the statment string
                ps.setTimestamp(index, new Timestamp(new Date().getTime())); // Set the first value - this is the last_modified timestamp
                // Set all the rest of the values in the prepared statement
                formElement = null;
                //itr = keys.iterator();
                iFormElementsKeys = modifiedElements.iterator();
                //submittedValues = null;
                while (iFormElementsKeys.hasNext()) {
                    index++; // Increment the index counter
                    formElement = (I_FormInputElement)iFormElementsKeys.next(); // Get the form element
                    // Begin duplicate code
                    ps = setParameterFromElement(formElement, ps, index);
                    /*
                    submittedValues = formElement.getSubmission(); // Get the submitted values (for text/textarea there there is only one value) for the form element
                    if (submittedValues == null) {
                        // No submitted value => set null value
                        ps.setNull(index, Types.VARCHAR);
                    } else {
                        // Submitted value found => Determine how best to set it.
                        if (formElement.getType() == I_FormInputElement.DATETIME) {
                            Date submittedTimestamp = ((InputTypeDateTime)formElement).getDate();
                            if (submittedTimestamp == null) {
                                ps.setNull(index, Types.TIMESTAMP);
                            } else {
                                ps.setTimestamp(index, new Timestamp(submittedTimestamp.getTime()));
                            }
                        } else { 
                            // If the element type is not DATETIME, then we're inserting into a VARCHAR field
                            value = "";
                            // Construct the value string
                            for (int i = 0; i < submittedValues.length; i++) {
                                value += (i == 0 ? "" : A_InputTypeMultiSelect.VALUE_SEPARATOR) 
                                        + submittedValues[i];
                            }
                            ps.setString(index, value);
                        }
                    }
                    //*/
                    // End duplicate code
                }
                result = ps.executeUpdate();
                break;
                
                
            default:
                result = -1;
                break;
        }
        
        this.lastStatement = ps;
        
        return result;
    }
    
    /**
     * Sets the parameter at the given index to the given form element's 
     * submitted value.
     * <p>
     * Depending on the type of element, as well as its submitted value (if 
     * any), the parameter is set either as <code>null</code>, a timestamp or a 
     * string.
     * 
     * @param element The form element.
     * @param ps The prepared statement for which to set the parameter.
     * @param parameterIndex The index of the parameter.
     * @return The updated prepared statement.
     * @throws SQLException 
     */
    private PreparedStatement setParameterFromElement(I_FormInputElement element, PreparedStatement ps, int parameterIndex) 
            throws SQLException {
        // Get the form element's submitted values 
        // (text & textarea will have just a single value)
        String[] submittedValues = element.getSubmission();
        
        if (submittedValues == null) {
            // No submitted value => set null value
            ps.setNull(parameterIndex, Types.VARCHAR);
        } else {
            // Submitted value found => Determine how best to set it
            if (element.getType() == I_FormInputElement.DATETIME) {
                Date submittedTimestamp = ((InputTypeDateTime)element).getDate();
                if (submittedTimestamp == null) {
                    ps.setNull(parameterIndex, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(parameterIndex, new Timestamp(submittedTimestamp.getTime()));
                }
            } else { 
                // Element type not DATETIME => we're inserting into a VARCHAR field
                String value = "";
                // Construct the value string
                for (int i = 0; i < submittedValues.length; i++) {
                    value += (i == 0 ? "" : A_InputTypeMultiSelect.VALUE_SEPARATOR) 
                            + submittedValues[i];
                }
                ps.setString(parameterIndex, value);
            }
        }
        
        return ps;
    }
    
    /**
     * <strong>Deletes all submitted form data</strong> from the database by 
     * dropping the corresponding table.
     * <p>
     * Needless to say, use this method very carefully.
     * 
     * @return <code>true</code> if the table is dropped. Otherwise, an exception is thrown.
     * @throws java.lang.InstantiationException if the SQLAgent object cannot be instantiated
     */
    public boolean dropTable()throws InstantiationException, SQLException {
        SQLAgent sqlAgent = null;
        try {
            sqlAgent = new SQLAgent(cmso);
        } catch (Exception e) {
            throw new InstantiationException("Could not create the SQLAgent object, no database transactions can be performed. Please check the configuration file.");
        }
        String tableName = form.getTableName();
        String statement = "DROP TABLE `" + tableName + "`;";
        try {
            PreparedStatement ps = sqlAgent.getConnection().prepareStatement(statement);
            ps.execute();
            return true;
        } catch (SQLException sqle) {
            throw new SQLException("Failed to drop table \"" + tableName + "\": " + sqle.getMessage());
        }
    }
    
    /**
     * Deletes the data for a single entry of a submitted form, by deleting the 
     * corresponding row from the database table.
     * <p>
     * Needless to say, use this method very carefully.
     * 
     * @param entryId The ID of the entry (table row) to delete.
     * @return <code>true</code> if the entry was deleted successfully. Otherwise, an exception is thrown.
     * @throws java.lang.InstantiationException if the SQLAgent object cannot be instantiated
     */
    public boolean deleteEntry(int entryId)throws InstantiationException, SQLException {
        SQLAgent sqlAgent               = null;
        try {
            sqlAgent                    = new SQLAgent(cmso);
        } catch (Exception e) {
            throw new InstantiationException("Could not create the SQLAgent object, no database transactions can be performed. Please check the configuration file.");
        }
        String tableName                = form.getTableName();
        String statement                = "DELETE FROM `" + tableName + "` WHERE `" + TABLE_COL_NAME_ID + "`=" + entryId + ";";
        try {
            PreparedStatement ps            = sqlAgent.getConnection().prepareStatement(statement);
            ps.execute();
            return true;
        } catch (SQLException sqle) {
            throw new SQLException("Failed to delete entry with ID " + entryId + " from table \"" + tableName + "\": " + sqle.getMessage());
        }
    }
    
    /**
     * Gets the most recent statement executed by this manager.
     * 
     * @return the most recent statement executed by this manager.
     */
    public PreparedStatement getLastExecutedStatement() { return lastStatement; }
}
