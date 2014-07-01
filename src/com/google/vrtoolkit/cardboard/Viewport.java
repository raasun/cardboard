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

import android.opengl.GLES20;

/** 
 * Defines a viewport rectangle. 
 */
public class Viewport
{
	public int x;
	public int y;
	public int width;
	public int height;

	public void setViewport(int x, int y, int width, int height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void setGLViewport()
	{
		GLES20.glViewport(x, y, width, height);
	}

	public void setGLScissor()
	{
		GLES20.glScissor(x, y, width, height);
	}

	public void getAsArray(int[] array, int offset)
	{
		if (offset + 4 > array.length) {
			throw new IllegalArgumentException("Not enough space to write the result");
		}

		array[offset] = x;
		array[(offset + 1)] = y;
		array[(offset + 2)] = width;
		array[(offset + 3)] = height;
	}

	public String toString()
	{
		return "Viewport {x:" + x + " y:" + y + " width:" + width + " height:" + height + "}";
	}
}