/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.emergency.action.broadcast;

import static android.telecom.TelecomManager.EXTRA_CALL_SOURCE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.util.Log;

import com.android.emergency.action.service.EmergencyActionForegroundService;
import com.android.settingslib.emergencynumber.EmergencyNumberUtils;

/**
 * Broadcast receiver to handle actions for emergency gesture foreground service and notification
 */
public class EmergencyActionBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "EmergencyActionRcvr";


    private static final String ACTION_START_EMERGENCY_CALL =
            "com.android.emergency.broadcast.MAKE_EMERGENCY_CALL";
    private static final String ACTION_CANCEL_COUNTDOWN =
            "com.android.emergency.broadcast.CANCEL_EMERGENCY_COUNTDOWN";

    public static PendingIntent newCallEmergencyPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_START_EMERGENCY_CALL).setClass(context,
                        EmergencyActionBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent newCancelCountdownPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_CANCEL_COUNTDOWN).setClass(context,
                        EmergencyActionBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        String action = intent.getAction();
        switch (action) {
            case ACTION_START_EMERGENCY_CALL:
                Log.i(TAG, "Starting to call emergency number");
                placeEmergencyCall(context);
                // Intentionally fall through to cancel service
            case ACTION_CANCEL_COUNTDOWN:
                Log.i(TAG, "Cancelling scheduled emergency calls and foreground service");
                alarmManager.cancel(newCallEmergencyPendingIntent(context));
                EmergencyActionForegroundService.stopService(context);
                break;
            default:
                Log.w(TAG, "Unknown action received, skipping: " + action);
        }
    }

    private void placeEmergencyCall(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            Log.i(TAG, "Telephony is not supported, skipping.");
            return;
        }
        Bundle extras = new Bundle();
        extras.putBoolean(TelecomManager.EXTRA_IS_USER_INTENT_EMERGENCY_CALL, true);
        extras.putInt(EXTRA_CALL_SOURCE, TelecomManager.CALL_SOURCE_EMERGENCY_SHORTCUT);
        TelecomManager telecomManager = context.getSystemService(TelecomManager.class);
        EmergencyNumberUtils emergencyNumberUtils = new EmergencyNumberUtils(context);
        telecomManager.placeCall(
                Uri.fromParts(PhoneAccount.SCHEME_TEL, emergencyNumberUtils.getPoliceNumber(),
                        /* fragment= */ null), extras);
    }
}
