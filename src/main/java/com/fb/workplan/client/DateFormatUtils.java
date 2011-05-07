package com.fb.workplan.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;

public class DateFormatUtils {
    public static  String format (Date date) {
        return dtf.format(date);
    }
    public static  String monthFormat (Date date) {
        return monthFormatter.format(date);
    }
    public static  Date parse(String date) {
        return dtf.parse(date);
    }

    public static String formatDay(Date date) {
        return dayFormatter.format(date);
    }

    public static String formatWeek(Date date) {
        return weekFormatter.format(date);
    }

    static DateTimeFormat dtf = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
    static DateTimeFormat monthFormatter = DateTimeFormat.getFormat("MMM / yyyy");
    static DateTimeFormat dayFormatter = DateTimeFormat.getFormat("dd");
    static DateTimeFormat weekFormatter = DateTimeFormat.getFormat("dd");

    public static class DateBoxFormatter implements DateBox.Format {
        public String format(DateBox dateBox, Date date) {
            if (date == null) {
                return "";
            }
            return dtf.format(date);
        }

        public Date parse(DateBox dateBox, String text, boolean reportError) {
            if (text == null || text.trim().length()< 0) {
                return null;
            }
            return dtf.parse(text);
        }

        public void reset(DateBox dateBox, boolean abandon) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
