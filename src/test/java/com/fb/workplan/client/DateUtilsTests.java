package com.fb.workplan.client;

import java.util.Date;

import org.testng.annotations.Test;

public class DateUtilsTests {

    @Test
    public void testBeginningOfMonth() {
        Date testDate = DateUtils.date(2011, 3, 3);
        Date beginningOfMonthDate = DateUtils.date(2011, 3, 1);
        testDate = DateUtils.beginningOfMonth(testDate);
        assert testDate.equals(beginningOfMonthDate) : "Beginning of month does not match";
        testDate = DateUtils.date(2011,3,1);
        testDate = DateUtils.beginningOfMonth(testDate);
        assert testDate.equals(beginningOfMonthDate) : "Beginning of month does not match";
    }

    @Test public void testRollMonth() {
        Date testDate = DateUtils.date(2011, 4, 1);
        Date nextMonth = DateUtils.date(2011, 5, 1);
        assert DateUtils.rollMonth(testDate, 1).equals(nextMonth);
        assert ! DateUtils.rollMonth(testDate, 2).equals(nextMonth);
        nextMonth = DateUtils.date(2011, 5, 2);
        assert ! DateUtils.rollMonth(testDate, 1).equals(nextMonth);

        nextMonth = DateUtils.date(2012, 1, 1);
        testDate = DateUtils.date(2011, 12, 1);
        assert DateUtils.rollMonth(testDate, 1).equals(nextMonth);

        Date prevMonth = DateUtils.date(2011, 11, 1);
        assert DateUtils.rollMonth(testDate, -1).equals(prevMonth);

        testDate = DateUtils.date(2012, 1, 1);
        prevMonth = DateUtils.date(2011, 12, 1);
        assert DateUtils.rollMonth(testDate, -1).equals(prevMonth) : DateUtils.rollMonth(testDate, -1);

    }

    @Test public void testDaysDiff() {
        assert DateUtils.getDaysDiff(DateUtils.date(2011, 1, 1), DateUtils.date(2011, 1, 2)) == 1;
        assert DateUtils.getDaysDiff(DateUtils.date(2011, 1, 2), DateUtils.date(2011, 1, 1)) == -1;
        assert DateUtils.getDaysDiff(DateUtils.date(2011, 2, 28), DateUtils.date(2011, 3, 1)) == 1;
    }

    @Test
    public void testSameMonth() {
        assert DateUtils.isSameMonth(DateUtils.date(2011, 1, 1), DateUtils.date(2011, 1, 2));
        assert ! DateUtils.isSameMonth(DateUtils.date(2011, 1, 1), DateUtils.date(2011, 2, 1));
        assert ! DateUtils.isSameMonth(DateUtils.date(2011, 1, 1), DateUtils.date(2010, 1, 1));
        assert ! DateUtils.isSameMonth(null, DateUtils.date(2011, 1, 2));
        assert ! DateUtils.isSameMonth(null, null);
        assert ! DateUtils.isSameMonth(DateUtils.date(2011, 1, 2), null);
    }

    @Test
    public void testBeginningOfWeek() {
        assert DateUtils.beginningOfWeek(DateUtils.date(2011, 4, 20), true).equals(DateUtils.date(2011, 4, 18));
        assert DateUtils.beginningOfWeek(DateUtils.date(2011, 4, 20), false).equals(DateUtils.date(2011, 4, 17));
        assert DateUtils.beginningOfWeek(DateUtils.date(2011, 4, 24), true).equals(DateUtils.date(2011, 4, 18)):  DateUtils.beginningOfWeek(DateUtils.date(2011, 4, 24), true);
        assert DateUtils.beginningOfWeek(DateUtils.date(2011, 4, 24), false).equals(DateUtils.date(2011, 4, 24));
    }

    @Test
    public void testMonthDiff() {
        assert DateUtils.getMonthDifference(DateUtils.date(2011, 4, 20), DateUtils.date(2011, 4, 21)) == 0;
        assert DateUtils.getMonthDifference(DateUtils.date(2011, 4, 20), DateUtils.date(2011, 5, 21)) == 1;
        assert DateUtils.getMonthDifference(DateUtils.date(2011, 4, 20), DateUtils.date(2012, 4, 21)) == 12;
        assert DateUtils.getMonthDifference(DateUtils.date(2012, 5, 20), DateUtils.date(2011, 4, 21)) == -13;
        assert DateUtils.getMonthDifference(DateUtils.date(2012, 1, 20), DateUtils.date(2011, 12, 21)) == -1;

    }
}
