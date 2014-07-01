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
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import java.util.ArrayList;

/**
 * Magnetometer sensor detector for the cardboard trigger. 
 */
public class MagnetSensor
{
	private TriggerDetector mDetector;
	private Thread mDetectorThread;

	public MagnetSensor(Context context)
	{
		mDetector = new TriggerDetector(context);
	}

	public void start()
	{
		mDetectorThread = new Thread(mDetector);
		mDetectorThread.start();
	}

	public void stop()
	{
		if (mDetectorThread != null) {
			mDetectorThread.interrupt();
			mDetector.stop();
		}
	}

	public void setOnCardboardTriggerListener(OnCardboardTriggerListener listener)
	{
		mDetector.setOnCardboardTriggerListener(listener, new Handler());
	}

	public void fakeTrigger() 
	{
		mDetector.handleButtonPressed();  
	}

	private static class TriggerDetector
	implements Runnable, SensorEventListener
	{
		private static final String TAG = "TriggerDetector";
		private static final int SEGMENT_SIZE = 20;
		private static final int NUM_SEGMENTS = 2;
		private static final int WINDOW_SIZE = 40;
		private static final int T1 = 30;
		private static final int T2 = 130;
		private SensorManager mSensorManager;
		private Sensor mMagnetometer;
		private ArrayList<float[]> mSensorData;
		private float[] mOffsets = new float[20];
		private MagnetSensor.OnCardboardTriggerListener mListener;
		private Handler mHandler;

		public TriggerDetector(Context context)
		{
			mSensorData = new ArrayList();
			mSensorManager = ((SensorManager)context.getSystemService("sensor"));
			mMagnetometer = mSensorManager.getDefaultSensor(2);
		}

		public synchronized void setOnCardboardTriggerListener(MagnetSensor.OnCardboardTriggerListener listener, Handler handler)
		{
			mListener = listener;
			mHandler = handler;
		}

		private void addData(float[] values, long time) {
			if (mSensorData.size() > 40) {
				mSensorData.remove(0);
			}
			mSensorData.add(values);

			evaluateModel();
		}

		private void evaluateModel()
		{
			if (mSensorData.size() < 40) {
				return;
			}

			float[] means = new float[2];
			float[] maximums = new float[2];
			float[] minimums = new float[2];

			float[] baseline = (float[])mSensorData.get(mSensorData.size() - 1);

			for (int i = 0; i < 2; i++) {
				int segmentStart = 20 * i;

				float[] mOffsets = computeOffsets(segmentStart, baseline);

				means[i] = computeMean(mOffsets);
				maximums[i] = computeMaximum(mOffsets);
				minimums[i] = computeMinimum(mOffsets);
			}

			float min1 = minimums[0];
			float max2 = maximums[1];

			if ((min1 < 30.0F) && (max2 > 130.0F))
				handleButtonPressed();
		}

		/** turned public to fake this event **/
		public void handleButtonPressed()
		{
			mSensorData.clear();

			synchronized (this) {
				if (mListener != null)
					mHandler.post(new Runnable()
					{
						public void run() {
							mListener.onCardboardTrigger();
						}
					});
			}
		}

		private float[] computeOffsets(int start, float[] baseline)
		{
			for (int i = 0; i < 20; i++) {
				float[] point = (float[])mSensorData.get(start + i);
				float[] o = { point[0] - baseline[0], point[1] - baseline[1], point[2] - baseline[2] };
				float magnitude = (float)Math.sqrt(o[0] * o[0] + o[1] * o[1] + o[2] * o[2]);
				mOffsets[i] = magnitude;
			}
			return mOffsets;
		}

		private float computeMean(float[] offsets) {
			float sum = 0.0F;
			for (float o : offsets) {
				sum += o;
			}
			return sum / offsets.length;
		}

		private float computeMaximum(float[] offsets) {
			float max = (1.0F / -1.0F);
			for (float o : offsets) {
				max = Math.max(o, max);
			}
			return max;
		}

		private float computeMinimum(float[] offsets) {
			float min = (1.0F / 1.0F);
			for (float o : offsets) {
				min = Math.min(o, min);
			}
			return min;
		}

		public void run()
		{
			Process.setThreadPriority(-19);
			Looper.prepare();
			mSensorManager.registerListener(this, mMagnetometer, 0);
			Looper.loop();
		}

		public void stop() {
			mSensorManager.unregisterListener(this);
		}

		public void onSensorChanged(SensorEvent event)
		{
			if (event.sensor.equals(mMagnetometer)) {
				float[] values = event.values;

				if ((values[0] == 0.0F) && (values[1] == 0.0F) && (values[2] == 0.0F)) {
					return;
				}
				addData((float[])event.values.clone(), event.timestamp);
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}
	}

	/** Interface for listeners of Cardboard trigger events. */
	public static abstract interface OnCardboardTriggerListener
	{
		public abstract void onCardboardTrigger();
	}
}