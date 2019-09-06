package net.zyuiop.rpmachine.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Louis Vialar
 */
public class TimeUtils {
    public static boolean sameDay(Date target) {
        GregorianCalendar date = new GregorianCalendar();
        date.setTime(new Date());

        GregorianCalendar compare = new GregorianCalendar();
        compare.setTime(target);

        return compare.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) && date.get(Calendar.MONTH) == compare.get(Calendar.MONTH) && date.get(Calendar.YEAR) == compare.get(Calendar.YEAR);
    }
}
