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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public class KiiGetUserInfoAsyncTask extends AsyncTask <String, Integer, LinkedHashMap<String, String>> {

	private static final String LOG_TAG = "Tracker/KiiGetUserInfo";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;

	public interface AsyncResponseUserInfo {
		void processFinish(LinkedHashMap<String, String> info);
	}
	public AsyncResponseUserInfo delegate = null;

	public KiiGetUserInfoAsyncTask(AsyncResponseUserInfo delegate, String appId, String appKey, String token) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;
        mOwnerToken = "Bearer " + token;
	}

    @Override
	protected LinkedHashMap<String, String> doInBackground(String...params) {
		
		InputStream inputStream = null;
		LinkedHashMap<String, String> info = new LinkedHashMap<>();

		try {
			String dataString = "https://api.kii.com/api/apps/" + mAppId + "/users/me";
            Log.e(LOG_TAG, "dataString = " + dataString);
			HttpGet getRequest = new HttpGet(dataString);
			getRequest.addHeader("x-kii-appid", mAppId);
			getRequest.addHeader("x-kii-appkey", mAppKey);
			getRequest.addHeader("Authorization", mOwnerToken);

			HttpResponse response = mHttpClient.execute(getRequest);
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
                    JSONObject jObject = new JSONObject(currentBatch);
                    String displayName = jObject.getString("displayName");
                    String emailAddress = jObject.getString("emailAddress");
                    String phoneNumber = jObject.getString("phoneNumber");
                    info.put("displayName", displayName);
                    info.put("emailAddress", emailAddress);
                    info.put("phoneNumber", phoneNumber);
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
		return info;
	}
    @Override
    protected void onPostExecute(LinkedHashMap<String, String> info) {
        delegate.processFinish(info);
    }
}