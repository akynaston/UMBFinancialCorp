package com.trivir.ace.api;

import java.util.List;
import java.util.Map;

import com.trivir.ace.AceToolkitException;

public interface AceApi {
	// User attributes
	public static final String ATTR_DEFAULT_LOGIN = "DefaultLogin";
	public static final String ATTR_DEFAULT_SHELL = "DefaultShell";
	public static final String ATTR_FIRST_NAME = "FirstName";
	public static final String ATTR_LAST_NAME = "LastName";
	public static final String ATTR_EMAIL_ADDRESS = "EmailAddress";
	public static final String ATTR_TOKEN_SERIAL_NUMBER = "TokenSerialNumber";
	public static final String ATTR_TOKEN_PIN = "TokenPIN";
	public static final String ATTR_PROFILE_NAME = "ProfileName";
	public static final String ATTR_MEMBER_OF = "MemberOf";
	public static final String ATTR_TEMP_USER = "TempUser";
	public static final String ATTR_START = "Start";
	public static final String ATTR_END = "End";
	public static final String ATTR_CREATE_PIN = "CreatePIN";
	public static final String ATTR_MUST_CREATE_PIN = "MustCreatePIN";
    public static final String ATTR_USER_NUM = "UserNum";
    public static final String ATTR_PASSWORD = "Password";

    // Token Attributes
    static final String ATTR_SERIAL_NUM = "SerialNum";
    static final String ATTR_PIN_CLEAR = "PINClear"; // 0 No PIN associated with token (old PIN, if any, cleared),  1 Token has a PIN associated with it
    static final String ATTR_NUM_DIGITS = "NumDigits";
    static final String ATTR_INTERVAL = "Interval";
    static final String ATTR_BIRTH = "Birth";
    static final String ATTR_DEATH = "Death";
    static final String ATTR_LAST_LOGIN = "LastLogin";
    static final String ATTR_TYPE = "Type";
    static final String ATTR_HEX = "Hex";
    static final String ATTR_NEW_PIN_MODE = "NewPINMode";
    static final String ATTR_NEXT_CODE_STATUS = "NextCodeStatus";
    static final String ATTR_BAD_TOKEN_CODES = "BadTokenCodes";
    static final String ATTR_BAD_PINS = "BadPINs";
    static final String ATTR_PIN_CHANGED_DATE = "PINChangedDate";
    static final String ATTR_DISABLED_DATE = "DisabledDate";
    static final String ATTR_COUNTS_LAST_MODIFIED = "CountsLastModified";
    static final String ATTR_PROTECTED = "Protected";
    static final String ATTR_DEPLOYMENT = "Deployment";
    static final String ATTR_DEPLOYED = "Deployed";
    static final String ATTR_COUNT = "Count";
    static final String ATTR_SOFT_PASSWORD = "SoftPassword";
    static final String ATTR_PIN = "PIN";
    static final String ATTR_DISABLED = "Disabled";
    static final String ATTR_ASSIGNED = "Assigned";
    static final String ATTR_SEED_SIZE = "SeedSize";
    static final String ATTR_KEYPAD = "Keypad";
    static final String ATTR_LOCAL_PIN = "LocalPIN";
    static final String ATTR_VERSION = "Version";
    static final String ATTR_FORM_FACTOR = "FormFactor";
    static final String ATTR_PIN_TYPE = "PINType";
    static final String ATTR_ASSIGNMENT = "Assignment";
    static final String ATTR_FIRST_LOGIN = "FirstLogin";
    static final String ATTR_LAST_DA_CODE = "LastDACode";
    static final String ATTR_EAC_EXPIRES = "EACExpires";
    static final String ATTR_EAC_PASSCODE = "EACPasscode";
    static final String ATTR_EMERGENCY_ACCESS = "EmergencyAccess";

    // API Options
	public static final String APPEND_SITE_TO_ALL_GROUPS = "appendSiteToAllGroups";
    public static final String COMMAND_CLIENT_USERNAME = "commandClientUsername";
	public static final String COMMAND_CLIENT_PASSWORD = "commandClientPassword";
    public static final String RSA_REALM = "rsaRealm";
    public static final String WEBLOGIC_LIB_DIR = "weblogicLibDir";
    public static final String RSA_KEYSTORE_FILE = "rsaKeystoreFile";
    public static final String RSA_IDENTITY_SOURCE = "rsaIdentitySource";
    public static final String API_VERSION = "apiVersion";
    public static final String SERVER = "server";
    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";

    
	public void addLoginToGroup(String groupLogin, String groupName, String groupShell, String tokenSerialOrLogin) throws AceToolkitException;

	public void addUser(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String password) throws AceToolkitException;

	public String apiRev() throws AceToolkitException;
	
	public void assignAnotherToken(String tokenSerialOrLogin, String tokenSerialNumber) throws AceToolkitException;

	public void assignProfile(String tokenSerialOrLogin, String profileName) throws AceToolkitException;

	public void assignToken(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String tokenSerialNumber, String password) throws AceToolkitException;

	public void changeAuthWith(int tknAuthWith, String tokenSerialNumber) throws AceToolkitException;
	
	public void clearHistory() throws AceToolkitException;

	public void deleteUser(String defaultLogin) throws AceToolkitException;

	public void delLoginFromGroup(String defaultLogin, String groupName) throws AceToolkitException;

	public void destroy() throws AceToolkitException;

	public void disableToken(String tokenSerialNumber) throws AceToolkitException;

	public void emergencyAccessOff(String tokenSerialNumber) throws AceToolkitException;

    public String emergencyAccessOTP(String tokenSerialNumber, int number, int digits, int flags, int lifetime, String dateExpire, int hourExpire) throws AceToolkitException;

	public void enableToken(String tokenSerialNumber) throws AceToolkitException;

	public String getAceApiVersion();
	
	public String getLibraryVersion();

	public List<String> getSerialByLogin(String defaultLogin, String number) throws AceToolkitException;

	public boolean isEmergencyAccess(String tokenSerialNumber) throws AceToolkitException;

	public List<String> listAllTokens() throws AceToolkitException;
	
	public List<String> listAllUsers() throws AceToolkitException;

	public List<String> listAssignedTokens() throws AceToolkitException;
	
	public Map<String,String> listExtensionsForUser(String tokenSerialOrLogin) throws AceToolkitException;

	public List<String> listGroupMembership(String tokenSerialOrLogin) throws AceToolkitException;

    public Map<String,Object> listTokenInfo(String tokenSerialNumber) throws AceToolkitException;

  	public List<String> listUnassignedTokens() throws AceToolkitException;
	
	public Map<String,Object> listUserInfo(String defaultLogin) throws AceToolkitException;
	
	public List<String> listUsersByLogin(String defaultLogin) throws AceToolkitException;

	public void newPin(String tokenSerialNumber) throws AceToolkitException;

	public void reinit() throws AceToolkitException;

	public void replaceToken(String oldTokenSerialNumber, String newTokenSerialNumber, boolean resetPin) throws AceToolkitException;

	public void rescindToken(String tokenSerialNumber, boolean revoke) throws AceToolkitException;

	public String setPin(String pin, String tokenSerialNumber) throws AceToolkitException;

	public void setPinToNTC(String tokenSerialNumber, String string) throws AceToolkitException;

	public void setTempUser(String dateStart, int hourStart, String dateEnd, int hourEnd, String tokenSerialOrLogin) throws AceToolkitException;

	public void setUser(String lastName, String firstName, String defaultLogin, String emailAddress, String defaultShell, String tokenSerialOrLogin, String password) throws AceToolkitException;

	public void unassignProfile(String tokenSerialOrLogin) throws AceToolkitException;
}