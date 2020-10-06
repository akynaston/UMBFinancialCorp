package com.trivir.ace.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    private TimeUtil() {}

    public static long ctimeFromLocalDateAndHour(int year, int month, int day, int hour) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DAY_OF_MONTH, day);
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        
        return date.getTimeInMillis() / 1000;
    }
    
    public static long ctimeFromUTCDateAndHour(String date, int hour) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.parse(date).getTime()/1000 + hour * 60 * 60;
    }

    public static long ctimeFromUTCDateAndSeconds(String date, int seconds) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.parse(date).getTime()/1000 + seconds;
    }

    public static String localDateFromCtime(long time) {
        /*
         * The following code converts a SYN_TIME value from seconds
         * since the epoc UTC to a date and hour in the local timezone.
         * The SimpleDateFormat object is created with the local timezone
         * so the values retrieved are converted.
         */
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(new Date(time * 1000));
    }

    public static int localHoursFromCtime(long time) {
        /*
         * The following code converts a SYN_TIME value from seconds
         * since the epoc UTC to a date and hour in the local timezone.
         * The Calendar object is created with the local timezone so the
         * values retrieved are converted.
         */
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time * 1000);
        return c.get(Calendar.HOUR_OF_DAY); 
    }

    public static String utcDateFromCtime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(time * 1000));
    }

    public static int utcSecondsFromCtime(long time) {
        /*
         * The following code converts a SYN_TIME value from seconds
         * since the epoc UTC to a date and hour in the local timezone.
         * The Calendar object is created with the local timezone so the
         * values retrieved are converted.
         */
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(time * 1000);
        return c.get(Calendar.HOUR_OF_DAY)*(60*60) + c.get(Calendar.MINUTE)*60 + c.get(Calendar.SECOND); 
    }
}
