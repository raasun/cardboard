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

public class Matrix3x3d
{
	public double[] m = new double[9];

	public Matrix3x3d()
	{
	}

	public Matrix3x3d(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22)
	{
		m[0] = m00;
		m[1] = m01;
		m[2] = m02;
		m[3] = m10;
		m[4] = m11;
		m[5] = m12;
		m[6] = m20;
		m[7] = m21;
		m[8] = m22;
	}

	public Matrix3x3d(Matrix3x3d o)
	{
		m[0] = o.m[0];
		m[1] = o.m[1];
		m[2] = o.m[2];
		m[3] = o.m[3];
		m[4] = o.m[4];
		m[5] = o.m[5];
		m[6] = o.m[6];
		m[7] = o.m[7];
		m[8] = o.m[8];
	}

	public void set(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22)
	{
		m[0] = m00;
		m[1] = m01;
		m[2] = m02;
		m[3] = m10;
		m[4] = m11;
		m[5] = m12;
		m[6] = m20;
		m[7] = m21;
		m[8] = m22;
	}

	public void set(Matrix3x3d o)
	{
		m[0] = o.m[0];
		m[1] = o.m[1];
		m[2] = o.m[2];
		m[3] = o.m[3];
		m[4] = o.m[4];
		m[5] = o.m[5];
		m[6] = o.m[6];
		m[7] = o.m[7];
		m[8] = o.m[8];
	}

	public void setZero()
	{
		double tmp63_62 = (m[2] = m[3] = m[4] = m[5] = m[6] = m[7] = m[8] = 0.0D); m[1] = tmp63_62; m[0] = tmp63_62;
	}

	public void setIdentity()
	{
		double tmp41_40 = (m[3] = m[5] = m[6] = m[7] = 0.0D); m[2] = tmp41_40; m[1] = tmp41_40;
		double tmp63_62 = (m[8] = 1.0D); m[4] = tmp63_62; m[0] = tmp63_62;
	}

	public void setSameDiagonal(double d)
	{
		double tmp19_18 = (m[8] = d); m[4] = tmp19_18; m[0] = tmp19_18;
	}

	public double get(int row, int col)
	{
		return m[(3 * row + col)];
	}

	public void set(int row, int col, double value)
	{
		m[(3 * row + col)] = value;
	}

	public void getColumn(int col, Vector3d v)
	{
		v.x = m[col];
		v.y = m[(col + 3)];
		v.z = m[(col + 6)];
	}

	public void setColumn(int col, Vector3d v)
	{
		m[col] = v.x;
		m[(col + 3)] = v.y;
		m[(col + 6)] = v.z;
	}

	public void scale(double s)
	{
		m[0] *= s;
		m[1] *= s;
		m[2] *= s;
		m[3] *= s;
		m[4] *= s;
		m[5] *= s;
		m[6] *= s;
		m[7] *= s;
		m[8] *= s;
	}

	public void plusEquals(Matrix3x3d b)
	{
		m[0] += b.m[0];
		m[1] += b.m[1];
		m[2] += b.m[2];
		m[3] += b.m[3];
		m[4] += b.m[4];
		m[5] += b.m[5];
		m[6] += b.m[6];
		m[7] += b.m[7];
		m[8] += b.m[8];
	}

	public void minusEquals(Matrix3x3d b)
	{
		m[0] -= b.m[0];
		m[1] -= b.m[1];
		m[2] -= b.m[2];
		m[3] -= b.m[3];
		m[4] -= b.m[4];
		m[5] -= b.m[5];
		m[6] -= b.m[6];
		m[7] -= b.m[7];
		m[8] -= b.m[8];
	}

	public void transpose()
	{
		double tmp = m[1];
		m[1] = m[3];
		m[3] = tmp;

		tmp = m[2];
		m[2] = m[6];
		m[6] = tmp;

		tmp = m[5];
		m[5] = m[7];
		m[7] = tmp;
	}

	public void transpose(Matrix3x3d result)
	{
		double m1 = m[1];
		double m2 = m[2];
		double m5 = m[5];
		result.m[0] = m[0];
		result.m[1] = m[3];
		result.m[2] = m[6];
		result.m[3] = m1;
		result.m[4] = m[4];
		result.m[5] = m[7];
		result.m[6] = m2;
		result.m[7] = m5;
		result.m[8] = m[8];
	}

	public static void add(Matrix3x3d a, Matrix3x3d b, Matrix3x3d result)
	{
		result.m[0] = a.m[0] + b.m[0];
		result.m[1] = a.m[1] + b.m[1];
		result.m[2] = a.m[2] + b.m[2];
		result.m[3] = a.m[3] + b.m[3];
		result.m[4] = a.m[4] + b.m[4];
		result.m[5] = a.m[5] + b.m[5];
		result.m[6] = a.m[6] + b.m[6];
		result.m[7] = a.m[7] + b.m[7];
		result.m[8] = a.m[8] + b.m[8];
	}

	public static void mult(Matrix3x3d a, Matrix3x3d b, Matrix3x3d result)
	{
		result.set(a.m[0] * b.m[0] + a.m[1] * b.m[3] + a.m[2] * b.m[6], a.m[0] * b.m[1] + a.m[1] * b.m[4] + a.m[2] * b.m[7], a.m[0] * b.m[2] + a.m[1] * b.m[5] + a.m[2] * b.m[8], a.m[3] * b.m[0] + a.m[4] * b.m[3] + a.m[5] * b.m[6], a.m[3] * b.m[1] + a.m[4] * b.m[4] + a.m[5] * b.m[7], a.m[3] * b.m[2] + a.m[4] * b.m[5] + a.m[5] * b.m[8], a.m[6] * b.m[0] + a.m[7] * b.m[3] + a.m[8] * b.m[6], a.m[6] * b.m[1] + a.m[7] * b.m[4] + a.m[8] * b.m[7], a.m[6] * b.m[2] + a.m[7] * b.m[5] + a.m[8] * b.m[8]);
	}

	public static void mult(Matrix3x3d a, Vector3d v, Vector3d result)
	{
		double x = a.m[0] * v.x + a.m[1] * v.y + a.m[2] * v.z;
		double y = a.m[3] * v.x + a.m[4] * v.y + a.m[5] * v.z;
		double z = a.m[6] * v.x + a.m[7] * v.y + a.m[8] * v.z;
		result.x = x;
		result.y = y;
		result.z = z;
	}

	public double determinant()
	{
		return get(0, 0) * (get(1, 1) * get(2, 2) - get(2, 1) * get(1, 2)) - get(0, 1) * (get(1, 0) * get(2, 2) - get(1, 2) * get(2, 0)) + get(0, 2) * (get(1, 0) * get(2, 1) - get(1, 1) * get(2, 0));
	}

	public boolean invert(Matrix3x3d result)
	{
		double d = determinant();
		if (d == 0.0D) {
			return false;
		}

		double invdet = 1.0D / d;

		result.set((m[4] * m[8] - m[7] * m[5]) * invdet, -(m[1] * m[8] - m[2] * m[7]) * invdet, (m[1] * m[5] - m[2] * m[4]) * invdet, -(m[3] * m[8] - m[5] * m[6]) * invdet, (m[0] * m[8] - m[2] * m[6]) * invdet, -(m[0] * m[5] - m[3] * m[2]) * invdet, (m[3] * m[7] - m[6] * m[4]) * invdet, -(m[0] * m[7] - m[6] * m[1]) * invdet, (m[0] * m[4] - m[3] * m[1]) * invdet);

		return true;
	}
}