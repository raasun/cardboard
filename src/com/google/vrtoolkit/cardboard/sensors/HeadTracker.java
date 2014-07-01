/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.vrtoolkit.cardboard.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import com.google.vrtoolkit.cardboard.sensors.internal.OrientationEKF;

/**
 * Provides head tracking information from the device IMU. 
 */
public class HeadTracker
{
	private static final String TAG = "HeadTracker";
	private static final double NS2S = 1.E-09D;
	private static final int[] INPUT_SENSORS = { 1, 4 };
	private final Context mContext;
	private final float[] mEkfToHeadTracker = new float[16];

	private final float[] mTmpHeadView = new float[16];

	private final float[] mTmpRotatedEvent = new float[3];
	private Looper mSensorLooper;
	private SensorEventListener mSensorEventListener;
	private volatile boolean mTracking;
	private final OrientationEKF mTracker = new OrientationEKF();
	private long mLastGyroEventTimeNanos;

	public HeadTracker(Context context)
	{
		mContext = context;
		Matrix.setRotateEulerM(mEkfToHeadTracker, 0, -90.0F, 0.0F, 0.0F);
	}

	public void startTracking()
	{
		if (mTracking) {
			return;
		}
		mTracker.reset();

		mSensorEventListener = new SensorEventListener()
		{
			public void onSensorChanged(SensorEvent event) {
				HeadTracker.this.processSensorEvent(event);
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy)
			{
			}
		};
		Thread sensorThread = new Thread(new Runnable()
		{
			public void run() {
				Looper.prepare();

				mSensorLooper = Looper.myLooper();
				Handler handler = new Handler();

				SensorManager sensorManager = (SensorManager)mContext.getSystemService("sensor");

				for (int sensorType : HeadTracker.INPUT_SENSORS) {
					Sensor sensor = sensorManager.getDefaultSensor(sensorType);
					sensorManager.registerListener(mSensorEventListener, sensor, 0, handler);
				}

				Looper.loop();
			}
		});
		sensorThread.start();
		mTracking = true;
	}

	public void stopTracking()
	{
		if (!mTracking) {
			return;
		}

		SensorManager sensorManager = (SensorManager)mContext.getSystemService("sensor");

		sensorManager.unregisterListener(mSensorEventListener);
		mSensorEventListener = null;

		mSensorLooper.quit();
		mSensorLooper = null;
		mTracking = false;
	}

	public void getLastHeadView(float[] headView, int offset)
	{
		if (offset + 16 > headView.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		synchronized (mTracker) {
			double secondsSinceLastGyroEvent = (System.nanoTime() - mLastGyroEventTimeNanos) * 1.E-09D;

			double secondsToPredictForward = secondsSinceLastGyroEvent + 0.03333333333333333D;
			double[] mat = mTracker.getPredictedGLMatrix(secondsToPredictForward);
			for (int i = 0; i < headView.length; i++) {
				mTmpHeadView[i] = ((float)mat[i]);
			}
		}

		Matrix.multiplyMM(headView, offset, mTmpHeadView, 0, mEkfToHeadTracker, 0);
	}

	private void processSensorEvent(SensorEvent event)
	{
		long timeNanos = System.nanoTime();

		mTmpRotatedEvent[0] = (-event.values[1]);
		mTmpRotatedEvent[1] = event.values[0];
		mTmpRotatedEvent[2] = event.values[2];
		synchronized (mTracker) {
			if (event.sensor.getType() == 1) {
				mTracker.processAcc(mTmpRotatedEvent, event.timestamp);
			} else if (event.sensor.getType() == 4) {
				mLastGyroEventTimeNanos = timeNanos;
				mTracker.processGyro(mTmpRotatedEvent, event.timestamp);
			}
		}
	}
}