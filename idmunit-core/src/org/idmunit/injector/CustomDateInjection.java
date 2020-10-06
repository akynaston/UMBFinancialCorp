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

package org.idmunit.injector;

import org.idmunit.ConfigLoader;
import org.idmunit.IdMUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inject date information into test data
 *
 * @author Ikani Samani, Technical Assistant, TriVir LLC
 * @version %I%, %G%
 * @see Injection
 * @see ConfigLoader
 */
public class CustomDateInjection implements Injection {
    private static Logger log = LoggerFactory.getLogger(CustomDateInjection.class);

    private List<String> opsList;
    private Calendar cal;
    private String timezone = null;

    public void mutate(String mutation) {
        log.trace("...Generating mutation operations with: " + mutation);
        opsList = new ArrayList<String>();

        String[] splitStrings = mutation.split(" ");
        ArrayList<String> operations = new ArrayList<String>(Arrays.asList(splitStrings));

        if (checkTimeZoneID(operations.get(0))) {
            timezone = operations.get(0);
            operations.remove(0);
        }

        for (String ops : operations) {
            log.trace("...Operation: " + ops);
            opsList.add(ops);
        }

    }

    public String getDataInjection(String formatter) throws IdMUnitException {
        log.trace("...Generating a string with formatter: " + formatter);

        if (opsList == null) {
            throw new IdMUnitException("Mutate function has not been set");
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat(formatter);

        if (timezone == null) {
            cal = Calendar.getInstance();
        } else {
            TimeZone tz = TimeZone.getTimeZone(timezone);
            cal = Calendar.getInstance(tz);
            // The Date object returned from Calendar.getTime() doesn't contain time zone information. Setting the time
            //  zone on the formatter ensures that the returned time value is in the correct time zone.
            dateFormatter.setTimeZone(tz);
        }

        getOperations(opsList);
        return dateFormatter.format(cal.getTime());
    }

    private void getOperations(List<String> operations) throws IdMUnitException {
        ArrayList<String> operationsList = new ArrayList<String>();
        Pattern pattern = Pattern.compile("[\\+?|\\-?]?[\\d]+|[days|day|months|month|years|year|weeks|week|hours|hour|minutes|minute]+", Pattern.CASE_INSENSITIVE);
        int number = 0;
        String date = "";
        String tempString = "";
        int flag = 1;
        for (String ops : operations) {
            Matcher matcher = pattern.matcher(ops);
            while (matcher.find()) {
                operationsList.add(matcher.group());
                if (1 == flag % 2) {
                    tempString = matcher.group();
                    if (tempString.startsWith("+")) {
                        tempString = tempString.substring(1);
                    }
                    try {
                        number = Integer.parseInt(tempString);
                    } catch (NumberFormatException e) {
                        throw new IdMUnitException("ERROR: expecting a number, check for malformed timezone or mutator");
                    }
                } else {
                    date = matcher.group();
                }
                ++flag;
            }

            date = date.toLowerCase();
            if ("days".equals(date) || "day".equals(date)) {
                changeDays(number);
            } else if ("months".equals(date) || "month".equals(date)) {
                changeMonths(number);
            } else if ("years".equals(date) || "year".equals(date)) {
                changeYears(number);
            } else if ("weeks".equals(date) || "week".equals(date)) {
                changeWeeks(number);
            } else if ("hours".equals(date) || "hour".equals(date)) {
                changeHours(number);
            } else if ("minutes".equals(date) || "minute".equals(date)) {
                changeMinutes(number);
            } else {
                throw new IdMUnitException("ERROR: expecting day,month,year,hour,minute,week");
            }

            log.trace("...Operation: " + date + "," + "amount to change by: " + number);
        }

    }

    private void changeDays(int amount) {
        this.cal.add(Calendar.DATE, amount);
    }

    private void changeMonths(int amount) {
        this.cal.add(Calendar.MONTH, amount);
    }

    private void changeYears(int amount) {
        this.cal.add(Calendar.YEAR, amount);
    }

    private void changeHours(int amount) {
        this.cal.add(Calendar.HOUR_OF_DAY, amount);
    }

    private void changeMinutes(int amount) {
        this.cal.add(Calendar.MINUTE, amount);
    }

    private void changeWeeks(int amount) {
        this.cal.add(Calendar.WEEK_OF_MONTH, amount);
    }

    private boolean checkTimeZoneID(String target) {
        String[] availableTimeZoneIDs = TimeZone.getAvailableIDs();
        for (String tz : availableTimeZoneIDs) {
            if (target.equals(tz)) {
                log.trace("...TimeZone Discovered: " + target);
                return true;
            }
        }
        log.trace("...No TimeZone Found");
        return false;
    }

}
