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

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.emergency.R;
import com.android.emergency.widgets.countdown.CountDownAnimationView;

import java.time.Duration;

public class EmergencyActionFragment extends Fragment {

    private static final String STATE_MILLIS_LEFT = "STATE_MILLIS_LEFT";

    private CountDownTimer mCountDownTimer;
    private long mCountDownMillisLeft;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emergency_action_fragment, container, false);

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
                        getActivity().finish();
                        // TODO(b/169946688): Call emergency service on count down finish.
                    }
                };

        mCountDownTimer.start();

        countDownAnimationView.start(Duration.ofMillis(mCountDownMillisLeft));
        countDownAnimationView.showCountDown();
    }
}
