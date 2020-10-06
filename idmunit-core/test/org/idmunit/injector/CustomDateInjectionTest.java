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

import junit.framework.TestCase;
import org.idmunit.IdMUnitException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CustomDateInjectionTest extends TestCase {
    private CustomDateInjection injector;

    @Override
    protected void setUp() throws Exception {
        injector = new CustomDateInjection();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testThrownMutateException() throws IdMUnitException {
        try {
            injector.getDataInjection("MMM-dd-yyyy");
            fail("Missing Mutate Exception");
        } catch (IdMUnitException e) {
            assertEquals("Mutate function has not been set", e.getMessage());
        }
    }

    public void testAddingTimeZone() throws IdMUnitException {
        String expected, actual;
        String timeZone = "GMT";
        // The time format should be more granular than days so that it's easy to tell whether the time zone component
        //  is reflected in the time value.
        String timeFormat = "yyyy-MMMM-ddHH:mm:ss";
        int days;

        days = 0;
        injector.mutate(timeZone + " " + days + "dAys");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal1.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        format.setTimeZone(cal1.getTimeZone());
        actual = format.format(cal1.getTime());
        expected = injector.getDataInjection(timeFormat);
        assertEquals(expected, actual);

        days = 31;
        injector.mutate(timeZone + " " + days + "day");
        cal1 = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal1.add(Calendar.DAY_OF_MONTH, days);
        format = new SimpleDateFormat(timeFormat);
        format.setTimeZone(cal1.getTimeZone());
        actual = format.format(cal1.getTime());
        expected = injector.getDataInjection(timeFormat);
        assertEquals(expected, actual);
    }


    public void testAddingBadTimeZone() throws IdMUnitException {
        injector.mutate("BAD 0dAys");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.add(Calendar.DAY_OF_MONTH, 0);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        try {
            assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
        } catch (IdMUnitException e) {
            assertEquals("ERROR: expecting a number, check for malformed timezone or mutator", e.getMessage());
        }


        injector.mutate("Bdda +31day");
        cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal1.add(Calendar.DAY_OF_MONTH, 31);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        try {
            assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
        } catch (IdMUnitException e) {
            assertEquals("ERROR: expecting a number, check for malformed timezone or mutator", e.getMessage());
        }
    }


    public void testAddingDayPostivie() throws IdMUnitException {
        injector.mutate("0dAys");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 0);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("+31day");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 31);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
    }

    public void testAddingDayNegative() throws IdMUnitException {
        injector.mutate("-10DaY");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -10);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("-31days");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -31);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
    }

    public void testAddingMonthPositive() throws Exception {
        injector.mutate("+1Month");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, 1);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("+11Months");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, 11);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
    }

    public void testAddingMonthNegative() throws IdMUnitException {
        injector.mutate("-1Month");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, -1);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("11Months");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, 11);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));
    }

    public void testWeekAddingPositive() throws IdMUnitException {
        injector.mutate("+1wEEks");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.WEEK_OF_MONTH, 1);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("53wEEks");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.WEEK_OF_MONTH, 53);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

    }

    public void testWeekAddingNegative() throws IdMUnitException {
        injector.mutate("-1wEEks");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.WEEK_OF_MONTH, -1);
        String expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

        injector.mutate("-53wEEks");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.WEEK_OF_MONTH, -53);
        expected = new SimpleDateFormat("yyyy-MMMM-dd").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("yyyy-MMMM-dd"));

    }

    public void testHourAddingPostive() throws IdMUnitException {
        injector.mutate("1Hour");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR_OF_DAY, 1);
        String expected = new SimpleDateFormat("dd-MM-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("dd-MM-yy:HH:mm"));

        injector.mutate("25Hour");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR_OF_DAY, 25);
        expected = new SimpleDateFormat("dd-MM-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("dd-MM-yy:HH:mm"));

    }

    public void testHourAddingNegative() throws IdMUnitException {
        injector.mutate("-1Hour");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR_OF_DAY, -1);
        String expected = new SimpleDateFormat("dd-MM-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("dd-MM-yy:HH:mm"));

        injector.mutate("-25Hour");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR_OF_DAY, -25);
        expected = new SimpleDateFormat("dd-MM-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("dd-MM-yy:HH:mm"));

    }

    public void testMinutesAddingPositive() throws IdMUnitException {
        injector.mutate("1minutes");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MINUTE, 1);
        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));

        injector.mutate("61minute");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.MINUTE, 61);
        expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));

    }

    public void testMinutesAddingNegative() throws IdMUnitException {
        injector.mutate("-1minutes");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MINUTE, -1);
        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));

        injector.mutate("-61minute");
        cal1 = Calendar.getInstance();
        cal1.add(Calendar.MINUTE, -61);
        expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));
    }

    public void testCombo1() throws IdMUnitException {
        injector.mutate("1month +1year 1day +1minutes");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, 1);
        cal1.add(Calendar.YEAR, 1);
        cal1.add(Calendar.DAY_OF_MONTH, 1);
        cal1.add(Calendar.MINUTE, 1);

        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));
    }

    public void testCombo2() throws IdMUnitException {
        injector.mutate("-1months -1year -1days -1minutes");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.MONTH, -1);
        cal1.add(Calendar.YEAR, -1);
        cal1.add(Calendar.DAY_OF_MONTH, -1);
        cal1.add(Calendar.MINUTE, -1);

        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));
    }

    public void testCombo3() throws IdMUnitException {
        injector.mutate("Pacific/Samoa -1month 0year 5day 5minutes 2hours");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("Pacific/Samoa"));
        cal1.add(Calendar.MONTH, -1);
        cal1.add(Calendar.YEAR, 0);
        cal1.add(Calendar.DAY_OF_MONTH, 5);
        cal1.add(Calendar.MINUTE, 5);
        cal1.add(Calendar.HOUR_OF_DAY, 2);

        SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yy:HH:mm");
        format.setTimeZone(cal1.getTimeZone());
        String expected = format.format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));

    }

    public void testCombo4() throws IdMUnitException {
        injector.mutate("2weeks 1day");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.WEEK_OF_MONTH, 2);
        cal1.add(Calendar.DAY_OF_MONTH, 1);

        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));
    }

    public void testCombo5() throws IdMUnitException {
        injector.mutate("2weeks 1day");
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);
        cal1.add(Calendar.WEEK_OF_MONTH, 2);

        String expected = new SimpleDateFormat("MMM-dd-yy:HH:mm").format(cal1.getTime());
        assertEquals(expected, injector.getDataInjection("MMM-dd-yy:HH:mm"));
    }


}
