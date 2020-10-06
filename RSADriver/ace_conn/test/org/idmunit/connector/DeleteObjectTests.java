package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.idmunit.IdMUnitException;

import com.trivir.ace.api.v71.AceApi71;
import com.trivir.ace.AceToolkitException;

public class DeleteObjectTests extends TestCase {
	AceApi71 atk = null;
	Connector conn = new ACE();
    
    protected void setUp() throws IdMUnitException, AceToolkitException {		
		atk = new AceApi71(AllTests.getDefaultRSAConnConfig());
		
		try {
			this.conn.setup(AllTests.getDefaultRSAConnConfig());
		} catch (IdMUnitException e) {
			this.atk.destroy();
			throw e;
		}
		
		try {
			atk.deleteUser("-tuserDelete");
		} catch (AceToolkitException e) {
			if (!"User with login 'tuserDelete' not found. (2163)".equalsIgnoreCase(e.getMessage().toString())) {
				throw e;
			}
		}
		atk.addUser("Kynaston", "Aaron", "tuserDelete", "email@email.com", "", "password");		
    }
    
    
    protected void tearDown() throws AceToolkitException {
		try {
			atk.deleteUser("-tuserDelete");
		} catch (AceToolkitException e) { }
		atk.destroy();
    }
    
    public void testDeleteUser() throws IdMUnitException, AceToolkitException {
		   
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "DefaultLogin", "tuserDelete");
        addSingleValue(ba, "objectClass", "User");   	
			    	
		atk.listUserInfo("-tuserDelete");
		try {
			conn.execute("deleteObject", ba);
		} catch (IdMUnitException e) {
			fail("Should not have thrown exception: " + e);
		}
		
		try {
			atk.listUserInfo("-tuserDelete");
			fail("Should have thrown exception, user should have been deleted.");
		} catch(AceToolkitException e) {
			assertTrue(e.getMessage().contains("2163"));
		}		
    }

    public void testDeleteNonExistentUser() throws IdMUnitException, AceToolkitException {
		   
        Map<String, Collection<String>> ba = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(ba, "DefaultLogin", "tuserDeleteDOESNTEXIST");
        addSingleValue(ba, "objectClass", "User");   	
			    	
		try {
            conn.execute("deleteObject", ba);
		} catch (IdMUnitException e) {
			fail("Should not have thrown exception, ace_conn ignores non exitsting user!" +  e);
		}
    }

    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
