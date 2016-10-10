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

public class AccountModel {

    private String mLoginName;
    private String mFirstName;
    private String mLastName;
    private String mPhoneNumber;
    private String mEmailAddress;

    private String mAccessToken = null;

    public AccountModel(String loginName, String firstName, String lastName,
                        String phoneNumber, String emailAddress) {
        mLoginName = loginName;
        mFirstName = firstName;
        mLastName = lastName;
        mPhoneNumber = phoneNumber;
        mEmailAddress = emailAddress;
    }
    public String getLoginName() {
        return mLoginName;
    }
    public String getFirstName() {
        return mFirstName;
    }
    public String getLastName() {
        return mLastName;
    }
    public String getPhoneNumber() {
        return mPhoneNumber;
    }
    public String getEmailAddress() {
        return mEmailAddress;
    }
    public String getAccessToken(){
        return mAccessToken;
    }
    public void setAccessToken(String token) {
        mAccessToken = token;
    }
}
