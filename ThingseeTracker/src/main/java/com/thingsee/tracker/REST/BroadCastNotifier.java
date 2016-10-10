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

package com.thingsee.tracker.REST;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.thingsee.tracker.libs.CommonConstants;

public class BroadCastNotifier {

    private LocalBroadcastManager mBroadcaster;

    public BroadCastNotifier(Context context) {
        mBroadcaster = LocalBroadcastManager.getInstance(context);
    }
    public void broadcastIntentWithStateAndSerial(int status, String serial) {
        Intent localIntent = new Intent();
        localIntent.setAction(CommonConstants.BROADCAST_ACTION);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        localIntent.putExtra(CommonConstants.EXTENDED_DATA_STATUS, status);
        localIntent.putExtra(CommonConstants.EXTENDED_SERIAL_NBR, serial);
        mBroadcaster.sendBroadcast(localIntent);
    }
    public void broadcastIntentWithPositionAndVendorThingId(int status, String vendorThingId, double latitude, double longitude, float accuracy, double timestamp, float current_level) {
        Intent localIntent = new Intent();
        localIntent.setAction(CommonConstants.BROADCAST_ACTION);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        localIntent.putExtra(CommonConstants.EXTENDED_DATA_STATUS, status);
        localIntent.putExtra(CommonConstants.EXTENDED_VENDOR_THING_ID, vendorThingId);
        localIntent.putExtra(CommonConstants.EXTENDED_LOCATION_ITEM_LAT, latitude);
        localIntent.putExtra(CommonConstants.EXTENDED_LOCATION_ITEM_LNG, longitude);
        localIntent.putExtra(CommonConstants.EXTENDED_LOCATION_ITEM_ACCURACY, accuracy);
        localIntent.putExtra(CommonConstants.EXTENDED_LOCATION_ITEM_TIMESTAMP, timestamp);
        localIntent.putExtra(CommonConstants.EXTENDED_CURRENT_LEVEL, current_level);
        mBroadcaster.sendBroadcast(localIntent);
    }
    public void broadcastIntentToExit() {
        Intent localIntent = new Intent();
        localIntent.setAction(CommonConstants.BROADCAST_ACTION);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        localIntent.putExtra(CommonConstants.EXTENDED_DATA_STATUS, CommonConstants.BROADCAST_STATUS_EXIT);
        mBroadcaster.sendBroadcast(localIntent);
    }
}