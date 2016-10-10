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
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class KiiRemoveTrackerAsyncTask extends AsyncTask <String, Integer, Boolean> {

	private static final String LOG_TAG = "Tracker";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;
	private String mObjectID;

	public interface AsyncResponseRemoveTracker {
		void processFinish(boolean result);
	}
	public AsyncResponseRemoveTracker delegate = null;

	public KiiRemoveTrackerAsyncTask(AsyncResponseRemoveTracker delegate, String appId, String appKey, String token, String objectID) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;
        mOwnerToken = "Bearer " + token;
		mObjectID = objectID;
	}

    @Override
	protected Boolean  doInBackground(String...params) {
		
		InputStream inputStream = null;
		Boolean result = false;
		
		try {
			String dataString = "https://api.kii.com/api/apps/" + mAppId + "/users/me/buckets/trackers/objects/" + mObjectID;
			HttpDelete deleteRequest = new HttpDelete(dataString);
			deleteRequest.addHeader("x-kii-appid", mAppId);
			deleteRequest.addHeader("x-kii-appkey", mAppKey);
			deleteRequest.addHeader("Authorization", mOwnerToken);
			deleteRequest.addHeader("If-match", "1");
			HttpResponse response = mHttpClient.execute(deleteRequest);
			Log.e(LOG_TAG, "REST HttpResponse = " + response.getStatusLine());
			if(response != null && response.getStatusLine().getStatusCode() == 204) {
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