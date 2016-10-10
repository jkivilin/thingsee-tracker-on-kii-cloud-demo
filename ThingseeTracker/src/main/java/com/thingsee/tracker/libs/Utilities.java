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

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import com.thingsee.tracker.R;
import java.util.Date;

public class Utilities {

    public static String getSmartTimeStampString(Context context, Resources res, double timeStampInMs) {
        String timeStamp = null;

        double minutes = (System.currentTimeMillis() - timeStampInMs) / (1000 * 60);
        double hours = minutes / 60;
        double days = hours / 24;

        if (minutes < 1) {
            //Less than a minute
            timeStamp = res.getString(R.string.less_than_min_ago);
        } else if (minutes < 60) {
            // xx mins ago
            timeStamp = String.format("%.0f ", minutes) + res.getString(R.string.minutes_ago);
        } else if (hours < 24) {
            // xx hours ago
            if (hours >= 1.5) {
                timeStamp = String.format("%.0f ", hours) + res.getString(R.string.hours_ago);
            } else {
                timeStamp = String.format("%.0f ", hours) + res.getString(R.string.hour_ago);
            }
        } else if (days < 7) {
            // Yesterday / Wed, 13:32
            timeStamp = SmartDateTimeUtil.getDateString_shortAndSmart(new Date((long) timeStampInMs), res, DateFormat.is24HourFormat(context));
        } else {
            //
            timeStamp = SmartDateTimeUtil.getDateString_shortAndSmart(new Date((long) timeStampInMs), res, DateFormat.is24HourFormat(context));
        }
        return timeStamp;
    }
}