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

import android.opengl.Matrix;

/** Describes the transformations to apply in an eye view. */
public class EyeTransform
{
	private final EyeParams mEyeParams;
	private final float[] mEyeView;
	private final float[] mPerspective;

	public EyeTransform(EyeParams params)
	{
		mEyeParams = params;
		mEyeView = new float[16];
		mPerspective = new float[16];

		Matrix.setIdentityM(mEyeView, 0);
		Matrix.setIdentityM(mPerspective, 0);
	}

	public float[] getEyeView()
	{
		return mEyeView;
	}

	public float[] getPerspective()
	{
		return mPerspective;
	}

	public EyeParams getParams()
	{
		return mEyeParams;
	}
}