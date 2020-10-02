/*+------------------------------------------------------------------------------------------------
 ||         1. Create functionality to test home directory create/delete request logs 
 ||            (SSH connector + SSH server for windows vm) 
 ||         2. Update PowerShell connector to be able to validate the three ACL's covering 
 ||            'user can't change password' flag.
 ||			3. We need the smtp connector to test regular expressions; so far we haven't had 
 ||            any issues with this, so it is still on old.
 ||         4. AK -19/20/26 - do we overwrite passwords, email address on subscriber matches in 
 ||               the staff driver?
 ||         5. AK-(Is pwdLastSet 0 on a match?) - 19 email validate-noregex support 
 ||         6. Need IdMUnit table reader for group validation 
 |+------------------------------------------------------------------------------------------------*/

package org.idmunit;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.idmunit.parser.ExcelParser;


/**
	This is a sample test runner and shows how the tests can be executed through a java runner file.
	See step 6., section ii. for details in the IdMUnitUsageGuide.html.
*/
public class SampleReferenceFromDocs extends TestCase {
		
	public static final String idmunitSpreadsheetCleanup="test/org/idmunit/SampleReferenceFromDocs.xls";

	// Main suite function
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(getTests()); // clean up the entire environment; removing all test accounts and framework accounts.		
		return suite;
	}
	
	// Used to run specific tests on demand:
	public static Test getTests() {
		TestSuite suite = new TestSuite();
		suite.addTest(ExcelParser.parseSheets(idmunitSpreadsheetCleanup
				,"testReference"
				,"testCleanup"
				,"testHeaders"				
		));
		return suite;
	}

	
}
