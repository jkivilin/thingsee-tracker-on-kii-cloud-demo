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

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KiiCreateObjectRequestAsyncTask extends AsyncTask <String, Integer, String> {

	private static final String LOG_TAG = "Tracker/KiiCreateObject";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;
	private String mTrackerName;
	private String mTrackerSerial;
    private String mVendorThingID;

	public interface AsyncResponseCreateObject {
		void processFinish(String objID);
	}
	public AsyncResponseCreateObject delegate = null;

	public KiiCreateObjectRequestAsyncTask(AsyncResponseCreateObject delegate, String appId, String appKey, String token, String name, String serial, String vendorThingID) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;

        mOwnerToken = "Bearer " + token;
        mTrackerName = name;
        mTrackerSerial = serial;
        mVendorThingID = vendorThingID;
	}

    @Override
	protected String doInBackground(String...params) {
		
		InputStream inputStream = null;
        String objID = null;
		
		try {
			String dataString = "https://api.kii.com/api/apps/" + mAppId + "/users/me/buckets/trackers/objects";
			HttpPost postRequest = new HttpPost(dataString);
			postRequest.addHeader("content-type", "application/json");
			postRequest.addHeader("x-kii-appid", mAppId);
			postRequest.addHeader("x-kii-appkey", mAppKey);
			postRequest.addHeader("Authorization", mOwnerToken);
			try {
				JSONObject tracker = new JSONObject();
                tracker.put("name",mTrackerName);
                tracker.put("serial",mTrackerSerial);
                tracker.put("vendorThingId",mVendorThingID);

				StringEntity se = new StringEntity(tracker.toString());
				postRequest.setEntity(se);
				Log.e(LOG_TAG, "Kii query, jsonobj = " + tracker.toString());
            } catch (JSONException e) {
				throw new RuntimeException(e);
			}
			HttpResponse response = mHttpClient.execute(postRequest);
			Log.e(LOG_TAG, "REST HttpResponse = " + response.getStatusLine());
			if(response != null && response.getStatusLine().getStatusCode() == 201) {
				inputStream = response.getEntity().getContent();
				if(inputStream != null) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line = null;
					String currentBatch = "";
					while ((line = reader.readLine()) != null) {
						currentBatch +=line;
					}
					reader.close();
					Log.e(LOG_TAG, "REST bucket query response = " + currentBatch);
                    JSONObject jObject = new JSONObject(currentBatch);
                    objID = jObject.getString("objectID");
                    reader.close();
					currentBatch = null;
				}
			}
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch (Exception e) {}
			}
		}
		return objID;
	}
    @Override
    protected void onPostExecute(String objID) {
        delegate.processFinish(objID);
    }
}