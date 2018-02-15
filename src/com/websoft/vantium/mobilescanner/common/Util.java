package com.websoft.vantium.mobilescanner.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.view.OrientationEventListener;


public class Util {

	public static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	public static int roundOrientation(int orientationInput) {
		int orientation = orientationInput;

		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
			orientation = 0;
		}

		orientation = orientation % 360;
		int retVal;
		if (orientation < (0 * 90) + 45) {
			retVal = 0;
		} else if (orientation < (1 * 90) + 45) {
			retVal = 90;
		} else if (orientation < (2 * 90) + 45) {
			retVal = 180;
		} else if (orientation < (3 * 90) + 45) {
			retVal = 270;
		} else {
			retVal = 0;
		}

		retVal += 90;
		retVal %= 360;

		return retVal;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
	
	
	public static boolean isPortrait(Activity actv){
	
		Point point = new Point();
		actv.getWindowManager().getDefaultDisplay().getSize(point);

		float scr_w = (float)point.x;
		float scr_h = (float)point.y;

		//check portrait or landscape.
		return (scr_h > scr_w);
	}
	
	public static int createPrimaryKey(){
		return (int)(System.currentTimeMillis() & 0xFFFFFFFF);
	}
}