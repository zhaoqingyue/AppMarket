package cld.navi.position.frame;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class GpsDataParam implements Parcelable {
	public int x;
	public int y;
	public int speed;
	public short high;
	public short derection;
	public int time;
	public int roaduid;
	
	public GpsDataParam() {
	}
	
	private GpsDataParam(Parcel source) {
		Bundle bundle = source.readBundle();
		this.x = bundle.getInt("x");
	    this.y = bundle.getInt("y");
		this.speed = bundle.getInt("speed");
		this.high = bundle.getShort("high");
		this.derection = bundle.getShort("derection");
		this.time = bundle.getInt("time");
		this.roaduid = bundle.getInt("roaduid");
	}
	
	public void setParam(int x, int y, int speed, short high, short derection,
			int time, int roaduid) {
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.high = high;
		this.derection = derection;
		this.time = time;
		this.roaduid = roaduid;
	}
	
	public void clearParam()
	{
		this.x = 0;
		this.y= 0;
		this.speed = 0;
		this.high = 0;
		this.derection = 0;
		this.time = 0;
		this.roaduid = -1;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putInt("x", x);
		bundle.putInt("y", y);
		bundle.putInt("speed", speed);
		bundle.putShort("high", high);
		bundle.putShort("derection", derection);
		bundle.putInt("time", time);
		bundle.putInt("roaduid", roaduid);
		dest.writeBundle(bundle);
		
	}
	
	public static final Parcelable.Creator<GpsDataParam> CREATOR = new Creator<GpsDataParam>() {

		@Override
		public GpsDataParam createFromParcel(Parcel source) {

			return new GpsDataParam(source);
		}

		@Override
		public GpsDataParam[] newArray(int size) {
			return new GpsDataParam[size];
		}
	};
}
