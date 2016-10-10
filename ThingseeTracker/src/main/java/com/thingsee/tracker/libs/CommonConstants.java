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

public final class CommonConstants {
    public static final int SPLASH_SCREEN_TIMEOUT = 3000; //ms
    public static final int CAMERA_ANIMATION_DURATION = 600; //ms

    public static final int SCREEN_PADDING_WITH_MAP_BOUNDS = 10;

    public static final int MAP_TAIL_LENGTH = 30;

    public static final int STARTUP_SETUP_DELAY = 5000; //ms
    public static final int NOTIF_TIMESTAMP_UPDATE_INTERVAL = 30000; //ms
    public static final float FOCUS_ZOOM_LEVEL = 17.5f;

    public static final double LOCATION_TIMEOUT_FOR_ALARM = 15*60000; //ms
    public static final float LOW_BATTERY_LEVEL_FOR_ALARM = 11;

    public static final String ACTION_NONE = "com.thingsee.tracker.ACTION_NONE";
    public static final String ACTION_PROCEED_TO_SETTINGS = "com.thingsee.tracker.ACTION_PROCEED_TO_SETTINGS";
    public static final String EXTENDED_DATA_STRING_SERIAL = "com.thingsee.tracker.STRING_SERIAL";

    public static final String KII_CLOUD_APP_ID = "xxxxx";
    public static final String KII_CLOUD_APP_KEY = "xxxxxxxxxxxxxxx";

    public static final String KII_CLOUD_APPS_BASE_ADDR = "https://api.kii.com/api/apps/";
    public static final String KII_AUTH_TOKEN_ADDR = "https://api.kii.com/api/oauth2/token";
    public static final String KII_VENDOR_THING_ID = "VENDOR_THING_ID:";
    public static final int PAGINATION_AMOUNT = 1;
    public static final String CONTENT_TYPE_KII_QUERY_JSON = "application/vnd.kii.QueryRequest+json";
    public static final String CONTENT_TYPE_KII_AUTH_TOKEN = "application/vnd.kii.OauthTokenRequest+json";

    public static final String BROADCAST_ACTION = "com.thingsee.tracker.rest.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.thingsee.tracker.rest.STATUS";
    public static final String EXTENDED_SERIAL_NBR = "com.thingsee.tracker.rest.SERIAL_NBR";
    public static final String EXTENDED_LOCATION_ITEM_ACCURACY = "com.thingsee.tracker.rest.LOCATION_ITEM_ACCURACY";
    public static final String EXTENDED_LOCATION_ITEM_LAT = "com.thingsee.tracker.rest.LOCATION_ITEM_LAT";
    public static final String EXTENDED_LOCATION_ITEM_LNG = "com.thingsee.tracker.rest.LOCATION_ITEM_LNG";
    public static final String EXTENDED_LOCATION_ITEM_TIMESTAMP = "com.thingsee.tracker.rest.LOCATION_ITEM_TIMESTAMP";
    public static final String EXTENDED_VENDOR_THING_ID = "com.thingsee.tracker.VENDOR_THING_ID";
    public static final String EXTENDED_CURRENT_LEVEL = "com.thingsee.tracker.CURRENT_LEVEL";

    public static final int BROADCAST_STATUS_DONE = 10;
    public static final int BROADCAST_STATUS_LOCATION_UPDATE = 20;
    public static final int BROADCAST_STATUS_EXIT = 50;

    public static final int KII_CLOUD_24H = 1000*60*60*24;
}