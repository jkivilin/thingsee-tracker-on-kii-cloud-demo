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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class TileProviderFactory {

    public static WMSTileProvider getKapsiWmsTileProvider() {

        final String KAPSI_WMS = "http://tiles.kartat.kapsi.fi/peruskartta?service=WMS&version=1.1.1&request=GetMap&format=image/png&styles=&bbox=%f,%f,%f,%f&width=256&height=256&srs=EPSG:900913&transparent=yes";
        
        WMSTileProvider tileProvider = new WMSTileProvider(256, 256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, KAPSI_WMS, bbox[MINX], bbox[MINY], bbox[MAXX], bbox[MAXY]);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                //Log.d("WMSTileProvider",s);
                return url;
            }
        };
        return tileProvider;
    }
    public static WMSTileProvider getKapsiWmsOrtoTileProvider() {

        final String KAPSI_WMS_ORTO =  "http://tiles.kartat.kapsi.fi/ortokuva?service=WMS&version=1.1.1&request=GetMap&format=image/png&layers=ortokuva&styles=&bbox=%f,%f,%f,%f&width=256&height=256&srs=EPSG:900913&transparent=yes";

        WMSTileProvider tileProvider = new WMSTileProvider(256, 256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                double[] bbox = getBoundingBox(x, y, zoom);
                String s = String.format(Locale.US, KAPSI_WMS_ORTO, bbox[MINX], bbox[MINY], bbox[MAXX], bbox[MAXY]);
                URL url = null;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                //Log.d("WMSTileProvider", "orto:" + s);
                return url;
            }
        };
        return tileProvider;
    }
}