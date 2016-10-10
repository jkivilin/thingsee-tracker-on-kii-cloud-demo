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

package com.thingsee.tracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.thingsee.tracker.REST.KiiGetUserInfoAsyncTask;
import com.thingsee.tracker.REST.KiiQueryTrackersAsyncTask;
import com.thingsee.tracker.REST.KiiSignInUserRequestAsyncTask;
import com.thingsee.tracker.libs.AccountModel;
import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.TrackTailModel;
import com.thingsee.tracker.libs.TrackerModel;
import com.thingsee.tracker.libs.Utilities;
import com.thingsee.tracker.tiles.TileProviderFactory;
import com.thingsee.tracker.views.ClearTextView;
import com.thingsee.tracker.views.TouchableWrapper;
import com.thingsee.tracker.views.TrackerMapFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressLint("InflateParams")
public class MainActivity extends FragmentActivity implements
        TouchableWrapper.UpdateMapAfterUserInterection, OnMapReadyCallback {

	private static Context mContext;
	private RelativeLayout splashScreen;
	private static GoogleMap mMap;
    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;

    public static final String PREFS_NAME = "TrackerPrefsFile";
    
	private Handler mSplashHandler = new Handler();
    private Dialog loginCredentials;

    private static Handler mHandler = new Handler();
    private ProgressDialog progressReadingTrackers;

    private boolean trackersLoaded = false;

    private static LinkedHashMap<String, TrackerModel> trackers = new LinkedHashMap<String, TrackerModel>();

    private static LinearLayout loginScreen;
    private boolean mPrepopulateLogin = false;

    private static HorizontalScrollView trackerList;
    private static LinearLayout mTrackerItemLayout;

    private static boolean onChildOnMapView = false;
	
	private boolean mapLoaded = false;
	private boolean splashReady = false;

    private int mapBoundsPadding = 0;

    private boolean touchActive = true;
    private boolean trackersActive = false;

    private static AccountModel userAccountModel;
    
    private static Resources mResources;

    private FetchCloudDataStateReceiver mFetchCloudDataStateReceiver;
    private boolean toBeDestroyed = false;

    private static SharedPreferences mSettings;
    private static TrackerModel trackerModelWithMarker = null;
    private static View trackerInfoWindow;

    private static ProgressDialog progress;
    private static boolean userZoomAndPanOnMap = false;
    private String mChildOnFocus = null;

    private boolean startupDone = false;

    LatLng mainari = new LatLng(61.137694, 21.991829);
    boolean firstTime = true;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = this;
        mResources = getResources();
        mSettings = getSharedPreferences(MainActivity.PREFS_NAME, 0);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mapBoundsPadding = size.x / CommonConstants.SCREEN_PADDING_WITH_MAP_BOUNDS;

        progress = new ProgressDialog(this);

		splashScreen = (RelativeLayout)findViewById(R.id.splash_screen);
		splashScreen.setVisibility(View.VISIBLE);

        loginScreen = (LinearLayout)findViewById(R.id.login_view);

        mSplashHandler.postDelayed(splashScreenOffFromDisplay, CommonConstants.SPLASH_SCREEN_TIMEOUT);
        Log.d("Tracker", "waiting for GoogleMaps");

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_area);
        mapFragment.getMapAsync(this);
	}

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        TileProvider wmsTileProvider = TileProviderFactory.getKapsiWmsTileProvider();
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider)
                .fadeIn(true));

        initGoogleMap();

        trackerList = (HorizontalScrollView) this.findViewById(R.id.tracker_scroll_area);
        mTrackerItemLayout = (LinearLayout) findViewById(R.id.trackers);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mainari, 16));
        Marker mark = mMap.addMarker(new MarkerOptions()
                .position(mainari));

        ImageView locateButton = (ImageView) findViewById(R.id.app_icon);

        //set the ontouch listener
        locateButton.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        if (onChildOnMapView) {
                            onUpPressed();
                        } else {
                            userZoomAndPanOnMap = false;
                            zoomToBoundingBox();
                        }
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().setColorFilter(mResources.getColor(R.color.white_effect), PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        ImageView settingsButton = (ImageView) findViewById(R.id.header_settings_icon);

        //set the ontouch listener
        settingsButton.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        final Dialog verificationQuery = new Dialog(mContext,android.R.style.Theme_Translucent_NoTitleBar);
                        verificationQuery.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        verificationQuery.setCancelable(false);
                        verificationQuery.setContentView(R.layout.request_admin_code);
                        ClearTextView ok = (ClearTextView) verificationQuery.findViewById(R.id.ok);
                        ok.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                verificationQuery.dismiss();
                                EditText code = (EditText)verificationQuery.findViewById(R.id.verification_code);
                                if (code.getText().toString().equalsIgnoreCase("password")) {
                                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });
                        ClearTextView cancel = (ClearTextView) verificationQuery.findViewById(R.id.cancel);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                verificationQuery.dismiss();
                            }
                        });
                        verificationQuery.show();
                        verificationQuery.getWindow().setDimAmount(0.5f);
                        verificationQuery.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().setColorFilter(mResources.getColor(R.color.white_effect), PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                if (onChildOnMapView) {
                    if (trackerModelWithMarker != null) {
                        trackerModelWithMarker.getMarker().showInfoWindow();
                    }
                }
            }
        });

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if (trackersActive) {
                    LatLng latlng = marker.getPosition();
                    userZoomAndPanOnMap = false;
                    if ((latlng.latitude == mainari.latitude) && (latlng.longitude == mainari.longitude)) {
                        if (onChildOnMapView) {
                            if (trackerModelWithMarker != null) {
                                trackerModelWithMarker.getMarker().showInfoWindow();
                            }
                        }
                    } else {
                        if (!onChildOnMapView) {
                            trackerModelWithMarker = null;
                            // Zoom to marker tapped
                            zoomToMarker(latlng);
                            //Remove other markers
                            for (String key : trackers.keySet()) {
                                TrackerModel trackerModel = trackers.get(key);
                                if (trackerModel.getLatestLatLng() != null) {
                                    if ((trackerModel.getLatestLatLng().latitude == latlng.latitude) &&
                                            (trackerModel.getLatestLatLng().longitude == latlng.longitude)) {
                                        focusOnChildOnMap(trackerModel.getSerialNumber());
                                        trackerModelWithMarker = trackerModel;
                                        trackerModelWithMarker.getMarker().showInfoWindow();
                                    }
                                }
                            }
                        } else {
                            trackerModelWithMarker.getMarker().showInfoWindow();
                            for (String key : trackers.keySet()) {
                                TrackerModel trackerModel = trackers.get(key);
                                if (trackerModel.getLatestLatLng() != null) {
                                    if ((trackerModel.getLatestLatLng().latitude == latlng.latitude) &&
                                            (trackerModel.getLatestLatLng().longitude == latlng.longitude)) {
                                        onBackPressed();
                                    }
                                }
                            }
                        }
                    }
                }
                return true;
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = null;

                for (String key : trackers.keySet()) {
                    TrackerModel trackerModel = trackers.get(key);
                    if (trackerModel.getLatestLatLng() != null) {
                        if ((trackerModel.getLatestLatLng().latitude == arg0.getPosition().latitude) &&
                                (trackerModel.getLatestLatLng().longitude == arg0.getPosition().longitude)) {
                            v = getLayoutInflater().inflate(R.layout.info_window, null);
                            trackerModelWithMarker = trackerModel;
                            TextView trackerAccuracy = (TextView) v.findViewById(R.id.tracker_marker_popup_accuracy);
                            if (trackerModelWithMarker.getAccuracy() != -1) {
                                trackerAccuracy.setText(String.format(mResources.getString(R.string.tracker_accuracy), trackerModelWithMarker.getAccuracy()));
                            } else {
                                trackerAccuracy.setText(String.format(mResources.getString(R.string.tracker_accuracy), 0.0f));
                            }
                            TextView trackerDistanceTs = (TextView) v.findViewById(R.id.tracker_marker_popup_update_timestamp);
                            if (trackerModelWithMarker.getLastLocationUpdate() != 0) {
                                String timeStampText = Utilities.getSmartTimeStampString(mContext, mResources, trackerModelWithMarker.getLastLocationUpdate());
                                trackerDistanceTs.setText(mResources.getString(R.string.tracker_timestamp) + " " + timeStampText);
                            } else {
                                trackerDistanceTs.setText(mResources.getString(R.string.tracker_timestamp) + " - ");
                            }
                            trackerInfoWindow = v;
                        }
                    }
                }
                // Returning the view containing InfoWindow contents
                return v;
            }
        });
        IntentFilter statusIntentFilter = new IntentFilter(
                CommonConstants.BROADCAST_ACTION);
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mFetchCloudDataStateReceiver = new FetchCloudDataStateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mFetchCloudDataStateReceiver,
                statusIntentFilter);

        mapLoaded = true;
        if (splashReady) {
            mSplashHandler.postDelayed(splashScreenOffFromDisplay, 0);
        }
    }
    public void onUpdateMapAfterUserInterection() {
        userZoomAndPanOnMap = true;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    private void zoomToMarker(LatLng latlng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latlng)
                .zoom(CommonConstants.FOCUS_ZOOM_LEVEL)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CommonConstants.CAMERA_ANIMATION_DURATION, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onCancel() {
            }
        });
    }
    private void zoomToBoundingBox() {
        //Zoom to bounding box with all tracked visible
        LatLngBounds.Builder builder = null;
        for (String key : trackers.keySet()) {
            if(trackers.get(key).getLatestLatLng() != null) {
                if (builder == null) {
                    builder = new LatLngBounds.Builder();
                }
                builder.include(trackers.get(key).getLatestLatLng());
            }
        }
        if (builder == null) {
            builder = new LatLngBounds.Builder();
        }
        builder.include(mainari);
        if(builder != null) {
            LatLngBounds bounds = adjustBoundsForMaxZoomLevel(builder.build());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapBoundsPadding), CommonConstants.CAMERA_ANIMATION_DURATION, null);
        }
    }
    private void focusOnChildOnMap(String serial) {
        if(trackersActive) {
            mChildOnFocus = serial;
            onChildOnMapView = true;
            removeTrackers(serial);
            trackerList.setVisibility(View.INVISIBLE);
        }
    }
    private void removeTrackers(String serial) {
        for (String key: trackers.keySet()) {
            TrackerModel trackerModel = trackers.get(key);
            if(!serial.equalsIgnoreCase(trackerModel.getSerialNumber())) {
                if(trackerModel.getMarker() != null) {
                    trackerModel.getMarker().setVisible(false);
                }
                trackerModel.setMarkerVisibility(false);
                if(trackerModel.getAccuracyCircle() != null) {
                    trackerModel.getAccuracyCircle().setVisible(false);
                }
            } else {
                trackerModel.getTrackTail().setVisibility(true);
                if(trackerModel.getAccuracyCircle() != null) {
                    trackerModel.getAccuracyCircle().setVisible(true);
                }
            }
        }
    }
    private void displayTrackers() {
        for (String key: trackers.keySet()) {
            TrackerModel trackerModel = trackers.get(key);
            trackerModel.setMarkerVisibility(true);
            trackerModel.getTrackTail().setVisibility(false);
            if(trackerModel.getMarker() != null) {
                trackerModel.getMarker().setVisible(true);
            }
            if(trackerModel.getAccuracyCircle() != null) {
                trackerModel.getAccuracyCircle().setVisible(false);
            }
        }
    }
    private void onUpPressed() {
        if(touchActive) {
            if (onChildOnMapView) {
                if ((trackerModelWithMarker != null) && (trackerModelWithMarker.getMarker().isInfoWindowShown())) {
                    trackerModelWithMarker.getMarker().hideInfoWindow();
                }
                displayTrackers();
                trackerList.setVisibility(View.VISIBLE);
                onChildOnMapView = false;
                userZoomAndPanOnMap = false;
                zoomToBoundingBox();
            }
        }
    }
	@Override
	public void onBackPressed() {
        if(touchActive) {
            if(onChildOnMapView){
                if ((trackerModelWithMarker != null) && (trackerModelWithMarker.getMarker().isInfoWindowShown())) {
                    trackerModelWithMarker.getMarker().hideInfoWindow();
                }
                displayTrackers();
                trackerList.setVisibility(View.VISIBLE);
                onChildOnMapView = false;
                userZoomAndPanOnMap = false;
                zoomToBoundingBox();
            } else {
                final Dialog exitQuery = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
                exitQuery.requestWindowFeature(Window.FEATURE_NO_TITLE);
                exitQuery.setCancelable(true);
                exitQuery.setContentView(R.layout.exit_app_query);
                ClearTextView cancel = (ClearTextView) exitQuery.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        exitQuery.cancel();
                    }
                });
                ClearTextView exit = (ClearTextView) exitQuery.findViewById(R.id.exit);
                exit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        exitApplication();
                        startupDone = false;
                        exitQuery.cancel();
                    }
                });
                exitQuery.show();
                exitQuery.getWindow().setDimAmount(0.5f);
                exitQuery.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }
	}
	private void exitApplication() {
        finish();
	}

	private Runnable splashScreenOffFromDisplay = new Runnable(){

        @SuppressLint("InflateParams")
		public void run() {
        	splashReady = true;
            Log.d("Tracker", "splashScreenOffFromDisplay called");
        	if(mapLoaded) {
                Log.d("Tracker", "splashScreenOffFromDisplay map ready, login user");
	        	splashScreen.setVisibility(View.GONE);
                String userName = mSettings.getString("Username", null);
                String passWord = mSettings.getString("Password", null);
                for (String key : trackers.keySet()) {
                    TrackerModel trackerModel = trackers.get(key);
                    if(trackerModel.getMarker() != null) {
                        trackerModel.getMarker().remove();
                    }
                    if(trackerModel.getTrackTail() != null) {
                        trackerModel.getTrackTail().removeTail();
                    }
                }
                trackers.clear();
                if((userName != null) && (passWord != null)) {
                    loginUser(userName, passWord, true);
                } else {
                    loginScreen.setVisibility(View.VISIBLE);
                }
        	} else {
                Log.d("Tracker", "splashScreenOffFromDisplay map not ready yet");
            }
        }
	};
    public void onClickShowLogin(View v) {
        loginCredentials = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        loginCredentials.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginCredentials.setCancelable(true);
        loginCredentials.setContentView(R.layout.login_credentials);
        final EditText usernameInput = (EditText) loginCredentials.findViewById(R.id.username);
        final EditText passwordInput = (EditText) loginCredentials.findViewById(R.id.password);
        if(mPrepopulateLogin) {
            String userName = mSettings.getString("Username", null);
            String passWord = mSettings.getString("Password", null);
            if((userName != null) && (passWord != null)) {
                usernameInput.setText(userName);
                passwordInput.setText(passWord);
            }
        }
        ClearTextView login = (ClearTextView) loginCredentials.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                loginUser(username, password, false);
            }
        });
        loginCredentials.show();
    }
    private void loginUser(final String username, final String password, final boolean saveCredentials) {
        Log.d("Tracker", "loginUser()");
        progress.setTitle(mResources.getString(R.string.login_in_user_title));
        progress.setMessage(mResources.getString(R.string.login_in_user_message));
        progress.setCancelable(false);
        progress.show();

        new KiiSignInUserRequestAsyncTask(new KiiSignInUserRequestAsyncTask.AsyncResponseSignInUser() {
            @Override
            public void processFinish(String accessToken) {
                if(accessToken != null){
                    if (!saveCredentials) {
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString("Username", username);
                        editor.putString("Password", password);
                        editor.commit();
                    }
                    if (loginCredentials != null) {
                        loginCredentials.cancel();
                    }
                    loadUserInfo(accessToken);
                } else {
                    progress.dismiss();
                    if (loginScreen.getVisibility() != View.VISIBLE) {
                        mPrepopulateLogin = true;
                        loginScreen.setVisibility(View.VISIBLE);
                    }
                    showError("Could not login, please check username and password!");
                }
            }
        }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY, password, username).execute();
    }
    private void loadUserInfo(final String accessToken){
        Log.d("Tracker", "loadUserInfo(), accessToken:" + accessToken);
        new KiiGetUserInfoAsyncTask(new KiiGetUserInfoAsyncTask.AsyncResponseUserInfo() {
            @Override
            public void processFinish(LinkedHashMap<String, String> info) {
                String[] splitStr = info.get("displayName").split("\\s+");
                String firstName = info.get("displayName");
                String lastName = info.get("displayName");
                if ((splitStr[0] != null) && (!splitStr[0].isEmpty())) {
                    firstName = splitStr[0];
                }
                if ((splitStr[1] != null) && (!splitStr[1].isEmpty())) {
                    lastName = splitStr[1];
                }
                userAccountModel = new AccountModel(info.get("emailAddress"),
                        firstName, lastName, info.get("phoneNumber"),
                        info.get("emailAddress"));
                userAccountModel.setAccessToken(accessToken);
                progress.dismiss();
                loginScreen.setVisibility(View.GONE);
                mHandler.postDelayed(startupSetupDelay, CommonConstants.STARTUP_SETUP_DELAY);
                progressReadingTrackers = new ProgressDialog(mContext);
                progressReadingTrackers.setTitle(mResources.getString(R.string.fetching_trackers_title));
                progressReadingTrackers.setMessage(mResources.getString(R.string.fetching_trackers_message));
                progressReadingTrackers.setCancelable(false);
                progressReadingTrackers.show();
                loadTrackers();
            }
        }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY, accessToken).execute();
    }
    private void loadTrackers(){
        Log.d("Tracker", "loadTrackers()");
        new KiiQueryTrackersAsyncTask(new KiiQueryTrackersAsyncTask.AsyncResponseQueryTrackers() {
            @Override
            public void processFinish(LinkedHashMap<String, TrackerModel> trackers) {
                for (String key: trackers.keySet()) {
                    TrackerModel trackerModel = trackers.get(key);
                    addTracker(trackerModel);
                    Log.d("Tracker", "added Tracker, key:" + key + " name:" + trackerModel.getName() + " token:" + trackerModel.getThing().getCurrentAccessToken());
                }
                trackersLoaded = true;
            }
        }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY, getCurrentAccountAccessToken()).execute();
    }
    private Runnable startupSetupDelay = new Runnable(){

        @SuppressLint("InflateParams")
        public void run() {
            Log.d("Tracker", "startupSetupDelay, trackersLoaded = " + trackersLoaded);
            if(trackersLoaded && !startupDone){
                startupDone = true;
                restoreTrackers();
                activateTrackers();
                progressReadingTrackers.dismiss();
                Toast.makeText(mContext, mResources.getString(R.string.login_ok) + " " +
                        userAccountModel.getFirstName() + " " + userAccountModel.getLastName() + "!", Toast.LENGTH_LONG).show();
                mHandler.postDelayed(updateNotifTimestamp, CommonConstants.NOTIF_TIMESTAMP_UPDATE_INTERVAL);
                mHandler.postDelayed(pollAccessTokens, 5000);
            } else {
                mHandler.postDelayed(startupSetupDelay, 1000);
            }
        }
    };
    private Runnable pollAccessTokens = new Runnable(){
        boolean missingToken = true;

        @SuppressLint("InflateParams")
        public void run() {
            Log.d("Tracker", "pollAccessTokens");
            if(trackers.size() > 0) {
                for (String key: trackers.keySet()) {
                    TrackerModel trackerModel = trackers.get(key);
                    String token = trackerModel.getThing().getCurrentAccessToken();
                    if(token == null || token.isEmpty()) {
                        missingToken = true;
                        break;
                    } else {
                        missingToken = false;
                    }
                }
                if(missingToken) {
                    mHandler.postDelayed(pollAccessTokens, 1000);
                } else {
                    mHandler.postDelayed(pollData, 1000);
                }
            }
        }
    };
    private Runnable pollData = new Runnable(){

        @SuppressLint("InflateParams")
        public void run() {
            Log.d("Tracker", "pollData");
            int amount = 1;
                if(trackers.size() > 0) {
                    if(firstTime) {
                        amount = CommonConstants.MAP_TAIL_LENGTH;
                        firstTime = false;
                    }
                    for (String key: trackers.keySet()) {
                        TrackerModel trackerModel = trackers.get(key);
                        trackerModel.getThing().downloadBuckets(mContext, System.currentTimeMillis() + CommonConstants.KII_CLOUD_24H, amount);
                        if(System.currentTimeMillis() - trackerModel.getLastLocationUpdate() > CommonConstants.LOCATION_TIMEOUT_FOR_ALARM) {
                            if(!trackerModel.isLocationTimeoutAlarmOn()) {
                                trackerModel.setLocationTimeoutAlarmOn(true);
                                refreshTrackerMarker(trackerModel.getSerialNumber());
                            }
                        } else {
                            if(trackerModel.isLocationTimeoutAlarmOn()) {
                                trackerModel.setLocationTimeoutAlarmOn(false);
                                refreshTrackerMarker(trackerModel.getSerialNumber());
                            }
                        }
                    }
                }
            mHandler.postDelayed(pollData, 60000);
        }
    };
    public void onClickCreate(View v) {
        Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
        startActivity(intent);
        trackers.clear();
        userAccountModel = null;
        activateTrackers();
    }
    private static Runnable updateNotifTimestamp = new Runnable(){
        @SuppressLint("InflateParams")
        public void run() {
            if(trackerModelWithMarker != null) {
                if(trackerModelWithMarker.getMarker().isInfoWindowShown() && (trackerInfoWindow != null)) {
                    trackerModelWithMarker.getMarker().hideInfoWindow();
                    TextView trackerAccuracy = (TextView) trackerInfoWindow.findViewById(R.id.tracker_marker_popup_accuracy);
                    if(trackerModelWithMarker.getAccuracy() != -1) {
                        trackerAccuracy.setText(String.format(mResources.getString(R.string.tracker_accuracy), trackerModelWithMarker.getAccuracy()));
                    } else {
                        trackerAccuracy.setText(String.format(mResources.getString(R.string.tracker_accuracy), 0.0f));
                    }
                    TextView trackerDistanceTs = (TextView)trackerInfoWindow.findViewById(R.id.tracker_marker_popup_update_timestamp);
                    if(trackerModelWithMarker.getLastLocationUpdate() != 0) {
                        String timeStampText = Utilities.getSmartTimeStampString(mContext, mResources, trackerModelWithMarker.getLastLocationUpdate());
                        trackerDistanceTs.setText(mResources.getString(R.string.tracker_timestamp) + " " + timeStampText);
                    } else {
                        trackerDistanceTs.setText(mResources.getString(R.string.tracker_timestamp) + " - ");
                    }
                    trackerModelWithMarker.getMarker().showInfoWindow();
                }
            }
            mHandler.postDelayed(updateNotifTimestamp, CommonConstants.NOTIF_TIMESTAMP_UPDATE_INTERVAL);
        }
    };
	@SuppressLint("InflateParams")
	private Marker makeMarkerToMap(LatLng latLng, boolean visible, boolean alarmActive, String name, double timeStamp, double accuracy) {
        String snippet;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    LinearLayout tv = (LinearLayout) inflater.inflate(R.layout.tracker_map_marker, null, false);
        TextView trackerName = (TextView)tv.findViewById(R.id.tracker_name);
        trackerName.setText(name);
        ImageView image2 = (ImageView) tv.findViewById(R.id.tracker_marker_icon);
        if(alarmActive) {
            image2.setColorFilter(mResources.getColor(R.color.red));
            trackerName.setTextColor(mResources.getColor(R.color.white_effect));
        } else {
            image2.setColorFilter(mResources.getColor(R.color.dark_blue));
            trackerName.setTextColor(mResources.getColor(R.color.white_effect));
        }
        tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight()); 
        tv.setDrawingCacheEnabled(true);
        tv.buildDrawingCache();
        Bitmap bm = tv.getDrawingCache();
        tv = null;

        if (accuracy != -1) {
            snippet = String.format(mResources.getString(R.string.tracker_accuracy), accuracy);
        } else {
            snippet = String.format(mResources.getString(R.string.tracker_accuracy), 0.0f);
        }
        if (timeStamp != 0) {
            String timeStampText = Utilities.getSmartTimeStampString(mContext, mResources, timeStamp);
            snippet = snippet + "\n" + mResources.getString(R.string.tracker_timestamp) + " " + timeStampText;
        } else {
            snippet = snippet + "\n" + mResources.getString(R.string.tracker_timestamp) + " - ";
        }
        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bm))
                .visible(visible)
                .title(name)
                .snippet(snippet));
	}
    public void onClickTrackerItem(View v) {
        if(trackersActive) {
            String tag = v.getTag().toString();
            TrackerModel trackerModel = trackers.get(tag);
            LatLng latlng = trackerModel.getLatestLatLng();
            if ((trackerModel != null) && (latlng != null)) {
                if (!onChildOnMapView) {
                    // Zoom to marker tapped
                    zoomToMarker(latlng);
                    //Remove other markers
                    focusOnChildOnMap(trackerModel.getSerialNumber());
                    trackerModelWithMarker = trackerModel;
                    trackerModelWithMarker.getMarker().showInfoWindow();
                } else {
                    onBackPressed();
                }
            }
        }
	}
	private void initGoogleMap() {
        Log.d("Tracker", "initGoogleMap");
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setMapType(mMapType);
    }
	@Override
    protected void onDestroy() {
        toBeDestroyed = true;
        if(mSplashHandler != null) {
        	mSplashHandler.removeCallbacks(splashScreenOffFromDisplay);
        }
        if(mHandler != null) {
            mHandler.removeCallbacks(startupSetupDelay);
            mHandler.removeCallbacks(updateNotifTimestamp);
        }
        super.onDestroy();
    }
    private static void createTrackers() {
        for (String key: trackers.keySet()) {
            TrackerModel trackerModel = trackers.get(key);
            View view = trackerModel.getView();
            view.setTag(trackerModel.getSerialNumber());
            TextView trackerName = (TextView)view.findViewById(R.id.tracker_name);
            trackerName.setText(trackerModel.getName());
            trackerName.setVisibility(View.VISIBLE);
            mTrackerItemLayout.addView(trackerModel.getView(), 0);
            Log.d("Tracker", "createTrackers, key:" + key + " name:" + trackerModel.getName());
        }
    }
    private void activateTrackers(){
        for (String key: trackers.keySet()) {
            TrackerModel trackerModel = trackers.get(key);
            View view = trackerModel.getView();
            view.setClickable(true);
        }
        trackersActive = true;
    }
    private class FetchCloudDataStateReceiver extends BroadcastReceiver {

        private FetchCloudDataStateReceiver() {
        }
        @Override
        public void onReceive(Context context, Intent intent) {

            synchronized (this) {
                if(!toBeDestroyed) {
                    switch (intent.getIntExtra(CommonConstants.EXTENDED_DATA_STATUS,
                            CommonConstants.BROADCAST_STATUS_DONE)) {
                        case CommonConstants.BROADCAST_STATUS_EXIT:
                            exitApplication();
                            break;
                        case CommonConstants.BROADCAST_STATUS_LOCATION_UPDATE:
                            LatLng latLng = new LatLng(intent.getDoubleExtra(CommonConstants.EXTENDED_LOCATION_ITEM_LAT, 0), intent.getDoubleExtra(CommonConstants.EXTENDED_LOCATION_ITEM_LNG, 0));
                            float accuracy = intent.getFloatExtra(CommonConstants.EXTENDED_LOCATION_ITEM_ACCURACY, 0);
                            String vendorThingId = intent.getStringExtra(CommonConstants.EXTENDED_VENDOR_THING_ID);
                            double ts = intent.getDoubleExtra(CommonConstants.EXTENDED_LOCATION_ITEM_TIMESTAMP, 0);
                            float batteryLevel = intent.getFloatExtra(CommonConstants.EXTENDED_CURRENT_LEVEL, 0);
                            TrackerModel trackerModel = getTrackerBasedOnVendorThingId(vendorThingId);
                            if((trackerModel != null) && (ts > trackerModel.getLastLocationUpdate())) {
                                trackerModel.setCurrentLatLng(latLng);
                                trackerModel.setLastLocationUpdate(ts);
                                if (trackerModel.getMarker() != null) {
                                    trackerModel.getMarker().remove();
                                }
                                if(System.currentTimeMillis() - trackerModel.getLastLocationUpdate() > CommonConstants.LOCATION_TIMEOUT_FOR_ALARM) {
                                    if(!trackerModel.isLocationTimeoutAlarmOn()) {
                                        trackerModel.setLocationTimeoutAlarmOn(true);
                                        ImageView trackerBg = (ImageView) trackerModel.getView().findViewById(R.id.tracker_image_bg);
                                        trackerBg.setColorFilter(mResources.getColor(R.color.red));
                                    }
                                } else {
                                    if(trackerModel.isLocationTimeoutAlarmOn()) {
                                        trackerModel.setLocationTimeoutAlarmOn(false);
                                        ImageView trackerBg = (ImageView) trackerModel.getView().findViewById(R.id.tracker_image_bg);
                                        trackerBg.setColorFilter(mResources.getColor(R.color.green));
                                    }
                                }
                                Marker newTrackerMarker = makeMarkerToMap(trackerModel.getLatestLatLng(), trackerModel.getMarkerVisible(), trackerModel.isLocationTimeoutAlarmOn(),
                                        trackerModel.getName(), trackerModel.getLastLocationUpdate(), accuracy);
                                if (newTrackerMarker == null) {
                                    throw new AssertionError("Map marker cannot be null in location update!!!");
                                }
                                trackerModel.setMarker(newTrackerMarker);
                                trackerModel.getTrackTail().add(trackerModel.getLatestLatLng());
                                if (trackerModel.getAccuracyCircle() != null) {
                                    trackerModel.getAccuracyCircle().remove();
                                }
                                if (accuracy != 0) {
                                    CircleOptions co = new CircleOptions();
                                    co.center(latLng);
                                    co.radius(accuracy);
                                    co.fillColor(mResources.getColor(R.color.accuracy_circle_green_fill));
                                    co.strokeColor(mResources.getColor(R.color.green_for_acc));
                                    co.strokeWidth(2.0f);
                                    co.visible(trackerModel.getMarkerVisible());
                                    co.zIndex(1000);
                                    trackerModel.setAccuracyCircle(mMap.addCircle(co));
                                    trackerModel.setAccuracy(accuracy);
                                }
                                if (onChildOnMapView) {
                                    if(trackerModelWithMarker != null) {
                                        trackerModelWithMarker.getMarker().showInfoWindow();
                                    }
                                     if(!userZoomAndPanOnMap && (mChildOnFocus.equalsIgnoreCase(trackerModel.getSerialNumber()))) {
                                         zoomToMarker(trackerModel.getLatestLatLng());
                                     }
                                } else if (!userZoomAndPanOnMap){
                                    zoomToBoundingBox();
                                }
                                View view = trackerModel.getView();
                                TextView trackerBattLevel = (TextView)view.findViewById(R.id.tracker_alarms_count);
                                trackerBattLevel.setText(String.format(" %.0f", batteryLevel) + "%");
                                ImageView trackerBattLevelBg = (ImageView)view.findViewById(R.id.tracker_alarm_icon_bg2);
                                if(batteryLevel < CommonConstants.LOW_BATTERY_LEVEL_FOR_ALARM) {
                                    trackerBattLevelBg.setColorFilter(mResources.getColor(R.color.red));
                                } else {
                                    trackerBattLevelBg.setColorFilter(mResources.getColor(R.color.green));
                                }
                                mHandler.postDelayed(updateNotifTimestamp, 0);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
    private LatLngBounds adjustBoundsForMaxZoomLevel(LatLngBounds bounds) {
        LatLng sw = bounds.southwest;
        LatLng ne = bounds.northeast;
        double deltaLat = Math.abs(sw.latitude - ne.latitude);
        double deltaLon = Math.abs(sw.longitude - ne.longitude);

        final double zoomN = 0.001; // minimum zoom coefficient
        if (deltaLat < zoomN) {
            sw = new LatLng(sw.latitude - (zoomN - deltaLat / 2), sw.longitude);
            ne = new LatLng(ne.latitude + (zoomN - deltaLat / 2), ne.longitude);
            bounds = new LatLngBounds(sw, ne);
        }
        else if (deltaLon < zoomN) {
            sw = new LatLng(sw.latitude, sw.longitude - (zoomN - deltaLon / 2));
            ne = new LatLng(ne.latitude, ne.longitude + (zoomN - deltaLon / 2));
            bounds = new LatLngBounds(sw, ne);
        }

        return bounds;
    }
    public static String getCurrentAccountFirstName() {
        return userAccountModel.getFirstName();
    }
    public static String getCurrentAccountLastName() {
        return userAccountModel.getLastName();
    }
    public static String getCurrentAccountAccessToken() {
        return userAccountModel.getAccessToken();
    }
    public static List<TrackerModel> getTrackers() {
        List<TrackerModel> listOfTrackerModels = new ArrayList<TrackerModel>();
        for (String key : trackers.keySet()) {
            listOfTrackerModels.add(trackers.get(key));
        }
        return listOfTrackerModels;
    }
    public static TrackerModel getTracker(String serial) {
        return trackers.get(serial);
    }
    public static void hideLoginScreen() {
        loginScreen.setVisibility(View.GONE);
    }

    public static void setOwner(AccountModel accountModel, String username, String password) {
        userAccountModel = accountModel;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("Username", username);
        editor.putString("Password", password);
        editor.commit();
    }
    public static void restoreTrackers() {
        mTrackerItemLayout.removeAllViews();
        createTrackers();
        mTrackerItemLayout.requestLayout();
        trackerList.requestLayout();
    }
    public static void addTracker(TrackerModel newTrackerModel) {
        LayoutInflater li = LayoutInflater.from(mContext);
        View viewTracker = li.inflate(R.layout.tracker_list_item_main, null);
        viewTracker.setClickable(true);
        newTrackerModel.setView(viewTracker);
        newTrackerModel.setMarkerVisibility(true);
        trackers.put(newTrackerModel.getSerialNumber(), newTrackerModel);
        trackers.get(newTrackerModel.getSerialNumber()).setTrackTail(new TrackTailModel(mContext, mMap, CommonConstants.MAP_TAIL_LENGTH));
    }
    public static void removeTracker(String serial) {
        if(trackers.get(serial).getMarker() != null) {
            trackers.get(serial).getMarker().remove();
        }
        if(trackers.get(serial).getAccuracyCircle() != null) {
            trackers.get(serial).getAccuracyCircle().remove();
        }
        if(trackers.get(serial).getTrackTail() != null) {
            trackers.get(serial).getTrackTail().removeTail();
        }
        trackers.remove(serial);
    }
    public static void showError(String errorMessage) {
        final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(mContext);
        errorDialogBuilder.setCancelable(false)
                .setTitle(R.string.title_error)
                .setNeutralButton(R.string.button_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                .setMessage(errorMessage);
        AlertDialog alertDialog = errorDialogBuilder.create();
        alertDialog.show();
    }
    public static void refreshTrackerMarker(String serial) {
        TrackerModel trackerModel = trackers.get(serial);
        if(trackerModel != null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            LinearLayout tv = (LinearLayout) inflater.inflate(R.layout.tracker_map_marker, null, false);
            ImageView image2 = (ImageView) tv.findViewById(R.id.tracker_marker_icon);
            TextView name = (TextView)tv.findViewById(R.id.tracker_name);
            name.setText(trackerModel.getName());
            ImageView trackerBg = (ImageView) trackerModel.getView().findViewById(R.id.tracker_image_bg);
            if(trackerModel.isLocationTimeoutAlarmOn()) {
                image2.setColorFilter(mResources.getColor(R.color.red));
                trackerBg.setColorFilter(mResources.getColor(R.color.red));
                name.setTextColor(mResources.getColor(R.color.white_effect));
            } else {
                image2.clearColorFilter();
                trackerBg.setColorFilter(mResources.getColor(R.color.green));
                name.setTextColor(mResources.getColor(R.color.black_effect));
            }
            tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
            tv.setDrawingCacheEnabled(true);
            tv.buildDrawingCache();
            Bitmap bm = tv.getDrawingCache();
            tv = null;
            if(trackerModel.getMarker() != null) {
                trackerModel.getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(bm));
            }
            if(trackerModelWithMarker != null) {
                if(onChildOnMapView) {
                    trackerModelWithMarker.getMarker().showInfoWindow();
                }
            }
        }
    }
    public static TrackerModel getTrackerBasedOnVendorThingId(String vendorThingId) {
        for (String key : trackers.keySet()) {
            TrackerModel trackerModel = trackers.get(key);
            if (trackerModel.getThing().getCurrentVendorThingID().equalsIgnoreCase(vendorThingId)) {
                return trackerModel;
            }
        }
        return null;
    }
}
