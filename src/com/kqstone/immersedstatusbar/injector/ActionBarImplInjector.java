package com.kqstone.immersedstatusbar.injector;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.FrameLayout;

import com.kqstone.immersedstatusbar.Utils;
import com.kqstone.immersedstatusbar.helper.BitMapColor;
import com.kqstone.immersedstatusbar.helper.ReflectionHelper;

public class ActionBarImplInjector {
	private Object mActionBar;
	private Activity mActivity;
	private ActivityInjector mActivityInjector;
	private int mColor;
	private boolean mDarkMode;

	public ActionBarImplInjector(Object actionBarImpl) {
		mActionBar = actionBarImpl;
		mActivity = (Activity) ReflectionHelper.getObjectField(actionBarImpl,
				"mActivity");
		mActivityInjector = (ActivityInjector) ReflectionHelper
				.getAdditionalInstanceField(mActivity, "mActivityInjector");
	}

	public void hookAfterShow() {
		FrameLayout container = (FrameLayout) ReflectionHelper.getObjectField(
				mActionBar, "mContainerView");
		if (container != null) {
			Drawable backgroundDrawable = (Drawable) ReflectionHelper
					.getObjectField(container, "mBackground");
			if (backgroundDrawable != null) {
				BitMapColor bmColor = Utils.getBitmapColor(backgroundDrawable);
				mColor = bmColor.Color;
				if (mColor == mActivityInjector.getCurrentColor())
					return;
				mDarkMode = Utils.getDarkMode(mColor);
				Drawable drawable = new ColorDrawable(mColor);
				if (bmColor.mType != BitMapColor.Type.FLAT) {
					((ActionBar) mActionBar).setBackgroundDrawable(drawable);
					container.invalidate();
				}
				updateActivityInjectorState();
				new Handler().post(new Runnable() {

					@Override
					public void run() {
						sendBroadcast();
					}});
			}
		}
	}

	public void hookAfterHide() {
		Bitmap bitmap = Utils.getBitMapFromActivityBackground(mActivity, false);
		if (bitmap != null) {
			BitMapColor bmColor = Utils.getBitmapColor(bitmap);
			mColor = bmColor.Color;
			if (mColor == mActivityInjector.getCurrentColor())
				return;
			mDarkMode = Utils.getDarkMode(mColor);
			updateActivityInjectorState();
			sendBroadcast();
		}
		bitmap.recycle();
	}

	public void hookAfteSetBackgroundDrawable(Drawable drawable) {
				BitMapColor bmColor = Utils.getBitmapColor(drawable);
				mColor = bmColor.Color;
				if (mColor == mActivityInjector.getCurrentColor())
					return;
				mDarkMode = Utils.getDarkMode(mColor);
				Drawable tempdrawable = new ColorDrawable(mColor);
				if (bmColor.mType != BitMapColor.Type.FLAT) {
					((ActionBar) mActionBar)
							.setBackgroundDrawable(tempdrawable);
				}
				tempdrawable = null;
				updateActivityInjectorState();
				sendBroadcast();
	}

	private void updateActivityInjectorState() {
		mActivityInjector.setCurrentColor(mColor);
		mActivityInjector.setCurrentDarkMode(mDarkMode);
		Utils.setDecorViewBackground(mActivity, new ColorDrawable(mColor), true);
	}

	private void sendBroadcast() {
		Utils.sendTintStatusBarIntent(mActivity, 0, mColor, null, mDarkMode,
				true);
	}

}
