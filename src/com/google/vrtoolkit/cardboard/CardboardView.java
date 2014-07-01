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

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Convenience extension of GLSurfaceView that can be used for VR rendering.
 * 
 * Designed to work in full screen mode with a landscape or reverse landscape orientation.
 * 
 * This view can be used as a normal GLSurfaceView by implementing one of its rendering interfaces:
 * 
 *   CardboardView.StereoRenderer: abstracts all stereoscopic rendering details from the renderer.
 *   CardboardView.Renderer: for complex engines that need to handle all stereo rendering details by themselves.
 * 
 * The CardboardView.StereoRenderer interface is recommended for all applications that can make use of it, while the CardboardView.Renderer interface is discouraged and should only be used if really needed.
 * 
 * The view allows switching from VR mode to normal rendering mode in stereo renderers at any time by calling the setVRModeEnabled method.
 */
public class CardboardView extends GLSurfaceView
{
	private static final String TAG = "CardboardView";
	private static final float DEFAULT_Z_NEAR = 0.1F;
	private static final float DEFAULT_Z_FAR = 100.0F;
	private RendererHelper mRendererHelper;
	private HeadTracker mHeadTracker;
	private HeadMountedDisplay mHmd;
	private DistortionRenderer mDistortionRenderer;
	private CardboardDeviceParamsObserver mCardboardDeviceParamsObserver;
	private boolean mVRMode = true;
	private volatile boolean mDistortionCorrectionEnabled = true;
	private volatile float mDistortionCorrectionScale = 1.0F;
	private float mZNear = 0.1F;
	private float mZFar = 100.0F;

	public CardboardView(Context context)
	{
		super(context);
		init(context);
	}

	public CardboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setRenderer(Renderer renderer)
	{
		mRendererHelper = (renderer != null ? new RendererHelper(renderer) : null);
		super.setRenderer(mRendererHelper);
	}

	public void setRenderer(StereoRenderer renderer)
	{
		setRenderer(renderer != null ? new StereoRendererHelper(renderer) : (Renderer)null);
	}

	public void setVRModeEnabled(boolean enabled)
	{
		mVRMode = enabled;

		if (mRendererHelper != null)
			mRendererHelper.setVRModeEnabled(enabled);
	}

	public boolean getVRMode()
	{
		return mVRMode;
	}

	public HeadMountedDisplay getHeadMountedDisplay()
	{
		return mHmd;
	}

	public void updateCardboardDeviceParams(CardboardDeviceParams cardboardDeviceParams)
	{
		if ((cardboardDeviceParams == null) || (cardboardDeviceParams.equals(mHmd.getCardboard()))) {
			return;
		}

		if (mCardboardDeviceParamsObserver != null) {
			mCardboardDeviceParamsObserver.onCardboardDeviceParamsUpdate(cardboardDeviceParams);
		}

		mHmd.setCardboard(cardboardDeviceParams);

		if (mRendererHelper != null)
			mRendererHelper.setCardboardDeviceParams(cardboardDeviceParams);
	}

	public void setCardboardDeviceParamsObserver(CardboardDeviceParamsObserver observer)
	{
		mCardboardDeviceParamsObserver = observer;
	}

	public CardboardDeviceParams getCardboardDeviceParams()
	{
		return mHmd.getCardboard();
	}

	public void updateScreenParams(ScreenParams screenParams)
	{
		if ((screenParams == null) || (screenParams.equals(mHmd.getScreen()))) {
			return;
		}

		mHmd.setScreen(screenParams);

		if (mRendererHelper != null)
			mRendererHelper.setScreenParams(screenParams);
	}

	public ScreenParams getScreenParams()
	{
		return mHmd.getScreen();
	}

	public void setInterpupillaryDistance(float distance)
	{
		mHmd.getCardboard().setInterpupillaryDistance(distance);

		if (mRendererHelper != null)
			mRendererHelper.setInterpupillaryDistance(distance);
	}

	public float getInterpupillaryDistance()
	{
		return mHmd.getCardboard().getInterpupillaryDistance();
	}

	public void setFovY(float fovY)
	{
		mHmd.getCardboard().setFovY(fovY);

		if (mRendererHelper != null)
			mRendererHelper.setFOV(fovY);
	}

	public float getFovY()
	{
		return mHmd.getCardboard().getFovY();
	}

	public void setZPlanes(float zNear, float zFar)
	{
		mZNear = zNear;
		mZFar = zFar;

		if (mRendererHelper != null)
			mRendererHelper.setZPlanes(zNear, zFar);
	}

	public float getZNear()
	{
		return mZNear;
	}

	public float getZFar()
	{
		return mZFar;
	}

	public void setDistortionCorrectionEnabled(boolean enabled)
	{
		mDistortionCorrectionEnabled = enabled;

		if (mRendererHelper != null)
			mRendererHelper.setDistortionCorrectionEnabled(enabled);
	}

	public boolean getDistortionCorrectionEnabled()
	{
		return mDistortionCorrectionEnabled;
	}

	public void setDistortionCorrectionScale(float scale)
	{
		mDistortionCorrectionScale = scale;

		if (mRendererHelper != null)
			mRendererHelper.setDistortionCorrectionScale(scale);
	}

	public float getDistortionCorrectionScale()
	{
		return mDistortionCorrectionScale;
	}

	public void onResume()
	{
		if (mRendererHelper == null) {
			return;
		}

		super.onResume();
		mHeadTracker.startTracking();
	}

	public void onPause()
	{
		if (mRendererHelper == null) {
			return;
		}

		super.onPause();
		mHeadTracker.stopTracking();
	}

	public void setRenderer(GLSurfaceView.Renderer renderer)
	{
		throw new RuntimeException("Please use the CardboardView renderer interfaces");
	}

	public void onDetachedFromWindow()
	{
		if (mRendererHelper != null) {
			synchronized (mRendererHelper) {
				mRendererHelper.shutdown();
				try {
					mRendererHelper.wait();
				} catch (InterruptedException e) {
					Log.e("CardboardView", "Interrupted during shutdown: " + e.toString());
				}
			}
		}

		super.onDetachedFromWindow();
	}

	private void init(Context context)
	{
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);

		WindowManager windowManager = (WindowManager)context.getSystemService("window");

		mHeadTracker = new HeadTracker(context);
		mHmd = new HeadMountedDisplay(windowManager.getDefaultDisplay());
	}

	private class StereoRendererHelper
	implements CardboardView.Renderer
	{
		private final CardboardView.StereoRenderer mStereoRenderer;
		private boolean mVRMode;

		public StereoRendererHelper(CardboardView.StereoRenderer stereoRenderer)
		{
			mStereoRenderer = stereoRenderer;
			mVRMode = CardboardView.this.mVRMode;
		}

		public void setVRModeEnabled(final boolean enabled) {
			queueEvent(new Runnable()
			{
				public void run() {
					mVRMode = enabled;
				}
			});
		}

		public void onDrawFrame(HeadTransform head, EyeParams leftEye, EyeParams rightEye)
		{
			mStereoRenderer.onNewFrame(head);
			GLES20.glEnable(3089);

			leftEye.getViewport().setGLViewport();
			leftEye.getViewport().setGLScissor();
			mStereoRenderer.onDrawEye(leftEye.getTransform());

			if (rightEye == null) {
				return;
			}

			rightEye.getViewport().setGLViewport();
			rightEye.getViewport().setGLScissor();
			mStereoRenderer.onDrawEye(rightEye.getTransform());
		}

		public void onFinishFrame(Viewport viewport)
		{
			viewport.setGLViewport();
			viewport.setGLScissor();
			mStereoRenderer.onFinishFrame(viewport);
		}

		public void onSurfaceChanged(int width, int height)
		{
			if (mVRMode)
			{
				mStereoRenderer.onSurfaceChanged(width / 2, height);
			}
			else mStereoRenderer.onSurfaceChanged(width, height);
		}

		public void onSurfaceCreated(EGLConfig config)
		{
			mStereoRenderer.onSurfaceCreated(config);
		}

		public void onRendererShutdown()
		{
			mStereoRenderer.onRendererShutdown();
		}
	}

	private class RendererHelper
	implements GLSurfaceView.Renderer
	{
		private final HeadTransform mHeadTransform;
		private final EyeParams mMonocular;
		private final EyeParams mLeftEye;
		private final EyeParams mRightEye;
		private final float[] mLeftEyeTranslate;
		private final float[] mRightEyeTranslate;
		private final CardboardView.Renderer mRenderer;
		private boolean mShuttingDown;
		private HeadMountedDisplay mHmd;
		private boolean mVRMode;
		private boolean mDistortionCorrectionEnabled;
		private float mDistortionCorrectionScale;
		private float mZNear;
		private float mZFar;
		private boolean mProjectionChanged;
		private boolean mInvalidSurfaceSize;

		public RendererHelper(CardboardView.Renderer renderer)
		{
			mRenderer = renderer;
			mHmd = new HeadMountedDisplay(CardboardView.this.mHmd);
			mHeadTransform = new HeadTransform();
			mMonocular = new EyeParams(0);
			mLeftEye = new EyeParams(1);
			mRightEye = new EyeParams(2);
			updateFieldOfView(mLeftEye.getFov(), mRightEye.getFov());
			mDistortionRenderer = new DistortionRenderer();

			mLeftEyeTranslate = new float[16];
			mRightEyeTranslate = new float[16];

			mVRMode = CardboardView.this.mVRMode;
			mDistortionCorrectionEnabled = CardboardView.this.mDistortionCorrectionEnabled;
			mDistortionCorrectionScale = CardboardView.this.mDistortionCorrectionScale;
			mZNear = CardboardView.this.mZNear;
			mZFar = CardboardView.this.mZFar;

			mProjectionChanged = true;
		}

		public void shutdown() {
			queueEvent(new Runnable()
			{
				public void run() {
					synchronized (this) {
						mShuttingDown = true;
						mRenderer.onRendererShutdown();
						notifyAll();
					}
				}
			});
		}

		public void setCardboardDeviceParams(CardboardDeviceParams newParams) {
			final CardboardDeviceParams deviceParams = new CardboardDeviceParams(newParams);
			queueEvent(new Runnable()
			{
				public void run() {
					mHmd.setCardboard(deviceParams);
					mProjectionChanged = true;
				}
			});
		}

		public void setScreenParams(ScreenParams newParams) {
			final ScreenParams screenParams = new ScreenParams(newParams);
			queueEvent(new Runnable()
			{
				public void run() {
					mHmd.setScreen(screenParams);
					mProjectionChanged = true;
				}
			});
		}

		public void setInterpupillaryDistance(final float interpupillaryDistance) {
			queueEvent(new Runnable()
			{
				public void run() {
					mHmd.getCardboard().setInterpupillaryDistance(interpupillaryDistance);
					mProjectionChanged = true;
				}
			});
		}

		public void setFOV(final float fovY) {
			queueEvent(new Runnable()
			{
				public void run() {
					mHmd.getCardboard().setFovY(fovY);
					mProjectionChanged = true;
				}
			});
		}

		public void setZPlanes(final float zNear, final float zFar) {
			queueEvent(new Runnable()
			{
				public void run() {
					mZNear = zNear;
					mZFar = zFar;
					mProjectionChanged = true;
				}
			});
		}

		public void setDistortionCorrectionEnabled(final boolean enabled) {
			queueEvent(new Runnable()
			{
				public void run() {
					mDistortionCorrectionEnabled = enabled;
					mProjectionChanged = true;
				}
			});
		}

		public void setDistortionCorrectionScale(final float scale) {
			queueEvent(new Runnable()
			{
				public void run() {
					mDistortionCorrectionScale = scale;
					mDistortionRenderer.setResolutionScale(scale);
				}
			});
		}

		public void setVRModeEnabled(final boolean enabled) {
			queueEvent(new Runnable()
			{
				public void run() {
					if (mVRMode == enabled) {
						return;
					}

					mVRMode = enabled;

					if ((mRenderer instanceof CardboardView.StereoRendererHelper)) {
						CardboardView.StereoRendererHelper stereoHelper = (CardboardView.StereoRendererHelper)mRenderer;
						stereoHelper.setVRModeEnabled(enabled);
					}

					mProjectionChanged = true;
					onSurfaceChanged((GL10)null, mHmd.getScreen().getWidth(), mHmd.getScreen().getHeight());
				}
			});
		}

		public void onDrawFrame(GL10 gl)
		{
			if ((mShuttingDown) || (mInvalidSurfaceSize)) {
				return;
			}

			ScreenParams screen = mHmd.getScreen();
			CardboardDeviceParams cdp = mHmd.getCardboard();

			mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);

			float halfInterpupillaryDistance = cdp.getInterpupillaryDistance() * 0.5F;

			if (mVRMode)
			{
				Matrix.setIdentityM(mLeftEyeTranslate, 0);
				Matrix.setIdentityM(mRightEyeTranslate, 0);

				Matrix.translateM(mLeftEyeTranslate, 0, halfInterpupillaryDistance, 0.0F, 0.0F);

				Matrix.translateM(mRightEyeTranslate, 0, -halfInterpupillaryDistance, 0.0F, 0.0F);

				Matrix.multiplyMM(mLeftEye.getTransform().getEyeView(), 0, mLeftEyeTranslate, 0, mHeadTransform.getHeadView(), 0);

				Matrix.multiplyMM(mRightEye.getTransform().getEyeView(), 0, mRightEyeTranslate, 0, mHeadTransform.getHeadView(), 0);
			}
			else
			{
				System.arraycopy(mHeadTransform.getHeadView(), 0, mMonocular.getTransform().getEyeView(), 0, mHeadTransform.getHeadView().length);
			}

			if (mProjectionChanged)
			{
				mMonocular.getViewport().setViewport(0, 0, screen.getWidth(), screen.getHeight());

				if (!mVRMode)
				{
					float aspectRatio = screen.getWidth() / screen.getHeight();
					Matrix.perspectiveM(mMonocular.getTransform().getPerspective(), 0, cdp.getFovY(), aspectRatio, mZNear, mZFar);
				}
				else if (mDistortionCorrectionEnabled) {
					updateFieldOfView(mLeftEye.getFov(), mRightEye.getFov());
					mDistortionRenderer.onProjectionChanged(mHmd, mLeftEye, mRightEye, mZNear, mZFar);
				}
				else
				{
					float distEyeToScreen = cdp.getVisibleViewportSize() / 2.0F / (float)Math.tan(Math.toRadians(cdp.getFovY()) / 2.0D);

					float left = screen.getWidthMeters() / 2.0F - halfInterpupillaryDistance;
					float right = halfInterpupillaryDistance;
					float bottom = cdp.getVerticalDistanceToLensCenter() - screen.getBorderSizeMeters();

					float top = screen.getBorderSizeMeters() + screen.getHeightMeters() - cdp.getVerticalDistanceToLensCenter();

					FieldOfView leftEyeFov = mLeftEye.getFov();
					leftEyeFov.setLeft((float)Math.toDegrees(Math.atan2(left, distEyeToScreen)));

					leftEyeFov.setRight((float)Math.toDegrees(Math.atan2(right, distEyeToScreen)));

					leftEyeFov.setBottom((float)Math.toDegrees(Math.atan2(bottom, distEyeToScreen)));

					leftEyeFov.setTop((float)Math.toDegrees(Math.atan2(top, distEyeToScreen)));

					FieldOfView rightEyeFov = mRightEye.getFov();
					rightEyeFov.setLeft(leftEyeFov.getRight());
					rightEyeFov.setRight(leftEyeFov.getLeft());
					rightEyeFov.setBottom(leftEyeFov.getBottom());
					rightEyeFov.setTop(leftEyeFov.getTop());

					leftEyeFov.toPerspectiveMatrix(mZNear, mZFar, mLeftEye.getTransform().getPerspective(), 0);

					rightEyeFov.toPerspectiveMatrix(mZNear, mZFar, mRightEye.getTransform().getPerspective(), 0);

					mLeftEye.getViewport().setViewport(0, 0, screen.getWidth() / 2, screen.getHeight());

					mRightEye.getViewport().setViewport(screen.getWidth() / 2, 0, screen.getWidth() / 2, screen.getHeight());
				}

				mProjectionChanged = false;
			}

			if (mVRMode) {
				if (mDistortionCorrectionEnabled) {
					mDistortionRenderer.beforeDrawFrame();

					if (mDistortionCorrectionScale == 1.0F) {
						mRenderer.onDrawFrame(mHeadTransform, mLeftEye, mRightEye);
					}
					else {
						int leftX = mLeftEye.getViewport().x;
						int leftY = mLeftEye.getViewport().y;
						int leftWidth = mLeftEye.getViewport().width;
						int leftHeight = mLeftEye.getViewport().height;
						int rightX = mRightEye.getViewport().x;
						int rightY = mRightEye.getViewport().y;
						int rightWidth = mRightEye.getViewport().width;
						int rightHeight = mRightEye.getViewport().height;

						mLeftEye.getViewport().setViewport((int)(leftX * mDistortionCorrectionScale), (int)(leftY * mDistortionCorrectionScale), (int)(leftWidth * mDistortionCorrectionScale), (int)(leftHeight * mDistortionCorrectionScale));

						mRightEye.getViewport().setViewport((int)(rightX * mDistortionCorrectionScale), (int)(rightY * mDistortionCorrectionScale), (int)(rightWidth * mDistortionCorrectionScale), (int)(rightHeight * mDistortionCorrectionScale));

						mRenderer.onDrawFrame(mHeadTransform, mLeftEye, mRightEye);

						mLeftEye.getViewport().setViewport(leftX, leftY, leftWidth, leftHeight);

						mRightEye.getViewport().setViewport(rightX, rightY, rightWidth, rightHeight);
					}

					mDistortionRenderer.afterDrawFrame();
				} else {
					mRenderer.onDrawFrame(mHeadTransform, mLeftEye, mRightEye);
				}
			}
			else mRenderer.onDrawFrame(mHeadTransform, mMonocular, null);

			mRenderer.onFinishFrame(mMonocular.getViewport());
		}

		public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			if (mShuttingDown) {
				return;
			}

			ScreenParams screen = mHmd.getScreen();
			if ((width != screen.getWidth()) || (height != screen.getHeight())) {
				if (!mInvalidSurfaceSize) {
					GLES20.glClear(16384);
					Log.w("CardboardView", "Surface size " + width + "x" + height + " does not match the expected screen size " + screen.getWidth() + "x" + screen.getHeight() + ". Rendering is disabled.");
				}

				mInvalidSurfaceSize = true;
			} else {
				mInvalidSurfaceSize = false;
			}

			mRenderer.onSurfaceChanged(width, height);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			if (mShuttingDown) {
				return;
			}

			mRenderer.onSurfaceCreated(config);
		}

		private void updateFieldOfView(FieldOfView leftEyeFov, FieldOfView rightEyeFov) {
			CardboardDeviceParams cdp = mHmd.getCardboard();
			ScreenParams screen = mHmd.getScreen();
			Distortion distortion = cdp.getDistortion();

			float idealFovAngle = (float)Math.toDegrees(Math.atan2(cdp.getLensDiameter() / 2.0F, cdp.getEyeToLensDistance()));

			float eyeToScreenDist = cdp.getEyeToLensDistance() + cdp.getScreenToLensDistance();

			float outerDist = (screen.getWidthMeters() - cdp.getInterpupillaryDistance()) / 2.0F;

			float innerDist = cdp.getInterpupillaryDistance() / 2.0F;
			float bottomDist = cdp.getVerticalDistanceToLensCenter() - screen.getBorderSizeMeters();

			float topDist = screen.getHeightMeters() + screen.getBorderSizeMeters() - cdp.getVerticalDistanceToLensCenter();

			float outerAngle = (float)Math.toDegrees(Math.atan2(distortion.distort(outerDist), eyeToScreenDist));

			float innerAngle = (float)Math.toDegrees(Math.atan2(distortion.distort(innerDist), eyeToScreenDist));

			float bottomAngle = (float)Math.toDegrees(Math.atan2(distortion.distort(bottomDist), eyeToScreenDist));

			float topAngle = (float)Math.toDegrees(Math.atan2(distortion.distort(topDist), eyeToScreenDist));

			leftEyeFov.setLeft(Math.min(outerAngle, idealFovAngle));
			leftEyeFov.setRight(Math.min(innerAngle, idealFovAngle));
			leftEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			leftEyeFov.setTop(Math.min(topAngle, idealFovAngle));

			rightEyeFov.setLeft(Math.min(innerAngle, idealFovAngle));
			rightEyeFov.setRight(Math.min(outerAngle, idealFovAngle));
			rightEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			rightEyeFov.setTop(Math.min(topAngle, idealFovAngle));
		}
	}

	/** Intercepts changes in the current Cardboard device parameters. */
	public static abstract interface CardboardDeviceParamsObserver
	{
		public abstract void onCardboardDeviceParamsUpdate(CardboardDeviceParams paramCardboardDeviceParams);
	}

	/** Interface for renderers that delegate all stereoscopic rendering details to the view. */
	public static abstract interface StereoRenderer
	{
		public abstract void onNewFrame(HeadTransform paramHeadTransform);

		public abstract void onDrawEye(EyeTransform paramEyeTransform);

		public abstract void onFinishFrame(Viewport paramViewport);

		public abstract void onSurfaceChanged(int paramInt1, int paramInt2);

		public abstract void onSurfaceCreated(EGLConfig paramEGLConfig);

		public abstract void onRendererShutdown();
	}

	/** Interface for renderers who need to handle all the stereo rendering details by themselves. */
	public static abstract interface Renderer
	{
		public abstract void onDrawFrame(HeadTransform paramHeadTransform, EyeParams paramEyeParams1, EyeParams paramEyeParams2);

		public abstract void onFinishFrame(Viewport paramViewport);

		public abstract void onSurfaceChanged(int paramInt1, int paramInt2);

		public abstract void onSurfaceCreated(EGLConfig paramEGLConfig);

		public abstract void onRendererShutdown();
	}
}