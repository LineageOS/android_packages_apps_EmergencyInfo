/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.emergency.emergencynumber;


import static com.android.settingslib.emergencynumber.EmergencyNumberUtils.EMERGENCY_GESTURE_CALL_NUMBER;
import static com.android.settingslib.emergencynumber.EmergencyNumberUtils.EMERGENCY_NUMBER_OVERRIDE_AUTHORITY;
import static com.android.settingslib.emergencynumber.EmergencyNumberUtils.METHOD_NAME_GET_EMERGENCY_NUMBER_OVERRIDE;
import static com.android.settingslib.emergencynumber.EmergencyNumberUtils.METHOD_NAME_SET_EMERGENCY_NUMBER_OVERRIDE;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Content provider that gets/sets emergency number override for emergency gesture.
 */
public class EmergencyNumberContentProvider extends ContentProvider {
    private static final String TAG = "EmergencyNumberContentP";
    private static final boolean DEBUG = true;
    private static final String SHARED_PREFERENCES_NAME =
            "local_emergency_number_override_shared_pref";

    @Override
    public Bundle call(String authority, String method, String arg, Bundle extras) {
        final Bundle bundle = new Bundle();
        final SharedPreferences preferences = getContext().getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        switch (method) {
            case METHOD_NAME_GET_EMERGENCY_NUMBER_OVERRIDE:
                if (DEBUG) {
                    Log.d(TAG, METHOD_NAME_GET_EMERGENCY_NUMBER_OVERRIDE);
                }
                bundle.putString(EMERGENCY_GESTURE_CALL_NUMBER,
                        preferences.getString(EMERGENCY_GESTURE_CALL_NUMBER, null));
                break;
            case METHOD_NAME_SET_EMERGENCY_NUMBER_OVERRIDE:
                if (DEBUG) {
                    Log.d(TAG, METHOD_NAME_SET_EMERGENCY_NUMBER_OVERRIDE);
                }
                final String inputNumber = extras.getString(EMERGENCY_GESTURE_CALL_NUMBER);
                preferences.edit().putString(EMERGENCY_GESTURE_CALL_NUMBER, inputNumber).apply();
                getContext().getContentResolver().notifyChange(EMERGENCY_NUMBER_OVERRIDE_AUTHORITY,
                        null);
                break;
        }
        return bundle;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
