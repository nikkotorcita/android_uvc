package com.example.com.usbhost.mantis;

import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

public class UvcControls {
	private static final String TAG = "UvcControls";
	
	//from the device
	private byte[] streamingControls = new byte[48];
	
	//to be sent to the device
	private byte[] _streamingControls = new byte[48];
	
	private int hint, keyFrameRate, pFrameRate, compQuality, compWindowSize, delay;
	private long frameInterval, maxVideoFrameSize, maxPayloadTransfer, clockFrequency;
	private short formatIndex, frameIndex, framingInfo, preferedVersion, minVersion, maxVersion;
	
	private UsbDeviceConnection mDeviceConnection;
	
	public UvcControls(UsbDeviceConnection conn) {
		mDeviceConnection = conn;
	}
	
	public void setDefault() {
		_streamingControls = streamingControls;
	}
	
	public void setHint(int hint) {
		_streamingControls[0] = (byte) (hint & 0x000000ff);
		_streamingControls[1] = (byte) ((hint & 0x0000ff00) >> 8);
	}
	
	public void setFormatIndex(int index) {
		_streamingControls[2] = (byte) ((short)index & 0x00ff);
	}
	
	public void setFrameIndex(int index) {
		_streamingControls[3] = (byte) ((short)index & 0x00ff);
	}
	
	public void setFrameInterval(long interval) {
		_streamingControls[4] = (byte) (interval & 0x00000000000000ff);
		_streamingControls[5] = (byte) ((interval & 0x000000000000ff00) >> 8);
		_streamingControls[6] = (byte) ((interval & 0x0000000000ff0000) >> 16);
		_streamingControls[7] = (byte) ((interval & 0x00000000ff000000) >> 24);
	}
	
	public void setKeyFrameRate(int rate) {
		_streamingControls[8] = (byte) (rate & 0x000000ff);
		_streamingControls[9] = (byte) ((rate & 0x0000ff00) >> 8);
	}
	
	public void setPFrameRate(int rate) {
		_streamingControls[10] = (byte) (rate & 0x000000ff);
		_streamingControls[11] = (byte) ((rate & 0x0000ff00) >> 8);
	}
	
	public void setCompQuality(int quality) {
		_streamingControls[12] = (byte) (quality & 0x000000ff);
		_streamingControls[13] = (byte) ((quality & 0x0000ff00) >> 8);
	}
	
	public void setCompWindowSize(int size) {
		_streamingControls[14] = (byte) (size & 0x000000ff);
		_streamingControls[15] = (byte) ((size & 0x0000ff00) >> 8);
	}
	
	public void setDelay(int delay) {
		_streamingControls[16] = (byte) (delay & 0x000000ff);
		_streamingControls[17] = (byte) ((delay & 0x0000ff00) >> 8);
	}
	
	public void setMaxVideoFrameSize(long size) {
		_streamingControls[18] = (byte) (size & 0x00000000000000ff);
		_streamingControls[19] = (byte) ((size & 0x000000000000ff00) >> 8);
		_streamingControls[20] = (byte) ((size & 0x0000000000ff0000) >> 16);
		_streamingControls[21] = (byte) ((size & 0x00000000ff000000) >> 24);
	}
	
	public void setMaxPayloadTransferSize(long size) {
		_streamingControls[22] = (byte) (size & 0x00000000000000ff);
		_streamingControls[23] = (byte) ((size & 0x000000000000ff00) >> 8);
		_streamingControls[24] = (byte) ((size & 0x0000000000ff0000) >> 16);
		_streamingControls[25] = (byte) ((size & 0x00000000ff000000) >> 24);
	}
	
	public void setClockFrequency(long freq) {
		_streamingControls[26] = (byte) (freq & 0x00000000000000ff);
		_streamingControls[27] = (byte) ((freq & 0x000000000000ff00) >> 8);
		_streamingControls[28] = (byte) ((freq & 0x0000000000ff0000) >> 16);
		_streamingControls[29] = (byte) ((freq & 0x00000000ff000000) >> 24);
	}
	
	public void setFramingInfo(int info) {
		_streamingControls[30] = (byte) ((short)info & 0x00ff);
	}
	
	public void setPreferedVersion(int ver) {
		_streamingControls[31] = (byte) ((short)ver & 0x00ff);
	}
	
	public void setMinVersion(int ver) {
		_streamingControls[32] = (byte) ((short)ver & 0x00ff);
	}
	
	public void setMaxVersion(int ver) {
		_streamingControls[33] = (byte) ((short)ver & 0x00ff);
	}
	
	public int getHint() {
		hint = (int) ((streamingControls[1] << 8) | streamingControls[0]);
		return hint;
	}
	
	public short getFormatIndex() {
		formatIndex = (short) streamingControls[2];
		return formatIndex;
	}
	
	public short getFrameIndex() {
		frameIndex = (short) streamingControls[3];
		return frameIndex;
	}
	
	public long getFrameInterval() {
		frameInterval = (long) ((streamingControls[7] << 24) | (streamingControls[6] << 16) | (streamingControls[5] << 8) | streamingControls[4]);
		return frameInterval;
	}
	
	public int getKeyFrameRate() {
		keyFrameRate = (int) ((streamingControls[9] << 8) | streamingControls[8]);
		return keyFrameRate;
	}
	
	public int getPFrameRate() {
		pFrameRate = (int) ((streamingControls[11] << 8) | streamingControls[10]);
		return pFrameRate;
	}
	
	public int getCompQuality() {
		compQuality = (int) ((streamingControls[13] << 8) | streamingControls[12]);
		return compQuality;
	}
	
	public int getCompWindowSize() {
		compWindowSize = (int) ((streamingControls[15] << 8) | streamingControls[14]);
		return compWindowSize;
	}
	
	public int getDelay() {
		delay = (int) ((streamingControls[17] << 8) | streamingControls[16]);
		return delay;
	}
	
	public long getMaxVideoFrameSize() {
		maxVideoFrameSize = (long) ((streamingControls[21] << 24) | (streamingControls[20] << 16) | (streamingControls[19] << 8) | streamingControls[18]);
		return maxVideoFrameSize;
	}
	
	public long getMaxPayloadTransferSize() {
		maxPayloadTransfer = (long) ((streamingControls[25] << 24) | (streamingControls[24] << 16) | (streamingControls[23] << 8) | streamingControls[22]);
		return maxPayloadTransfer;
	}
	
	public long getClockFrequency() {
		clockFrequency = (long) ((streamingControls[29] << 24) | (streamingControls[28] << 16) | (streamingControls[27] << 8) | streamingControls[26]);
		return clockFrequency;
	}
	
	public short getFramingInfo() {
		framingInfo = (short) streamingControls[30];
		return framingInfo;
	}
	
	public short getPreferedVersion() {
		preferedVersion = (short) streamingControls[31];
		return preferedVersion;
	}
	
	public short getMinVersion() {
		minVersion = (short) streamingControls[32];
		return minVersion;
	}
	
	public short getMaxVersion() {
		maxVersion = (short) streamingControls[33];
		return maxVersion;
	}
	
	public boolean probeControl(int request) {
		if (request == UvcConstants.SET_CUR) {
			if (mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_OUT, request, UvcConstants.PROBE_CONTROL, 
					UvcConstants.VIDEO_STREAMING_INTERFACE, streamingControls, streamingControls.length, 0) > -1) {
				return true;
			}
			else return false;
		}
		else {
			if (mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_IN, request, UvcConstants.PROBE_CONTROL, 
					UvcConstants.VIDEO_STREAMING_INTERFACE, streamingControls, streamingControls.length, 0) > -1) {
				return true;
			}
			else return false;
		}
	}
	
	public boolean commitControl(int request) {
		if (request == UvcConstants.SET_CUR) {
			if (mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_OUT, request, UvcConstants.COMMIT_CONTROL, 
					UvcConstants.VIDEO_STREAMING_INTERFACE, streamingControls, streamingControls.length, 0) > -1) {
				return true;
			}
			else return false;
		}
		else {
			if (mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_IN, request, UvcConstants.COMMIT_CONTROL, 
					UvcConstants.VIDEO_STREAMING_INTERFACE, streamingControls, streamingControls.length, 0) > -1) {
				return true;
			}
			else return false;
		}
	}
	
	public int getErrorCode() {
		byte[] code = new byte[1];
		
		if(mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_IN, UvcConstants.GET_CUR, UvcConstants.STREAM_ERROR_CODE_CONTROL, 
				UvcConstants.VIDEO_STREAMING_INTERFACE, code, code.length, 0) > -1) {
			return (int) code[0];
		}
		else {
			return -1;
		}
	}
	
	public int getScanMode() {
		byte[] mode = new byte[1];
		
		if(mDeviceConnection.controlTransfer(UvcConstants.CLASS_REQUEST_IN, UvcConstants.GET_CUR, UvcConstants.SCANNING_MODE_CONTROL, 
				UvcConstants.CAMERA_SENSOR, mode, mode.length, 0) > -1) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	public boolean startStreaming() {
		if(mDeviceConnection.controlTransfer(UvcConstants.STANDARD_REQUEST_OUT, UvcConstants.SET_INTERFACE, UvcConstants.OPERATIONAL, 
				UvcConstants.VIDEO_STREAMING_INTERFACE, null, 0, 0) > -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
//	private void getStillControls(byte[] controls) {
//		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_IN, GET_CUR, STILL_PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
//			Log.v(TAG, "still image controls get");
//		}
//		else {
//			Log.v(TAG, "still image controls failed");
//		}
//	}
//	
//	private void setStillControls(byte[] controls) {
//		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
//			Log.v(TAG, "still image controls set");
//		}
//		else {
//			Log.v(TAG, "still image controls failed");
//		}
//	}
//	
//	private void commitStillControls(byte[] controls) {
//		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_COMMIT_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
//			Log.v(TAG, "still image controls commit");
//		}
//		else {
//			Log.v(TAG, "still image controls failed");
//		}
//	}
//	
//	private void stillImageTrigger(byte[] trigger) {
//		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_IMAGE_TRIGGER_CONTROL, VIDEO_STREAMING_INTERFACE, trigger, trigger.length, 0) > -1) {
//			Log.v(TAG, "still trigger command set");
//		}
//		else {
//			Log.v(TAG, "still trigger command failed");
//		}
//	}
	
	public String formattedValues() {
		getHint();
		getFormatIndex();
		getFrameIndex();
		getFrameInterval();
		getKeyFrameRate();
		getPFrameRate();
		getCompQuality();
		getCompWindowSize();
		getDelay();
		getMaxVideoFrameSize();
		getMaxPayloadTransferSize();
		getClockFrequency();
		getFramingInfo();
		getPreferedVersion();
		getMinVersion();
		getMaxVersion();
		
		String str = "Hint = " + hint +
					 "\nFormatIndex = " + formatIndex +
					 "\nFrameIndex = " + frameIndex + 
					 "\nFrameInterval = " + frameInterval + 
					 "\nKeyFramerate = " + keyFrameRate + 
					 "\nPFrameRate 	= " + pFrameRate + 
					 "\nCompQuality	= " + compQuality + 
					 "\nCompWindowSize = " + compWindowSize + 
					 "\nDelay = " + delay + 
					 "\nMaxVideoFrameSize = " + maxVideoFrameSize +
					 "\nMaxPayloadTransferSize = " + maxVideoFrameSize + 
					 "\nClockFrequency = " + clockFrequency + 
					 "\nFramingInfo = " + framingInfo + 
					 "\nPreferedVersion = " + preferedVersion +
					 "\nMinVersion = " + minVersion +
					 "\nMaxVersion = " + maxVersion;
		
		return str;
	}
}