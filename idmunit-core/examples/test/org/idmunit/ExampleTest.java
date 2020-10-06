/**************************************************************************
 *  IdMUnit - Automated Testing Framework for Identity Management Solutions
 *
 * Purpose of this file: This is a sample test runner that provides an interface for
 * the selection and execution of a spreadsheet and the sheets therein.
 *
 *
 *******************************************************************************/
package org.idmunit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.idmunit.parser.ExcelParser;


public class ExampleTest extends TestCase {
    //This function is what sends the test data to the JUnit plugin in Designer.
    public static Test suite() {
        //This Test will run just the selected tests in the referenced xls file.
        Test exampleRunSelectedTests = ExcelParser.parseSheets(
                "test/org/idmunit/ExampleTest.xls"
                , "testConnections"
                , "testCleanup"
                , "testInitialize"
                , "test1_0AddUser"
        );

		/*

		//This Test will run ALL the tests in the referenced xls file.
		//Remember to run the .addTest method below if using the parseAllSheets function.
		Test exampleRunAllTestsInWorkbook = ExcelParser.parseAllSheets(
				"test/org/idmunit/ExampleTest.xls"
		);
		
		//*/

        //This is how to run the tests defined above.
        TestSuite testRun = new TestSuite();
        testRun.addTest(exampleRunSelectedTests);
        //testRun.addTest(exampleRunAllTestsInWorkbook);  //This Test object is commented out above.
        return testRun;

    }

}