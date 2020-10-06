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
import org.apache.commons.lang.StringUtils;
import org.idmunit.connector.ConnectionConfigData;
import org.idmunit.connector.Connector;
import org.idmunit.extension.BasicLogger;
import org.idmunit.extension.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements the core functionality of IdMUnit.
 *
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see TestCase
 */
public class IdMUnitTestCase extends TestCase {
    static final String OP_COMMENT = "Comment";
    static final String OP_WAIT = "Wait";
    static final String OP_PAUSE = "Pause";

    private static final long DEFAULT_DELAY = 5000; //5 seconds default
    private static final long DEFAULT_MAX_RETRY_COUNT = 10; //if an event fails and retry mode is enabled, the test step will be retried this many times before failing. Overrides in the properties and spreadsheet layers.
    private static Logger log = LoggerFactory.getLogger(IdMUnitTestCase.class);

    private ConnectionConfigData configData;
    private Connector connector = null;

    private String operation;
    private String comment;
    private long retryCount = DEFAULT_MAX_RETRY_COUNT;
    private long waitInterval = DEFAULT_DELAY;
    private boolean disabled = false;
    private boolean failureExpected = false;
    private boolean critical = false;
    private String repeatRange = null;

    private Map<String, Collection<String>> attributeMap;

    public IdMUnitTestCase(String name) {
        super(name);
    }

    public void runTest() throws IdMUnitException {
        if (disabled) {
            log.info(this.getName() + " Row disabled... skipping.");
            return;
        }

        if (operation.equalsIgnoreCase(OP_COMMENT)) {
            log.info("Comment data: (this row will not be processed)" +
                    "\n\n---------------------------------" +
                    "\nTest Case: " + this.getName() +
                    "\nDescription:\n" + comment +
                    "\n---------------------------------\n");
            return;
        } else if (operation.equalsIgnoreCase(OP_WAIT) || operation.equalsIgnoreCase(OP_PAUSE)) {
            delay();
            return;
        }

        String connectorDisabled = configData.getParam(ConnectionConfigData.DISABLED);
        if (connectorDisabled != null && Boolean.parseBoolean(connectorDisabled)) {
            log.info(this.getName() + " Connector '" + configData.getName() + "' is disabled... skipping.");
            return;
        }

        int retryMultiplier;
        if (this.configData != null && this.configData.ifMultiplierRetry()) {
            retryMultiplier = configData.getMultiplierRetry();
        } else {
            retryMultiplier = 1;
        }

        for (long retriesRemaining = (retryCount * retryMultiplier) - 1;; --retriesRemaining) {
            boolean succeeded = false;
            try {
                log.info(this.getName() + ": " + comment);

                if (repeatRange == null || repeatRange.length() == 0) {
                    //Process a single non-repeated transaction
                    connector.execute(operation, attributeMap);
                    succeeded = true;  //AVK added this to not wait after a test passes
                } else {
                    //Repeat operation range was detected, for each iteration perform the following:
                    //  1. Replace range counter for each data field
                    //  2. Execute the test step
                    //  3. If an error occurs add it to a list
                    //  4. If there are any errors in the list after completion, fail the test with a report of broken iterations
                    final String strRangeDelimiter = "-";
                    int rangeStart = Integer.parseInt(StringUtils.substringBefore(repeatRange, strRangeDelimiter));
                    int rangeEnd = Integer.parseInt(StringUtils.substringAfter(repeatRange, strRangeDelimiter));
                    log.info("### Repeat Operation Range detected: Start:" + rangeStart + " End: " + rangeEnd);
                    for (int ctr = rangeStart; ctr <= rangeEnd; ++ctr) {
                        log.info("### Execute repeated operation iteration: " + ctr);
                        //  1. Replace range counter for each data field //TODO: Leverage Data Injectors for this purpose if possible
                        //CommonUtil.interpolateCounter(m_data, ctr); //TODO: must refactor  TestStepAdd... to leverage Attributes rather than DataRowBeans, who's members are immutable
                        //Process the current repeated transaction
                        connector.execute(operation, attributeMap);
                        succeeded = true;
                    }
                }

                if (failureExpected == false) {
                    return;
                }
            } catch (IdMUnitFailureException e) {
                if (failureExpected) {
                    return;
                }

                if (retriesRemaining > 0) {
                    //If retries are enabled, retry the test.
                    log.info("RETRY (" + retriesRemaining + ") " + e.getMessage() + " " + this.getName());
                    continue;
                } else {
                    log.info("...FAILURE: " + e.getMessage());
                    logCriticalErrors(e.getMessage());
                    throw new AssertionFailedError(e.getMessage());
                }
            } catch (IdMUnitException e) {
                if (failureExpected) {
                    return;
                }

                String msg = "";
                if (e.getMessage() != null && e.getCause() != null && e.getCause().getMessage() != null) {
                    msg = e.getMessage() + " " + e.getCause().getMessage();
                } else if (e.getMessage() != null) {
                    msg = e.getMessage();
                } else if (e.getCause() != null && e.getCause().getMessage() != null) {
                    msg = e.getCause().getMessage();
                }

                if (retriesRemaining > 0) {
                    //If retries are enabled, retry the test after the defined wait interval
                    log.info("RETRY (" + retriesRemaining + ") " + msg + " " + this.getName());
                    continue;
                } else {
                    log.info("...FAILURE: " + msg);
                    logCriticalErrors(msg);
                    throw e;
                }
            } catch (AssertionFailedError e) {
                log.warn("The connector you are using incorrectly threw an AssertionFailedError. It needs to be updated to use an IdMUnitFailureException.");
                throw e;
            } finally {
                //Wait to allow for standard latency (interval override in spread sheet)
                if (retriesRemaining > 0) {
                    if (!succeeded && !failureExpected) {
                        delay();
                    } else if (succeeded && failureExpected) {
                        delay();
                    }
                }
            }

            String msg = "Test failed: would have succeeded, but ExpectFailure was set to TRUE";
            if (retriesRemaining > 0) {
                //If retries are enabled, retry the test after the defined wait interval
                delay();
                log.info("RETRY (" + retriesRemaining + ") " + msg + " " + this.getName());
                continue;
            } else {
                log.info("...FAILURE: " + msg);
                logCriticalErrors(msg);
                fail(msg);
            }
        }
    }

    private void delay() throws IdMUnitException {
        try {
            if (configData.ifMultiplierWait()) {
                log.info("...delaying  " + (waitInterval * configData.getMultiplierWait()) / 1000 + " secs");
                Thread.sleep(waitInterval * configData.getMultiplierWait());
            } else {
                log.info("...delaying " + waitInterval / 1000 + " secs");
                Thread.sleep(waitInterval);
            }
        } catch (InterruptedException e) {
            log.info("delay interrupted");
        }
    }

    private void logCriticalErrors(String msg) throws IdMUnitException {
        //Generate email/log alerts if this row was marked as "critical" and if alerts are enabled.
        boolean isAlertingEnabled = (this.configData.getIdmunitAlerts() != null) ? true : false;

        if (critical && isAlertingEnabled) {
            //Iterate through each configured alert for this connection configuration (may contain globally-defined alerts as well) and fire off each enabled alert
            Map<String, Alert> alerts = configData.getIdmunitAlerts();
            Iterator<String> alertItr = alerts.keySet().iterator();
            while (alertItr.hasNext()) {
                String alertName = (String)alertItr.next();
                Alert idmunitAlert = (Alert)alerts.get(alertName);
                sendAlerts(idmunitAlert, msg);
            }
        }
    }

    private void sendAlerts(Alert idmunitAlert, String exceptionMessage) throws IdMUnitException {
        try {
            String alertMessage = this.getName() + " failed.";
            if (idmunitAlert.isEmailAlertingEnabled()) {
                log.info("### Sending email alert...");
                EmailUtil.sendEmailNotification(idmunitAlert, alertMessage, exceptionMessage);
            }
            if (idmunitAlert.isLogAlertingEnabled()) {
                log.info("### Writing log alert...");
                BasicLogger.fileLog(alertMessage + exceptionMessage, idmunitAlert.getLogPath());
            }
        } catch (IdMUnitException e) {
            throw new IdMUnitException(e.getMessage() + " " + "### Email alert failed!!");
        }
    }


    public Map<String, Collection<String>> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, Collection<String>> attrMap) {
        this.attributeMap = attrMap;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ConnectionConfigData getConfig() {
        return configData;
    }

    public void setConfig(ConnectionConfigData config) {
        this.configData = config;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isFailureExpected() {
        return failureExpected;
    }

    public void setFailureExpected(boolean expectFailure) {
        this.failureExpected = expectFailure;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean isCritical) {
        this.critical = isCritical;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public long getWaitInterval() {
        return waitInterval;
    }

    public void setWaitInterval(long waitInterval) {
        this.waitInterval = waitInterval;
    }

    public long getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(long retryCount) {
        this.retryCount = retryCount;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public String getRepeatRange() {
        return repeatRange;
    }

    public void setRepeatRange(String repeatRange) {
        this.repeatRange = repeatRange;
    }
}
