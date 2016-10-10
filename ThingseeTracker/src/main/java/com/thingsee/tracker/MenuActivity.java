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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thingsee.tracker.libs.CommonConstants;
import com.thingsee.tracker.libs.TrackerModel;
import com.thingsee.tracker.views.ClearTextView;

import java.util.List;

public class MenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);

        String name = MainActivity.getCurrentAccountFirstName() + " " + MainActivity.getCurrentAccountLastName();
        //Currently logged in account
        TextView currentAccount = (TextView)findViewById(R.id.current_account);
        currentAccount.setText(name);

        //Trackers
        LinearLayout trackersList = (LinearLayout)findViewById(R.id.menu_tracker_list);
        List<TrackerModel> trackerModels = MainActivity.getTrackers();
        LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        for (int i = 0; i < trackerModels.size(); ++i) {
            TrackerModel trackerModel = trackerModels.get(i);
            View view = inflater.inflate(R.layout.tracker_list_menu, null);
            TextView trackerName = (TextView) view.findViewById(R.id.label);
            trackerName.setText(trackerModel.getName());
            trackerName.setTag(trackerModel.getSerialNumber());
            trackersList.addView(view);
        }
        View view = inflater.inflate(R.layout.tracker_list_menu, null);
        TextView trackerName = (TextView) view.findViewById(R.id.label);
        trackerName.setText(getResources().getString(R.string.menu_register_tracker));
        trackerName.setTag(getResources().getString(R.string.menu_register_tracker));
        ImageView trackerImage = (ImageView)view.findViewById(R.id.icon);
        trackerImage.setImageDrawable(getResources().getDrawable(R.drawable.add));
        trackerImage.setColorFilter(getResources().getColor(R.color.black_effect));
        trackersList.addView(view);
    }
    public void onClickAccount(View v) {
        Intent intent = new Intent(MenuActivity.this, MenuAccountSubActivity.class);
        startActivity(intent);
        finish();
    }
    public void OnClickTrackerSelected(View v) {
        String tag = v.getTag().toString();
        if(tag.equals((getResources().getString(R.string.menu_register_tracker)))){
            Intent intent = new Intent(MenuActivity.this, AddTrackerActivity.class);
            intent.setAction(CommonConstants.ACTION_NONE);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(MenuActivity.this, EditTrackerActivity.class);
            intent.putExtra(CommonConstants.EXTENDED_DATA_STRING_SERIAL, tag);
            startActivity(intent);
            finish();
        }
    }
    public void onClickAbout(View v){
        String versionName = BuildConfig.VERSION_NAME;

        final Dialog aboutDialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        aboutDialog.setCancelable(true);
        aboutDialog.setContentView(R.layout.about_app_dialog);
        TextView about = (TextView)aboutDialog.findViewById(R.id.about);
        about.setText(versionName);

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
}
