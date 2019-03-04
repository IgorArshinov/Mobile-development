package be.igorarshinov.avatar_creator.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatetimeConverter {

    private static final String LOGTAG = "AvatarAdapter";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DATE_FORMAT_FOR_VIEWS = "yyyy/MM/dd HH:mm";
    //    private static DateFormat dateFormat;
    private static final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_FOR_VIEWS);
    private static final DateFormat dateFormatForCursor = new SimpleDateFormat(DATE_FORMAT);

    public static Date getDatetimeObjectFromString(String date) {

        if (date != null) {
            try {
                return dateFormat.parse(date);
            } catch (
                    ParseException e) {
                Log.e(LOGTAG, e.getMessage());
            }
        }

        return null;
    }

    public static Date getDatetimeObjectFromCursorString(String date) {

        if (date != null) {
            try {
                return dateFormatForCursor.parse(date);
            } catch (
                    ParseException e) {
                Log.e(LOGTAG, e.getMessage());
            }
        }

        return null;
    }

    public static String getDatetimeStringFromDate(Date date) {

        if (date != null) {
            return dateFormat.format(date);
        }
        return null;
    }
}
