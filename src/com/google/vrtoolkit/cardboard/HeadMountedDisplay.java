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

import android.view.Display;

/**
 * Encapsulates the parameters describing a head mounted stereoscopic display device composed of a screen and a Cardboard-compatible device holding it. 
 */
public class HeadMountedDisplay
{
	private ScreenParams mScreen;
	private CardboardDeviceParams mCardboard;

	public HeadMountedDisplay(Display display)
	{
		mScreen = new ScreenParams(display);
		mCardboard = new CardboardDeviceParams();
	}

	public HeadMountedDisplay(HeadMountedDisplay hmd)
	{
		mScreen = new ScreenParams(hmd.mScreen);
		mCardboard = new CardboardDeviceParams(hmd.mCardboard);
	}

	public void setScreen(ScreenParams screen)
	{
		mScreen = new ScreenParams(screen);
	}

	public ScreenParams getScreen()
	{
		return mScreen;
	}

	public void setCardboard(CardboardDeviceParams cardboard)
	{
		mCardboard = new CardboardDeviceParams(cardboard);
	}

	public CardboardDeviceParams getCardboard()
	{
		return mCardboard;
	}

	public boolean equals(Object other)
	{
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof HeadMountedDisplay)) return false;
		HeadMountedDisplay o = (HeadMountedDisplay)other;

		return (mScreen.equals(o.mScreen)) && (mCardboard.equals(o.mCardboard));
	}
}