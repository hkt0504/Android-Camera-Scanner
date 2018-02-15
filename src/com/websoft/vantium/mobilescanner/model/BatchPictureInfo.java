package com.websoft.vantium.mobilescanner.model;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import com.websoft.vantium.mobilescanner.common.BitmapWrapper;
import com.websoft.vantium.mobilescanner.common.Common;


public class BatchPictureInfo implements Parcelable {
	
	private Bitmap  picBitmap;
	private String 	picPath;
	private int 	rotation;

	private Rect    picOrgBitmapRect = new Rect();
	private PointF 	cropPoints[] = new PointF[4];
	private int 	inflateWidht;
	private int 	inflateHeight;

	public BatchPictureInfo(){

		rotation = 0;
		picBitmap = null;
		picPath = "";
		inflateWidht = 0;
		inflateHeight = 0;

		for (int i=0; i<4; i++){
			cropPoints[i] = new PointF(0.0f, 0.0f);
		}
	}

	public void setRotation(int rotation){
		this.rotation = rotation;
	}
	
	public int getRotation(){
		return rotation;
	}
	
	public void setOrgBitmapRect(Rect rect){
		picOrgBitmapRect.set(rect);
	}

	public Rect getOrgBitmapRect(){
		return picOrgBitmapRect;
	}

	public void releaseBitmap(){

		if (picBitmap != null){
			if (!picBitmap.isRecycled())
				picBitmap.recycle();

			picBitmap = null;
		}
	}

	public void loadBitmap(){
	
		releaseBitmap();

		int rotation = BitmapWrapper.getPhotoOrientation(picPath);
		picBitmap = BitmapWrapper.readScaledBitmap565(picPath, Common.CROPVIEW_SIZE);

		if (rotation != 0){

			Bitmap bmp = BitmapWrapper.RotateBitmap(picBitmap, rotation);
			picBitmap.recycle();
			picBitmap = bmp;
		}
	}
	
	public void setPicPath(String picPath){
		this.picPath = picPath;
	}

	public Bitmap getPicBitmap(){

		return picBitmap;
	}

	public String getPicPath(){
		return picPath;
	}

	public PointF[] getCropPoints(){
		return cropPoints;
	}

//	public void setCropPoints(int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4){
//		cropPoints[0].set(x1, y1);
//		cropPoints[1].set(x2, y2);
//		cropPoints[2].set(x3, y3);
//		cropPoints[3].set(x4, y4);
//	}

	public void setCropPoints(float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4){
		cropPoints[0].set(x1, y1);
		cropPoints[1].set(x2, y2);
		cropPoints[2].set(x3, y3);
		cropPoints[3].set(x4, y4);
	}
	
	public void setInflateSize(int inflateWidht, int inflateHeight){

		this.inflateWidht = inflateWidht;
		this.inflateHeight = inflateHeight;
	}

	public int getInflateWidth(){
		return inflateWidht;
	}

	public int getInflateHeight(){
		return inflateHeight;
	}

	public static Parcelable.Creator<BatchPictureInfo> getCreator() {
		return CREATOR;
	}

	public static final Parcelable.Creator<BatchPictureInfo> CREATOR = new Parcelable.Creator<BatchPictureInfo>() {
		@Override
		public BatchPictureInfo createFromParcel(Parcel in) {
			BatchPictureInfo info = new BatchPictureInfo();
			
			info.rotation = in.readInt();
			info.picPath = in.readString();
			info.picOrgBitmapRect.set(in.readInt(), in.readInt(), in.readInt(), in.readInt());
			info.cropPoints[0].set(in.readFloat(), in.readFloat());
			info.cropPoints[1].set(in.readFloat(), in.readFloat());
			info.cropPoints[2].set(in.readFloat(), in.readFloat());
			info.cropPoints[3].set(in.readFloat(), in.readFloat());
			info.inflateWidht = in.readInt();
			info.inflateHeight = in.readInt();
			return info;
		}

		@Override
		public BatchPictureInfo[] newArray(int size) {
			return new BatchPictureInfo[size];
		}

	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		
		out.writeInt(rotation);
		out.writeString(picPath);
		out.writeInt(picOrgBitmapRect.left);
		out.writeInt(picOrgBitmapRect.top);
		out.writeInt(picOrgBitmapRect.right);
		out.writeInt(picOrgBitmapRect.bottom);
		out.writeFloat(cropPoints[0].x);
		out.writeFloat(cropPoints[0].y);
		out.writeFloat(cropPoints[1].x);
		out.writeFloat(cropPoints[1].y);
		out.writeFloat(cropPoints[2].x);
		out.writeFloat(cropPoints[2].y);
		out.writeFloat(cropPoints[3].x);
		out.writeFloat(cropPoints[3].y);
		out.writeInt(inflateWidht);
		out.writeInt(inflateHeight);
	}
}