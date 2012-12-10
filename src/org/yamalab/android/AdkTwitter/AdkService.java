package org.yamalab.android.AdkTwitter;
 
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AdkService extends Service {

	AdkThread demoKitActivity;
	private static final String TAG = "AdkService";

    public static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	public FileInputStream mInputStream;
	public FileOutputStream mOutputStream;
	Intent callerIntent;
	AdkThread thread;
	boolean mBound;
//	PukiWikiConnectorService connectorService;
	public long readCommandInterval=0; // default 10 min.
	public long sendResultInterval=0; // default 10 min.
	String pageName="";
	
	@Override
	public void onStart(Intent intent, int startId) {
		callerIntent=intent;
		Log.d(TAG,"onStart start");
	}

    /** For showing and hiding our notification. */ 
  	NotificationManager mNM;
  	/** Keeps track of all current registered clients. */
  	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
  	/** Holds last value set by a client. */
  	int mValue = 0;
  	/*
  	 * Command to the service to register a client, receiving callbacks
  	 * from the service.  The Message's replyTo field must be a Messenger of
  	 * the client where callbacks should be sent.      
  	 */
  	static final int MSG_REGISTER_CLIENT = 1;
  	/*
  	 * Command to the service to unregister a client, ot stop receiving callbacks
  	 * from the service.  The Message's replyTo field must be a Messenger of
  	 * the client as previously given with MSG_REGISTER_CLIENT.
     */
  	static final int MSG_UNREGISTER_CLIENT = 2;
  	/*
  	 * Command to service to set a new value.  This can be sent to the
  	 * service to supply a new value, and will be sent by the service to
  	 * any registered clients with the new value.
  	 */
  	static final int MSG_SET_VALUE = 3;
  	/* stop */
  	static final int MSG_STOP = 4;
  	/* file descriptor */
  	static final int MSG_SET_USBFILEDESCRIPTOR = 5;
  	/* commands */
  	static final int MSG_EXEC_COMMAND = 6;
  	
  	/**     * Handler of incoming messages from clients.     */
  	class IncomingHandler extends Handler {
  		@Override
  		public void handleMessage(Message msg) {
  			switch (msg.what) {
  			case MSG_REGISTER_CLIENT:
  				Log.d(TAG, "handleMessage-MSG_REGISTER_CLIENT");
  				mClients.add(msg.replyTo);
  				break;
  			case MSG_UNREGISTER_CLIENT:
  				Log.d(TAG, "handleMessage-MSG_UNREGISTER_CLIENT");
  				mClients.remove(msg.replyTo);
  				break;
  			case MSG_SET_VALUE:
  				Log.d(TAG, "handleMessage-MSG_SET_VALUE");
  				mValue = msg.arg1;
  				for (int i=mClients.size()-1; i>=0; i--) {
  					try {
  						mClients.get(i).send(Message.obtain(null,
  								MSG_SET_VALUE, mValue, 0));
  					}
  					catch (RemoteException e) {
  							// The client is dead.  Remove it from the list;
  							// we are going through the list from back to front
  							// so this is safe to do inside the loop.
  							mClients.remove(i);
  					}
  				}
  				break;
  			case MSG_SET_USBFILEDESCRIPTOR:
  				Log.d(TAG, "handleMessage-MSG_SET_USBFILEDESCRIPTOR");
  				mFileDescriptor=(ParcelFileDescriptor)(msg.obj);
  				if(mFileDescriptor!=null){
  	      		    FileDescriptor fd = mFileDescriptor.getFileDescriptor();
  	      		    mInputStream = new FileInputStream(fd);
  	      		    mOutputStream = new FileOutputStream(fd);
  				}
  	      		startThread();
  				break;
  			case MSG_STOP:
  				Log.d(TAG, "handleMessage-MSG_REGISTER_CLIENTonCreate");
  				onDestroy();
  				break;
  			case MSG_EXEC_COMMAND:
  				Log.d(TAG, "handleMessage-MSG_EXEC_COMMAND");
  				StringMsg cmd=(StringMsg)(msg.obj);
  				parseCommand(cmd);
  			default:
  				super.handleMessage(msg);
  			}
  		}
  	}
  	public void sendMessage(Message m){
  		if(mClients==null) return;
		if(!isBound()) return;
		for (int i=mClients.size()-1; i>=0; i--) {
			try {
				mClients.get(i).send(m);
			}
			catch (RemoteException e) {
					// The client is dead.  Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
			}
		}  		
  	}

	private static final String mActivityName = AdkTwitterActivity.class.getCanonicalName(); 
	public boolean isActivityRunning() {
		Log.d(TAG, "isActivityRunning");
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningApp = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : runningApp) {
			if (mActivityName.equals(info.service.getClassName())) {
				Log.d(TAG, "isActivityRunning ... true");
				return true;
			}
		}
		Log.d(TAG, "isActivityRunning ... false");
		return false;
	}
  	/*
  	 * Target we publish for clients to send messages to IncomingHandler.
  	 */
  	final Messenger mMessenger = new Messenger(new IncomingHandler());
  	@Override
  	public void onCreate() {
		Log.d(TAG, "onCreate");
  		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  		// Display a notification about us starting.
  		showNotification();
//  		connectUsb();
//        connectorService=new PukiWikiConnectorService(this);
  		this.thread=null;
  	}
  	@Override
  	public void onDestroy() {
  		// Cancel the persistent notification.
		Log.d(TAG, "onDestroy");
  		this.mBound=false;
  		if(this.thread!=null){
  			this.thread.stop();
  		}
  		if(this.mInputStream!=null){
  			try{
  			mInputStream.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mInputStream.close error:"+e.toString());
  			}
  		}
  		if(this.mOutputStream!=null){
  			try{
  			mOutputStream.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mOutputStream.close error:"+e.toString());
  			}
  		}
  		if(this.mFileDescriptor!=null){
  			try{
  				mFileDescriptor.close();
  			}
  			catch(Exception e){
  				Log.d(TAG,"onDestroy-mFileDescriptor.close error:"+e.toString());  				
  			}
  		}
//  		this.closeAccessory();
  		mNM.cancel(0);
  		// Tell the user we stopped.
  		Toast.makeText(this, "remote_service_stopped",	Toast.LENGTH_SHORT).show();
		Toast.makeText(this, "Service has been terminated.", Toast.LENGTH_SHORT).show();
//		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
  	}

  	/*
  	 * When binding to the service, we return an interface to our messenger
  	 * for sending messages to the service.    
  	 */
  	@Override
  	public IBinder onBind(Intent intent) {
  		this.mBound=true;
		callerIntent=intent;
		Log.d(TAG,"onBind");
		/*
		if(this.mFileDescriptor==null){
			this.connectUsb();
		}
		*/
//        this.startThread();
  		return mMessenger.getBinder();
  	}
  	/*
  	 * Show a notification while this service is running.
  	 */
  	private void showNotification() {
  		// In this sample, we'll use the same text for the ticker and the expanded notification
  		//CharSequence text = getText(R.string.remote_service_started);
  		CharSequence text = "remote_service_started";
  		// Set the icon, scrolling text and timestamp
//  		Notification notification = new Notification(R.drawable.stat_sample, text,
//  		System.currentTimeMillis());
  		Notification notification = new Notification(R.drawable.indicator_button1_off_noglow, text,
  		System.currentTimeMillis());
  		// The PendingIntent to launch our activity if the user selects this notification
  		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
  				new Intent(this, AdkTwitterActivity.class), 0);
  		// Set the info for the views that show in the notification panel.
  		notification.setLatestEventInfo(this, "AdkService",
  				text, contentIntent);
  		// Send the notification.
  		// We use a string id because it is a unique number.  We use it later to cancel.
  		mNM.notify(0, notification);
  	}
  	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		Log.d(TAG, "onUnbind");
		this.mBound=false;
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
		/*
		if(this.mFileDescriptor==null){
			this.connectUsb();
		}
		*/
//		this.startThread();
		this.mBound=true;
	}
	
	public boolean isBound(){
		return this.mBound;
	}

	public void startThread(){
		Log.d(TAG, "startThread");
		this.sendCommandToActivity("activity request setting", "");
  		if(thread==null){
   		    Log.d(TAG,"onBind-new thread");

	        thread = new AdkThread(this); //(this)
       		Log.d(TAG,"onBind-start AdkThread");
 		    thread.start();
       	  
  		}
	}

	protected boolean parseCommand(StringMsg m){
		if(m==null) return false;
		String x=m.getCommand();
		Log.d(TAG, "parseCommand("+x+")");
		String cmd=Util.skipSpace(x);
		String [] rest=new String[1];
		String [] match=new String[1];
		int [] intv = new int[1];
		if(Util.parseKeyWord(cmd,"connector ",rest)){
		    String subcmd=Util.skipSpace(rest[0]);
		    /*
			if (connectorService != null) {
				return connectorService.parseCommand(subcmd, m.getValue());
			}		    	
			*/
		}
		else
		if(Util.parseKeyWord(cmd,"adk ",rest)){
			String subcmd=Util.skipSpace(rest[0]);
			if(subcmd.startsWith("#")) return true;
			if(subcmd.startsWith("command:")){
				String command=subcmd.substring("command:".length());
				command=Util.skipSpace(command);
				boolean rtn=parseInputCommand(command);
				return rtn;
			}
		}
		return false;
	}

	public void sendCommandToActivity(String c, String v) {
		// TODO Auto-generated method stub
		Log.d(TAG,"sendCommandToActivity("+c+","+v+")");
		StringMsg mx=new StringMsg(c,v); 		
		Message msg = Message.obtain(null,AdkThread.MESSAGE_STRING,mx);
		sendMessage(msg);					
	}	
	public void outputDevice(byte command, byte target, int value) {
		Log.d(TAG,"outputDevice("+command+"-"+target+","+value+")");
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	String saveText="";
	int uploadInterval=10;
    int uploadNumber;	
    Vector<String> reports=new Vector();
    int reportQueueLength=120;
	public void putSendBuffer(String x){
		Log.d(TAG,"putSendBuffer("+x+")");
		this.sendCommandToActivity("activity append output", x);
//		mPirOutputView.append(line);
		reports.add(x);
		this.sendCommandToActivity("connector setMessage", "");
//		mMessageView.setText("");
		uploadNumber++;
	}

	boolean parseInputCommand(String line){
		String [] rest=new String[1];
		String cmd=Util.skipSpace(line);
		Log.d(TAG,"parseInputCommand-"+cmd);
		if(Util.parseKeyWord(cmd,"get ",rest)){
//			return this.parseGetCommand(rest[0]);
		}
		else
		if(Util.parseKeyWord(cmd,"set ",rest)){
			return this.parseSetCommand(rest[0]);
		}
		return false;
	}

    char[] devKind=new char[]{'a','d'};
	boolean parseSetCommand(String x){
		String [] rest=new String[1];
		int[] intv1=new int[1];
		int[] intv2=new int[1];
		char[] chrtn=new char[1];
		String cmd=Util.skipSpace(x);
		Log.d(TAG,"parseSetCommand-"+cmd);
		if(Util.parseKeyWord(cmd,"out-",rest)){
			String v1=rest[0];
			if(!Util.parseChar(v1, devKind, chrtn, rest)) return false;
			char dc=chrtn[0]; // a (analog) or d (digital)
			String s2=rest[0];
			if(!Util.parseKeyWord(s2, "-", rest)) return false;
			s2=rest[0];
			if(!Util.parseInt(s2, intv1, rest)) return false;
			int port=intv1[0];
			v1=Util.skipSpace(rest[0]);
			if(!Util.parseKeyWord(v1, "=", rest)) return false;
			v1=Util.skipSpace(rest[0]);
			if(!Util.parseInt(v1, intv2, rest)) return false;
			this.outputDevice((byte)dc,(byte)port,(byte)(intv2[0]));
			this.sendCommandToActivity("activity set device "+dc+" "+port+" "+intv2[0], "");
			return true;
		}
		else
		if(Util.parseKeyWord(cmd,"xout-",rest)){
			String v1=rest[0];
			if(!Util.parseChar(v1, devKind, chrtn, rest)) return false;
			char dc=chrtn[0]; // a (analog) or d (digital)
			String s2=rest[0];
			if(!Util.parseKeyWord(s2, "-", rest)) return false;
			s2=rest[0];
			if(!Util.parseInt(s2, intv1, rest)) return false;
			int port=intv1[0];
			v1=Util.skipSpace(rest[0]);
			if(!Util.parseKeyWord(v1, "=", rest)) return false;
			v1=Util.skipSpace(rest[0]);
			if(!Util.parseInt(v1, intv2, rest)) return false;
			this.outputDevice((byte)dc,(byte)port,(byte)(intv2[0]));
			return true;
		}

		return false;
	}

  }
