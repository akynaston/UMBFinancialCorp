LDAP Connector
---------------
The LdapConnector provides methods to allow you to execute LDAP operations against an LDAP directory and optionally validate objects and attributes from IdMUnit tests.

See the Special Considerations section below for additional notes.

OPERATIONS
----------
AddAttr:
* dn - LDAP DN of the object to update
* [column names] - column values to be added as attributes to the object
    - Note: DN value may be a full explicit Dn of the account, or an LDAP Filter like this:
    (&(objectClass=User)(sn=black*)(workforceID=9000*)),base=ou=Employees,ou=Staff,o=FCPS
Following the format:
    [LDAP FILTER],base=[base dn]
    Remember: all standard LDAP searching rules come into play here: indexing attributes to be searched etc.
    Caveots:
         - Be cautious when validating this way: if your search could possibly return more than one account, the row will simply find the first result that matches, and call it a success; make sure this is the desired result.
         - When deleting using filters (DeleteObject action) the connector only deletes the first one it finds then stops; for security reasons. If you need to repeat the delete for multiple accounts; use the iterationConnector with the Ldap connector; but be sure to use a very strict filter when deleting this way.

AddObject:
* dn - LDAP DN of the object to create
* [column names] - column values to be added as attributes of the object

ClearAttr:
* dn - LDAP DN of the object to update or an LDAP filter that will result in the object to update
* [column names] - specify an '*' as the value for attributes to be cleared

DeleteObject:
* dn - LDAP DN of the object to delete or an LDAP filter that will result in the object to delete

MoveObject:
* dn - LDAP DN of the object to move
* newdn - New LDAP DN for the object

RemoveAttr:
* dn - LDAP DN of the object to update or an LDAP filter that will result in the object to update
* [column names] - column values to be removed from attributes of the object. If DirXML-Associations is specified as the column name then only the driver DN portion of the column value is compared to the current values of the object to determine whether or not to remove a value. 

RenameObject:
* dn - LDAP DN of the object to rename
* newdn - New LDAP DN for the object

ReplaceAttr:
* dn - LDAP DN of the object to update
* [column names] - column values will replace the attribute values of the object

AttrDoesNotExist:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* [column names] - specify an '*' as the value for attributes to confirm are not populated.

ValidateObject:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* [column names] - column values to compare with attributes of the object

ValidateObjectDoesNotExist:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate

ValidatePassword:
* dn - LDAP DN of the object to validate or an LDAP filter that will result in the object to validate
* userPassword - password to be validated

SPECIAL CONSIDERATIONS
----------------------
Keep the following in mind when using the LdapConnector:
 - When working with DirXML-Associations:
    - Association value formats appear as follows:
        [Driver LDAP DN]#[Association state]#[Association value]
    - DirXML association values:
        0 - Disabled
        1 - Associated
        2 - Pending
        3 - Manual
        4 - Migrate
     - When Validating: Use the format of "[driver dn]#[association value expected]#.+", without the quotes. This is a valid regluar expression that will properly ensure objects have the association value expected.
     - When Modifying: Adding the second pound sign on a DirXML association signifies that the value immediately after is the association value.  If you provide the # character, but do not add an association, this means the association will be deleted.  If you do not provide the second # sign, the association will be left intact.
        Examples:
            cn=driver,cn=driverset,o=services#1#NewAssociatoin
                - Manually associates an account
            cn=driver,cn=driverset,o=services#4#
                - Manually causes a <sync> event, and deletes the association: causes an modify to add scenario:.
            cn=driver,cn=driverset,o=services#4
                - Manually causes a <sync> event, but preserves the association, and will be a simple full modification of an account if it has an association value in the tree.

 - When connected to Active Directory:
     - Use 'unicodePwd' for the password column name, and configure SSL to set passwords.  Ensure the SSL port used (typically 636).

 - When connected to OpenDJ
    - Use 'userPassword' for the password column name
    - Use 'newPassword' to indicate the password that we are using to update userPassword.
    - When updating password on a user, use 'replaceAttr' operation and provide 'userPassword', and 'newPassword' as columns.

 - When working with binary (octet) attributes:
    - Prefixing binary attributes is required for proper handling of binary data.
    - Supported prefixes are "bin_" and "b64_" (Base64). These prefixes inform IdMUnit that the attribute is of type octet. 
      E.g., bin_jpegPhoto or b64_jpegPhoto.
    - Both prefixes support relative and absolute paths to files containing binary or Base64-encoded data respectively.
    - Both prefixes support file URIs. E.g., file://c:/temp/test/bin. Note: File URIs are absolute by nature.
    - For convenience, attributes with the "b64_" prefix may contain inline Base64-encoded data. That is, Base64-encoded
      data can be input directly into the IdMUnit spreadsheet rather than referenced in an external file.
    - It isn't necessary to remove "--" comments from Base64-encoded files.

	 

CONFIGURATION
-------------
To configure this connector you need to specify a server, user, and password.

    <connection>
        <name>IDV</name>
        <description>Connector to an LDAP server</description>
        <type>com.trivir.idmunit.connector.LdapConnector</type>
        <server>192.168.1.3</server>
        <user>cn=admin,o=services</user>
        <password>B2vPD2UsfKc=</password>
        <multiplier/>
        <substitutions/>
        <data-injections/>
    </connection>

The following configuration parameters are optional:

* port: specifies the port to be used to connect to the server. If this is not specified, 389 will be used unless use-tls is true, in which case 636 will be used.
* use-tls (true|false): specifies whether or not TLS will be used
* keystore-path: path to keystore containing the certificate for the server or the certificate of the CA that signed the server's certificate. If this option is specified, trust-all-certs and trusted-cert-file are ignored.
* trusted-cert-file: path to a file containing the certificate for the server or the certificate of the CA that signed the server's certificate. 
* trust-all-certs (true|false):

If TLS is enabled and the certificate chain validation fails then the certificate chain is written to a file. If a value for trusted-cert-file is specified, that will be used as the file name. Otherwise, the file name will be the [server].cer if the port is 636 or [server]_[port].cer if a port other than 636 is specified.
