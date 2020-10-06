RSA Connector
---------------
The RSA REST connector provides operations to allow you to manage RSA resources in IdMUnit tests. This connector supports versions 6.1 and 7.1 of the RSA API.

OPERATIONS
----------
AddObject: Create the specified object type.
* objectClass - Only "User" is supported.
* LastName -  The user's last name.
* FirstName -  The user's first name.
* DefaultLogin - The desired login for the user.
* DefaultShell - The desired shell for the user.
* EmailAddress - the desired email address for the user.
* Password - The password of the user to use.
* TokenSerialNumber - <optional> The token to be assigned to the user.
* ProfileName - <optional> The profile to be assigned to the user.
* MemberOf - <optional> The group to which the user should be assigned.

DeleteObject: Delete the specified object type.
* objectClass - Only "User" is supported.
* DefaultLogin - The login to be deleted.

AddAttr: Add an attribute to the specified object type.
* objectClass - Only "User" is supported.
* DefaultLogin - The login for the user to which the attribute should be added.
* MemberOf - <optional> The group to which the user should be assigned.
* TokenSerialNumber - The token serial to assign to the user.

ReplaceAttr: Add an attribute to the specified object type.
* objectClass - "User" or "Token" is supported.
    * objectClass "User"
        * DefaultLogin - The login for the user to which the attribute should be added.
	* LastName - The user's last name.
	* FirstName - The user's first name.
	* DefaultShell - The user's default shell.
    * objectClass "Token"
        * TokenSerialNumber - The token to which the attribute should be added.
	* Disabled - <optional> true/false. Disable or enable the token.
	* PIN - <optional> The PIN to set on the token.

RemoveAttr: Remove an attribute from the specified object type.
* objectClass - Only "User" is supported.
* DefaultLogin - The login for the user to which the attribute should be added.
* LastName - The user's last name.
* FirstName - The user's first name.
* DefaultShell - The user's default shell.

ValidateObject: Validate the specified object type against a set of attributes.
* objectClass - "User" or "Token" is supported.
    * objectClass "User"
        * DefaultLogin - The login for the user to be validated.
	* TokenSerialNumber - <optional> The assigned token serial to validate.
	* TokenPIN - <optional> The assigned token PIN to validate.
	* ProfileName - <optional> The profile name to validate.
	* MemberOf - <optional> The group membership to validate.
	* UserNumber - <optional> The user number to validate.
	* LastName - <optional> The last name to validate.
	* FirstName - <optional> the first name to validate.
	* DefaultShell - <optional> The default shell to validate.
	* TempUser - <optional> true/false. The temporary account state to validate.
	* dateStart - <optional> ctime. A ctime value for the account start date to validate.
	* dateEnd - <optional> ctime. A ctime value for the account end date to validate.
    * objectClass "Token"
        * TokenSerialNumber - The token to be validated.
	* PIN - <optional> The PIN to validate.
	* Disabled - <optional> true/false. The token state to validate.
	* NewPinMode - <optional> true/false. The new pin state to validate.

CONFIGURATION
-------------
Configuration differs for API 6.1 vs 7.1. 

-------------------------
RSA API 6.1 Configuration
-------------------------
    <connection>
        <name>RSA61</name>
        <description>Connector for RSA61</description>
        <type>com.trivir.idmunit.connector.RsaConnector</type>
	<apiVersion>61</apiVersion>
	<multiplier/>
	<substitutions/>
    </connection>


-------------------------
RSA API 7.1 Configuration
-------------------------
    <connection>
        <name>RSA61</name>
        <description>Connector for RSA61</description>
        <type>com.trivir.idmunit.connector.RsaConnector</type>
	<apiVersion>71</apiVersion>
	<server>t3s://amserver.trivir.com:7002</server>
	<username>adminUser</username>
	<password>adminPassword</password>
	<commandClientUsername>commandClientUser</commandClientUsername>
	<commandClientPassword>commandClientPassword</commandClientPassword>
	<rsaRealm>realmName</rsaRealm>
	<weblogicLibDir>c:/path/to/weblogicdir</weblogicLibDir>
	<rsaKeystoreFile>c:/path/to/trust.jks</rsaKeystoreFile>
	<multiplier/>
	<substitutions/>
    </connection>
