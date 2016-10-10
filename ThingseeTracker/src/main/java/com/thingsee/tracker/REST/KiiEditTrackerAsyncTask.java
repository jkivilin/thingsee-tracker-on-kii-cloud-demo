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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KiiEditTrackerAsyncTask extends AsyncTask <String, Integer, Boolean> {

	private static final String LOG_TAG = "Tracker";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;
	private String mObjectID;
	private String mName;

	public interface AsyncResponseEditTracker {
		void processFinish(boolean result);
	}
	public AsyncResponseEditTracker delegate = null;

	public KiiEditTrackerAsyncTask(AsyncResponseEditTracker delegate, String appId, String appKey, String token, String objectID, String name) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;
        mOwnerToken = "Bearer " + token;
		mObjectID = objectID;
		mName = name;
	}

    @Override
	protected Boolean  doInBackground(String...params) {
		
		InputStream inputStream = null;
		Boolean result = false;
		
		try {
			String dataString = "https://api.kii.com/api/apps/" + mAppId + "/users/me/buckets/trackers/objects/" + mObjectID;
			HttpPost updateRequest = new HttpPost(dataString);
            updateRequest.addHeader("x-kii-appid", mAppId);
            updateRequest.addHeader("x-kii-appkey", mAppKey);
            updateRequest.addHeader("Authorization", mOwnerToken);
            updateRequest.addHeader("X-HTTP-Method-Override", "PATCH");
            try {
                JSONObject trackerName = new JSONObject();
                trackerName.put("name", mName);

                StringEntity se = new StringEntity(trackerName.toString());
                updateRequest.setEntity(se);
                Log.e(LOG_TAG, "Kii query, jsonobj = " + trackerName.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
			HttpResponse response = mHttpClient.execute(updateRequest);
			Log.e(LOG_TAG, "REST HttpResponse = " + response.getStatusLine());
			if(response != null && response.getStatusLine().getStatusCode() == 200) {
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
                    reader.close();
					currentBatch = null;
				}
				result = true;
			}
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); } catch (Exception e) {}
			}
		}
		return result;
	}
    @Override
    protected void onPostExecute(Boolean result) {
        delegate.processFinish(result);
    }
}