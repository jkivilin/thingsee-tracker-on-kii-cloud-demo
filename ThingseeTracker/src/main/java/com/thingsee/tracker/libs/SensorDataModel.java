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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SensorDataModel {
    public double timeStamp = 0; 	// Thingsee epoch ts

    public double latitude = 0; 	// degrees
    public double longitude = 0; 	// degrees
    public float altitude = 0; 		// meters
    public float accuracy = 0; 		// meters
    public double gpsTimeStamp = 0; // GPS given ts

    public float groundSpeed = 0; 	// m/s
    public float airSpeed = 0; 		// m/s

    public float heading = 0;		// degrees
    public float tilt = 0;			// degrees
    public float pitch = 0;			// degrees
    public float roll = 0;			// degrees

    public float forwardBackwardGs = 0;		// G-force
    public float horizontalGs = 0;	// G-force
    public float verticalGs = 0;		// G-force
    public float impactGs = 0;		// G-force

    public float temperature = 0;	// Celcius
    public float humidity = 0;		// Percents
    public int ambientLight = 0;	// lux
    public float airPressure = 0;	// mbar

    public float current_level = 0;	// Voltages

    private static final String LATITUDE_DATA = 		"0x00010100";
    private static final String LONGITUDE_DATA = 		"0x00010200";
    private static final String ALTITUDE_DATA = 		"0x00010300";
    private static final String ACCURACY_DATA = 		"0x00010400";
    private static final String GPS_TIMESTAMP_DATA = 	"0x00010500";

    private static final String GROUND_SPEED_DATA = 	"0x00020100";
    private static final String AIR_SPEED_DATA = 		"0x00020200";

    private static final String CURRENT_LEVEL_DATA = 	"0x00030200";

    private static final String HEADING_DATA = 		"0x00040100";
    private static final String TILT_DATA = 			"0x00040200";
    private static final String PITCH_DATA = 			"0x00040300";
    private static final String ROLL_DATA = 			"0x00040400";

    private static final String FORWARD_BACKWARD_G_DATA = 	"0x00050100";
    private static final String HORIZONTAL_G_DATA = 		"0x00050200";
    private static final String VERTICAL_G_DATA = 			"0x00050300";
    private static final String IMPACT_G_DATA = 			"0x00050400";

    private static final String TEMPERATURE_DATA = 	"0x00060100";
    private static final String HUMIDITY_DATA = 		"0x00060200";
    private static final String AMBIENT_LIGHT_DATA =	"0x00060300";
    private static final String AIR_PRESSURE_DATA =	"0x00060400";
	
	public static SensorDataModel parseSensorDataFromJSON(JSONArray array) throws JSONException {
		SensorDataModel data = new SensorDataModel();
        
        for(int i=0; i<array.length(); i++){
            JSONObject json_data = array.getJSONObject(i);
            String id = json_data.getString("sId");
            if(id.equals(LATITUDE_DATA)) {
                data.latitude = json_data.getDouble("val");
            } else if(id.equals(LONGITUDE_DATA)) {
                data.longitude = json_data.getDouble("val");
            } else if(id.equals(ALTITUDE_DATA)) {
                data.altitude = json_data.getInt("val");
            } else if(id.equals(ACCURACY_DATA)) {
                data.accuracy = json_data.getInt("val");
            } else if(id.equals(GPS_TIMESTAMP_DATA)) {
                data.gpsTimeStamp = json_data.getDouble("val");
            } else if(id.equals(GROUND_SPEED_DATA)) {
                data.groundSpeed = json_data.getInt("val");
            } else if(id.equals(AIR_SPEED_DATA)) {
                data.airSpeed = json_data.getInt("val");
            } else if(id.equals(CURRENT_LEVEL_DATA)) {
                data.current_level = json_data.getInt("val");
            } else if(id.equals(HEADING_DATA)) {
                data.heading = json_data.getInt("val");
            } else if(id.equals(TILT_DATA)) {
                data.tilt = json_data.getInt("val");
            } else if(id.equals(PITCH_DATA)) {
                data.pitch = json_data.getInt("val");
            } else if(id.equals(ROLL_DATA)) {
                data.roll = json_data.getInt("val");
            } else if(id.equals(FORWARD_BACKWARD_G_DATA)) {
                data.forwardBackwardGs = json_data.getInt("val");
            } else if(id.equals(HORIZONTAL_G_DATA)) {
                data.horizontalGs = json_data.getInt("val");
            } else if(id.equals(VERTICAL_G_DATA)) {
                data.verticalGs = json_data.getInt("val");
            } else if(id.equals(IMPACT_G_DATA)) {
                data.impactGs = json_data.getInt("val");
            } else if(id.equals(TEMPERATURE_DATA)) {
                data.temperature = json_data.getInt("val");
            } else if(id.equals(HUMIDITY_DATA)) {
                data.humidity = json_data.getInt("val");
            } else if(id.equals(AMBIENT_LIGHT_DATA)) {
                data.ambientLight = json_data.getInt("val");
            } else if(id.equals(AIR_PRESSURE_DATA)) {
                data.airPressure = json_data.getInt("val");
            }
        }
        return data;
    }
}