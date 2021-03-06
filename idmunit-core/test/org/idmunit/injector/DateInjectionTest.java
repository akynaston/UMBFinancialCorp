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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateInjectionTest extends TestCase {
    private DateInjection injector;
    private Calendar cal;
    private String testString;

    protected void setUp() throws Exception {
        injector = new DateInjection();
        cal = Calendar.getInstance();
    }

    protected void tearDown() throws Exception {
    }

    public void testGetTodayDate() {
        injector.mutate("0");
        testString = new SimpleDateFormat("yyyy-MMMM-dd").format(cal.getTime());
        //System.out.println("testGetTodayDate: " + testString);
        assertEquals(testString, injector.getDataInjection("yyyy-MMMM-dd"));
    }

    public void testPositiveMutator() {
        injector.mutate("30");
        cal.add(Calendar.DATE, 30);
        testString = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
        //System.out.println("testPositiveMutator: " + testString);
        assertEquals(testString, injector.getDataInjection("yyyyMMdd"));

    }

    public void testNegativeMutator() {
        injector.mutate("-7");
        cal.add(Calendar.DATE, -7);
        testString = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
        //System.out.println("testNegativeMutator: " + testString);
        assertEquals(testString, injector.getDataInjection("yyyyMMdd"));

    }

    public void testBadMutator() {
        injector.mutate("ee");
        cal.add(Calendar.DATE, 0);
        testString = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());

        assertEquals(testString, injector.getDataInjection("yyyyMMdd"));
    }

}
