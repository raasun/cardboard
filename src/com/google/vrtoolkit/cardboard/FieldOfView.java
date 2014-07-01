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

/**
 * Encapsulates a field of view composed of 4 half angles (left, right, bottom, top) as would be passed to glFrustum. 
 */
public class FieldOfView
{
	private float mLeft;
	private float mRight;
	private float mBottom;
	private float mTop;

	public FieldOfView()
	{
	}

	public FieldOfView(float left, float right, float bottom, float top)
	{
		mLeft = left;
		mRight = right;
		mBottom = bottom;
		mTop = top;
	}

	public FieldOfView(FieldOfView other)
	{
		mLeft = other.mLeft;
		mRight = other.mRight;
		mBottom = other.mBottom;
		mTop = other.mTop;
	}

	public void setLeft(float left)
	{
		mLeft = left;
	}

	public float getLeft()
	{
		return mLeft;
	}

	public void setRight(float right)
	{
		mRight = right;
	}

	public float getRight()
	{
		return mRight;
	}

	public void setBottom(float bottom)
	{
		mBottom = bottom;
	}

	public float getBottom()
	{
		return mBottom;
	}

	public void setTop(float top)
	{
		mTop = top;
	}

	public float getTop()
	{
		return mTop;
	}

	public void toPerspectiveMatrix(float near, float far, float[] perspective, int offset)
	{
		if (offset + 16 > perspective.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		float l = (float)-Math.tan(Math.toRadians(mLeft)) * near;
		float r = (float)Math.tan(Math.toRadians(mRight)) * near;
		float b = (float)-Math.tan(Math.toRadians(mBottom)) * near;
		float t = (float)Math.tan(Math.toRadians(mTop)) * near;
		Matrix.frustumM(perspective, offset, l, r, b, t, near, far);
	}

	public boolean equals(Object other)
	{
		if (other == null) {
			return false;
		}

		if (other == this) {
			return true;
		}

		if (!(other instanceof FieldOfView)) {
			return false;
		}

		FieldOfView o = (FieldOfView)other;
		return (mLeft == o.mLeft) && (mRight == o.mRight) && (mBottom == o.mBottom) && (mTop == o.mTop);
	}

	public String toString()
	{
		return "FieldOfView {left:" + mLeft + " right:" + mRight + " bottom:" + mBottom + " top:" + mTop + "}";
	}
}