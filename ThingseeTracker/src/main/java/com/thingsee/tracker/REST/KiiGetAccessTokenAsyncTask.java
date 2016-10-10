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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.thingsee.tracker.libs.CommonConstants;


public class KiiGetAccessTokenAsyncTask extends AsyncTask<String, Integer, List<String>> {
	
	private static final String LOG_TAG = "KiiGetAccessToken";

	private HttpClient mHttpClient;
	private String mAppId;
	private String mAppKey;
	private String mVendorThingId;
	private String mPassword;

	public KiiGetAccessTokenAsyncTask(String appId, String appKey, String vendorThingId, String password) {
		mHttpClient = new DefaultHttpClient();
		mAppId = appId;
		mAppKey = appKey;
		mVendorThingId = vendorThingId;
		mPassword = password;
	}

	protected List<String> doInBackground(String... params) {

		List<String> result = new ArrayList<String>();
		HttpPost postRequest = new HttpPost(CommonConstants.KII_AUTH_TOKEN_ADDR);
        postRequest.addHeader("content-type", CommonConstants.CONTENT_TYPE_KII_AUTH_TOKEN);
        postRequest.addHeader("x-kii-appid", mAppId);
        postRequest.addHeader("x-kii-appkey", mAppKey);
        Log.d(LOG_TAG, "pw:" + mPassword + " tid:" + mVendorThingId);
        try {
            JSONObject authEntity = new JSONObject();
            authEntity.put("password", mPassword);
            authEntity.put("username", CommonConstants.KII_VENDOR_THING_ID + mVendorThingId);

            StringEntity se = new StringEntity(authEntity.toString());
            postRequest.setEntity(se);  
            HttpResponse response = mHttpClient.execute(postRequest);
            HttpEntity entity = response.getEntity();
            String content = getStringFromStream(entity.getContent());
            String deviceId = getJSONDeviceId(content);
            String accessToken = getJSONAccessTokenType(content) + " " + getJSONAccess(content);
            result.add(0, deviceId);
            result.add(1, accessToken);
            return result;
        } catch (Exception e) {
        	Log.e(LOG_TAG, e.getMessage());
            return null;
        }
	}
	private String getStringFromStream(InputStream is) {
        if (is != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    Log.e(LOG_TAG, "REST accessToken response = " + line);
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
	private String getJSONDeviceId(String content) {
        JSONObject json = null;
		try {
			json = new JSONObject(content);
		} catch (JSONException e1) {
			return null;
		}
        try {
            return json.optString("id");
        } catch (Exception e) {
            return null;
        }
    }
	private String getJSONAccess(String content) {
        JSONObject json = null;
		try {
			json = new JSONObject(content);
		} catch (JSONException e1) {
			return null;
		}
        try {
            return json.optString("access_token");
        } catch (Exception e) {
            return null;
        }
    }
	private String getJSONAccessTokenType(String content) {
        JSONObject json = null;
		try {
			json = new JSONObject(content);
		} catch (JSONException e1) {
			return null;
		}
        try {
            return json.optString("token_type");
        } catch (Exception e) {
            return null;
        }
    }
}