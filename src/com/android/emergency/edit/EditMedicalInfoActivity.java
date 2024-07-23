/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.emergency.edit;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.internal.annotations.VisibleForTesting;

/** Activity for editing medical information. */
public class EditMedicalInfoActivity extends Activity {
    private EditMedicalInfoFragment mEditInfoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(this.findViewById(android.R.id.content),
        (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });

        // We only add a new EditInfoFragment if no fragment is restored.
        Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
        if (fragment == null) {
            mEditInfoFragment = new EditMedicalInfoFragment();
            getFragmentManager().beginTransaction()
                .add(android.R.id.content, mEditInfoFragment)
                .commit();
        } else {
            mEditInfoFragment = (EditMedicalInfoFragment) fragment;
        }
    }

    @VisibleForTesting
    public EditMedicalInfoFragment getFragment() {
        return mEditInfoFragment;
    }
}
