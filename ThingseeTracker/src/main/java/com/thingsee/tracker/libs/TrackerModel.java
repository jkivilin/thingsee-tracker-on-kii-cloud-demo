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

import android.view.View;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class TrackerModel {

    private String mName = null;
    private LatLng mCurrentLatLng = null;
    private Marker mMarker = null;
    private boolean mMarkerVisible = false;
    private View mTrackerView = null;
    private TrackTailModel mTrackTailModel;
    private String mSerialNumber;
    private String mObjectID;

    private ThingModel mThingModel = null;

    private Circle mAccuracyCircle = null;
    private float mAccuracy = -1;
    private double mLastLocationUpdate = 0;
    private boolean mLocationTimeoutAlarmOn = false;

    public TrackerModel(String name, LatLng currentLatLng, String serial, ThingModel thingModel) {
        mName = name;
        mCurrentLatLng = currentLatLng;
        mSerialNumber = serial;
        mThingModel = thingModel;
    }
    public String getName() {
        return mName;
    }
    public LatLng getLatestLatLng() {
        return mCurrentLatLng;
    }
    public TrackTailModel getTrackTail() {
        return mTrackTailModel;
    }
    public Marker getMarker() {
        return mMarker;
    }
    public boolean getMarkerVisible() {
        return mMarkerVisible;
    }
    public View getView(){
        return mTrackerView;
    }
    public String getSerialNumber() {
        return mSerialNumber;
    }
    public ThingModel getThing() {
        return mThingModel;
    }
    public Circle getAccuracyCircle() {
        return mAccuracyCircle;
    }
    public double getLastLocationUpdate() {
        return mLastLocationUpdate;
    }
    public boolean isLocationTimeoutAlarmOn() {
        return mLocationTimeoutAlarmOn;
    }
    public float getAccuracy(){
        return mAccuracy;
    }
    public String getObjectId(){
        return mObjectID;
    }

    public void setName(String name) {
        mName = name;
    }
    public void setMarker(Marker marker) {
        mMarker = marker;
    }
    public void setCurrentLatLng(LatLng latLng) {
        mCurrentLatLng = latLng;
    }
    public void setTrackTail(TrackTailModel trackTailModel) {
        mTrackTailModel = trackTailModel;
    }
    public void setView(View view) {
        mTrackerView = view;
    }
    public void setMarkerVisibility(boolean visible) {
        mMarkerVisible = visible;
    }
    public void setAccuracyCircle(Circle accuracy) {
        mAccuracyCircle = accuracy;
    }
    public void setLastLocationUpdate(double ts) {
        mLastLocationUpdate = ts;
    }
    public void setLocationTimeoutAlarmOn(boolean alarmOn) {
        mLocationTimeoutAlarmOn = alarmOn;
    }
    public void setAccuracy(float accuracy) {
        mAccuracy = accuracy;
    }
    public void setObjectID(String objID) {mObjectID = objID;}
}
