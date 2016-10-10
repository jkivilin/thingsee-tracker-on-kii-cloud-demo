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
import android.util.Log;

import com.thingsee.tracker.REST.BroadCastNotifier;
import com.thingsee.tracker.REST.KiiBucketRequestAsyncTask;
import com.thingsee.tracker.REST.KiiGetAccessTokenAsyncTask;

import java.util.List;

public class ThingModel {
	
	public static final String LOG_TAG = "KiiCloud";

    private HistoryCallback accessTokenCallBack = new kiiQueryCallBackImpl();
	private HistoryCallback fetchBucketsCallBack = new kiiQueryCallBackImpl();
	
	public String mAppID = null;
	public String mAppKey = null;
	public String mVendorThingID = null;
    private String mSerialNbr = null;

	private double mStopTimeStamp = 0;
	
	private BroadCastNotifier mBroadCastNotifier;

    private String mAccessToken = null;

    public void initialize(String appId, String appKey, String vendorThingId, String serial) {

        mAppID = appId;
        mAppKey = appKey;
        mVendorThingID = vendorThingId;
        mSerialNbr = serial;

        Log.d("Tracker -thing", "load access token...");
        DoAccessTokenQuery(accessTokenCallBack, mAppID, mAppKey, mVendorThingID, mSerialNbr);
    }
    public String getCurrentVendorThingID() {
        return mVendorThingID;
    }

    public String getCurrentAccessToken() {
        return mAccessToken;
    }

	public void downloadBuckets(Context context, double stopTimeStamp, int amount) {
		
		mBroadCastNotifier = new BroadCastNotifier(context);

		mStopTimeStamp = stopTimeStamp;
		if(mAccessToken != null) {
            DoFetchBucketsQuery(context, fetchBucketsCallBack, mAppID, mAppKey, mAccessToken, mVendorThingID, "sensor_data", mStopTimeStamp, amount);
        } else {
            Log.e(LOG_TAG, "downloadBuckets: no accessToken???");
        }
	}
	
	private interface HistoryCallback {
        void onAccessTokenHandle(List<String> authInfo);
        void onFetchBucketsHandle(Boolean result);
    }

    private class kiiQueryCallBackImpl implements HistoryCallback {
        public void onAccessTokenHandle(List<String> authInfo) {
            if (authInfo!=null) {
                if(mAccessToken == null) {
                    mAccessToken = authInfo.get(1);
                }
                Log.d(LOG_TAG, "accessToken fetched:" + mAccessToken);
            } else {
                Log.e(LOG_TAG, "no accessToken!!!");
            }
        }
        public void onFetchBucketsHandle(Boolean result) {
        	mBroadCastNotifier.broadcastIntentWithStateAndSerial(CommonConstants.BROADCAST_STATUS_DONE, mSerialNbr);
        }
    }

	void DoFetchBucketsQuery(Context context, final HistoryCallback callback, String appId, String appKey, String ownerToken, String vendorThingId, String bucketId, double stopTs, int amount) {

        new KiiBucketRequestAsyncTask(context, appId, appKey, ownerToken, vendorThingId, bucketId, stopTs, amount) {
            @Override
            protected void onPostExecute(Boolean result) {
                callback.onFetchBucketsHandle(result);
            }
        }.execute();
    }

    void DoAccessTokenQuery(final HistoryCallback callback, String appId, String appKey, String vendorThingId, String password) {

        new KiiGetAccessTokenAsyncTask(appId, appKey, vendorThingId, password) {
            @Override
            protected void onPostExecute(List<String> result) {
                callback.onAccessTokenHandle(result);
            }
        }.execute();
    }
}