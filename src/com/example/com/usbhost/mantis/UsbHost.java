package com.example.com.usbhost.mantis;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UsbHost extends Activity implements OnClickListener{

	private static final String TAG = "Video Device";
	private static final String ACTION_USB_PERMISSION = "com.usbhost.mantis.USB_PERMISSION";
	private static boolean isPermitted = false;
	private static boolean isConnected = false;
	
	private static final int REQ_VIDCONSTREAM_INTERFACE = 0x01;
	private static final int GET_INFO_REQUEST = 0x86;
	private static final int PU_BRIGHTNESS_CONTROL = 0x0200;
	
	private static final int GET_CUR = 0x81;
	private static final int GET_MIN = 0x82;
	private static final int GET_MAX = 0x83;
	private static final int GET_RES = 0x84;
	private static final int GET_INFO = 0x86;
	private static final int GET_DEF = 0x87;
	private static final int SET_CUR = 0x01;
	
	private static final int SET_INTERFACE = 0x0b;
	private static final int OPERATIONAL = 0x0001;
	private static final int ZERO_BANDWIDTH	= 0x0000;
	
	private static final int VIDEO_STREAMING_INTERFACE = 0x0001;
	
	private static final int PROBE_CONTROL = 0x0100;
	private static final int COMMIT_CONTROL = 0x0200;
	private static final int STILL_IMAGE_TRIGGER_CONTROL = 0x0500;
	private static final int STILL_PROBE_CONTROL = 0x0300;
	private static final int STILL_COMMIT_CONTROL = 0x0400;
	
	private static final int CAMERA_SENSOR = 0x0100;
	private static final int OUTPUT_TERMINAL = 0x0200;
	private static final int PROCESSING_UNIT = 0x0300;
	private static final int EXTENSION_UNIT = 0x0400;
	
	private static final int CLASS_REQUEST_IN = 0xa1;
	private static final int CLASS_REQUEST_OUT = 0x21;
	private static final int STANDARD_REQUEST_OUT = 0x01;
	
	
	private static final int VIDEO_STREAMING_INTERFFACE = 0x0001;
	
	private File imageFile = null;
	private FileOutputStream osr = null;
	private DataOutputStream out = null;
	
	private byte[] streamingControls = new byte[48];
	
	ByteBuffer imgBuf;
	
	byte[] bImgBuf = new byte[622592];
	byte[] epBuf = new byte[20000];
	
	Button bConnect,bScan, bCaptureFrame;
	TextView status;
	
	UsbManager mManager;
	UsbInterface mControlIntf;
	UsbInterface mStreamingIntf;
	UsbDevice mDevice;
	UsbDeviceConnection mDeviceConnection;
	UsbEndpoint mIntEndpointIn;
	UsbEndpoint mBulkEpIn;
	UsbRequest mUsbRequest;
	
	UvcControls mUvcControls = new UvcControls();
	
	String mDeviceName;
	
	PendingIntent mPermissionIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_host);
		
		imgBuf = ByteBuffer.allocate(622592);
		
		bScan = (Button) findViewById(R.id.scan);
		bConnect = (Button) findViewById(R.id.connect);
		bCaptureFrame = (Button) findViewById(R.id.capture_frame);
		
		bScan.setOnClickListener(this);
		bConnect.setOnClickListener(this);
		bCaptureFrame.setOnClickListener(this);
		
		status = (TextView) findViewById(R.id.deviceName);
		
	    mManager = (UsbManager)getSystemService(Context.USB_SERVICE);
	    mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	    registerReceiver(mUsbReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		deInit();
		
		unregisterReceiver(mUsbReceiver);
		
		super.onDestroy();
	}
	
	@Override
	public void onPause() {
		deInit();
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.scan:
			scanForDevice();
			break;
		case R.id.connect:
			connectToDevice();
			break;
		case R.id.capture_frame:
			try {
				captureFrame();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(ACTION_USB_PERMISSION.equals(action)) {
				synchronized(this) {
					if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						isPermitted = true;
					}
					else
						isPermitted = false;
				}
			}
		}
	};
	
	private void scanForDevice() {
		HashMap<String, UsbDevice> deviceList = mManager.getDeviceList();
		
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		while(deviceIterator.hasNext()) {
			UsbDevice device = deviceIterator.next();
			for(int i = 0; i < device.getInterfaceCount(); i++) {
				UsbInterface intf = device.getInterface(i);
				if(intf.getInterfaceClass() == 14 && intf.getInterfaceSubclass() == 1 && intf.getInterfaceProtocol() == 0) {
					Log.v(TAG, "Found video control interface");
					mControlIntf = intf;
				}
				
				else if(intf.getInterfaceClass() == 14 && intf.getInterfaceSubclass() == 2 && intf.getInterfaceProtocol() == 0) {
					Log.v(TAG, "Found video streaming interface");
					mStreamingIntf = intf; 
					mBulkEpIn = mStreamingIntf.getEndpoint(0);
//					Log.v(TAG, "bulk ep addr = " + mBulkEpIn.getAddress() + " direction = " + mBulkEpIn.getDirection());
				}
			}
			
			if(mControlIntf != null && mStreamingIntf != null) {
				mDevice = device;
				mDeviceName = device.getDeviceName().toString();
				status.setText("Found video device :\n " + mDeviceName);
				
				mManager.requestPermission(mDevice, mPermissionIntent);
			}
			
			else {
				mDevice = null;
				status.setText("no video device found");
			}
		}
	}
	
	private void connectToDevice() {
		if(!isPermitted) {
			status.setText("Permission denied to connect to device");
			return;
		}
		
		if(!isConnected) {
			if(mDevice != null) {
				mDeviceConnection = mManager.openDevice(mDevice);
				if(mDeviceConnection != null) {
					isConnected = mDeviceConnection.claimInterface(mStreamingIntf, false);
				}
				else {
					status.setText("Failed connecting to device:\n" + mDeviceName);
					Log.v(TAG, "Device open failed");
					return;
				}
			}
		}
		interfaceRequest();
	}
	
	private void interfaceRequest() {
		//byte[] info = new byte[1];
		byte[] info = new byte[11];
		byte[] temp = new byte[1];
		byte[] brightness = new byte[2];
		byte[] controls = new byte[48];
	
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	
		mUsbRequest = new UsbRequest();
		
		if(isConnected) {
			mUsbRequest.initialize(mDeviceConnection, mBulkEpIn);
			
			mUvcControls.setControls(getStreamingControls());
			Log.v(TAG, "###########");
			Log.v(TAG, mUvcControls.formattedValues());
			
//			mUvcControls.setFormatIndex((short) 2);
			setStreamingControls(mUvcControls.getControls());
			Log.v(TAG, "###########");
			Log.v(TAG, mUvcControls.formattedValues());
			
			commitStreamingControls(mUvcControls.getControls());
			startStreaming();
			
			getStillControls(info);
			for(int i = 0; i < info.length; i++) {
				Log.v(TAG, "still image controls : " + info[i]);
			}
			//info[0] = 0x02;
			setStillControls(info);
			for(int i = 0; i < info.length; i++) {
				Log.v(TAG, "still image controls : " + info[i]);
			}
			info[0] = 0x02;
			commitStillControls(info);
		}
	}	
	
	private void setStreamingControls(byte[] controls) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
			Log.v(TAG, "probe control set");
		}
		else {
			Log.v(TAG, "probe control set failed");
		}
	}
	
	private byte[] getStreamingControls() {
		byte[] controls = new byte[48];
		
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_IN, GET_CUR, PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > - 1) {
			Log.v(TAG, "probe control get");
		}
		else {
			Log.v(TAG, "probe control get failed");
		}
		
		return controls;
	}
	
	private void commitStreamingControls(byte[] controls) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, COMMIT_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
			Log.v(TAG, "commit control set");
		}
		else {
			Log.v(TAG, "commit control set failed");
		}
	}
	
	private void startStreaming() {
		if(mDeviceConnection.controlTransfer(STANDARD_REQUEST_OUT, SET_INTERFACE, ZERO_BANDWIDTH, VIDEO_STREAMING_INTERFACE, null, 0, 0) > -1) {
			Log.v(TAG, "start streaming");
		}
		else {
			Log.v(TAG, "start streaming failed");
		}
	}
	
	private void getStillControls(byte[] controls) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_IN, GET_CUR, STILL_PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
			Log.v(TAG, "still image controls get");
		}
		else {
			Log.v(TAG, "still image controls failed");
		}
	}
	
	private void setStillControls(byte[] controls) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_PROBE_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
			Log.v(TAG, "still image controls set");
		}
		else {
			Log.v(TAG, "still image controls failed");
		}
	}
	
	private void commitStillControls(byte[] controls) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_COMMIT_CONTROL, VIDEO_STREAMING_INTERFACE, controls, controls.length, 0) > -1) {
			Log.v(TAG, "still image controls commit");
		}
		else {
			Log.v(TAG, "still image controls failed");
		}
	}
	
	private void stillImageTrigger(byte[] trigger) {
		if(mDeviceConnection.controlTransfer(CLASS_REQUEST_OUT, SET_CUR, STILL_IMAGE_TRIGGER_CONTROL, VIDEO_STREAMING_INTERFACE, trigger, trigger.length, 0) > -1) {
			Log.v(TAG, "still trigger command set");
		}
		else {
			Log.v(TAG, "still trigger command failed");
		}
	}
	
	private void captureFrame() throws IOException {
		int cnt = 0;
		int hdr_ctr = 0;
		int packet_ctr = 0;
		int stray_ctr = 0;
		byte[] trigger = new byte[1];
		trigger[0] = 0x01;	
		
		int[] arr = new int[500];
	
		//stillImageTrigger(trigger);
		
//		for(int i = 0; i < arr.length; i++) {
//			cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 18000, 0);
//			arr[i] = cnt;
//		}
//		
//		for(int i = 0; i < arr.length; i++) {
//			Log.v(TAG, "cnt["+ i + "] = " + arr[i]);
//		}
		
		while(mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0) == 16384);
		
		for(long i = 0; i < 38; i++) {
			cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
			if(i == 0)
				imgBuf.put(epBuf, 12, cnt - 12);
			else
				imgBuf.put(epBuf, 0, cnt);
		}
		
		if(imageFile == null) {
			imageFile = new File("/storage/extSdCard/yuv_image.yuv");
		}
		
		if(osr == null) {
			try {
				osr = new FileOutputStream(imageFile, false);
				out = new DataOutputStream(osr);
				out.write(imgBuf.array());
				out.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			imgBuf.clear();
			
			out.close();
			osr.close();
			
			out = null;
			osr = null;
			imageFile = null;
		}
		
		Log.v(TAG, "frame captured!");
	}
	
	private void deInit() {
		if(mDeviceConnection != null) {
			if(mControlIntf != null) {
				mDeviceConnection.releaseInterface(mControlIntf);
				mControlIntf = null;
			}

			if(mStreamingIntf != null) {
				mDeviceConnection.releaseInterface(mStreamingIntf);
				mStreamingIntf = null;
			}
			mDeviceConnection.close();
			mDeviceConnection = null;
		}
		
		mDevice = null;	
		isConnected = false;
	}
}
