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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Inject date information into test data
 *
 * @author Brent Kynaston, Software Engineer, TriVir LLC
 * @version %I%, %G%
 * @see Injection
 * @see ConfigLoader
 * @see DdRowBehaviour
 */
public class DateInjection implements Injection {
    private Logger log = LoggerFactory.getLogger(DateInjection.class);
    private int mutator;

    private static String getCurrentTimeStamp(Date date, String formatter) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(formatter);
        return dateFormatter.format(date);
    }

    public void mutate(String mutation) {
        if (mutation.startsWith("+", 0)) {
            mutation = mutation.substring(1);
        }
        try {
            this.mutator = Integer.parseInt(mutation);
        } catch (NumberFormatException e) {
            this.mutator = 0;
        }
    }

    public String getDataInjection(String formatter) {
        log.trace("...Generating date with an offset of: [" + mutator + "]");
        Date date = getTodaysDate(mutator);
        return getCurrentTimeStamp(date, formatter);
    }

    private Date getTodaysDate(long offSet) {
        //The offset will increment or decrement the date by offSet number of days
        Date today = new Date();
        if (offSet != 0) {
            long dateoffSet = offSet * 24 * 60 * 60 * 1000;
            long modifiedDate = today.getTime() + dateoffSet;
            today = new Date(modifiedDate);
        }
        return today;
    }

}
