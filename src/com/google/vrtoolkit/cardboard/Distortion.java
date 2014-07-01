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

/** Defines all required parameters to correct the distortion caused by the lenses. 
 */
public class Distortion
{
	private static final float[] DEFAULT_COEFFICIENTS = { 250.0F, 50000.0F };
	private float[] mCoefficients;

	public Distortion()
	{
		mCoefficients = new float[2];
		mCoefficients[0] = DEFAULT_COEFFICIENTS[0];
		mCoefficients[1] = DEFAULT_COEFFICIENTS[1];
	}

	public Distortion(Distortion other)
	{
		mCoefficients = new float[2];
		mCoefficients[0] = other.mCoefficients[0];
		mCoefficients[1] = other.mCoefficients[1];
	}

	public void setCoefficients(float[] coefficients)
	{
		mCoefficients[0] = coefficients[0];
		mCoefficients[1] = coefficients[1];
	}

	public float[] getCoefficients()
	{
		return mCoefficients;
	}

	public float distortionFactor(float radius)
	{
		float rSq = radius * radius;
		return 1.0F + mCoefficients[0] * rSq + mCoefficients[1] * rSq * rSq;
	}

	public float distort(float radius)
	{
		return radius * distortionFactor(radius);
	}

	public float distortInverse(float radius)
	{
		float r0 = radius / 0.9F;
		float r1 = radius * 0.9F;

		float dr0 = radius - distort(r0);

		while (Math.abs(r1 - r0) > 0.0001D) {
			float dr1 = radius - distort(r1);
			float r2 = r1 - dr1 * ((r1 - r0) / (dr1 - dr0));
			r0 = r1;
			r1 = r2;
			dr0 = dr1;
		}
		return r1;
	}

	public boolean equals(Object other)
	{
		if (other == null) {
			return false;
		}

		if (other == this) {
			return true;
		}

		if (!(other instanceof Distortion)) {
			return false;
		}

		Distortion o = (Distortion)other;
		return (mCoefficients[0] == o.mCoefficients[0]) && (mCoefficients[1] == o.mCoefficients[1]);
	}

	public String toString()
	{
		return "Distortion {" + mCoefficients[0] + ", " + mCoefficients[1] + "}";
	}
}