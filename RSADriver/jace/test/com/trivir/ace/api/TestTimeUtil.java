package com.trivir.ace.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

public class TestTimeUtil extends TestCase {

    final int tzoffset = Calendar.getInstance().getTimeZone().getOffset(new Date().getTime());
    
    public void testTimeCtimeFromLocalDateAndHour() {
        assertEquals(9*(60*60) - tzoffset/1000, TimeUtil.ctimeFromLocalDateAndHour(1970, Calendar.JANUARY, 1, 9));
        long ctime = TimeUtil.ctimeFromLocalDateAndHour(2008, Calendar.NOVEMBER, 12, 8); // Months are 0 based, e.g. 0=January

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ctime*1000);
        
        assertEquals(8, c.get(Calendar.HOUR_OF_DAY));
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals("11/12/2008", formatter.format(new Date(ctime*1000)));
    }
    
    public void testCtimeFromUTCDateAndHour() throws ParseException {
        assertEquals(9*(60*60), TimeUtil.ctimeFromUTCDateAndHour("1/1/1970", 9));
        assertEquals(1196877600, TimeUtil.ctimeFromUTCDateAndHour("12/5/2007", 18));
    }
    
    public void testCtimeFromUTCDateAndSeconds() throws ParseException {
        assertEquals(9, TimeUtil.ctimeFromUTCDateAndSeconds("1/1/1970", 9));
        assertEquals(1196878487, TimeUtil.ctimeFromUTCDateAndSeconds("12/5/2007", 65687));
    }
    
    public void testLocalFromCtime() {
        assertEquals("12/05/2007", TimeUtil.localDateFromCtime(1196877600));
        assertEquals(11, TimeUtil.localHoursFromCtime(1196877600));
    }
}
