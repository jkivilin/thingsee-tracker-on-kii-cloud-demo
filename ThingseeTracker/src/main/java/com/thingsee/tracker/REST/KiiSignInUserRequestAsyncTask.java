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

public class KiiSignInUserRequestAsyncTask extends AsyncTask <String, Integer, String> {

	private static final String LOG_TAG = "Tracker/KiiSignInUser";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mPassword;
    private String mEmailAddress;

	public interface AsyncResponseSignInUser {
		void processFinish(String accessToken);
	}
	public AsyncResponseSignInUser delegate = null;

	public KiiSignInUserRequestAsyncTask(AsyncResponseSignInUser delegate, String appId, String appKey, String password, String emailAddress) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;

        mPassword = password;
        mEmailAddress = emailAddress;
	}

    @Override
	protected String doInBackground(String...params) {
		
		InputStream inputStream = null;
        String accessToken = null;
		
		try {
			String dataString = "https://api.kii.com/api/oauth2/token";
			HttpPost postRequest = new HttpPost(dataString);
			postRequest.addHeader("content-type", "application/json");
			postRequest.addHeader("x-kii-appid", mAppId);
			postRequest.addHeader("x-kii-appkey", mAppKey);
			try {
				JSONObject user = new JSONObject();
                user.put("password",mPassword);
                user.put("username",mEmailAddress);

				StringEntity se = new StringEntity(user.toString());
				postRequest.setEntity(se);
				Log.e(LOG_TAG, "Kii post, jsonobj = " + user.toString());
            } catch (JSONException e) {
				throw new RuntimeException(e);
			}
			HttpResponse response = mHttpClient.execute(postRequest);
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
					accessToken = jObject.getString("access_token");
					Log.e(LOG_TAG, "User access token = " + accessToken);
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
		return accessToken;
	}
    @Override
    protected void onPostExecute(String token) {
        delegate.processFinish(token);
    }
}