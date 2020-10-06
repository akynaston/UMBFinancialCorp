package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

import junit.framework.TestCase;

import com.trivir.ace.api.v71.AceApi71;
import com.trivir.ace.AceToolkitException;

public class ModifyUserTests extends TestCase {
	AceApi71 atk = null;
	Connector conn = new ACE();
	
    private static final String tokenSerialNum = AllTests.tokenSerialNum;
    private static final String tokenSerialNum2 = AllTests.tokenSerialNum2;
    private String userNum = "";
    String startYear;
    String endYear;
    
	public void setUp() throws AceToolkitException, IdMUnitException {
		atk = new AceApi71(AllTests.getDefaultRSAConnConfig());
		try {
			atk.deleteUser("-tuserModify");
		} catch (AceToolkitException e1) {
			// ignore . .
		}
		
//		String revString = atk.apiRev();
//        if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
//            startYear = AllTests.START_YEAR_61; 
//            endYear = AllTests.END_YEAR_61;
//        } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
//            startYear = AllTests.START_YEAR_52; 
//            endYear = AllTests.END_YEAR_61;
//        } else {
//            throw new IdMUnitException("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
//        }
        
		atk.addUser("lastname", "firstname", "tuserModify", "testemail@email.com", "", "password");
//		fail("change to string/string");
//		//String userInfo = atk.listUserInfo("-tuserModify");
//    	String userInfo = "";
//		userNum = userInfo.split(" *[|] *")[0];
//		assertEquals(userNum + " | Kynaston | Aaron | tuserModify | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", userInfo);
		Map<String,Object> userInfo = atk.listUserInfo("-tuserModify"); // (byte)'|');
        
        assertEquals("tuserModify", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals(null, userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(null, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
		try {
            this.conn.setup(AllTests.getDefaultRSAConnConfig());
        } catch (IdMUnitException e) {
            this.atk.destroy();
            throw e;
        }
	}
	
	private void removeUserFromGroup(String user, String group) throws AceToolkitException, IdMUnitException {
		try {
			atk.delLoginFromGroup(user, group);
		} catch (AceToolkitException e) {
			if (!"Sd_DelLoginFromGroup Error Invalid group login".equalsIgnoreCase(e.getMessage())) {
				throw e;
			}
		}
	}
		
	public void tearDown() throws AceToolkitException, IdMUnitException {
		removeUserFromGroup("tuserModify", "TestGroup1");
		removeUserFromGroup("tuserModify", "TestGroup2");
		removeUserFromGroup("tuserModify", "TestGroup3@TestSecurityDomain");
		atk.deleteUser("-tuserModify");
		atk.destroy();
		conn.tearDown();
	}
	
	public void testModifyGroupMembership() throws IdMUnitException, AceToolkitException {
					   
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "MemberOf", "TestGroup1");
			    	
        conn.execute("addAttr", ba);
		
		addSingleValue(ba, "MemberOf", "TestGroup2");
			    	
        conn.execute("addAttr", ba);
		
        List<String> memberships = atk.listGroupMembership("-tuserModify");
        Iterator<String> membershipsIterator = memberships.iterator(); 
        
        assertEquals("TestGroup1",membershipsIterator.next());
        assertEquals("TestGroup2",membershipsIterator.next());
        assertFalse(membershipsIterator.hasNext());
                
	}
	
	public void testModifyGroupMembershipWithSite() throws IdMUnitException, AceToolkitException {
		   
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "MemberOf", "TestGroup3@TestSecurityDomain");
			    	
        conn.execute("addAttr", ba);
		
        List<String> memberships = atk.listGroupMembership("-tuserModify");
        Iterator<String> list = memberships.iterator();
        		
        assertEquals("TestGroup3@TestSecurityDomain", list.next());
        assertFalse(list.hasNext());
	}
	
	public void testModifyUserAddToken() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum);
		
        conn.execute("addAttr", ba);
		
		assertEquals(AllTests.tokenSerialNum, atk.getSerialByLogin("tuserModify", "0").iterator().next());
	}
	
	public void testModifyUserAddMulitpleTokens() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum);
		
        conn.execute("addAttr", ba);

        addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum2);

        conn.execute("addAttr", ba);
		
        Set<String> tokensSet = new HashSet<String>(atk.getSerialByLogin("tuserModify", "0"));
        
        assertTrue(tokensSet.remove(tokenSerialNum));
        assertTrue(tokensSet.remove(tokenSerialNum2));		
	}
	
	public void testModifyUserRemoveToken()throws IdMUnitException, AceToolkitException {

		atk.assignAnotherToken("-tuserModify", AllTests.tokenSerialNum);
		atk.assignAnotherToken("-tuserModify", AllTests.tokenSerialNum2);
		// confirm we're starting with two tokens:
//		assertEquals(AllTests.tokenSerialNum + " , " + AllTests.tokenSerialNum2, atk.getSerialByLogin("tuserModify", "0"));

        Set<String> tokensSet = new HashSet<String>(atk.getSerialByLogin("tuserModify", "0"));
        
        assertTrue(tokensSet.remove(tokenSerialNum));
        assertTrue(tokensSet.remove(tokenSerialNum2));		
		
		// remove one:
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum);		
        conn.execute("removeAttr", ba);
		assertEquals(AllTests.tokenSerialNum2, atk.getSerialByLogin("tuserModify", "0").iterator().next());
		
		// now confirm multiple values work properly:
        ba.clear();
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum2);			    	
        conn.execute("removeAttr", ba);
		assertFalse(atk.getSerialByLogin("tuserModify", "0").iterator().hasNext());
	}
	
	public void testModifyUserFirstName() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "FirstName", "newfirstname");
		
        conn.execute("replaceAttr", ba);
		
		//assertEquals(userNum + " | Kynaston | MyNewFirstName | tuserModify | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuserModify"));
        
        Map<String,Object> userInfo = atk.listUserInfo("-tuserModify"); // (byte)'|');
        
        assertEquals("tuserModify", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("newfirstname", userInfo.remove("FirstName"));
    	assertEquals(null, userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(null, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
        
	}
	
	public void testModifyUserLastName() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "LastName", "MyNewLastName");
		
        conn.execute("replaceAttr", ba);
		
        //assertEquals(userNum + " | MyNewLastName | Aaron | tuserModify | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuserModify"));
        Map<String,Object> userInfo = atk.listUserInfo("-tuserModify"); // (byte)'|');
        
        assertEquals("tuserModify", userInfo.remove("DefaultLogin"));
    	assertEquals("MyNewLastName", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals(null, userInfo.remove("DefaultShell")); // missing
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(null, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));    	        
	}

	public void testModifyUserDefaultShell() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "DefaultShell", "newDefaultShell");
		
        conn.execute("replaceAttr", ba);
		
		//assertEquals(userNum + " | Kynaston | Aaron | tuserModify | 1 | 0 | newDefaultShell | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", atk.listUserInfo("-tuserModify"));
        
        Map<String,Object> userInfo = atk.listUserInfo("-tuserModify"); // (byte)'|');
        
        assertEquals("tuserModify", userInfo.remove("DefaultLogin"));
    	assertEquals("lastname", userInfo.remove("LastName"));
    	assertEquals("firstname", userInfo.remove("FirstName"));
    	assertEquals("newDefaultShell", userInfo.remove("DefaultShell"));
    	//assertEquals("User", userInfo.remove("objectClass")); // not returned by our API
    	// FAILS: assertEquals(groupName, userInfo.remove("MemberOf"));  // not returned by our API
    	assertEquals(null, userInfo.remove("ProfileName"));
    	// FAILS: assertEquals("tokenSerialNum", userInfo.remove("TokenSerialNumber"));  // not returned by our API
    	assertEquals("testemail@email.com", userInfo.remove("EmailAddress"));
    	// FAILS: assertEquals("UsP4ssw0rd1!", userInfo.remove("Password"));  // not returned by our API
    	assertEquals("FALSE", userInfo.remove("TempUser"));
    	
	}

	public void testModifyUserMissingObjectClass() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "DefaultLogin", "tuserModify");
		addSingleValue(ba, "LastName", "TheLastName");
		
		try {
            conn.execute("replaceAttr", ba);
		} catch (IdMUnitException e) {
			assertEquals("No objectClass specified.", e.getMessage());
		}
		
	}

	public void testModifyUserMissingDefaultLogin() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
		addSingleValue(ba, "objectClass", "User");   	
		addSingleValue(ba, "LastName", "TheLastName");
		
		try {
            conn.execute("replaceAttr", ba);
		} catch (IdMUnitException e) {
			assertEquals("Default login must be provided to modify Users!", e.getMessage());
		}
		
	}

    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
