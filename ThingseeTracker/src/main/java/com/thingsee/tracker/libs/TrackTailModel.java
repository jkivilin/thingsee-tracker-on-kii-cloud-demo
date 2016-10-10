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

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.thingsee.tracker.R;

import java.util.LinkedList;

public class TrackTailModel {
    private Context mContext;
    private GoogleMap mMap;
    private int limit;
    private LinkedList<LatLng> tail = new LinkedList<LatLng>();
    private LinkedList<Polyline> tailLine = new LinkedList<Polyline>();
    private boolean mVisible = false;

    public TrackTailModel(Context context, GoogleMap map, int limit) {
        mContext = context;
        this.mMap = map;
        this.limit = limit;
    }

    public boolean add(LatLng location) {
        if(tail.size() != 0) {
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(tail.getLast(), location)
                    .width(5)
                    .color(mContext.getResources().getColor(R.color.dark_blue))
                    .visible(mVisible));
            tailLine.add(line);
            line.setZIndex(1000);
        }
        tail.add(location);
        while (tail.size() > limit) {
            tail.removeFirst();
            Polyline lineToRemove = tailLine.getFirst();
            lineToRemove.remove();
            tailLine.remove();
        }
        return true;
    }
    public int size() {
        return tail.size();
    }
    public void setVisibility(boolean visible) {
        mVisible = visible;
        for(int i = 0; i < tailLine.size(); i++) {
            tailLine.get(i).setVisible(visible);
        }
    }
    public boolean isVisible() {
        return mVisible;
    }
    public void removeTail(){
        tail.clear();
        for(int i = 0; i < tailLine.size(); i++) {
            tailLine.get(i).remove();
        }
        tailLine.clear();
    }
}