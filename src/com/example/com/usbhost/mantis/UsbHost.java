package com.example.com.usbhost.mantis;

import java.io.ByteArrayOutputStream;
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
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.util.ByteArrayBuffer;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class UsbHost extends Activity implements OnClickListener{

	private static final String TAG = "Video Device";
	private static final String ACTION_USB_PERMISSION = "com.usbhost.mantis.USB_PERMISSION";
	private static boolean isPermitted = false;
	private static boolean isConnected = false;
	
	private static final int BITS_PER_PIXEL = 2;
	
	private static final int IMAGE_WIDTH = 352;
	private static final int IMAGE_HEIGHT = 288;
	private static final int IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * BITS_PER_PIXEL;
	
	private static final int PAYLOAD_SIZE = 16384;
	private static final int PAYLOAD_COUNT = 3;
	private static final int FRAME_BUFFER_SIZE = PAYLOAD_SIZE * PAYLOAD_COUNT;
	
	private File imageFile1 = null;
	private File imageFile2 = null;
	private FileOutputStream osr = null;
	private DataOutputStream out = null;
	
	private byte[] streamingControls = new byte[48];
	
	private BlockingQueue<byte[]> fifoBuffer;
	
	ByteArrayBuffer rawBuf;
	ByteBuffer bBuffer;
	
	byte[] epBuf = new byte[20000];
	
	ByteArrayOutputStream imageStream;
	YuvImage yuvImage;
	
	FrameGrabberThread frameGrabber;
	
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
	
	UvcControls mUvcControls;;
	
	String mDeviceName;
	
	PendingIntent mPermissionIntent;
	
	RenderingView renderingView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_host);
		
		bBuffer = ByteBuffer.allocate(16384);
		rawBuf = new ByteArrayBuffer(IMAGE_SIZE);
		fifoBuffer = new ArrayBlockingQueue<byte[]>(3);
		
		frameGrabber = new FrameGrabberThread();
		renderingView = new RenderingView(this);
		
		bScan = (Button) findViewById(R.id.scan);
		bConnect = (Button) findViewById(R.id.connect);
		bCaptureFrame = (Button) findViewById(R.id.capture_frame);
		
		bScan.setOnClickListener(this);
		bConnect.setOnClickListener(this);
		bCaptureFrame.setOnClickListener(this);
		
		status = (TextView) findViewById(R.id.deviceName);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(renderingView);
		
	    mManager = (UsbManager)getSystemService(Context.USB_SERVICE);
	    mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
	    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	    registerReceiver(mUsbReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		deInit();
		frameGrabber.mStop = true;
		
		unregisterReceiver(mUsbReceiver);
		
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		deInit();
		frameGrabber.mStop = true;
		renderingView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(renderingView != null)
			renderingView.onResume();
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
			captureFrame();
			break;
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
					mUvcControls = new UvcControls(mDeviceConnection);
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
	
		mUsbRequest = new UsbRequest();
		mUsbRequest.initialize(mDeviceConnection, mBulkEpIn);
		
		if(isConnected) {
			initUvc();
		}
	}	
	
	private void initUvc() {
		
		Log.v(TAG, "scanning mode = " + mUvcControls.getScanMode());
		
		if(!mUvcControls.probeControl(UvcConstants.GET_CUR)) {
			Log.e(TAG, "error in probe control [GET_CUR]");
		}
		
		Log.v(TAG, "[1]control values = " + mUvcControls.formattedValues());
		
		mUvcControls.setDefault();
		
		mUvcControls.setFrameIndex(2);
		
		if(!mUvcControls.probeControl(UvcConstants.SET_CUR)) {
			Log.e(TAG, "error in probe control [SET_CUR]");
		}
		
		if(!mUvcControls.probeControl(UvcConstants.GET_CUR)) {
			Log.e(TAG, "error in probe control [GET_CUR]");
		}
		
		Log.v(TAG, "[2]control values = " + mUvcControls.formattedValues());
		
		if(!mUvcControls.commitControl(UvcConstants.SET_CUR)) {
			Log.e(TAG, "error in commit control");
		}
		
		if(!mUvcControls.startStreaming()) {
			Log.e(TAG, "error starting streaming");
		}
	}
	
	private void captureFrame() {
		int cnt = 0;
		int old_cnt = 0;
		int hdr_ctr = 0;
		int packet_ctr = 0;
		int stray_ctr = 0;
		byte[] trigger = new byte[1];
		int errorCode = 0;
		trigger[0] = 0x01;	
		
		int[] arr = new int[500];
		
		String strace = "";
		
//		int i = 0;
//		
//		while(i < 38) {
//			cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
//			
//			if(i == 0) {
//				if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
//					rawBuf.append(epBuf, 12, cnt - 12);
//				}
//				else {
//					continue;
//				}
//			}
//			else {
//				rawBuf.append(epBuf, 0, cnt);
//			}
//			
//			i++;
//		}
//		
//		Log.v(TAG, "frame captured!");
//		
//		imageStream = new ByteArrayOutputStream();
//		yuvImage = new YuvImage(rawBuf.buffer(), ImageFormat.YUY2, IMAGE_WIDTH, IMAGE_HEIGHT, null);
//		yuvImage.compressToJpeg(new Rect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT), 90, imageStream);
//		
//		if(imageFile1 == null) {
//			imageFile1 = new File("/storage/extSdCard/image.jpeg");
//		}
//		
//		if(osr == null) {
//			try {
//				osr = new FileOutputStream(imageFile1, false);
//				out = new DataOutputStream(osr);
//				out.write(imageStream.toByteArray());
//				out.flush();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//			rawBuf.clear();
//			
//			out.close();
//			osr.close();
//			
//			out = null;
//			osr = null;
//			imageFile1 = null;
//		}
//		
//		imageStream.flush();
//		imageStream.reset();
//		
//		Log.v(TAG, "converted to JPEG!");
		
		frameGrabber.start();
	}
	
	private class FrameGrabberThread extends Thread {
		public boolean mStop = false;
		boolean complete = false;
		byte[] frameBuf = new byte[IMAGE_SIZE];
		int i = 0;
		int cnt = 0;
		long start = 0;
		long end = 0;
		long diff = 0;
		
		public void run() {
			Log.v(TAG, "started framegrabber thread");
			while(!mStop) {
				i = 0;
				
//				do{
//					mUsbRequest.queue(bBuffer, 16384);
//					mDeviceConnection.requestWait();
//					if(bBuffer.get(0) == (byte)0x0c && (bBuffer.get(1) == (byte)0x8e)) {
//						start = System.nanoTime();
//						do {
//							i++;
//							mUsbRequest.queue(bBuffer, 16384);
//							mDeviceConnection.requestWait();
//							if(bBuffer.get(0) == (byte)0x0c && (bBuffer.get(1) == (byte)0x8f)) {
//								break;
//							}
//						}while(true);
//						end = System.nanoTime();
//					
//					}
//				}while(i == 0);	
				
				//this only happens during the beginning of the thread
				if(cnt == 0) {
					do {
						cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
						if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
							break;
						}
					}while(true);
				}

				//new frame! strip off the first 12 bytes
				rawBuf.append(epBuf, 12, cnt -12);
				
				do {
					cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
					if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
						System.arraycopy(rawBuf.buffer(), 0, frameBuf, 0, IMAGE_SIZE);
						try {
							fifoBuffer.put(frameBuf);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						rawBuf.clear();
						break;
					}
					else {
						rawBuf.append(epBuf, 0, cnt);
					}
				}while(true);
				
				
//				do{
//					cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
//					if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
//						rawBuf.append(epBuf, 12, cnt - 12);
//						do {
//							i++;
//							cnt = mDeviceConnection.bulkTransfer(mBulkEpIn, epBuf, 16384, 0);
//							if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
//								break;
//							}
//						}while(true);
//					}
//				}while(i == 0);				
				
//				if(i == 0) {
//					if(epBuf[0] == (byte)0x0c && (epBuf[1] == (byte)0x8e || epBuf[1] == (byte)0x8f)) {
//						rawBuf.append(epBuf, 12, cnt - 12);
//					}
//					else {
//						continue;
//					}
//				}
//				else {
//					rawBuf.append(epBuf, 0, cnt);
//				}
//					
//					i++;
				
				//					fifoBuffer.put(rawBuf.toByteArray());
//				rawBuf.clear();
			}
		}
	}
	
	class RenderingView extends SurfaceView implements Runnable {
		
		Thread renderer = null;
		SurfaceHolder surfaceHolder;
		Canvas canvas;
		byte[] yuvData;
		YuvImage yuvImage;
		Bitmap bitmap;
		ByteArrayOutputStream imgStream = new ByteArrayOutputStream();
		volatile boolean running = false;
		
		private Rect rect = new Rect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		public RenderingView(Context context) {
			super(context);
			surfaceHolder = getHolder();
		}
		
		public void onResume() {
			running = true;
			renderer = new Thread(this);
			renderer.start();
			Log.v(TAG, "started renderer thread");
		}
		
		public void onPause() {
			boolean retry = true;
			running = false;
			while(retry) {
				try {
					renderer.join();
					retry = false;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void run() {
			while(running) {
				try {
					yuvData = fifoBuffer.take();
					yuvImage = new YuvImage(yuvData, ImageFormat.YUY2, IMAGE_WIDTH, IMAGE_HEIGHT, null);
					yuvImage.compressToJpeg(rect, 50, imgStream);
					bitmap = BitmapFactory.decodeByteArray(imgStream.toByteArray(), 0, imgStream.toByteArray().length);
					imgStream.reset();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(surfaceHolder.getSurface().isValid() && bitmap != null) {
					canvas = surfaceHolder.lockCanvas();
					canvas.drawBitmap(bitmap, 0, 0, paint);
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
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
