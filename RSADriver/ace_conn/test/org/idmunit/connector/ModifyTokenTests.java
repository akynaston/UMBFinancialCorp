package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

import junit.framework.TestCase;

import com.trivir.ace.api.v71.AceApi71;
import com.trivir.ace.AceToolkitException;
import com.trivir.idmunit.connector.RsaConnector;

public class ModifyTokenTests extends TestCase {
	AceApi71 atk = null;
	Connector conn = new RsaConnector();
	
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
		
        String revString = atk.apiRev();
        /*
        if (revString.startsWith("Release: 6.1")) {  //Release: 6.1, Date: Oct 26 2005 14:39:38
            startYear = AllTests.START_YEAR_61; 
            endYear = AllTests.END_YEAR_61;
        } else if (revString.startsWith("Release: 5.2")) { //Release: 5.2, Date: Nov  4 2003 11:28:11
            startYear = AllTests.START_YEAR_52; 
            endYear = AllTests.END_YEAR_61;
        } else {
            throw new IdMUnitException("FATAL: Don't know how to handle new ace version: [" + revString + "]");                
        }
        */

		atk.addUser("lastname", "firstname", "tuserModify", "testemail@email.com", "", "password");
		//String userInfo = atk.listUserInfo("-tuserModify");
		// 		String userInfo = "";
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
	    	
		//userNum = userInfo.split(" *[|] *")[0];		
		//assertEquals(userNum + " | Kynaston | Aaron | tuserModify | 1 | 0 |  | 0 | 01/01/" + startYear + " | 0 | 01/01/" + endYear + " | 0", userInfo);
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
			if (!e.getMessage().contains("not found")) {
				throw e;
			}
		}
	}
		
	public void tearDown() throws AceToolkitException, IdMUnitException {
		removeUserFromGroup("tuserModify", "Test Group1");
		removeUserFromGroup("tuserModify", "Test Group2");
		removeUserFromGroup("tuserModify", "Test Group3@TestSite");
		atk.deleteUser("-tuserModify");
		atk.destroy();
		conn.tearDown();
	}

	public void testModifyTokenMissingTokenSerialNumber() throws IdMUnitException, AceToolkitException {
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "objectClass", "Token");   	
        addSingleValue(ba, "LastName", "TheLastName");
		
		try {
            conn.execute("replaceAttr", ba);
		} catch (IdMUnitException e) {
			assertEquals("TokenSerialNumber must be provided to modify Tokens!", e.getMessage());
		}
		
	}
	
	public void testModifyTokenSetDisabled() throws IdMUnitException, AceToolkitException {
		atk.assignAnotherToken("-tuserModify", AllTests.tokenSerialNum);
    	// Currently enabled:
		
		//assertEquals("1", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
		
		assertEquals("FALSE", atk.listTokenInfo(AllTests.tokenSerialNum).get("Disabled"));
		

        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(ba, "objectClass", "Token");     
        addSingleValue(ba, "Disabled", "TRUE");
			
        conn.execute("replaceAttr", ba);
//        fail("change to string/string");
//        assertEquals("TRUE", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12].equals("1") ? "FALSE" : "TRUE");
        assertEquals("TRUE", atk.listTokenInfo(AllTests.tokenSerialNum).get("Disabled"));
        
	}
	
	public void testModifyTokenSetEnabled() throws IdMUnitException, AceToolkitException {
		atk.assignAnotherToken("-tuserModify", AllTests.tokenSerialNum);
		atk.disableToken(AllTests.tokenSerialNum);
		// Currently disabled:
		//    	assertEquals("0", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12]);
		
		assertEquals("TRUE", atk.listTokenInfo(AllTests.tokenSerialNum).get("Disabled"));

        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(ba, "objectClass", "Token");     
        addSingleValue(ba, "Disabled", "FALSE");
			
        conn.execute("replaceAttr", ba);
        //        assertEquals("FALSE", atk.listTokenInfo(tokenSerialNum).split(" *, *")[12].equals("1") ? "FALSE" : "TRUE");
        assertEquals("FALSE", atk.listTokenInfo(AllTests.tokenSerialNum).get("Disabled"));
	}

    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
