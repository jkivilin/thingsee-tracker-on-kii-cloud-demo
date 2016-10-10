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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.thingsee.tracker.REST.KiiCreateObjectRequestAsyncTask;
import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.ThingModel;
import com.thingsee.tracker.libs.TrackerModel;

public class AddTrackerActivity extends Activity {
    private static Activity activity;
    private static Context mContext;

    private static EditText serial;
    private static EditText tracker_id;

    private static String trackerName;
    private static String trackerSerial;

    private boolean allOk = false;

    private static String action;

    private static ProgressDialog progress;
    private static ThingModel thingModelDevice;
    private static String vendorThingID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tracker_main);

        mContext = this;
        activity = this;
        action = getIntent().getAction();

        progress = new ProgressDialog(this);

        serial = (EditText) findViewById(R.id.serial_nbr);
        tracker_id = (EditText) findViewById(R.id.vendor_thing_id);
    }
    public void onClickRegister(View v) {

        EditText name = (EditText) findViewById(R.id.display_name);

        trackerName = name.getText().toString();
        vendorThingID = tracker_id.getText().toString();
        trackerSerial = serial.getText().toString();

        if((trackerName == null) || (trackerName.isEmpty())){
            showError(getResources().getString(R.string.error_tracker_name_mandatory));
        } else if((vendorThingID == null) || (vendorThingID.isEmpty())){
            showError(getResources().getString(R.string.error_tracker_id_mandatory));
        } else if((trackerSerial == null) || (trackerSerial.isEmpty())){
            showError(getResources().getString(R.string.error_tracker_serial_mandatory));
        } else {
            allOk = true;
        }

        if(allOk) {
            progress.setTitle(getResources().getString(R.string.adding_tracker_to_cloud_title));
            progress.setMessage(getResources().getString(R.string.adding_tracker_to_cloud_message));
            progress.setCancelable(false);
            progress.show();

            thingModelDevice = new ThingModel();

            new KiiCreateObjectRequestAsyncTask(new KiiCreateObjectRequestAsyncTask.AsyncResponseCreateObject() {
                @Override
                public void processFinish(String objID) {
                    if (objID != null) {
                        finishUp(objID);
                    } else {
                        progress.dismiss();
                        showError("Something went wrong, tracker not created to cloud!");
                    }
                }
            }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                    MainActivity.getCurrentAccountAccessToken(), trackerName, trackerSerial, vendorThingID).execute();
        }
    }
    private static void finishUp(String objectID) {
        thingModelDevice.initialize(CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                vendorThingID, trackerSerial);
        TrackerModel trackerModel = new TrackerModel(trackerName,
                null, trackerSerial, thingModelDevice);
        trackerModel.setObjectID(objectID);
        MainActivity.addTracker(trackerModel);
        MainActivity.restoreTrackers();

        thingModelDevice.downloadBuckets(mContext, System.currentTimeMillis() + CommonConstants.KII_CLOUD_24H, CommonConstants.MAP_TAIL_LENGTH);
        progress.dismiss();
        if (action.equalsIgnoreCase(CommonConstants.ACTION_PROCEED_TO_SETTINGS)) {
            Intent intent = new Intent(mContext, MenuActivity.class);
            activity.startActivity(intent);
        }
        activity.finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddTrackerActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
    private static void showError(String errorMessage) {
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
}
