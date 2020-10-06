package com.trivir.ace;

import com.trivir.ace.api.TestTimeUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static final String START_YEAR_61  = "1986";
    public static final String END_YEAR_61  = "1986";
    
    public static final String START_YEAR_52  = "2001";
    public static final String END_YEAR_52  = "2010";
		    
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(UserTests.class));
        suite.addTest(new TestSuite(TokenAssignmentTests.class));
        suite.addTest(new TestSuite(TestTimeUtil.class));        
        return suite; 
    }


}
