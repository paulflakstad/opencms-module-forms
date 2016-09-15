********************************************************************************
                      Preparing the forms module for use
********************************************************************************


First, install the module by importing the module zip.
 
After the module is installed, some initial setup is required. Since this forms 
module will typically not use OpenCms' native database or database connection, 
it relies on its own settings whenever it needs to interact with the database. 
Typically, it will also use a separate database.

NOTE: You will probably need root access to MySQL for the initial setup. No 
root credentials will be stored. They are needed only to create a dedicated 
database and user for this module.


Setup via script:
--------------------------------------------------------------------------------
1.) Make sure you are in your "Offline" project and the site "/" (system). 

2.) Navigate to the configuration folder: 
/system/modules/no.npolar.common.forms/config/

3.) Then click the setup script: 
setup.jsp

4.) A simple form should appear in your browser. 
Fill out and submit the form.

The setup script is just a convenient and user-friendly way to get everything up 
and running both on the MySQL and OpenCms side.

If you encounter any problems, or need more control, you can do a manual setup 
instead (see below).


Manual setup:
--------------------------------------------------------------------------------
1.) Create a new (or choose an existing) MySQL user for the forms module. 
    Employing the root user is *really* not recommended, unless you know what 
    you're doing.

2.) Create a new (or choose an existing) database for the forms module. 
    This database will hold all the forms' data.

3.) Grant the user all privileges on the database.

4.) Put the user's credentials and database name (and other settings) in the 
    configuration file: 
    /system/modules/no.npolar.common.forms/config/opencms-forms.xml

NOTE: The configuration file should *always* be flagged as "internal"!