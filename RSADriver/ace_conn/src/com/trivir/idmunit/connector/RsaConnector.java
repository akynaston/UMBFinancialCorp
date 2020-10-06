package com.trivir.idmunit.connector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.AssertionFailedError;

import org.idmunit.IdMUnitException;
import org.idmunit.connector.BasicConnector;

import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.v71.AceApi71;

public class RsaConnector extends BasicConnector {
    private AceApi api;

    public void setup(Map<String, String> config) throws IdMUnitException {
        try {
            api = new AceApi71(config);
        } catch (AceToolkitException e) {
            throw new IdMUnitException("Error creating connection to ACE server");
        }
    }

    public void tearDown() throws IdMUnitException {
//        try {
//            //atk.destroy();
//        } catch (AceToolkitException e) {
//            throw new IdMUnitException("Error closing connection to ACE server");
//        }
//        atk = null;
    }

    public void opAddObject(Map<String, Collection<String>> data) throws IdMUnitException {
        Map<String, String> newObjectValues = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        // Set emtpy string values for the add user call so at least none of these are null.
        newObjectValues.put("LastName", "");
        newObjectValues.put("FirstName", "");
        newObjectValues.put("DefaultLogin", "");
        newObjectValues.put("DefaultShell", "");
        newObjectValues.put("EmailAddress", "");
        newObjectValues.put("Password", "");
                
        for (String name : data.keySet()) {
            newObjectValues.put(name, getSingleValue(data, name));
        }
        
        String objectClass = newObjectValues.remove("objectClass");
        if (!"User".equalsIgnoreCase(objectClass)) {
            if (objectClass == null) {
                objectClass = "no class specified";
            }
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported, only 'User' is supported.");
        }

        String lastName = newObjectValues.remove("LastName");
        String firstName = newObjectValues.remove("FirstName");
        String defaultLogin = newObjectValues.remove("DefaultLogin");
        String defaultShell = newObjectValues.remove("DefaultShell");
        String tokenSerialNumber = newObjectValues.remove("TokenSerialNumber");
        String emailAddress = newObjectValues.remove("EmailAddress");
        String password = newObjectValues.remove("Password");
        
        if (tokenSerialNumber == null) {            
            try {
                api.addUser(lastName, firstName, defaultLogin, emailAddress, defaultShell, password);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error adding user: " + e, e.getCause());
            } 
        } else {
            try {
                api.assignToken(lastName, firstName, defaultLogin, emailAddress, defaultShell, tokenSerialNumber, password);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error adding user and assinging token: " + e, e.getCause());
            } 
        }
            
        String profileName = newObjectValues.remove("ProfileName");
        if (profileName != null) {
            try {
                api.assignProfile("-" + defaultLogin, profileName);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error while assigning profile: '" + profileName + "'", e.getCause());
            }
        }           
        
        String group = newObjectValues.remove("MemberOf");
        if (group != null) {
            try {
                // Note: GroupShell is not supported here, "" for now.
                api.addLoginToGroup("", group, "", "-" + defaultLogin);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error while assigning group: '" + group + "', error was: " + e, e);
            }
        }
        
        if (newObjectValues.size() > 0) {
            throw new IdMUnitException("Add failed: Attribute(s) [" + newObjectValues.keySet() + "] is/are unknown.");
        }
    }

    public void opDeleteObject(Map<String, Collection<String>> data) throws IdMUnitException {
        String objectClass = getSingleValue(data, "objectClass");

        if (objectClass == null) {
            throw new IdMUnitException("No objectClass specified.");
        } else if ("User".equalsIgnoreCase(objectClass)) {
            deleteUser(data);
        } else {
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported");
        }
    }

    private void deleteUser(Map<String, Collection<String>> data) throws IdMUnitException {
        String defaultLogin = getSingleValue(data, "DefaultLogin");
        try {
            api.deleteUser("-" + defaultLogin);
        } catch (AceToolkitException e) {
        	// 2163 signifies the user was not found; if it's not found, don't throw an error:
            if (!e.toString().contains("2163")) {
                throw new IdMUnitException("Error deleting user " + e);
            }
        }
    }

    public void opAddAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String objectClass = getSingleValue(data, "objectClass");
        Map<String, String> newData = convertToSingleValues(data);
        newData.remove("objectClass");
        if (objectClass == null) {
            throw new IdMUnitException("No objectClass specified.");
        } else if ("User".equalsIgnoreCase(objectClass)) {
            handleModifyUserAddAttrs(newData);
        } else if ("Token".equalsIgnoreCase(objectClass)) {
            throw new IdMUnitException("AddAttr not supported for tokens.");
        } else {
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported.");
        }
    }

    public void opReplaceAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String objectClass = getSingleValue(data, "objectClass");
        Map<String, String> newData = convertToSingleValues(data);
        newData.remove("objectClass");
        if (objectClass == null) {
            throw new IdMUnitException("No objectClass specified.");
        } else if ("User".equalsIgnoreCase(objectClass)) {
            handleModifyUserReplaceAttrs(newData);            
        } else if ("Token".equalsIgnoreCase(objectClass)) {
            handleModifyToken(newData);           
        } else {
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported.");
        }
    }

    public void opRemoveAttr(Map<String, Collection<String>> data) throws IdMUnitException {
        String objectClass = getSingleValue(data, "objectClass");
        Map<String, String> newData = convertToSingleValues(data);
        newData.remove("objectClass");
        if (objectClass == null) {
            throw new IdMUnitException("No objectClass specified.");
        } else if ("User".equalsIgnoreCase(objectClass)) {
            handleModifyUserRemoveAttrs(newData);
        } else if ("Token".equalsIgnoreCase(objectClass)) {
            throw new IdMUnitException("RemoveAttr not supported for tokens.");
        } else {
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported.");
        }
    }

    // TODO: automated tests
    private void handleModifyToken(Map<String, String> attrs) throws IdMUnitException {
        if (attrs.containsKey("TokenSerialNumber") == false) {
            throw new IdMUnitException("TokenSerialNumber must be provided to modify Tokens!");
        }
        String tokenSerialNumber = attrs.remove("TokenSerialNumber"); 

    	String newDisabled = attrs.remove("Disabled");
    	if (newDisabled != null && newDisabled.length() > 0) {
	    	String disabledCurrently = null;
			try {
				disabledCurrently = (String) api.listTokenInfo(tokenSerialNumber).get(AceApi.ATTR_DISABLED);
			} catch (AceToolkitException e) {
				throw new IdMUnitException("Could not find Token: [" + tokenSerialNumber + "] to modify!", e);
			}
	    	
	    	if (disabledCurrently == null || disabledCurrently.length() == 0) {
	    		throw new IdMUnitException("Could not read current disabled setting for token: [" + tokenSerialNumber + "]");
	    	}
	
	    	if (disabledCurrently != newDisabled ) {
	        	try {
	        		if ("TRUE".equalsIgnoreCase(newDisabled)) {
	        			api.disableToken(tokenSerialNumber);
	        		} else {
	        			api.enableToken(tokenSerialNumber);
	        		}
	    		} catch (AceToolkitException e) {
	    			throw new IdMUnitException("Error while modifying Token in ACE", e);
	    		}
	    	}
    	}
    	
    	String pin = attrs.remove("PIN");
    	if (pin != null && pin.length() > 0) {
        	try {
    			api.setPin(pin, tokenSerialNumber);
    		} catch (AceToolkitException e) {
    			throw new IdMUnitException("Error while setting PIN for token", e);
    		}
    	}
    	
    	if (attrs.size() > 0) {
			throw new IdMUnitException("Modification to replace values failed: Attribute(s) [" + attrs.keySet() + "] is/are unknown.");
		}
	}
   
    // TODO: automated tests
    private void handleModifyUserReplaceAttrs(Map<String, String> attrs) throws IdMUnitException {
        if (attrs.containsKey("DefaultLogin") == false) {
            throw new IdMUnitException("Default login must be provided to modify Users!");
        }

    	String defaultLogin = attrs.remove("DefaultLogin"); 
    	
    	// Store the current values:
    	Map<String, Object> userInfo;
		try {
			userInfo = api.listUserInfo("-" + defaultLogin);
		} catch (AceToolkitException e) {
			throw new IdMUnitException("Could not find user: [" + defaultLogin + "] to modify!", e);
		}

        Map<String, String> changeValues = new HashMap<String, String>();
        changeValues.put("LastName", (String)userInfo.get(AceApi.ATTR_LAST_NAME));
    	changeValues.put("FirstName", (String)userInfo.get(AceApi.ATTR_FIRST_NAME));
    	changeValues.put("DefaultShell", (String)userInfo.get(AceApi.ATTR_DEFAULT_SHELL));
    	
    	// overwrite given values to our changeValues:    	
    	changeValues.putAll(attrs);
    	
    	try {
			api.setUser(changeValues.remove("LastName"), 
					changeValues.remove("FirstName"),
					defaultLogin,
					changeValues.remove("EmailAddress"),
					changeValues.remove("DefaultShell"),
					"-" + defaultLogin,
					changeValues.remove("Password"));
		} catch (AceToolkitException e) {
			throw new IdMUnitException("Error while modifying user in ACE", e);
		}
    	
    	if (changeValues.size() > 0) {
			throw new IdMUnitException("Modification to replace values failed: Attribute(s) [" + changeValues.keySet() + "] is/are unknown.");
		}
    }

    // TODO: automated tests
    private void handleModifyUserRemoveAttrs(Map<String, String> attrs) throws IdMUnitException {
        if (attrs.containsKey("DefaultLogin") == false) {
            throw new IdMUnitException("Default login must be provided to modify Users!");
        }

    	Map<String, String> removeObjectValues = new HashMap<String, String>(attrs);
    	// Set emtpy string values for the add user call so at least none of these are null.
    	removeObjectValues.remove("DefaultLogin");
    	String tokenSerialNumber = removeObjectValues.remove("TokenSerialNumber");    	
        removeObjectValues.remove("objectClass");

    	if (tokenSerialNumber != null && tokenSerialNumber.length() != 0) {
            try {
				api.rescindToken(tokenSerialNumber, false);
			} catch (AceToolkitException e) {
				throw new IdMUnitException("Could not rescind token specified: [" + tokenSerialNumber + "], error:", e);		
			}
    	}
    	
		if (removeObjectValues.size() > 0) {
			throw new IdMUnitException("Modification to remove values failed: Attribute(s) [" + removeObjectValues.keySet() + "] is/are unknown.");
		}
    }
   
    // TODO: automated tests
    private void handleModifyUserAddAttrs(Map<String, String> attrs) throws IdMUnitException {
        if (attrs.containsKey("DefaultLogin") == false) {
            throw new IdMUnitException("Default login must be provided to modify Users!");
        }

        Map<String, String> newObjectValues = new HashMap<String, String>();
    	// Set emtpy string values for the add user call so at least none of these are null.
    	newObjectValues.put("MemberOf", "");
    	newObjectValues.put("TokenSerialNumber", "");
    	newObjectValues.putAll(attrs);
        newObjectValues.remove("objectClass");
    	
		String group = newObjectValues.remove("MemberOf");
		String defaultLogin = newObjectValues.remove("DefaultLogin");
		String tokenSerialNumber = newObjectValues.remove("TokenSerialNumber");
		    	
		if (group != null && group.length() > 0) {
			try {
				// Note: GroupShell is not supported here, "" for now.
		        api.addLoginToGroup("", group, "", "-" + defaultLogin);
			} catch (AceToolkitException e) {
				throw new IdMUnitException("Error while assigning group: '" + group + "'", e);
			}
		}
		
		if (tokenSerialNumber != null && tokenSerialNumber.length() > 0) {
			try {
				api.assignAnotherToken("-" + defaultLogin, tokenSerialNumber);
			} catch (AceToolkitException e) {
				throw new IdMUnitException("Error while creating user: assignAnotherToken: '" + group + "'", e);
			}
		}		
		
		if (newObjectValues.size() > 0) {
			throw new IdMUnitException("Modification to add values failed: Attribute(s) [" + newObjectValues.keySet() + "] is/are unknown.");
		}
    }
    
    public void opValidateObject(Map<String, Collection<String>> data) throws IdMUnitException {
        String objectClass = getSingleValue(data, "objectClass");

        if (objectClass.equals("User")) {
            validateUser(data);
        } else if (objectClass.equals("Token")) {
            validateToken(data);
        } else {
            throw new IdMUnitException("objectClass '" + objectClass + "' not supported");
        }
    }
    
    private void validateToken(Map<String, Collection<String>> data) throws IdMUnitException {
        String serialNumber;
        if (data.get("SerialNumber") != null) {
            serialNumber = getSingleValue(data, "SerialNumber");
        } else {
            serialNumber = getSingleValue(data, "TokenSerialNumber");
        }

        String expectedPIN = getSingleValue(data, "PIN");
        if (expectedPIN != null) {
            String pinSet;
            try {
                pinSet = api.setPin("PIN", serialNumber);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error checking PIN assignment", e);
            }
            
            if (pinSet.equals(expectedPIN) == false) {
                throw new AssertionFailedError("Validation failed: Attribute [TokenPIN] not equal.  Expected dest value: [" + expectedPIN +"] Actual dest value(s): [" + pinSet + "]");
            }
        }

        String expectedDisabled = getSingleValue(data, "Disabled");
        if (expectedDisabled != null) {
	        String enabled = "";
            try {
            	enabled = api.listTokenInfo(serialNumber).get(AceApi.ATTR_DISABLED).equals("TRUE") ? "FALSE" : "TRUE";
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error checking token info for enable status", e);
            }
            String disabled = (enabled.equals("TRUE") ? "FALSE" : "TRUE"); // Set to string value, and reverse boolean as the attr name in ACE is 'enabled'
            if (expectedDisabled.equalsIgnoreCase(disabled) == false) {
                throw new AssertionFailedError("Validation failed: Attribute [Disabled] not equal.  Expected dest value: [" + expectedDisabled +"] Actual dest value(s): [" + disabled + "]");
            }
        }        

        String expectedNewPinMode = getSingleValue(data, "NewPinMode");
		if (expectedNewPinMode != null) {
			String newPinMode = "";
			try {
				newPinMode = (String) api.listTokenInfo(serialNumber).get(AceApi.ATTR_NEW_PIN_MODE);
			} catch (AceToolkitException e) {
				throw new IdMUnitException("Error checking token info for new pin mode value", e);
    		}
            if (!expectedNewPinMode.equalsIgnoreCase(newPinMode)) {
                throw new AssertionFailedError("Validation failed: Attribute [newPinMode] not equal.  Expected dest value: [" + expectedNewPinMode +"] Actual dest value(s): [" + newPinMode + "]");
            }
		}	
    }

    private void validateUser(Map<String, Collection<String>> data) throws IdMUnitException {
        String defaultLogin = getSingleValue(data, "DefaultLogin");

        Map<String, Object> userInfo;
        try {
            userInfo = api.listUserInfo("-" + defaultLogin);
        } catch (AceToolkitException e) {
            throw new IdMUnitException("Error getting user info", e);
        }

        Set<String> attrNames = new HashSet<String>(data.keySet());

        String expectedSerialNumber = getSingleValue(data, "TokenSerialNumber");
        if (expectedSerialNumber != null) {
            attrNames.remove("TokenSerialNumber");
            List<String> serialNumbers = null;
            try {
                serialNumbers = api.getSerialByLogin(defaultLogin, "0");
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error getting assigned token serial numbers", e);
            }

            if (serialNumbers.size() != 1) {
                throw new AssertionFailedError("Validation failed: More than once [TokenSerialNumber] is present.");
            }
            
            String serialNumber = serialNumbers.get(0);
            
            if (serialNumber.equals(expectedSerialNumber) == false) {
                throw new AssertionFailedError("Validation failed: Attribute [TokenSerialNumber] not equal.  Expected dest value: [" + expectedSerialNumber +"] Actual dest value(s): [" + serialNumber + "]");
            }

            String expectedPIN = getSingleValue(data, "TokenPIN");
            if (expectedPIN != null) {
                attrNames.remove("TokenPIN");
                String pinSet;
                try {
                    pinSet = api.setPin("PIN", serialNumber);
                } catch (AceToolkitException e) {
                    throw new IdMUnitException("Error checking PIN assignment", e);
                }
                if (pinSet.equals(expectedPIN) == false) {
                    throw new AssertionFailedError("Validation failed: Attribute [TokenPIN] not equal.  Expected dest value: [" + expectedPIN +"] Actual dest value(s): [" + pinSet + "]");
                }
            }
        }

        String expectedProfileName = getSingleValue(data, "ProfileName");
        if (expectedProfileName != null) {
            attrNames.remove("ProfileName");
            String profileName;
            try {
                profileName = (String) api.listUserInfo("-" + defaultLogin).get(AceApi.ATTR_PROFILE_NAME);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error getting assigned profile", e);
            }
            if (profileName.equals(expectedProfileName) == false) {
                throw new AssertionFailedError("Validation failed: Attribute [ProfileName] not equal.  Expected dest value: [" + expectedProfileName +"] Actual dest value(s): [" + profileName + "]");
            }
        }

        String expectedMemberOf = getSingleValue(data, "MemberOf");
        if (expectedMemberOf != null) {
            attrNames.remove("MemberOf");
            List<String> membershipInfo;
            try {
                membershipInfo = api.listGroupMembership("-" + defaultLogin);
            } catch (AceToolkitException e) {
                throw new IdMUnitException("Error getting group membership", e);
            }
            
            if(!membershipInfo.contains(expectedMemberOf)) {
                throw new AssertionFailedError("Validation failed: User not a member of " + expectedMemberOf + ".");
            }
        }

        Map<String, String> info = new HashMap<String, String>();
        info.put("objectClass", "User");
        
        info.put("UserNumber", (String) userInfo.get(AceApi.ATTR_USER_NUM));
        info.put("LastName", (String) userInfo.get(AceApi.ATTR_LAST_NAME));
        info.put("FirstName", (String) userInfo.get(AceApi.ATTR_FIRST_NAME));
        info.put("EmailAddress", (String) userInfo.get(AceApi.ATTR_EMAIL_ADDRESS));        
        info.put("DefaultLogin", (String) userInfo.get(AceApi.ATTR_DEFAULT_LOGIN));
        info.put("CreatePIN", (String) userInfo.get(AceApi.ATTR_CREATE_PIN));
        info.put("MustCreatePIN", (String) userInfo.get(AceApi.ATTR_MUST_CREATE_PIN));
        info.put("DefaultShell", (String) userInfo.get(AceApi.ATTR_DEFAULT_SHELL));
        info.put("TempUser", (String) userInfo.get(AceApi.ATTR_TEMP_USER));
        //info.put("dateStart", (String) userInfo.get(AceApi.ATTR_START)); // Not part of 8.1 api apparently . . .
        // info.put("todStart", values[9]); NOTE: Doesn't exist anymore. The dateStart is a ctime in longs.
        //info.put("dateEnd", (String) userInfo.get(AceApi.ATTR_END)); // Not part of 8.1 api apparently . . .
        info.put("Password", (String) userInfo.get(AceApi.ATTR_PASSWORD));
        // info.put("todEnd", values[11]); NOTE: Doesn't exist anymore. The dateEnd is a ctime in longs.

        for (String attrName : attrNames) {
            String expected = getSingleValue(data, attrName);
            Object actual = info.get(attrName);
            if (actual == null) {
                throw new AssertionFailedError("Validation failed: Attribute [" + attrName + "] unknown.");
            }
            if (actual.equals(expected) == false) {
                throw new AssertionFailedError("Validation failed: Attribute [" + attrName + "] not equal.  Expected dest value: [" + expected +"] Actual dest value(s): [" + actual + "]");
            }
        }
    }

    private static String getSingleValue(Map<String, Collection<String>> data, String key) {
    	if (data.get(key) == null) {
    		return null;
    	} else {    		
    		return data.get(key).iterator().next();
    	}
	}

	private static Map<String, String> convertToSingleValues(Map<String, Collection<String>> data) throws IdMUnitException {
        Map<String, String> newData = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        
        for (String key : data.keySet()) {
            newData.put(key, getSingleValue(data, key));
        }

        return newData;
    }
}
