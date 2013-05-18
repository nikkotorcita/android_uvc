package com.example.com.usbhost.mantis;

public class UvcControls {
	private static final String TAG = "UvcControls";
	
	private byte[] streamingControls = new byte[48];
	
	private int hint, keyFrameRate, pFrameRate, compQuality, compWindowSize, delay;
	private long frameInterval, maxVideoFrameSize, maxPayloadTransfer, clockFrequency;
	private short formatIndex, frameIndex, framingInfo, preferedVersion, minVersion, maxVersion;
	
	public byte[] getControls() {
		return streamingControls;
	}
	
	public void setControls(byte[] controls) {
		streamingControls = controls;
	}
	
	public void setHint(int hint) {
		streamingControls[0] = (byte) (hint & 0x000000ff);
		streamingControls[1] = (byte) ((hint & 0x0000ff00) >> 8);
	}
	
	public void setFormatIndex(short index) {
		streamingControls[2] = (byte) (index & 0x00ff);
	}
	
	public void setFrameIndex(short index) {
		streamingControls[3] = (byte) (index & 0x00ff);
	}
	
	public void setFrameInterval(long interval) {
		streamingControls[4] = (byte) (interval & 0x00000000000000ff);
		streamingControls[5] = (byte) ((interval & 0x000000000000ff00) >> 8);
		streamingControls[6] = (byte) ((interval & 0x0000000000ff0000) >> 16);
		streamingControls[7] = (byte) ((interval & 0x00000000ff000000) >> 24);
	}
	
	public void setKeyFrameRate(int rate) {
		streamingControls[8] = (byte) (rate & 0x000000ff);
		streamingControls[9] = (byte) ((rate & 0x0000ff00) >> 8);
	}
	
	public void setPFrameRate(int rate) {
		streamingControls[10] = (byte) (rate & 0x000000ff);
		streamingControls[11] = (byte) ((rate & 0x0000ff00) >> 8);
	}
	
	public void setCompQuality(int quality) {
		streamingControls[12] = (byte) (quality & 0x000000ff);
		streamingControls[13] = (byte) ((quality & 0x0000ff00) >> 8);
	}
	
	public void setCompWindowSize(int size) {
		streamingControls[14] = (byte) (size & 0x000000ff);
		streamingControls[15] = (byte) ((size & 0x0000ff00) >> 8);
	}
	
	public void setDelay(int delay) {
		streamingControls[16] = (byte) (delay & 0x000000ff);
		streamingControls[17] = (byte) ((delay & 0x0000ff00) >> 8);
	}
	
	public void setMaxVideoFrameSize(long size) {
		streamingControls[18] = (byte) (size & 0x00000000000000ff);
		streamingControls[19] = (byte) ((size & 0x000000000000ff00) >> 8);
		streamingControls[20] = (byte) ((size & 0x0000000000ff0000) >> 16);
		streamingControls[21] = (byte) ((size & 0x00000000ff000000) >> 24);
	}
	
	public void setMaxPayloadTransferSize(long size) {
		streamingControls[22] = (byte) (size & 0x00000000000000ff);
		streamingControls[23] = (byte) ((size & 0x000000000000ff00) >> 8);
		streamingControls[24] = (byte) ((size & 0x0000000000ff0000) >> 16);
		streamingControls[25] = (byte) ((size & 0x00000000ff000000) >> 24);
	}
	
	public void setClockFrequency(long freq) {
		streamingControls[26] = (byte) (freq & 0x00000000000000ff);
		streamingControls[27] = (byte) ((freq & 0x000000000000ff00) >> 8);
		streamingControls[28] = (byte) ((freq & 0x0000000000ff0000) >> 16);
		streamingControls[29] = (byte) ((freq & 0x00000000ff000000) >> 24);
	}
	
	public void setFramingInfo(short info) {
		streamingControls[30] = (byte) (info & 0x00ff);
	}
	
	public void setPreferedVersion(short ver) {
		streamingControls[31] = (byte) (ver & 0x00ff);
	}
	
	public void setMinVersion(short ver) {
		streamingControls[32] = (byte) (ver & 0x00ff);
	}
	
	public void setMaxVersion(short ver) {
		streamingControls[33] = (byte) (ver & 0x00ff);
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