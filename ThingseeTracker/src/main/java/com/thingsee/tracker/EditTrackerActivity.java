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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thingsee.tracker.REST.KiiEditTrackerAsyncTask;
import com.thingsee.tracker.REST.KiiRemoveTrackerAsyncTask;
import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.TrackerModel;
import com.thingsee.tracker.views.ClearTextView;

public class EditTrackerActivity extends Activity {
    private static Context mContext;
    String trackerSerial;

    private static Dialog editTrackerDialog = null;

    TrackerModel trckr = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_edit_menu);

        mContext = this;

        trackerSerial = getIntent().getStringExtra(CommonConstants.EXTENDED_DATA_STRING_SERIAL);
        trckr = MainActivity.getTracker(trackerSerial);
        TextView trackerName = (TextView)findViewById(R.id.tracker_name);
        trackerName.setText(trckr.getName());
    }

    public void OnClickRemoveTracker(View v) {
        final Dialog removeQuery = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        removeQuery.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeQuery.setCancelable(true);
        removeQuery.setContentView(R.layout.remove_tracker_query);
        final TextView trackerName = (TextView)removeQuery.findViewById(R.id.tracker_name);
        trackerName.setText(trckr.getName());
        TextView query = (TextView)removeQuery.findViewById(R.id.remove_query);

        ClearTextView cancel = (ClearTextView) removeQuery.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removeQuery.cancel();
            }
        });
        ClearTextView remove = (ClearTextView) removeQuery.findViewById(R.id.remove);
        remove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(mContext);
                progress.setTitle(getResources().getString(R.string.removing_tracker_from_cloud_title));
                progress.setMessage(getResources().getString(R.string.removing_tracker_from_cloud_message));
                progress.setCancelable(false);
                progress.show();
                new KiiRemoveTrackerAsyncTask(new KiiRemoveTrackerAsyncTask.AsyncResponseRemoveTracker() {
                    @Override
                    public void processFinish(boolean result) {
                        if (result) {
                            progress.dismiss();
                            MainActivity.removeTracker(trckr.getSerialNumber());
                            MainActivity.restoreTrackers();
                            Toast.makeText(mContext, getResources().getString(R.string.tracker_removed_note), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditTrackerActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            progress.dismiss();
                            showError("Something went wrong, tracker not removed from cloud!");
                        }
                    }
                }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                        MainActivity.getCurrentAccountAccessToken(), trckr.getObjectId()).execute();
            }
        });
        removeQuery.show();
        removeQuery.getWindow().setDimAmount(0.5f);
        removeQuery.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditTrackerActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
    public void onClickAbout(View v){
        final Dialog aboutDialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setCancelable(true);
        aboutDialog.setContentView(R.layout.about_tracker_dialog);
        TextView trackerName = (TextView)aboutDialog.findViewById(R.id.tracker_name);
        trackerName.setText(trckr.getName());
        TextView about = (TextView)aboutDialog.findViewById(R.id.about);
        about.setText(getResources().getString(R.string.about_tracker_serial) + trackerSerial);
        ClearTextView exit = (ClearTextView) aboutDialog.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                aboutDialog.cancel();
            }
        });
        aboutDialog.show();
        aboutDialog.getWindow().setDimAmount(0.5f);
        aboutDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
    public void OnClickEditTracker(View v){
        editTrackerDialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        editTrackerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editTrackerDialog.setCancelable(false);
        editTrackerDialog.setContentView(R.layout.edit_tracker);
        TextView trackerName = (TextView)editTrackerDialog.findViewById(R.id.display_name);
        trackerName.setText(trckr.getName());

        ClearTextView cancel = (ClearTextView) editTrackerDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editTrackerDialog.cancel();
            }
        });
        ClearTextView save = (ClearTextView) editTrackerDialog.findViewById(R.id.save_changes);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final ProgressDialog progress = new ProgressDialog(mContext);
                progress.setTitle(getResources().getString(R.string.saving_tracker_changes_to_cloud_title));
                progress.setMessage(getResources().getString(R.string.saving_tracker_changes_to_cloud_message));
                progress.setCancelable(false);
                progress.show();
                Log.d("EditTracker", "starting save...");
                EditText name = (EditText) editTrackerDialog.findViewById(R.id.display_name);
                final String trackerName = name.getText().toString();
                if ((trackerName == null) || (trackerName.isEmpty())) {
                    showError(getResources().getString(R.string.error_tracker_name_mandatory));
                } else {
                    new KiiEditTrackerAsyncTask(new KiiEditTrackerAsyncTask.AsyncResponseEditTracker(){
                        @Override
                        public void processFinish(boolean result) {
                            if (result) {
                                progress.dismiss();
                                Toast.makeText(mContext, getResources().getString(R.string.tracker_updated), Toast.LENGTH_SHORT).show();
                                trckr.setName(trackerName);

                                TextView trackersName = (TextView) findViewById(R.id.tracker_name);
                                trackersName.setText(trackerName);

                                MainActivity.restoreTrackers();
                                MainActivity.refreshTrackerMarker(trackerSerial);

                                editTrackerDialog.cancel();
                            } else {
                                progress.dismiss();
                                showError("Something went wrong, tracker info not updated to cloud!");
                            }
                        }
                    }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                            MainActivity.getCurrentAccountAccessToken(), trckr.getObjectId(), trackerName).execute();
                }
            }
        });
        editTrackerDialog.show();
        editTrackerDialog.getWindow().setDimAmount(0.5f);
        editTrackerDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
