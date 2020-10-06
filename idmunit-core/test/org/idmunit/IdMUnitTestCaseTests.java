/*
 * IdMUnit - Automated Testing Framework for Identity Management Solutions
 * Copyright (c) 2005-2016 TriVir, LLC
 *
 * This program is licensed under the terms of the GNU General Public License
 * Version 2 (the "License") as published by the Free Software Foundation, and
 * the TriVir Licensing Policies (the "License Policies").  A copy of the License
 * and the Policies were distributed with this program.
 *
 * The License is available at:
 * http://www.gnu.org/copyleft/gpl.html
 *
 * The Policies are available at:
 * http://www.idmunit.org/licensing/index.html
 *
 * Unless required by applicable law or agreed to in writing, this program is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied.  See the License and the Policies
 * for specific language governing the use of this program.
 *
 * www.TriVir.com
 * TriVir LLC
 * 13890 Braddock Road
 * Suite 310
 * Centreville, Virginia 20121
 *
 */

package org.idmunit;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.connector.Connector;

import java.util.*;

import static org.easymock.EasyMock.*;

public class IdMUnitTestCaseTests extends TestCase {
    private static final String OP_TEST = "test";
    private static final long EXPECTED_TIME_DIFF = 50;
    private static final long DEFAULT_WAIT_INTERVAL = 1000;
    private IdMUnitTestCase testCase;
    private Map<String, Collection<String>> attrMap;

    static void assertExpectedTimeVersusActualTime(long expectedTime, long duration) {
        long expectedMax = expectedTime + EXPECTED_TIME_DIFF;
        long expectedMin = expectedTime - EXPECTED_TIME_DIFF;
        if (duration > expectedMax) {
            fail(String.format("Operation took too long. Expected: %dms, Actual: %dms", expectedTime, duration));
        }
        if (duration < expectedMin) {
            fail(String.format("Operation didn't take long enough. Expected: %dms, Actual: %dms", expectedTime, duration));
        }
    }

    @SuppressWarnings("serial")
    protected void setUp() throws Exception {
        testCase = new IdMUnitTestCase("Testing");

        attrMap = Collections.unmodifiableMap(new HashMap<String, Collection<String>>() {{
                put("attr1", Arrays.asList(new String[]{"val1"}));
                put("attr2", Arrays.asList(new String[]{"val2"}));
                put("attr3", Arrays.asList(new String[]{"val3.1", "val3.2"}));
            }});

        testCase.setAttributeMap(attrMap);
        testCase.setComment("Default Comment");
        ConnectionConfigData configData = new ConnectionConfigData("Default Connection", "Default Type");
        testCase.setConfig(configData);
        testCase.setCritical(false);
        testCase.setDisabled(false);
        testCase.setFailureExpected(false);
        testCase.setOperation(OP_TEST);
        testCase.setRetryCount(0);
        testCase.setWaitInterval(DEFAULT_WAIT_INTERVAL);
    }

    protected void tearDown() throws Exception {
        testCase = null;
    }

    public void testRunTest_Success() throws IdMUnitException {
        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_IdMUnitFailure() {
        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an AssertionFailedError");
        } catch (IdMUnitException e) {
            fail("Unexpected IdMUnitException");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0;
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_IdMUnitException() {
        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitException("Test failed")).once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an IdMUnitException");
        } catch (IdMUnitException e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0;
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_RowDisabled() throws IdMUnitException {
        testCase.setDisabled(true);

        Connector connector = createStrictMock(Connector.class);
        //Connector shouldn't call anything
        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_OperationComment() throws IdMUnitException {
        testCase.setOperation(IdMUnitTestCase.OP_COMMENT);

        Connector connector = createStrictMock(Connector.class);
        //Connector shouldn't call anything
        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_ConnectionDisabled() throws IdMUnitException {
        testCase.getConfig().setParam(ConnectionConfigData.DISABLED, "true");

        Connector connector = createStrictMock(Connector.class);
        //Connector shouldn't call anything
        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_FailureExpectedTestSucceeded() {
        testCase.setFailureExpected(true);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an IdMUnitException");
        } catch (IdMUnitException e) {
            fail("Unexpected IdMUnitException");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0;
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed: would have succeeded, but ExpectFailure was set to TRUE", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_FailureExpectedTestFailed() {
        testCase.setFailureExpected(true);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitException("Test failed")).once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        try {
            long start = System.currentTimeMillis();
            testCase.runTest();
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0;
            assertExpectedTimeVersusActualTime(expectedTime, duration);
        } catch (IdMUnitException e) {
            fail("Unexpected IdMUnitException");
        } catch (AssertionFailedError e) {
            fail("Unexpected AssertionFailedError");
        }

        verify(connector);
    }

    public void testRunTest_OperationWaitOrPause() throws IdMUnitException {
        long longerWaitInterval = DEFAULT_WAIT_INTERVAL * 2;

        testCase.setOperation(IdMUnitTestCase.OP_WAIT);
        testCase.setWaitInterval(longerWaitInterval);

        Connector connector = createStrictMock(Connector.class);
        //Connector shouldn't call anything
        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = longerWaitInterval;
        assertExpectedTimeVersusActualTime(expectedTime, duration);


        verify(connector);

        testCase.setOperation(IdMUnitTestCase.OP_PAUSE);
        testCase.setWaitInterval(longerWaitInterval);

        connector = createStrictMock(Connector.class);
        //Connector shouldn't call anything
        replay(connector);

        testCase.setConnector(connector);

        start = System.currentTimeMillis();
        testCase.runTest();
        duration = System.currentTimeMillis() - start;
        expectedTime = longerWaitInterval;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_ThreeTriesAllFails() throws IdMUnitException {
        int rowRetryMultiplier = 3;

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).times(rowRetryMultiplier);
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            testCase.setRetryCount(rowRetryMultiplier);
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an AssertionFailedError");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = DEFAULT_WAIT_INTERVAL * (rowRetryMultiplier - 1);
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_ThreeTriesFirstSucceeds() {
        //TODO This test is written to expect 0 wait intervals. The current implementation waits 1 interval when there are successive retries.
        int rowRetryMultiplier = 3;
        testCase.setRetryCount(rowRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0;
            assertExpectedTimeVersusActualTime(expectedTime, duration);

        } catch (IdMUnitException e) {
            fail("Unexpected IdMUnitException");
        }

        verify(connector);
    }

    public void testRunTest_ThreeTriesSecondSucceeds() throws IdMUnitException {
        //TODO This test is written to expect 1 wait interval. The current implementation waits 2 intervals when there are successive retries.
        int rowRetryMultiplier = 3;
        testCase.setRetryCount(rowRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).once();
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = DEFAULT_WAIT_INTERVAL;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_WaitMultiplier_ThreeTriesAllFail() throws IdMUnitException {
        int waitMultiplier = 2;
        int rowRetryMultiplier = 3;
        testCase.getConfig().setMultiplierWait(waitMultiplier);
        testCase.setRetryCount(rowRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).times(rowRetryMultiplier);
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an AssertionFailedError");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = DEFAULT_WAIT_INTERVAL * waitMultiplier * (rowRetryMultiplier - 1);
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_WaitMultiplier_ThreeTriesFirstSucceeds() throws IdMUnitException {
        //TODO This test is written to expect 0 wait intervals. The current implementation waits 1 interval when there are successive retries.
        int waitMultiplier = 2;
        int rowRetryMultiplier = 3;
        testCase.getConfig().setMultiplierWait(waitMultiplier);
        testCase.setRetryCount(rowRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_WaitMultiplier_ThreeTriesSecondSucceeds() throws IdMUnitException {
        //TODO This test is written to expect 1 wait interval. The current implementation waits 2 intervals when there are successive retries.
        int waitMultiplier = 2;
        int rowRetryMultiplier = 3;
        testCase.getConfig().setMultiplierWait(waitMultiplier);
        testCase.setRetryCount(rowRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).once();
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = waitMultiplier * DEFAULT_WAIT_INTERVAL;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_RetryMultiplier_ThreeTriesAllFail() throws IdMUnitException {
        int rowRetryMultiplier = 3;
        int configRetryMultiplier = 2;
        testCase.setRetryCount(rowRetryMultiplier);
        testCase.getConfig().setMultiplierRetry(configRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).times(rowRetryMultiplier * configRetryMultiplier);
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected an AssertionFailedError");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = DEFAULT_WAIT_INTERVAL * (configRetryMultiplier * rowRetryMultiplier - 1);
            assertExpectedTimeVersusActualTime(expectedTime, duration);

            assertEquals("Test failed", e.getMessage());
        }

        verify(connector);
    }

    public void testRunTest_RetryMultiplier_ThreeTriesFirstSucceeds() throws IdMUnitException {
        //TODO This test is written to expect 0 wait intervals. The current implementation waits 1 interval when there are successive retries.
        int rowRetryMultiplier = 3;
        int retryMultiplier = 2;
        testCase.setRetryCount(rowRetryMultiplier);
        testCase.getConfig().setMultiplierRetry(retryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_RetryMultiplier_ThreeTriesSecondSucceeds() throws IdMUnitException {
        int rowRetryMultiplier = 3;
        int configRetryMultiplier = 2;    //This value does not effect this use case. This multiplies the number of retries. NOT the number WAIT_TIME.
        testCase.setRetryCount(rowRetryMultiplier);
        testCase.getConfig().setMultiplierRetry(configRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).once();
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = DEFAULT_WAIT_INTERVAL;    //Still just takes the 1 wait interval since the configRetryMultiplier doesn't effect the WAIT_TIME.
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }

    public void testRunTest_WaitMultiplierAndRetryMultiplierAllFail() throws IdMUnitException {
        int rowRetryCount = 2;
        int configRetryMultiplier = 2;
        int configWaitMultiplier = 2;
        testCase.setRetryCount(rowRetryCount);
        testCase.getConfig().setMultiplierRetry(configRetryMultiplier);
        testCase.getConfig().setMultiplierWait(configWaitMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).times(configRetryMultiplier * rowRetryCount);
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = configWaitMultiplier * DEFAULT_WAIT_INTERVAL * (configRetryMultiplier * rowRetryCount - 1);
            assertExpectedTimeVersusActualTime(expectedTime, duration);
        }

        verify(connector);
    }

    public void testRunTest_NoRetryCountAndRetryMultiplierAllFail() throws IdMUnitException {
        /*TODO:
		 * This test is correct, but it's failing with the current implementation.
		 *
		 * Because a retryCount of 0 and 1 both result in 1 test run, it is unclear if
		 * there should be multiple test runs when configRetryMultiplier > 1.
		 *
		 * The current implementation will run 1 time if the retryCount is set to 0 regardless of what the retryMultiplier is set to.
		*/
        int rowRetryCount = 0;
        int configRetryMultiplier = 2;
        testCase.setRetryCount(rowRetryCount);
        testCase.getConfig().setMultiplierRetry(configRetryMultiplier);

        Connector connector = createMock(Connector.class);
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().andThrow(new IdMUnitFailureException("Test failed")).times(1);
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = Long.MAX_VALUE;
        try {
            start = System.currentTimeMillis();
            testCase.runTest();

            fail("Expected AssertionFailedError");
        } catch (AssertionFailedError e) {
            long duration = System.currentTimeMillis() - start;
            long expectedTime = 0; //There is no time spent here since retries = 0. No waiting is involved in this use case.
            assertExpectedTimeVersusActualTime(expectedTime, duration);
        }

        verify(connector);
        //Not sure why this happens - Carl
/*
 * java.lang.AssertionError:
  Expectation failure on verify:
    Connector.execute("test", {attr2=[val2], attr1=[val1], attr3=[val3.1, val3.2]}): expected: 2, actual: 1
	at org.easymock.internal.MocksControl.verify(MocksControl.java:226)
	at org.easymock.EasyMock.verify(EasyMock.java:2080)
	at org.idmunit.IdMUnitTestCaseTests.testRunTest_NoRetryCountAndRetryMultiplierAllFail(IdMUnitTestCaseTests.java:653)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Unknown Source)
	at junit.framework.TestCase.runTest(TestCase.java:154)
	at junit.framework.TestCase.runBare(TestCase.java:127)
	at junit.framework.TestResult$1.protect(TestResult.java:106)
	at junit.framework.TestResult.runProtected(TestResult.java:124)
	at junit.framework.TestResult.run(TestResult.java:109)
	at junit.framework.TestCase.run(TestCase.java:118)
	at org.eclipse.jdt.internal.junit.runner.junit3.JUnit3TestReference.run(JUnit3TestReference.java:131)
	at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
	at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)


 */

    }

    public void testRunTest_SuccessNoPause() throws IdMUnitException {
        Connector connector = createMock(Connector.class);

        testCase.setRetryCount(2); // we have to have at least two retries so that the finally block is hit, and incorrectly delays even after success.
        testCase.setWaitInterval(3000); // lets wait at least 3 seconds to trigger an error below if we paused on success.
        try {
            connector.execute(OP_TEST, attrMap);
            expectLastCall().once();
        } catch (IdMUnitException e) {
            fail("This should never happen");
        }

        replay(connector);

        testCase.setConnector(connector);

        long start = System.currentTimeMillis();
        testCase.runTest();
        long duration = System.currentTimeMillis() - start;
        long expectedTime = 0;
        assertExpectedTimeVersusActualTime(expectedTime, duration);

        verify(connector);
    }
}
