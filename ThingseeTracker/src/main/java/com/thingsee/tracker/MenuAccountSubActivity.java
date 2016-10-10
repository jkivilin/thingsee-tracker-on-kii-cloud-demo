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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.thingsee.tracker.REST.BroadCastNotifier;
import com.thingsee.tracker.views.ClearTextView;

public class MenuAccountSubActivity extends Activity{
    private BroadCastNotifier mBroadCastNotifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_account_sub);

        mBroadCastNotifier = new BroadCastNotifier(this);

        String name = MainActivity.getCurrentAccountFirstName() + " " + MainActivity.getCurrentAccountLastName();
        TextView pageHeader = (TextView)findViewById(R.id.page_header);
        pageHeader.setText(name);
    }
    public void onClickLogout(View v) {
        final Dialog logoutQuery = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar);
        logoutQuery.requestWindowFeature(Window.FEATURE_NO_TITLE);
        logoutQuery.setCancelable(true);
        logoutQuery.setContentView(R.layout.logout_query);
        ClearTextView cancel = (ClearTextView) logoutQuery.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logoutQuery.cancel();
            }
        });
        ClearTextView exit = (ClearTextView) logoutQuery.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Username", null);
                editor.putString("Password", null);
                editor.commit();
                mBroadCastNotifier.broadcastIntentToExit();
                finish();
            }
        });
        logoutQuery.show();
        logoutQuery.getWindow().setDimAmount(0.5f);
        logoutQuery.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MenuAccountSubActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
