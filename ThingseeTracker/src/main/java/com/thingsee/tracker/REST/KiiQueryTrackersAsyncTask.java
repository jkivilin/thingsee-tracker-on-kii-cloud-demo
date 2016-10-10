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

import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.ThingModel;
import com.thingsee.tracker.libs.TrackerModel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public class KiiQueryTrackersAsyncTask extends AsyncTask <String, Integer, LinkedHashMap<String, TrackerModel>> {

	private static final String LOG_TAG = "Tracker";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;

	public interface AsyncResponseQueryTrackers {
		void processFinish(LinkedHashMap<String, TrackerModel> trackers);
	}
	public AsyncResponseQueryTrackers delegate = null;

	public KiiQueryTrackersAsyncTask(AsyncResponseQueryTrackers delegate, String appId, String appKey, String token) {
		this.delegate = delegate;
		mHttpClient = new DefaultHttpClient();

		mAppId = appId;
		mAppKey = appKey;
        mOwnerToken = "Bearer " + token;
	}

    @Override
	protected LinkedHashMap<String, TrackerModel>  doInBackground(String...params) {
		
		InputStream inputStream = null;
		LinkedHashMap<String, TrackerModel> trackers = new LinkedHashMap<>();
		
		try {
			String dataString = "https://api.kii.com/api/apps/" + mAppId + "/users/me/buckets/trackers/query";
			HttpPost postRequest = new HttpPost(dataString);
			postRequest.addHeader("content-type", "application/vnd.kii.QueryRequest+json");
			postRequest.addHeader("x-kii-appid", mAppId);
			postRequest.addHeader("x-kii-appkey", mAppKey);
			postRequest.addHeader("Authorization", mOwnerToken);
			try {
				JSONObject type = new JSONObject();
				type.put("type", "all");
                JSONObject clause = new JSONObject();
                clause.put("clause", type);
				JSONObject query = new JSONObject();
                query.put("bucketQuery", clause);

				StringEntity se = new StringEntity(query.toString());
				postRequest.setEntity(se);
				Log.e(LOG_TAG, "Kii query, jsonobj = " + query.toString());
            } catch (JSONException e) {
				throw new RuntimeException(e);
			}
			HttpResponse response = mHttpClient.execute(postRequest);
			Log.d(LOG_TAG, "REST HttpResponse = " + response.getStatusLine());
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
					Log.d(LOG_TAG, "REST bucket query response = " + currentBatch);
                    JSONObject jObject = new JSONObject(currentBatch);
                    JSONArray results = jObject.getJSONArray("results");
                    for(int i = 0; i < results.length(); i++){
                        ThingModel thingModelDevice = new ThingModel();
                        thingModelDevice.initialize(CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                                results.getJSONObject(i).getString("vendorThingId"), results.getJSONObject(i).getString("serial"));
                        TrackerModel trackerModel = new TrackerModel(results.getJSONObject(i).getString("name"),
                                null,
                                results.getJSONObject(i).getString("serial"),
								thingModelDevice);
						trackerModel.setObjectID(results.getJSONObject(i).getString("_id"));
                        trackers.put(results.getJSONObject(i).getString("vendorThingId"), trackerModel);
                        Log.d(LOG_TAG, "Added tracker = " + results.getJSONObject(i).getString("name"));
                        Log.d(LOG_TAG, "Added tracker = " + results.getJSONObject(i).toString());
                    }
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
		return trackers;
	}
    @Override
    protected void onPostExecute(LinkedHashMap<String, TrackerModel> trackers) {
        delegate.processFinish(trackers);
    }
}