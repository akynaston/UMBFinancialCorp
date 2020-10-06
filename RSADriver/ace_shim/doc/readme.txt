**********************************************************
All existing RSA driver implementations must update the location of the RSA Authentication Manager self-signed root certificate. Previous versions of the RSA driver looked for this cert in a trust.jks keystore. This RSA driver expects the RSA Authentication Manager self-signed root certificate to be found in the cacerts keystore with all other certs referenced by Identity Manager. Please see 2.2.2 Exporting the Root Certificate below for details.
**********************************************************
In the information below indicates the required updates to the online RSA driver documentation found here: https://www.netiq.com/documentation/idm402drivers/rsa/data/bktitle.html

1.1 Supported Software Version
The following RSA Authentication Manager versions are supported:
-7.1
-8.1
The following Novell Identity Manager versions are supported:
-4.0.1
-4.0.2
-4.5


1.3.1 Local and Remote Platforms
You can install the RSA driver locally or remotely.

A local configuration is when the RSA driver is installed on the same computer with an Identity Vault and the Metadirectory engine. A remote configuration is when the RSA driver is installed along with a remote loader on a computer without an Identity Vault and Metadirectory engine.

RSA Authentication Manager v7.1 supports both a local and remote configuration. The remote loader can be installed on the RSA Authentication Manager server.
RSA Authentication Manager v8.1 is provided as an appliance and does not support a remote configuration where the remote loader is installed on the Authentication Manager server. Consequently, the RSA driver may be installed with a local configuration or if a remote configuration is required, the remote loader must be installed on a computer other than the RSA Authentication Manager server.


2.0 Installing the Driver Files
The RSA driver files are not installed during the Identity Manager installation at this time. Installation of these files must be performed manually.
The following sections explain how to install the RSA driver files from the Identity Manager installation media and how to install file dependencies for RSA Authentication Manager:
-Section 2.1, Installing the Driver Files
-Section 2.2, Copying Required Files and Information from RSA Authentication Manager


2.1 Installing the Driver Files
The RSA driver files should be installed as follows based on the chosen configuration:

Metadirectory Server
-Copy the jace.jar and ACEShim.jar to the following directory on your IDM server
--Windows: \Novell\NDS\lib
--Linux: /opt/novell/eDirectory/lib/dirxml/classes
---Set privileges on the jar files to 755

Remote Loader
-Copy the jace.jar and ACEShim.jar to the following directory on your Remote Loader server
--Windows: \novell\RemoteLoader\lib
--Linux: /opt/novell/eDirectory/lib/dirxml/classes
---Set privileges on the jar files to 755


2.2 Copying Required Files and Information from RSA Authentication Manager
Several files from your RSA Authentication Manager installation need to be copied to the Identity Manager installation. The following sections contain instructions for copying these files and and authentication information. The RSA Authentication Manager files must be copied to the appropriate Identity Manager driver library directory for your installation.

Windows: \Novell\NDS\lib
Linux/Unix: /opt/novell/eDirectory/lib/dirxml/classes


2.2.1 Copying RSA Files

2.2.1.1 Copying RSA Authentication Manager 7.1 Files
As listed in current online docs

2.2.1.2 Copying RSA Authentication Manager 8.1 Files
Copy the following files from the sdk directory of the RSA Authentication Manager 8.1 installation media to the Identity Manager driver library.

am-client.jar
commons-beanutils.jar
commons-logging.jar
iScreen.jar
log4j.jar
ognl.jar
spring-asm.jar
spring-beans.jar
spring-context.jar
spring-core.jar
spring-expression.jar
wlfullclient.jar

Copy the following files: 
gson-2.2.4.jar - Google Code google—gson project
hibernate-3.2.2.jar - SourceForge Hibernate project
hsqldb.jar - SourceForge HyperSQL Database Engine project


2.2.2 Exporting the Root Certificate
When you install RSA Authentication Manager, the system creates a self-signed root certificate. You must export this certificate from the server and import it into the Java keystore file used by Identity Manager. 

v7.1
To export the server root certificate:
1. Change directory to RSA_AM_HOME/appserver
2. Export the root cert by typing:
jdk/jre/bin/keytool -export -keystore RSA_AM_HOME/server/security/server_name.jks -file am_root.cer -alias rsa_am_ca
3. When prompted for the keystore password, press Enter without typing a password.
NOTE: A warning screen is displayed, but the server root certificate is still exported.
The Java Keytool utility writes the certificate file to the RSA_AM_HOME/appserver directory. 
4. Copy the am_root.cer to the Identity Manager server.
5. Import the certificate into the Identity Manager Java keystore by running the following command on the Identity Manager server:
/opt/novell/eDirectory/lib64/nds-modules/jre1.6.0_31/bin/keytool -v -import -file am_root.cer -alias RSA7 -keystore cacerts
NOTE: You must provide a cacerts keystore password to import the server root certificate into the java keystore. The Java default is changeit.
The Java Keytool utility displays a confirmation that the certificate was added to the keystore.

v8.1
To export the server root certificate:
1. Change directory to /opt/rsa/am/appserver
2. Export the root cert by typing:
jdk/bin/keytool -export -keystore /opt/rsa/am/server/security/trust.jks -file am_root.cer -alias rsa-am-ca
3. When prompted for the keystore password, press Enter without typing a password.
NOTE: A warning screen is displayed, but the server root certificate is still exported.
The Java Keytool utility writes the certificate file to the /opt/rsa/am/appserver directory.
4. Copy the am_root.cer to the Identity Manager server.
5. Import the certificate into the Identity Manager Java keystore by running the following command on the Identity Manager server:
/opt/novell/eDirectory/lib64/nds-modules/jre1.6.0_31/bin/keytool -v -import -file am_root.cer -alias RSA8 -keystore cacerts
NOTE: You must provide a cacerts keystore password to import the server root certificate into the java keystore. The Java default is changeit.
The Java Keytool utility displays a confirmation that the certificate was added to the keystore.


2.2.3 Obtaining the Command Client Username and Password
1. From a command prompt on your RSA Authentication Manager host, change directories to RSA_AM_HOME/utils (7.1) or /opt/rsa/am/utils (8.1).


2.2.4 Setting IDM Java Startup Properties for RSA Authentication Manager
For the RSA Driver to communicate correctly with RSA Authentication Manager, Java startup properties for IDM must be added.

2.2.4.1 RSA Authentication Manager 7.1
As described in online docs

2.2.4.2 RSA Authentication Manager 8.1
On Windows
1-6 as described in online docs
7. In the Variable Value field, add the following text, ensuring that it is properly separated from any existing text by a space character:
-Dsun.lang.ClassLoader.allowArraySyntax=true 
-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl

On Linux
Update the /opt/novell/eDirectory/sbin/pre_ndsd script with the following:
DHOST_JVM_OPTIONS=“-Dsun.lang.ClassLoader.allowArraySyntax=true  -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl”


3.0 Preparing RSA Authentication Manager
To prepare the RSA Authentication Manager server you are connecting to, you must create a user account through which the RSA driver can authenticate to the RSA Authentication Manager server. You will need to create an RSA Authentication Manager user object with SuperAdminRole rights for the RSA driver. Make sure the User object that the driver uses to authenticate with is not used for any other purpose.

The created credentials will be used while configuring the driver in Section 4.1.2, Installing the Driver Packages.

1. Login to the RSA Security Console with an account that has SuperAdminRole rights.
2. From the Identity menu, select Users > Manage Existing > Add New.
3. Fill out the user information.
4. Confirm that Require user to change password at next logon is unchecked.
5. Click Save
6. From the Administration menu, select Administrative Roles > Manage Existing.
7. Select the SuperAdminRole, then click Assign More.
8. Search for the user you created for the service account.
9. Select the user, then click Assign to Role.


4.1.2 Installing the Driver Packages

10. On the Application Authentication page, fill in the following fields:

Authentication ID: Specify the username for the RSA user created for the driver.

Connection Information: Specify the connection information for the driver to connect to the RSA server. The connection information should be specified in the form t3s://<ip or hostname>:<port> (e.g. ). The default port for an RSA Authentication Manager is 7002; for an RSA Authentication Manager Appliance, the default port is 7004.

Password: Specify the password for the RSA user created for the driver.


7.4 Invalid RSA Authentication Credentials


A.1.3 Authentication
Authentication ID: Specifies the RSA Authentication Manager administrative user that the driver will use for authentication. For example: rsadriver. This is the user created inSection 3.0, Preparing RSA Authentication Manager.

Application Password: Specify the password for the user object listed in the Authentication ID field. This is the password created in Section 3.0, Preparing RSA Authentication Manager. 


A.1.5 Driver Parameters
Driver Options

RSA Command Client User: Specify the command client user for your RSA installation. This information was gathered in Obtaining the Command Client Username and Password.

RSA Command Client Password: Specify the command client password for your RSA installation. This information was gathered in Obtaining the Command Client Username and Password

RSA Realm: Specify the RSA realm containing the driver user specified in the Authentication ID. Currently only the default SystemDomain realm is supported.

Weblogic Library Directory: Specify the location of the RSA Weblogic .jar files that were copied during Copying RSA Files. The default locations are:
-Windows: C:\Novell\NDS\lib
-Linux/Unix: /opt/novell/eDirectory/lib/dirxml/classes

RSA Identity Source: Specify the case-sensitive name of the Identity Source with which to synchronize. If the field is empty, the first Identity Source in the Realm will be used. If in doubt, specify an Identity Source.
********************************************************** 