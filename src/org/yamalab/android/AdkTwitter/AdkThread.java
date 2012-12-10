/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yamalab.android.AdkTwitter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;

public class AdkThread implements Runnable {
	private static final String TAG = "AdkThread";

	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

	public static final int MESSAGE_DIGITAL = 1;
	public static final int MESSAGE_ANALOG = 2;
	public static final int MESSAGE_CONNECT=6;
	public static final int MESSAGE_STRING=7;

	public Properties setting;
	private AdkService adkService;
	private ArduinoProcessor arduinoProcessor;
	
	public int[] analogVals=new int[16];
	public byte digitalVals;
	
	protected class DigitalMsg {
		private byte state;

		public DigitalMsg(byte s) {
			this.state = s;
		}

		public byte getState() {
			return state;
		}

	}

	protected class AnalogMsg {
		private int port;
		private int val;

		public AnalogMsg(int p, int v) {
			this.port=p;
			this.val = v;
		}

		public int getVal() {
			return val;
		}
		public int getPort(){
			return port;
		}
	}
	
	public AdkThread(AdkService s){
		Log.d(TAG,"AdkThread");
		this.adkService=s;
        this.mInputStream=s.mInputStream;
        this.mOutputStream=s.mOutputStream;
        this.arduinoProcessor=new ArduinoProcessor(s);
        
/* ... */		
	}
	Thread me=null;
	public void start(){
		Log.d(TAG,"start");
	    lastReadTime=(new Date()).getTime();
	    lastSendTime=lastReadTime;
		if(me==null){
			me=new Thread(this,"adkThread");
			me.start();
		}
	}
	public void stop(){
		Log.d(TAG,"stop");
		if(me!=null){
			me=null;
		}
	}
	

	protected class LightMsg {
		private int light;

		public LightMsg(int light) {
			this.light = light;
		}

		public int getLight() {
			return light;
		}
	}

	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}
    public void sendMessage(Message m){
    	if(this.adkService!=null){
    	    adkService.sendMessage(m);
    	}
    }
    public int currentPir;
    public int currentLight;
    public int currentTemp;
    public int currentJoy0, currentJoy1;
    public int currentSwitch0, currentSwitch1;
    private long lastReadTime=0;
    private long lastSendTime=0;
    public void wait(int t){
		try{
		    Thread.sleep((long)t);
		}
		catch(Exception e){
			
		}
    	
    }
    public void test(){// PC debugging
		Message m = Message.obtain(null, MESSAGE_ANALOG);
		int port=0;
		int val=50;
		m.obj = new AnalogMsg(port,val);
		this.analogVals[port]=val;
		sendMessage(m);
		this.arduinoProcessor.processAnalogInput(port, val);
		wait(5000);
		m = Message.obtain(null, MESSAGE_ANALOG);
		port=0;
		val=0;
		m.obj = new AnalogMsg(port,val);
		this.analogVals[port]=val;
		sendMessage(m);
		this.arduinoProcessor.processAnalogInput(port, val);
		wait(5000);		
    }
	public void run() {
		Log.d(TAG,"run");
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
		int t=0;

		while (ret >= 0) {
			/* */
			if(me==null) return;
			if(mInputStream==null) { // test
				if((t % 50)==0){
					Log.d(TAG,"run ...t="+t);
					this.test();
				}
				t++;
				wait(100);
				continue;
			}
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}
/* */
			i = 0;
			if(ret<0){
			  Log.d(TAG,"run ret<0, ret="+ret);
			}
			while (i < ret) {
				int len = ret - i;
/* */
				switch (buffer[i]) {
				case 'a': // analog input
					if (len >= 4) {
						Message m = Message.obtain(null, MESSAGE_ANALOG);
						int port=buffer[i+1];
						int val=composeInt(buffer[i+2],buffer[i+3]);
						m.obj = new AnalogMsg(port,val);
						this.analogVals[port]=val;
						sendMessage(m);
						this.arduinoProcessor.processAnalogInput(port, val);
					}
					i += 4;
					break;

				case 'd': // digital input
					if (len >= 4) {
						Message m1 = Message.obtain(null,
								MESSAGE_DIGITAL);
						byte dd=buffer[i+1];
						m1.obj = new DigitalMsg(dd);
						this.digitalVals=dd;
						sendMessage(m1);
						this.arduinoProcessor.processDigitalInput(dd);
					}
					i += 4;
					break;

				default:
					Log.d(TAG, "unknown msg: " + buffer[i]);
					i = len;
					break;
				}
	/*			*/
			}

		}
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d(TAG,"onStoptrackingTouch start");

	}

}
