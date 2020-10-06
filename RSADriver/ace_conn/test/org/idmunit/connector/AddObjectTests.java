package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

import com.trivir.ace.AceToolkitException;
import com.trivir.ace.api.AceApi;
import com.trivir.ace.api.v71.AceApi71;

import junit.framework.TestCase;

public class AddObjectTests extends TestCase {
    private AceApi atk;
    private Connector conn = new ACE();
    //private static final String groupName = "VPN - Remotely Anywhere Applications"; // MemberOf - See user is member of group after assignment 
    //private static final String profileName = "VPN - Remotely Anywhere Applications"; // ProfileName - test assignment - succeeds/fails
    
   
    //private static final String tokenSerialNum = "000044619408"; // Currently, the only available token in system. assigned to frankh
    /*
     *  tokens in system:
     *  https://rsa-am81.example.com:7004/console-ims/ListToken.do?pageaction=nvPreSearchAll&ptoken=9BVJ1CM9545QJJI7
     *  
     *  	
     *  Serial Number000157228469	SecurID 700			 	5/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000157228470	SecurID 700			 	5/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000157228471	SecurID 700			 	5/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000157228472	SecurID 700			 	5/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000157228473	SecurID 700			 	5/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000158770497	SecurID Software Token			 	3/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000158770498	SecurID Software Token			 	3/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000158770499	SecurID Software Token			 	3/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000158770500	SecurID Software Token			 	3/30/19 6:00:00 PM MDT	SystemDomain
	Serial Number000158770501
     *  
     */
    
    protected void setUp() throws IdMUnitException, AceToolkitException {
        atk = new AceApi71(AllTests.getDefaultRSAConnConfig());
        
        try {
            this.conn.setup(AllTests.getDefaultRSAConnConfig());
        } catch (IdMUnitException e) {
            this.atk.destroy();
            throw e;
        }
		try {
			atk.deleteUser("-tuserAdd");
		} catch (AceToolkitException e) { }

    }
    
    protected void tearDown() throws AceToolkitException {
		try {
			atk.deleteUser("-tuserAdd");
		} catch (AceToolkitException e) { }
		atk.destroy();
    }

    public void testAddUser() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");
    	
        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);
        
        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        
    	conn.execute("addObject", alDataValues);
    	Map<String, Object> userInfo = atk.listUserInfo("-tuserAdd"); //, (byte)'|');
    	// 2192 | lastname | firstname | tuserAdd | 1 | 0 | none | 0 | 01/01/1986 | 0 | 01/01/1986 | 0
    			//"tuserAdd , none , VPN - Remotely Anywhere Applications ,
    	        // VPN - Remotely Anywhere Applications
    	
    	//int index = userInfo.indexOf('|');
    	
    	assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals("noShell", userInfo.remove("DefaultShell"));
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(AllTests.profileName, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	
//    	assertEquals("| lastname | firstname | tuserAdd | 1 | 0 | noShell | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));
//    	fail("change to string/string");
    	//assertEquals(profileName, atk.listUserInfoExt("-tuserAdd", 7));
    	assertEquals("TestGroup1", atk.listGroupMembership("-tuserAdd").iterator().next());
    	//Don't do this any more: just returns list: assertEquals("Done", atk.listGroupMembership("-tuserAdd"));
    	assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserAdd", "0").iterator().next());
    	
    }
    
    public void testAddUserMissingDefaultLogin() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

		try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown an exception . .");
		} catch (IdMUnitException e) {
			// old api 71 error: //assertEquals("Error adding user and assinging token: com.trivir.ace.AceToolkitException: Sd_AssignToken Error Invalid login", e.getCause());
			assertEquals("com.rsa.command.exception.ValidationException: Invalid input data. Validation failed. Field: 'User ID', message: 'User ID is a required field.'", e.getCause().toString());
		}    	
    }
    
    public void testAddUserMissingDefaultLoginAndToken() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);

        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown an exception . .");
		} catch (IdMUnitException e) {
			// old api 71 error:			//assertEquals("Error adding user: com.trivir.ace.AceToolkitException: Sd_AddUser Error Invalid login/last name", e.getMessage());
			assertEquals("com.rsa.command.exception.ValidationException: Invalid input data. Validation failed. Field: 'User ID', message: 'User ID is a required field.'", e.getCause().toString());
		}    	
    }
        
    public void testAddUserMissingLastName() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");
    	
        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown an exception . .");
		} catch (IdMUnitException e) {
			// old API 71 error:assertEquals("Error adding user and assinging token: com.trivir.ace.AceToolkitException: Sd_AssignToken Error Invalid last name", e.getMessage());
			assertEquals("com.rsa.command.exception.ValidationException: Invalid input data. Validation failed. Field: 'Last Name', message: 'Last Name is a required field.'", e.getCause().toString());			
		}    	
    }
    
    public void testAddUserMissingFirstName() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");
    	
        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);
        
        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

    	conn.execute("addObject", alDataValues);
    	
    	Map<String,Object> userInfo = atk.listUserInfo("-tuserAdd"); // (byte)'|');
//    	int index = userInfo.indexOf('|');
//    	assertEquals("| lastname |  | tuserAdd | 1 | 0 | noShell | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));
    	
    	assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals(null, userInfo.remove("FirstName")); // first name is missing
    	assertEquals("noShell", userInfo.remove("DefaultShell"));
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(AllTests.profileName, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	

    	//assertEquals(profileName, atk.listUserInfoExt("-tuserAdd", 7));
    	assertEquals("TestGroup1", atk.listGroupMembership("-tuserAdd").iterator().next());
    	assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserAdd", "0").iterator().next());
    }

    public void testAddUserMissingDefaultShell() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
    	
        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        conn.execute("addObject", alDataValues);
    	
		// this creation is allowed - lets confirm our default shell field is blank . .
    	//String userInfo = atk.listUserInfo("-tuserAdd");
    	//String userInfo = "";
    	//int index = userInfo.indexOf('|');
    	//assertEquals("| lastname | firstname | tuserAdd | 1 | 0 |  | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));
    	//fail("change to string/string");
        
        Map<String,Object> userInfo = atk.listUserInfo("-tuserAdd"); // (byte)'|');
        
        assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals(null, userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(AllTests.profileName, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	
        
//    	assertEquals(profileName, atk.listUserInfoExt("-tuserAdd", 7));
    	assertEquals("TestGroup1", atk.listGroupMembership("-tuserAdd").iterator().next());
    	assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserAdd", "0").iterator().next());
    }
    
    public void testAddUserUknownAttr() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);
    	
        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        addSingleValue(alDataValues, "Doesntgetprocessed", "someextravalue");

		try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown exception!");
		} catch (Exception e) {
			assertEquals("Add failed: Attribute(s) [[Doesntgetprocessed]] is/are unknown.", e.getMessage());
		}
    }
    	
    public void testAddUserMissingObjectClass() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);
    	
        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        addSingleValue(alDataValues, "Doesntgetprocessed", "someextravalue");

		try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown exception!");
		} catch (Exception e) {
			assertEquals("objectClass 'no class specified' not supported, only 'User' is supported.", e.getMessage());
		}
		
    }
	
    public void testAddUserMissingGroup() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
		} catch (Exception e) {
			fail("Failed adding object for test: " + e);
		}		

		//String userInfo = atk.listUserInfo("-tuserAdd");
    	//String userInfo = "";
		//int index = userInfo.indexOf('|');    	
    	//assertEquals("| lastname | firstname | tuserAdd | 1 | 0 | noShell | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));
    	//fail("change to string/string");
		
		Map<String,Object> userInfo = atk.listUserInfo("-tuserAdd"); // (byte)'|');
	        
        assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals("noShell", userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(AllTests.profileName, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	
	        
	    	
		
    	//assertEquals(profileName, atk.listUserInfoExt("-tuserAdd", 7));
    	// see that we have no groups:
    	assertFalse(atk.listGroupMembership("-tuserAdd").iterator().hasNext());
    	assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserAdd", "0").iterator().next());
    	
    }

    public void testAddUserMissingProfile() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
		} catch (Exception e) {
			fail("Failed adding object for test: " + e);
		}		

//    	String userInfo = atk.listUserInfo("-tuserAdd");
    	//String userInfo = "";
		//int index = userInfo.indexOf('|');
    	//assertEquals("| lastname | firstname | tuserAdd | 1 | 0 | noShell | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));
    	// See no profile returned . .
		
		Map<String,Object> userInfo = atk.listUserInfo("-tuserAdd"); // (byte)'|');
        
        assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals("noShell", userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(null, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	
		
    	//assertEquals("", atk.listUserInfoExt("-tuserAdd", 7));
    	assertEquals("TestGroup1", atk.listGroupMembership("-tuserAdd").iterator().next());
    	assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserAdd", "0").iterator().next());
    }
    
    public void testAddUserInvalidProfile() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", "Someprofilethatdoesntexist");
        addSingleValue(alDataValues, "TokenSerialNumber", AllTests.tokenSerialNum);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertEquals("Error while assigning profile: 'Someprofilethatdoesntexist'", e.getMessage());
		}		
    }
    
    public void testAddUserMissingTokenSerialNumber() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> alDataValues = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
    	
        addSingleValue(alDataValues, "DefaultLogin", "tuserAdd");
        addSingleValue(alDataValues, "LastName", "lastname");
        addSingleValue(alDataValues, "FirstName", "firstname");
        addSingleValue(alDataValues, "DefaultShell", "noShell");

        addSingleValue(alDataValues, "objectClass", "User");
        addSingleValue(alDataValues, "MemberOf", AllTests.groupName);
        addSingleValue(alDataValues, "ProfileName", AllTests.profileName);

        addSingleValue(alDataValues, "EmailAddress", "testemail@email.com");
        addSingleValue(alDataValues, "Password", "P4ssw0rd1!");

        try {
			conn.execute("addObject", alDataValues);
		} catch (Exception e) {
			fail("Failed adding object for test: " + e);
		}		

//		String userInfo = atk.listUserInfo("-tuserAdd");
//		int index = userInfo.indexOf('|');
//    	assertEquals("| lastname | firstname | tuserAdd | 1 | 0 | noShell | 0 | 01/01/1986 | 0 | 01/01/1986 | 0", userInfo.substring(index));

		Map<String,Object> userInfo = atk.listUserInfo("-tuserAdd"); // (byte)'|');
        
        assertEquals("tuserAdd", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals("noShell", userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals("TESTPROFILE1", userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    			
		
    	//    	assertEquals(profileName, atk.listUserInfoExt("-tuserAdd", 7));
    	assertEquals("TestGroup1", atk.listGroupMembership("-tuserAdd").iterator().next());
    	// see no tokens returned . .
    	assertFalse(atk.getSerialByLogin("tuserAdd", "0").iterator().hasNext());
    }

    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
