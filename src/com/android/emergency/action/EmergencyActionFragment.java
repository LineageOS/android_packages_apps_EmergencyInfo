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

package com.android.emergency.action;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.telecom.TelecomManager.EXTRA_CALL_SOURCE;
import static android.telephony.emergency.EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_POLICE;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.emergency.EmergencyNumber;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.emergency.R;
import com.android.emergency.widgets.countdown.CountDownAnimationView;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class EmergencyActionFragment extends Fragment {

    private static final String TAG = "EmergencyActionFrag";
    private static final String STATE_MILLIS_LEFT = "STATE_MILLIS_LEFT";

    private TelephonyManager mTelephonyManager;
    private TelecomManager mTelecomManager;
    private SubscriptionManager mSubscriptionManager;
    private CountDownTimer mCountDownTimer;
    private long mCountDownMillisLeft;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mTelephonyManager = context.getSystemService(TelephonyManager.class);
        mSubscriptionManager = context.getSystemService(SubscriptionManager.class);
        mTelecomManager = context.getSystemService(TelecomManager.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emergency_action_fragment, container, false);

        TextView subtitleView = view.findViewById(R.id.subtitle);
        subtitleView.setText(getString(R.string.emergency_action_subtitle, getEmergencyNumber()));

        if (savedInstanceState != null) {
            mCountDownMillisLeft = savedInstanceState.getLong(STATE_MILLIS_LEFT);
        } else {
            mCountDownMillisLeft =
                    getResources().getInteger(R.integer.emergency_action_count_down_millis);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startTimer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_MILLIS_LEFT, mCountDownMillisLeft);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mCountDownTimer != null) {
            CountDownAnimationView countDownAnimationView =
                    getView().findViewById(R.id.count_down_view);
            countDownAnimationView.stop();
            mCountDownTimer.cancel();
        }
    }

    private String getEmergencyNumber() {
        if (getContext().checkSelfPermission(READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_PHONE_STATE permission is not granted.");
            return getContext().getString(R.string.fallback_emergency_number);
        }

        Map<Integer, List<EmergencyNumber>> emergencyNumberListMap =
                mTelephonyManager.getEmergencyNumberList(EMERGENCY_SERVICE_CATEGORY_POLICE);
        if (!emergencyNumberListMap.isEmpty()) {
            List<EmergencyNumber> emergencyNumberList =
                    emergencyNumberListMap.get(
                            mSubscriptionManager.getDefaultSubscriptionId());
            if (!emergencyNumberList.isEmpty()) {
                String emergencyNumber = emergencyNumberList.get(0).getNumber();
                Log.i(TAG, "Emergency number from TelephonyManager: " + emergencyNumber);
                return emergencyNumber;
            }
        }

        Log.w(TAG, "Unable to get emergency number from TelephonyManager.");
        return getContext().getString(R.string.fallback_emergency_number);
    }

    private void startTimer() {
        CountDownAnimationView countDownAnimationView =
                getView().findViewById(R.id.count_down_view);

        if (mCountDownTimer != null) {
            countDownAnimationView.stop();
            mCountDownTimer.cancel();
        }

        mCountDownTimer =
                new CountDownTimer(
                        mCountDownMillisLeft,
                        getResources().getInteger(R.integer.emergency_action_count_down_interval)) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        CountDownAnimationView countDownAnimationView =
                                getView().findViewById(R.id.count_down_view);
                        if (countDownAnimationView != null) {
                            countDownAnimationView.setCountDownLeft(
                                    Duration.ofMillis(millisUntilFinished));
                        }

                        mCountDownMillisLeft = millisUntilFinished;
                    }

                    @Override
                    public void onFinish() {
                        startEmergencyCall();
                        getActivity().finish();
                    }
                };

        mCountDownTimer.start();

        countDownAnimationView.start(Duration.ofMillis(mCountDownMillisLeft));
        countDownAnimationView.showCountDown();
    }

    private void startEmergencyCall() {
        Bundle extras = new Bundle();
        extras.putBoolean(TelecomManager.EXTRA_IS_USER_INTENT_EMERGENCY_CALL, true);
        extras.putInt(EXTRA_CALL_SOURCE, TelecomManager.CALL_SOURCE_EMERGENCY_SHORTCUT);

        mTelecomManager.placeCall(
                Uri.fromParts(PhoneAccount.SCHEME_TEL, getEmergencyNumber(), null), extras);
    }
}
