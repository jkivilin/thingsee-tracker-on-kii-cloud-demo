/* Copyright 2016 Marko Parttimaa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.thingsee.tracker.libs;

import android.content.res.Resources;

import com.thingsee.tracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
public class SmartDateTimeUtil {
    private static String getHourMinuteString(Date date, boolean hrs24_clock){
        SimpleDateFormat hourMinuteFormat;
        if(hrs24_clock) {
            hourMinuteFormat = new SimpleDateFormat(" kk:mm");
        } else {
            hourMinuteFormat = new SimpleDateFormat(" hh:mm a");
        }
        return hourMinuteFormat.format(date);
    }

    private static String getDateString(Date date, boolean hrs24_clock){
        SimpleDateFormat dateStringFormat;
        if(hrs24_clock) {
            dateStringFormat = new SimpleDateFormat("EEE',' d MMM yyyy',' kk:mm");
        } else {
            dateStringFormat = new SimpleDateFormat("EEE',' d MMM yyyy',' hh:mm a");
        }
        return dateStringFormat.format(date);
    }

    private static boolean isToday (DateTime dateTime) {
        DateMidnight today = new DateMidnight();
        return today.equals(dateTime.toDateMidnight());
    }

    private static boolean isYesterday (DateTime dateTime) {
        DateMidnight yesterday = (new DateMidnight()).minusDays(1);
        return yesterday.equals(dateTime.toDateMidnight());
    }

    private static boolean isTomorrow(DateTime dateTime){
        DateMidnight tomorrow = (new DateMidnight()).plusDays(1);
        return tomorrow.equals(dateTime.toDateMidnight());
    }
    private static String getDayString(Date date, Resources res, boolean hrs24_clock) {
        SimpleDateFormat weekdayFormat;

        if(hrs24_clock) {
            weekdayFormat =new SimpleDateFormat("EEE',' kk:mm");
        } else {
            weekdayFormat =new SimpleDateFormat("EEE',' hh:mm a");
        }
        String s;
        if (isToday(new DateTime(date)))
            s = res.getString(R.string.timestamp_today);
        else if (isYesterday(new DateTime(date)))
            s = res.getString(R.string.timestamp_yesterday) + "," + getHourMinuteString(date, hrs24_clock);
        else if(isTomorrow(new DateTime(date)))
            s = res.getString(R.string.timestamp_tomorrow) + "," +getHourMinuteString(date, hrs24_clock);
        else
            s = weekdayFormat.format(date);
        return s;
    }

    public static String getDateString_shortAndSmart(Date date, Resources res, boolean hrs24_clock) {
        String s;
        DateTime nowDT = new DateTime();
        DateTime dateDT = new DateTime(date);
        int days = Days.daysBetween(dateDT, nowDT).getDays();
        if (isToday(new DateTime(date)))
            s = res.getString(R.string.timestamp_today) + "," +getHourMinuteString(date, hrs24_clock);
        else if (days < 7)
            s = getDayString(date, res, hrs24_clock);
        else
            s = getDateString(date, hrs24_clock);
        return s;
    }

}