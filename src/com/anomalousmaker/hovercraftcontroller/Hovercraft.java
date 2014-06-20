package com.anomalousmaker.hovercraftcontroller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import android.os.Handler;

public class Hovercraft {
	private final int CENTER = 0;
	private final int LEFT   = 1;
	private final int RIGHT  = 2;
	private final int SERVO  = 3;
	
	private OutputStream btOutStream;
	private Handler cmdQueueHandler;
	private Runnable cmdQueueWorker;
	
	private DecimalFormat floatFormat;

	private String lastCmd[];
	

	public Hovercraft(OutputStream os)
	{
		// Init Bluetooth OutputStream
		btOutStream = os;
		
		lastCmd = new String[4];
		
		floatFormat = new DecimalFormat("0.00");
	}
	
	public void write(String str)
	{	
		try {
			btOutStream.write(str.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			
			// TODO: Bluetooth has disconnected
		}
	}
	
	public void enableAll()
	{
		// Set EDF limits
		// TODO: Fix this in FW
		//write("s0l=0.25,0.35;");
		//write("s1l=0.25,0.35;");
		//write("s2l=0.25,0.35;");
		
		// Set EDF values
		//  (Start at 0)
		setHover(0);
		setThrust(0);
		setServo(0.5f);
		
		// Enable EDFs + Servo
		write("s0e;");
		write("s1e;");
		write("s2e;");
		write("s3e;");
	}
	
	public void disableAll()
	{
		// Disable EDFs + Servo
		write("s0d;");
		write("s1d;");
		write("s2d;");
		write("s3d;");
		
		cmdQueueHandler.removeCallbacks(cmdQueueWorker);
	}
	
	private void setEDF(float value, int edf)
	{
		String cmd;
		
		// Workaround:
		//  Value ranges from 0-1.
		//  Math it such that it ranges from 0.25 to 0.7
		value = value * 0.45f + 0.25f;
		cmd = "s" + String.valueOf(edf) + "=" + floatFormat.format(value) + ";";
		
		if (cmd != lastCmd[edf])
		{
			write(cmd);
			lastCmd[edf] = cmd;
		}
	}
	
	public void setHover(float value)
	{
		setEDF(value, CENTER);
	}
	
	public void setThrust(float value)
	{
		setEDF(value, RIGHT);
		setEDF(value, LEFT);
	}
	
	public void setServo(float angle)
	{

		String cmd;
		
		// Invert servo direction
		angle = 1.0f - angle;
		
		// Workaround:
		//  Value ranges from 0-1.
		//  Math it such that it ranges from 0% - 75%
		angle = angle * 0.75f;
		cmd = "s" + String.valueOf(SERVO) + "=" + floatFormat.format(angle) + ";";
		
		if (cmd != lastCmd[SERVO])
		{
			write(cmd);
			lastCmd[SERVO] = cmd;
		}
	}
}
