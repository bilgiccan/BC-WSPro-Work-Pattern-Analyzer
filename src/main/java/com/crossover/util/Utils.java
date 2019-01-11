package com.crossover.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String HOUR_FORMAT = "HH:mm";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final String EFFECTIVE_DATE_BEGIN = "effectiveDateBegin";
    private static final String EFFECTIVE_DATE_END = "effectiveDateEnd";
    private static final String MANAGER = "manager";
    private static final String PRINTABLE_NAME = "printableName";

    private static final String TIME_ZONE_UTC = "UTC";

    public static Properties getProperties(File file) throws IOException {
        Properties properties = new Properties();
        InputStream in = new FileInputStream(file);
        properties.load(in);
        return properties;
    }

    public static Date stringToDate(String str) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = format.parse(str);
        return date;
    }

    public static String dateToString(Date date) {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return format.format(date);
    }

    public static String getDayOfDate(String str, long timeOffset) throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
        Date date = isoFormat.parse(str);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_UTC));
        cal.setTime(date);
        cal.setTimeInMillis(date.getTime() + timeOffset);
        DateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return df.format(cal.getTime());
    }

    public static String getLocalFormattedHour(String str, long timeOffset) throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
        Date date = isoFormat.parse(str);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_UTC));
        cal.setTime(date);
        cal.setTimeInMillis(date.getTime() + timeOffset);
        DateFormat df = new SimpleDateFormat(HOUR_FORMAT, Locale.ENGLISH);
        return df.format(cal.getTime());
    }

    public static Date getFirstDayOfWeek(String str) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = format.parse(str);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return calendar.getTime();
    }

    public static boolean validateDates(String strStartDate, String strEndDate) {
        if (strStartDate == null || strEndDate == null) {
            return false;
        }
        try {
            DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
            Date strDate = format.parse(strStartDate);
            Date endDate = format.parse(strEndDate);
            if (strDate.compareTo(endDate) > 0) {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String activeTimeManager(List<LinkedHashMap> assignmentHistories, String date, String alt)
            throws ParseException {
        for (LinkedHashMap<String, Object> history : assignmentHistories) {
            String strDateBegin = (String) history.get(EFFECTIVE_DATE_BEGIN);
            Date momentDate = stringToDate(date);
            Date dateBegin = stringToDate(strDateBegin);
            String endDate = null;
            if (history.containsKey(EFFECTIVE_DATE_END)) {
                endDate = (String) history.get(EFFECTIVE_DATE_END);
            }
            if (endDate == null && momentDate.compareTo(dateBegin) >= 0) {
                LinkedHashMap<String, Object> lhp = (LinkedHashMap<String, Object>) history.get(MANAGER);
                return (String) lhp.get(PRINTABLE_NAME);
            } else if (momentDate.compareTo(dateBegin) >= 0 && momentDate.compareTo(stringToDate(endDate)) <= 0) {
                LinkedHashMap<String, Object> lhp = (LinkedHashMap<String, Object>) history.get(MANAGER);
                return (String) lhp.get(PRINTABLE_NAME);
            }
        }
        return alt;
    }
}
