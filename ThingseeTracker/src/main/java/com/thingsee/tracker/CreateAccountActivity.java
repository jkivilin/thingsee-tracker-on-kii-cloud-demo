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
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.thingsee.tracker.REST.KiiCreateUserRequestAsyncTask;
import com.thingsee.tracker.REST.KiiSignInUserRequestAsyncTask;
import com.thingsee.tracker.libs.AccountModel;
import com.thingsee.tracker.libs.CommonConstants;

public class CreateAccountActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account);
    }
    public void onClickOk(View v) {
        boolean allOk = false;

        EditText firstName = (EditText) findViewById(R.id.first_name);
        EditText lastName = (EditText) findViewById(R.id.last_name);
        EditText phoneNbr = (EditText) findViewById(R.id.phone_number);
        EditText emailAddress = (EditText) findViewById(R.id.email_address);
        EditText password = (EditText) findViewById(R.id.password);
        EditText passwordRetyped = (EditText) findViewById(R.id.retyped_password);

        final String accountFirstName = firstName.getText().toString();
        final String accountLastName = lastName.getText().toString();
        final String accountPhoneNbr = phoneNbr.getText().toString();
        final String accountEmailAddress = emailAddress.getText().toString();
        final String accountPassword = password.getText().toString();
        if(accountFirstName.isEmpty()) {
            showError(getResources().getString(R.string.mandatory_information_first_name_missing));
        } else if(accountLastName.isEmpty()) {
            showError(getResources().getString(R.string.mandatory_information_last_name_missing));
        } else if(accountPhoneNbr.isEmpty()) {
            showError(getResources().getString(R.string.mandatory_information_phone_nbr_missing));
        } else if(accountEmailAddress.isEmpty()) {
            showError(getResources().getString(R.string.mandatory_information_email_address_missing));
        } else if(!password.getText().toString().equals(passwordRetyped.getText().toString())) {
            showError(getResources().getString(R.string.password_do_not_match_retyped_password));
        } else if(accountPassword.equals(passwordRetyped.getText().toString())) {
            if (accountPassword.length() < 8) {
                showError(getResources().getString(R.string.password_less_than_8_chars));
            } else {
                allOk = true;
            }
        }

        if(allOk) {
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setTitle(getResources().getString(R.string.registering_user_title));
            progress.setMessage(getResources().getString(R.string.registering_user_message));
            progress.setCancelable(false);
            progress.show();
            new KiiCreateUserRequestAsyncTask(new KiiCreateUserRequestAsyncTask.AsyncResponseCreateUser() {
                @Override
                public void processFinish(String userID) {
                    if (userID != null) {
                        new KiiSignInUserRequestAsyncTask(new KiiSignInUserRequestAsyncTask.AsyncResponseSignInUser() {
                            @Override
                            public void processFinish(String accessToken) {
                                progress.dismiss();
                                if (accessToken != null) {
                                    AccountModel newAccountModel = new AccountModel(
                                            accountEmailAddress,
                                            accountFirstName,
                                            accountLastName,
                                            accountPhoneNbr,
                                            accountEmailAddress);
                                    newAccountModel.setAccessToken(accessToken);
                                    MainActivity.setOwner(newAccountModel, accountEmailAddress, accountPassword);
                                    progress.dismiss();
                                    finish();
                                } else {
                                    showError("Something went wrong, user not created to cloud!");
                                }
                            }
                        }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                            accountPassword, accountEmailAddress).execute();
                    } else {
                        progress.dismiss();
                        showError("Something went wrong, user not created to cloud!");
                    }
                }
            }, CommonConstants.KII_CLOUD_APP_ID, CommonConstants.KII_CLOUD_APP_KEY,
                    accountFirstName + accountLastName, accountFirstName + " " + accountLastName, accountPassword, accountEmailAddress, accountPhoneNbr).execute();
        }
    }
    private void showError(String errorMessage) {
        final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(this);
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
