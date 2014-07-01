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
package com.google.vrtoolkit.cardboard.sensors.internal;

public class Vector3d
{
	public double x;
	public double y;
	public double z;

	public Vector3d()
	{
	}

	public Vector3d(double xx, double yy, double zz)
	{
		set(xx, yy, zz);
	}

	public void set(double xx, double yy, double zz)
	{
		x = xx;
		y = yy;
		z = zz;
	}

	public void setComponent(int i, double val)
	{
		if (i == 0)
			x = val;
		else if (i == 1)
			y = val;
		else
			z = val;
	}

	public void setZero()
	{
		x = (this.y = this.z = 0.0D);
	}

	public void set(Vector3d other)
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public void scale(double s)
	{
		x *= s;
		y *= s;
		z *= s;
	}

	public void normalize()
	{
		double d = length();
		if (d != 0.0D)
			scale(1.0D / d);
	}

	public static double dot(Vector3d a, Vector3d b)
	{
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public double length()
	{
		return Math.sqrt(x * x + y * y + z * z);
	}

	public boolean sameValues(Vector3d other)
	{
		return (x == other.x) && (y == other.y) && (z == other.z);
	}

	public static void sub(Vector3d a, Vector3d b, Vector3d result)
	{
		result.set(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static void cross(Vector3d a, Vector3d b, Vector3d result)
	{
		result.set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}

	public static void ortho(Vector3d v, Vector3d result)
	{
		int k = largestAbsComponent(v) - 1;
		if (k < 0) {
			k = 2;
		}
		result.setZero();
		result.setComponent(k, 1.0D);

		cross(v, result, result);
		result.normalize();
	}

	public static int largestAbsComponent(Vector3d v)
	{
		double xAbs = Math.abs(v.x);
		double yAbs = Math.abs(v.y);
		double zAbs = Math.abs(v.z);

		if (xAbs > yAbs) {
			if (xAbs > zAbs) {
				return 0;
			}
			return 2;
		}

		if (yAbs > zAbs) {
			return 1;
		}
		return 2;
	}
}