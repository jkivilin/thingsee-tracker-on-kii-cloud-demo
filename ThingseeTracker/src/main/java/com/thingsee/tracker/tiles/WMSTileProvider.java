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

package com.thingsee.tracker.tiles;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

import junit.framework.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class WMSTileProvider extends UrlTileProvider {

    // Web Mercator n/w corner of the map.
    private static final double[] TILE_ORIGIN = { -20037508.34789244, 20037508.34789244 };
    // array indexes for that data
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1; // "

    // Size of square world map in meters, using WebMerc projection.
    private static final double MAP_SIZE = 20037508.34789244 * 2;

    // array indexes for array to hold bounding boxes.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;

    // cql filters
    private String cqlString = "";

    // Construct with tile size in pixels, normally 256, see parent class.
    public WMSTileProvider(int x, int y) {
        super(x, y);
    }

    protected String getCql() {
        String jep = null;
        try {
            jep = URLEncoder.encode(cqlString, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            // Oh dear...
            Assert.fail("kapsi coding...");
        }
        Log.e("WMSTileProvider", jep);
        return jep;
    }

    public void setCql(String c) {
        cqlString = c;
    }

    // Return a web Mercator bounding box given tile x/y indexes and a zoom
    // level.
    protected double[] getBoundingBox(int x, int y, int zoom) {
        double tileSize = MAP_SIZE / Math.pow(2, zoom);
        double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
        double maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize;
        double miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize;
        double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

        double[] bbox = new double[4];
        bbox[MINX] = minx;
        bbox[MINY] = miny;
        bbox[MAXX] = maxx;
        bbox[MAXY] = maxy;
        
        //Log.e("WMSTileProvider, minx", Double.toString(minx));
        //Log.e("WMSTileProvider, miny", Double.toString(miny));
        //Log.e("WMSTileProvider, maxx", Double.toString(maxx));
        //Log.e("WMSTileProvider, maxy", Double.toString(maxy));

        return bbox;
    }

}