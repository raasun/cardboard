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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.util.Log;
import com.google.vrtoolkit.cardboard.CardboardDeviceParams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** 
 * Class providing Cardboard NFC tag detection events. 
 */
public class NfcSensor
{
	public static final String NFC_DATA_SCHEME = "cardboard";
	public static final String FIRST_TAG_VERSION = "v1.0.0";
	private static final String TAG = "NfcSensor";
	private static final int MAX_CONNECTION_FAILURES = 1;
	private static final long NFC_POLLING_INTERVAL_MS = 250L;
	private static NfcSensor sInstance;
	private final Context mContext;
	private final NfcAdapter mNfcAdapter;
	private final Object mTagLock;
	private final List<ListenerHelper> mListeners;
	private IntentFilter[] mNfcIntentFilters;
	private volatile Ndef mCurrentTag;
	private Timer mNfcDisconnectTimer;
	private int mTagConnectionFailures;

	public static NfcSensor getInstance(Context context)
	{
		if (sInstance == null) {
			sInstance = new NfcSensor(context);
		}

		return sInstance;
	}

	private NfcSensor(Context context) {
		mContext = context.getApplicationContext();
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
		mListeners = new ArrayList();
		mTagLock = new Object();

		if (mNfcAdapter == null) {
			return;
		}

		IntentFilter ndefIntentFilter = new IntentFilter("android.nfc.action.NDEF_DISCOVERED");
		ndefIntentFilter.addDataScheme("cardboard");
		mNfcIntentFilters = new IntentFilter[] { ndefIntentFilter };

		mContext.registerReceiver(new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent) {
				onNfcIntent(intent);
			}
		}
		, ndefIntentFilter);
	}

	public void addOnCardboardNfcListener(OnCardboardNfcListener listener)
	{
		if (listener == null) {
			return;
		}

		synchronized (mListeners) {
			for (ListenerHelper helper : mListeners) {
				if (helper.getListener() == listener) {
					return;
				}
			}

			mListeners.add(new ListenerHelper(listener, new Handler()));
		}
	}

	public void removeOnCardboardNfcListener(OnCardboardNfcListener listener)
	{
		if (listener == null) {
			return;
		}

		synchronized (mListeners) {
			for (ListenerHelper helper : mListeners)
				if (helper.getListener() == listener) {
					mListeners.remove(helper);
					return;
				}
		}
	}

	public boolean isNfcSupported()
	{
		return mNfcAdapter != null;
	}

	public boolean isNfcEnabled()
	{
		return (isNfcSupported()) && (mNfcAdapter.isEnabled());
	}

	public boolean isDeviceInCardboard()
	{
		return mCurrentTag != null;
	}

	public CardboardDeviceParams getCardboardDeviceParams()
	{
		NdefMessage tagContents = null;
		synchronized (mTagLock) {
			try {
				tagContents = mCurrentTag.getCachedNdefMessage();
			} catch (Exception e) {
				return null;
			}
		}

		if (tagContents == null) {
			return null;
		}

		return CardboardDeviceParams.createFromNfcContents(tagContents);
	}

	public void onResume(Activity activity)
	{
		if (!isNfcEnabled()) {
			return;
		}

		Intent intent = new Intent("android.nfc.action.NDEF_DISCOVERED");
		intent.setPackage(activity.getPackageName());

		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
		mNfcAdapter.enableForegroundDispatch(activity, pendingIntent, mNfcIntentFilters, (String[][])null);
	}

	public void onPause(Activity activity)
	{
		if (!isNfcEnabled()) {
			return;
		}

		mNfcAdapter.disableForegroundDispatch(activity);
	}

	public void onNfcIntent(Intent intent)
	{
		if ((!isNfcEnabled()) || (intent == null) || (!"android.nfc.action.NDEF_DISCOVERED".equals(intent.getAction())))
		{
			return;
		}

		Uri uri = intent.getData();
		Tag nfcTag = (Tag)intent.getParcelableExtra("android.nfc.extra.TAG");
		if ((uri == null) || (nfcTag == null)) {
			return;
		}

		Ndef ndef = Ndef.get(nfcTag);
		if ((ndef == null) || (!uri.getScheme().equals("cardboard")) || ((!uri.getHost().equals("v1.0.0")) && (uri.getPathSegments().size() == 2)))
		{
			return;
		}

		synchronized (mTagLock) {
			boolean isSameTag = false;

			if (mCurrentTag != null)
			{
				byte[] tagId1 = nfcTag.getId();
				byte[] tagId2 = mCurrentTag.getTag().getId();
				isSameTag = (tagId1 != null) && (tagId2 != null) && (Arrays.equals(tagId1, tagId2));

				closeCurrentNfcTag();
				if (!isSameTag) {
					sendDisconnectionEvent();
				}
			}

			NdefMessage nfcTagContents;
			try
			{
				ndef.connect();
				nfcTagContents = ndef.getCachedNdefMessage();
			} catch (Exception e) {
				Log.e("NfcSensor", "Error reading NFC tag: " + e.toString());

				if (isSameTag) {
					sendDisconnectionEvent();
				}

				return;
			}

			mCurrentTag = ndef;

			if (!isSameTag) {
				synchronized (mListeners) {
					for (ListenerHelper listener : mListeners) {
						listener.onInsertedIntoCardboard(CardboardDeviceParams.createFromNfcContents(nfcTagContents));
					}

				}

			}

			mTagConnectionFailures = 0;
			mNfcDisconnectTimer = new Timer("NFC disconnect timer");
			mNfcDisconnectTimer.schedule(new TimerTask()
			{
				public void run() {
					synchronized (mTagLock) {
						if (!mCurrentTag.isConnected()) {
							// NfcSensor.access$204(NfcSensor.this);

							if (mTagConnectionFailures > 1) {
								NfcSensor.this.closeCurrentNfcTag();
								NfcSensor.this.sendDisconnectionEvent();
							}
						}
					}
				}
			}
			, 250L, 250L);
		}
	}

	private void closeCurrentNfcTag()
	{
		if (mNfcDisconnectTimer != null) {
			mNfcDisconnectTimer.cancel();
		}

		try
		{
			mCurrentTag.close();
		} catch (IOException e) {
			Log.w("NfcSensor", e.toString());
		}

		mCurrentTag = null;
	}

	private void sendDisconnectionEvent() {
		synchronized (mListeners) {
			for (ListenerHelper listener : mListeners)
				listener.onRemovedFromCardboard();
		}
	}

	private static class ListenerHelper
	implements NfcSensor.OnCardboardNfcListener
	{
		private NfcSensor.OnCardboardNfcListener mListener;
		private Handler mHandler;

		public ListenerHelper(NfcSensor.OnCardboardNfcListener listener, Handler handler)
		{
			mListener = listener;
			mHandler = handler;
		}

		public NfcSensor.OnCardboardNfcListener getListener() {
			return mListener;
		}

		public void onInsertedIntoCardboard(final CardboardDeviceParams deviceParams)
		{
			mHandler.post(new Runnable()
			{
				public void run() {
					mListener.onInsertedIntoCardboard(deviceParams);
				}
			});
		}

		public void onRemovedFromCardboard()
		{
			mHandler.post(new Runnable()
			{
				public void run() {
					mListener.onRemovedFromCardboard();
				}
			});
		}
	}

	/** Interface for listeners of Cardboard NFC events. */
	public static abstract interface OnCardboardNfcListener
	{
		public abstract void onInsertedIntoCardboard(CardboardDeviceParams paramCardboardDeviceParams);

		public abstract void onRemovedFromCardboard();
	}
}