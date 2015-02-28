package com.koalcat.view;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
//import android.os.Build;
import android.support.v4.util.LruCache;

public class Blur {
	
	private static final String TAG = "Blur";
	
	private static final float BITMAP_SCALE_NOMAL = 0.8f;
	private static final float BLUR_RADIUS_NOMAL = 12.0f;
	
	private static final float BITMAP_SCALE_FAST = 0.4f;
	private static final float BLUR_RADIUS_FAST = 10.0f;

	private BaseRender mRender;

	private LruCache<String, Bitmap> mMemoryCache; 

	public Blur(Context context) {
		mRender = new ScriptIntrinsicBlurRender(context);
		
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
		final int cacheSize = maxMemory / 16;  
		  
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
			@Override  
			protected int sizeOf(String key, Bitmap bitmap) {  
				return bitmap.getByteCount() / 1024;  
			}  
		};
	}

	public Bitmap blur(Bitmap image, boolean fast) {

		float scale = BITMAP_SCALE_NOMAL;
		float radius = BLUR_RADIUS_NOMAL;
		
		if (fast) {
			scale = BITMAP_SCALE_FAST;
			radius = BLUR_RADIUS_FAST;
		}
		
		int width = Math.round(image.getWidth() * scale);
		int height = Math.round(image.getHeight() * scale);

		Bitmap bitmap = Bitmap.createScaledBitmap(image, width, height, true);
		Bitmap outputBitmap = getBitmapFromMemCache("" + width + height);
		if (outputBitmap == null) {
			outputBitmap = bitmap.copy(bitmap.getConfig(), true);
			addBitmapToMemoryCache("" + width + height, outputBitmap);
		}

		mRender.blur(radius, bitmap, outputBitmap);
		bitmap.recycle();
		
		return outputBitmap;
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
	    if (getBitmapFromMemCache(key) == null) {  
	        mMemoryCache.put(key, bitmap);  
	    }  
	}  
	  
	private Bitmap getBitmapFromMemCache(String key) {  
	    return mMemoryCache.get(key);  
	}
    
	public void Destroy() {
		if (mRender != null) mRender.destroy();
	}
	
}

