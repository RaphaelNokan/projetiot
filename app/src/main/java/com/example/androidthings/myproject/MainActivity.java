/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.androidthings.myproject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;


import java.io.IOException;

/**
 * Skeleton of the main Android Things activity. Implement your device's logic
 * in this class.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManager. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManager manager = PeripheralManager.getInstance();
 * mLedGpio = manager.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    //private static final String TAG = "ButtonActivity";
    private static final String BUTTON_PIN_NAME = "BCM20"; // GPIO port wired to the button
    private static int INTERVAL_BETWEEN_BLINKS_MS = 1000;
    private static final String LED_PIN_NAME = "BCM6"; // GPIO port wired to the LED

    private boolean stopled = false;

    private Gpio mButtonGpio;
    private Handler mHandler = new Handler();
    private Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Button b1 = (Button) findViewById(R.id.button);
        Button b2 = (Button) findViewById(R.id.button2);
        Button b3 = (Button) findViewById(R.id.button3);
        Button b4 = (Button) findViewById(R.id.button4);


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                INTERVAL_BETWEEN_BLINKS_MS = 500;

            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                INTERVAL_BETWEEN_BLINKS_MS = 1000;

            }
        });


        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopled = true;

            }
        });


        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopled = false;
                mHandler.post(mBlinkRunnable);

            }
        });


        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            // Step 1. Create GPIO connection.
            mButtonGpio = manager.openGpio(BUTTON_PIN_NAME);
            // Step 2. Configure as an input.
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            // Step 3. Enable edge trigger events.
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            // Step 4. Register an event callback.
            mButtonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }


        try {
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            // Step 4. Repeat using a handler.
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }


    // Step 4. Register an event callback.
    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "GPIO changed, button pressed");

            // Step 5. Return true to keep callback active.
            return true;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Step 6. Close the resource
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }


        // Step 4. Remove handler events on close.
        mHandler.removeCallbacks(mBlinkRunnable);

        // Step 5. Close the resource.
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }


    }


    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit if the GPIO is already closed
            if (mLedGpio == null) {
                return;
            }

            try {

                if (stopled== false) {
                    // Step 3. Toggle the LED state
                    mLedGpio.setValue(!mLedGpio.getValue());

                    // Step 4. Schedule another event after delay.
                    mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);

                } else
                    mLedGpio.setValue(true);


            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

}

