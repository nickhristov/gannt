package com.fb.workplan.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SuppressWarnings({"deprecation", "UnusedDeclaration"})
public class DateUtils {
    private DateUtils() { }
    public static  Date date(int year, int month, int day) {
        Date result = new Date();
        result.setDate(day);
        result.setMonth(month-1);
        result.setYear(year-1900);
        result.setMinutes(0);
        result.setHours(0);
        result.setSeconds(0);
        result.setTime((result.getTime() / 1000) * 1000);  // truncate milliseconds
		return result;
    }

    public static int dayOfMonth(Date date) {
        return date.getDate();
    }

    public static int year(Date date) {
        return date.getYear() + 1900;
    }

    public static int month (Date date) {
        return date.getMonth() + 1;
    }

    public static int getDaysDiff(Date smaller, Date bigger) {
        return (int) ((bigger.getTime() - smaller.getTime())/(1000*3600*24));
    }

    public static List<Date> getDatesForMonth(Date month) {
    	Date startOfMonth = beginningOfMonth(month);
		Date nextMonth = rollMonth(startOfMonth, 1);
		Date tdate = startOfMonth;
		ArrayList<Date> result = new ArrayList<Date>(31);
		while(tdate.before(nextMonth)) {
			result.add(tdate);
			tdate = rollMonth(tdate, 1);
		}
		return result;
	}
    
    public static List<Date> getMondaysForMonth(Date month) {
        List<Date> mondays = new ArrayList<Date>(5);
        Date monday = DateUtils.getFirstWeekdayOfMonth(month, true);
        while (DateUtils.isSameMonth(monday, month)) {
            mondays.add(monday);
            monday = DateUtils.rollDays(monday, 7);
        }
        return mondays;
    }
    @SuppressWarnings({"SimplifiableIfStatement"})
    public static boolean isSameMonth(Date date, Date startDate) {
        if (startDate == null || date == null) {
            return false;
        }
        return date.getYear() == startDate.getYear() && date.getMonth() == startDate.getMonth();
    }


    public static Date beginningOfMonth(Date date) {
        Date newDate = (Date) date.clone();
        newDate.setDate(1);
        return newDate;
    }

    public static Date beginningOfWeek(Date date, boolean isMondayStartOfWeek) {
        int weekStartDay = isMondayStartOfWeek ? -6 : 0;
        int dayOfWeek = date.getDay() - weekStartDay;  // Sunday always 0.
        int dayRoll = - dayOfWeek % 7;
        return rollDays(date, dayRoll);
    }

    public static int getWeekDiff(Date smaller, Date bigger) {
        Date smallerN = beginningOfWeek(smaller, true);
        Date biggerN = beginningOfWeek(bigger, true);
        return (int) ((bigger.getTime() - smaller.getTime()) / (24*3600*1000*7));
    }
    public static Date rollDays(Date date, int increment) {
        return new Date( date.getTime() + increment * 24 * 3600*1000);
    }

    public static Date rollMonth(Date date, int increment) {
        Date newDate = (Date) date.clone();
        int newTargetMonth = newDate.getMonth() + increment;
        newDate.setYear(newDate.getYear() + newTargetMonth / 12);
        newDate.setMonth(newTargetMonth % 12);
        return newDate;
    }
    
    public static int getMonthDifference(Date smaller, Date bigger) {
        return (bigger.getYear() - smaller.getYear())*12 + (bigger.getMonth() - smaller.getMonth());
    }

    public static Date getFirstWeekdayOfMonth(Date date, boolean isMondayStartOfWeek) {
        date = beginningOfMonth(date);
        Date rolledDate = beginningOfWeek(date, isMondayStartOfWeek);
        if (! isSameMonth(rolledDate, date)) {
            return rollDays(date, 7 - getDaysDiff(rolledDate, date));
        } else {
            // degenerate case, 1st is monday
            return rolledDate;
        }

    }

	public static Date midnight(Date date) {
		Date result = new Date(date.getTime());
        result.setMinutes(0);
        result.setHours(0);
        result.setSeconds(0);
		result.setTime((result.getTime() / 1000) * 1000); // truncate milliseconds
		return result;
	}
}
