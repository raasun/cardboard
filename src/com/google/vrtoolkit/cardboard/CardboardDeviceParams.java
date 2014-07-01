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
package com.google.vrtoolkit.cardboard;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.util.Log;
import java.util.List;

/** 
 * Defines the physical parameters of a Cardboard-compatible device.
 * 
 * The model assumes the following for any Cardboard device:
 * 
 * 1. Lenses are parallel to the screen of the inserted device.
 * 2. The center of each lens is at the same height from the bottom of the inserted device.
 * 3. Lenses are symmetrically placed with respect to the center of the inserted device screen.
 * 4. The size of the area visible from the center of each lens is the same.
 * 
 * These parameters might be different for variations of different devices or lenses. For simplicity, they can be stored in the NFC tag of the Cardboard.
 */
public class CardboardDeviceParams
{
	private static final String TAG = "CardboardDeviceParams";
	private static final String DEFAULT_VENDOR = "com.google";
	private static final String DEFAULT_MODEL = "cardboard";
	private static final String DEFAULT_VERSION = "1.0";
	private static final float DEFAULT_INTERPUPILLARY_DISTANCE = 0.06F;
	private static final float DEFAULT_VERTICAL_DISTANCE_TO_LENS_CENTER = 0.035F;
	private static final float DEFAULT_LENS_DIAMETER = 0.025F;
	private static final float DEFAULT_SCREEN_TO_LENS_DISTANCE = 0.037F;
	private static final float DEFAULT_EYE_TO_LENS_DISTANCE = 0.011F;
	private static final float DEFAULT_VISIBLE_VIEWPORT_MAX_SIZE = 0.06F;
	private static final float DEFAULT_FOV_Y = 65.0F;
	private NdefMessage mNfcTagContents;
	private String mVendor;
	private String mModel;
	private String mVersion;
	private float mInterpupillaryDistance;
	private float mVerticalDistanceToLensCenter;
	private float mLensDiameter;
	private float mScreenToLensDistance;
	private float mEyeToLensDistance;
	private float mVisibleViewportSize;
	private float mFovY;
	private Distortion mDistortion;

	public CardboardDeviceParams()
	{
		mVendor = "com.google";
		mModel = "cardboard";
		mVersion = "1.0";

		mInterpupillaryDistance = 0.06F;
		mVerticalDistanceToLensCenter = 0.035F;
		mLensDiameter = 0.025F;
		mScreenToLensDistance = 0.037F;
		mEyeToLensDistance = 0.011F;

		mVisibleViewportSize = 0.06F;
		mFovY = 65.0F;

		mDistortion = new Distortion();
	}

	public CardboardDeviceParams(CardboardDeviceParams params)
	{
		mNfcTagContents = params.mNfcTagContents;

		mVendor = params.mVendor;
		mModel = params.mModel;
		mVersion = params.mVersion;

		mInterpupillaryDistance = params.mInterpupillaryDistance;
		mVerticalDistanceToLensCenter = params.mVerticalDistanceToLensCenter;
		mLensDiameter = params.mLensDiameter;
		mScreenToLensDistance = params.mScreenToLensDistance;
		mEyeToLensDistance = params.mEyeToLensDistance;

		mVisibleViewportSize = params.mVisibleViewportSize;
		mFovY = params.mFovY;

		mDistortion = new Distortion(params.mDistortion);
	}

	public static CardboardDeviceParams createFromNfcContents(NdefMessage tagContents)
	{
		if (tagContents == null) {
			Log.w("CardboardDeviceParams", "Could not get contents from NFC tag.");
			return null;
		}

		CardboardDeviceParams deviceParams = new CardboardDeviceParams();

		for (NdefRecord record : tagContents.getRecords()) {
			if (deviceParams.parseNfcUri(record))
			{
				break;
			}

		}

		return deviceParams;
	}

	public NdefMessage getNfcTagContents()
	{
		return mNfcTagContents;
	}

	public void setVendor(String vendor)
	{
		mVendor = vendor;
	}

	public String getVendor()
	{
		return mVendor;
	}

	public void setModel(String model)
	{
		mModel = model;
	}

	public String getModel()
	{
		return mModel;
	}

	public void setVersion(String version)
	{
		mVersion = version;
	}

	public String getVersion()
	{
		return mVersion;
	}

	public void setInterpupillaryDistance(float interpupillaryDistance)
	{
		mInterpupillaryDistance = interpupillaryDistance;
	}

	public float getInterpupillaryDistance()
	{
		return mInterpupillaryDistance;
	}

	public void setVerticalDistanceToLensCenter(float verticalDistanceToLensCenter)
	{
		mVerticalDistanceToLensCenter = verticalDistanceToLensCenter;
	}

	public float getVerticalDistanceToLensCenter()
	{
		return mVerticalDistanceToLensCenter;
	}

	public void setVisibleViewportSize(float visibleViewportSize)
	{
		mVisibleViewportSize = visibleViewportSize;
	}

	public float getVisibleViewportSize()
	{
		return mVisibleViewportSize;
	}

	public void setFovY(float fovY)
	{
		mFovY = fovY;
	}

	public float getFovY()
	{
		return mFovY;
	}

	public void setLensDiameter(float lensDiameter)
	{
		mLensDiameter = lensDiameter;
	}

	public float getLensDiameter()
	{
		return mLensDiameter;
	}

	public void setScreenToLensDistance(float screenToLensDistance)
	{
		mScreenToLensDistance = screenToLensDistance;
	}

	public float getScreenToLensDistance()
	{
		return mScreenToLensDistance;
	}

	public void setEyeToLensDistance(float eyeToLensDistance)
	{
		mEyeToLensDistance = eyeToLensDistance;
	}

	public float getEyeToLensDistance()
	{
		return mEyeToLensDistance;
	}

	public Distortion getDistortion()
	{
		return mDistortion;
	}

	public boolean equals(Object other)
	{
		if (other == null) {
			return false;
		}

		if (other == this) {
			return true;
		}

		if (!(other instanceof CardboardDeviceParams)) {
			return false;
		}

		CardboardDeviceParams o = (CardboardDeviceParams)other;

		return (mVendor == o.mVendor) && (mModel == o.mModel) && (mVersion == o.mVersion) && (mInterpupillaryDistance == o.mInterpupillaryDistance) && (mVerticalDistanceToLensCenter == o.mVerticalDistanceToLensCenter) && (mLensDiameter == o.mLensDiameter) && (mScreenToLensDistance == o.mScreenToLensDistance) && (mEyeToLensDistance == o.mEyeToLensDistance) && (mVisibleViewportSize == o.mVisibleViewportSize) && (mFovY == o.mFovY) && (mDistortion.equals(o.mDistortion));
	}

	private boolean parseNfcUri(NdefRecord record)
	{
		Uri uri = record.toUri();
		if (uri == null) {
			return false;
		}

		if (uri.getHost().equals("v1.0.0")) {
			mVendor = "com.google";
			mModel = "cardboard";
			mVersion = "1.0";
			return true;
		}

		List segments = uri.getPathSegments();
		if (segments.size() != 2) {
			return false;
		}

		mVendor = uri.getHost();
		mModel = ((String)segments.get(0));
		mVersion = ((String)segments.get(1));

		return true;
	}
}