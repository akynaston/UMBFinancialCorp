package org.idmunit.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.idmunit.IdMUnitException;

import com.trivir.ace.api.v71.AceApi71;
import com.trivir.ace.AceToolkitException;

import junit.framework.TestCase;

public class VerifyTokenTests extends TestCase {
    AceApi71 atk;
    Connector conn = new ACE();
    
    public static final String startDate = "11/11/2011";
    public static final int startHour = 11;// 11 am in utc
    public static final String endDate = "12/12/2012";
    public static final int endHour = 12; // 12 pm in utc

    private static final String tokenSerialNum = AllTests.tokenSerialNum;
    private static final String tokenDefaultPin = "9999";

    
    protected void setUp() throws IdMUnitException, AceToolkitException {
        atk = new AceApi71(AllTests.getDefaultRSAConnConfig());
        
        createTempUser("tuserToken", "user", "test", startDate, startHour, endDate, endHour);
        atk.assignAnotherToken("-tuserToken", tokenSerialNum);
        //atk.setPin(tokenDefaultPin, tokenSerialNum);
                
        try {
            this.conn.setup(AllTests.getDefaultRSAConnConfig());
        } catch (IdMUnitException e) {
            this.atk.destroy();
            throw e;
        }
    }
    
    protected void tearDown() throws IdMUnitException, AceToolkitException {
    	atk.deleteUser("-tuserToken");    	
        atk.destroy();
        conn.tearDown();
    }
    
    public void testValidateNewPinMode() throws IdMUnitException {
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "NewPinMode", "TRUE");

        conn.execute("validateObject", attrs);
    }
    
    private void createTempUser(String defaultLogin, String lastName, String firstName, String startDate, int startHour, String endDate, int endHour) throws AceToolkitException {
        // ========================================================
        // Added the following to clean up user if it already exists - tear down appears to not be hit at times . .
        try {
        	atk.listUserInfo("-" + defaultLogin);
            atk.deleteUser("-" + defaultLogin);
            System.out.println("Note: had to delete [" + defaultLogin + "] . .");
        } catch (AceToolkitException e) { }        
        // ========================================================
        
        String emailAddress = "hardcodedemail@email.com";
        String password = "default password";
        
        atk.addUser(lastName, firstName, defaultLogin, emailAddress, "", password);        
        // ensure our test user is a temp user.
        atk.setTempUser(startDate, startHour, endDate, endHour, "-" + defaultLogin);
    }

    public void testValidateNewPinModeOff() throws IdMUnitException, AceToolkitException {
    	
    	atk.setPin("1234", tokenSerialNum);
    	
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "NewPinMode", "FALSE");

        conn.execute("validateObject", attrs);
    }
    
    public void testValidateSetPIN() throws IdMUnitException, AceToolkitException {
    	
    	atk.setPin("1234", tokenSerialNum);
    	
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "PIN", "TRUE");

        conn.execute("validateObject", attrs);
    }
    
    public void testValidateClearPIN() throws IdMUnitException, AceToolkitException {
    	
    	// Pin is already cleared and in new pin mode after setup; don't need to explicitly clear it.
    	//atk.setPin("", tokenSerialNum);
    	
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "PIN", "FALSE");

        conn.execute("validateObject", attrs);
    }
  
    public void testValidateTokenEnabled() throws IdMUnitException, AceToolkitException {

    	atk.setPin("1234234", AllTests.tokenSerialNum);
    	atk.enableToken(tokenSerialNum);
    	    	    	
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "Disabled", "FALSE");
   
        conn.execute("validateObject", attrs);
    }
  
    public void testValidateTokenDisabled() throws IdMUnitException, AceToolkitException {
    	  
    	atk.disableToken(tokenSerialNum);
    	
        Map<String, Collection<String>> attrs = new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER);
        addSingleValue(attrs, "objectClass", "Token");
        addSingleValue(attrs, "TokenSerialNumber", AllTests.tokenSerialNum);
        addSingleValue(attrs, "Disabled", "TRUE");

        conn.execute("validateObject", attrs);
    }
  
    private static void addSingleValue(Map<String, Collection<String>> data, String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        data.put(name, values);
    }
}
