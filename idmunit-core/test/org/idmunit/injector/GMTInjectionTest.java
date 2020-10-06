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

/*
 *  GMT Injection Test
 *  This test is base on these premises:
 *  	- The System running this test is set to MST time zone
 *  	- The GMT time zone is 6 hours ahead of the the MST time zone.
 *
*/

package org.idmunit.injector;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GMTInjectionTest extends TestCase {
    final int timeDifference = 6;
    final String format = "dd-MMM-YYYYY - hh:mm";
    private GMTInjection injector;
    private Calendar cal;
    private String testString;

    protected void setUp() throws Exception {
        injector = new GMTInjection();
        cal = Calendar.getInstance();
    }


    protected void tearDown() throws Exception {
    }

    public void testGetTodayDate() {
        injector.mutate("0");
        cal.add(Calendar.HOUR_OF_DAY, timeDifference);
        testString = new SimpleDateFormat(format).format(cal.getTime());
        assertEquals(testString, injector.getDataInjection(format));
    }

    public void testPositiveMutator() {
        injector.mutate("30");
        cal.add(Calendar.HOUR_OF_DAY, timeDifference);
        cal.add(Calendar.DATE, 30);
        testString = new SimpleDateFormat(format).format(cal.getTime());
        //System.out.println("testPositiveMutator: " + testString);
        assertEquals(testString, injector.getDataInjection(format));

    }

    public void testNegativeMutator() {
        injector.mutate("-7");
        cal.add(Calendar.HOUR_OF_DAY, timeDifference);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        testString = new SimpleDateFormat(format).format(cal.getTime());
        //System.out.println("testNegativeMutator: " + testString);
        assertEquals(testString, injector.getDataInjection(format));

    }

    public void testBadMutator() {
        injector.mutate("ee");
        cal.add(Calendar.DATE, 0);
        cal.add(Calendar.HOUR_OF_DAY, timeDifference);
        testString = new SimpleDateFormat(format).format(cal.getTime());

        assertEquals(testString, injector.getDataInjection(format));
    }

}
