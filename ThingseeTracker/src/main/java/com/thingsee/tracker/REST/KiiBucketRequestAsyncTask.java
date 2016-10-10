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
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.thingsee.tracker.MainActivity;
import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.SensorDataModel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

public class KiiBucketRequestAsyncTask extends AsyncTask<String, Integer, Boolean> {
	
	private static final String LOG_TAG = "Tracker - KiiBucket";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mOwnerToken;
	private String mVendorThingId;
	private String mBucketId;
	private double mUpperLimitTs;
    private int mBestEffortLimit;
	
	private BroadCastNotifier mBroadCastNotifier;
	
	public KiiBucketRequestAsyncTask(Context context, String appId, String appKey, String ownerToken, String vendorThingId, String bucketId, double tsUpperLimit, int paginationAmount) {
		mHttpClient = new DefaultHttpClient();
		
		mBroadCastNotifier = new BroadCastNotifier(context);
		mAppId = appId;
		mAppKey = appKey;
		mOwnerToken = ownerToken;
		mVendorThingId = vendorThingId;
		mBucketId = bucketId;
		mUpperLimitTs = tsUpperLimit;
        mBestEffortLimit = paginationAmount;
	}

	protected Boolean doInBackground(String... params) {
		
		InputStream inputStream = null;
		
		try {
			String dataString = CommonConstants.KII_CLOUD_APPS_BASE_ADDR + mAppId + "/things/VENDOR_THING_ID:" + mVendorThingId + "/buckets/" + mBucketId + "/query";
			HttpPost postRequest = new HttpPost(dataString);
            Log.d(LOG_TAG, "token = " + mOwnerToken);
			postRequest.addHeader("Authorization", mOwnerToken);
			postRequest.addHeader("content-type", CommonConstants.CONTENT_TYPE_KII_QUERY_JSON);
			postRequest.addHeader("x-kii-appid", mAppId);
			postRequest.addHeader("x-kii-appkey", mAppKey);
			try {
				JSONObject selection = new JSONObject();
				selection.put("type","range");
				selection.put("field","_created");
				if(mUpperLimitTs != 0) {
					selection.put("upperLimit",mUpperLimitTs);
				}
				JSONObject clause = new JSONObject();
				clause.put("orderBy","_created");
				clause.put("descending","true");
				clause.put("clause",selection);
				JSONObject jsonobj = new JSONObject();
				jsonobj.put("bucketQuery",clause);
				jsonobj.put("bestEffortLimit", mBestEffortLimit);

				StringEntity se = new StringEntity(jsonobj.toString());
				postRequest.setEntity(se);
				Log.e(LOG_TAG, "Kii query, jsonobj = " + jsonobj.toString());
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			HttpResponse response = mHttpClient.execute(postRequest);
			Log.e(LOG_TAG, "REST HttpResponse = " + response.getStatusLine());
			if(response != null) {
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
					readSensorData(currentBatch);
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
		return true;
	}
    private JSONArray readSensorDataFromString(String input, int offset) {
        StringReader reader = new StringReader(input);
        try {
            reader.skip(offset);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        JsonReader jsonReader = new JsonReader(reader);
        JSONArray jsonArray = new JSONArray();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                JSONObject jsonObject
                        = readSingleData(jsonReader);
                jsonArray.put(jsonObject);
            }
            jsonReader.endArray();
        } catch (IOException e) {
            // Ignore for brevity
        } catch (JSONException e) {
            // Ignore for brevity
        }
        try {
            jsonReader.close();
        } catch (IOException e) {
            // Ignore for brevity
        }
        reader.close();
        return jsonArray;
    }

    private JSONObject readSingleData(JsonReader jsonReader)
            throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonReader.beginObject();
        JsonToken token;
        do {
            String name = jsonReader.nextName();
            if ("sId".equals(name)) {
                jsonObject.put("sId", jsonReader.nextString());
            } else if ("val".equals(name)) {
                jsonObject.put("val", jsonReader.nextDouble());
            } else if ("ts".equals(name)) {
                jsonObject.put("ts", jsonReader.nextLong());
            } else if ("_owner".equals(name)) {
                jsonObject.put("_owner", jsonReader.nextString());
            }
            
            token = jsonReader.peek();
        } while (token != null && !token.equals(JsonToken.END_OBJECT));
        jsonReader.endObject();
        return jsonObject;
    }
    
    private void readSensorData(String input) {

        int skip = 0;
        int start = 0;
        JSONArray array = null;
        double ts = 0;
        String timeStamp = null;
        while((skip != -1) && (input != null)){
            if(input != null) {
                // Find sensor data from the input JSON
                skip = input.indexOf("senses", skip);
                if(skip != -1) {
                    array = readSensorDataFromString(input, (skip+8));
                    SensorDataModel temp = null;
                    try {
                        temp = SensorDataModel.parseSensorDataFromJSON(array);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    skip = input.indexOf("_modified", skip);
                    start = skip + 10;
                    skip = input.indexOf(",", start);
                    timeStamp = input.substring(start+1, skip);
                    if(temp != null) {
                        if((temp.latitude != 0) && (temp.longitude !=0)) {
                            // Broadcast coordinates
                            temp.timeStamp = Double.parseDouble(timeStamp);
                            if(temp.timeStamp > MainActivity.getTrackerBasedOnVendorThingId(mVendorThingId).getLastLocationUpdate()) {
                                mBroadCastNotifier.broadcastIntentWithPositionAndVendorThingId(CommonConstants.BROADCAST_STATUS_LOCATION_UPDATE, mVendorThingId, temp.latitude, temp.longitude, temp.accuracy, temp.timeStamp, temp.current_level);
                            }
                        }
                    }
                    skip +=8;
                    temp = null;
                }
            }
        }
    }
}