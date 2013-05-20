package com.example.com.usbhost.mantis;

	public class UvcConstants {
		
	public static final int GET_CUR = 0x81;
	public static final int GET_MIN = 0x82;
	public static final int GET_MAX = 0x83;
	public static final int GET_RES = 0x84;
	public static final int GET_INFO = 0x86;
	public static final int GET_DEF = 0x87;
	public static final int SET_CUR = 0x01;
	
	public static final int SET_INTERFACE = 0x0b;
	public static final int OPERATIONAL = 0x0001;
	public static final int ZERO_BANDWIDTH	= 0x0000;
	
	public static final int VIDEO_STREAMING_INTERFACE = 0x0001;
	
	public static final int PROBE_CONTROL = 0x0100;
	public static final int COMMIT_CONTROL = 0x0200;
	public static final int STREAM_ERROR_CODE_CONTROL = 0x0600;
	public static final int STILL_IMAGE_TRIGGER_CONTROL = 0x0500;
	public static final int STILL_PROBE_CONTROL = 0x0300;
	public static final int STILL_COMMIT_CONTROL = 0x0400;
	
	public static final int SCANNING_MODE_CONTROL = 0x0100;
	
	public static final int CAMERA_SENSOR = 0x0100;
	public static final int OUTPUT_TERMINAL = 0x0200;
	public static final int PROCESSING_UNIT = 0x0300;
	public static final int EXTENSION_UNIT = 0x0400; 
	
	public static final int CLASS_REQUEST_IN = 0xa1;
	public static final int CLASS_REQUEST_OUT = 0x21;
	public static final int STANDARD_REQUEST_OUT = 0x01;
	
	public static final int VIDEO_STREAMING_INTERFFACE = 0x0001;
}