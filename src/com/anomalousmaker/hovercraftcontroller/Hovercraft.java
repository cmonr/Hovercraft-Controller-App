package com.anomalousmaker.hovercraftcontroller;

import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;

public class Hovercraft {
	private final int CENTER = 0;
	private final int LEFT   = 1;
	private final int RIGHT  = 2;
	
	private OutputStream btOutStream;
	private Handler cmdQueueHandler;
	private Runnable cmdQueueWorker;
	
	private DecimalFormat floatFormat;

	private String lastCmd[];
	

	public Hovercraft(OutputStream os)
	{
		// Init Bluetooth OutputStream
		btOutStream = os;
		
		lastCmd = new String[3];
		
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
		setLeft(0);
		setCenter(0);
		setRight(0);
		
		// Enable EDFs
		write("s0e;");
		write("s1e;");
		write("s2e;");
	}
	
	public void disableAll()
	{
		// Disable EDFs
		write("s0d;");
		write("s1d;");
		write("s2d;");
		
		cmdQueueHandler.removeCallbacks(cmdQueueWorker);
	}
	
	private void setEDF(float value, int edf)
	{
		String cmd;
		
		// Workaround:
		//  Value ranges from 0-1.
		//  Math it such that it ranges from 0.25 to 0.35
		value = value * 0.2f + 0.25f;
		cmd = "s" + String.valueOf(edf) + "=" + floatFormat.format(value) + ";";
		
		if (cmd != lastCmd[edf])
		{
			write(cmd);
			lastCmd[edf] = cmd;
		}
		
	}
	
	public void setCenter(float value)
	{
		setEDF(value, CENTER);
	}
	
	public void setLeft(float value)
	{
		setEDF(value, LEFT);
	}
	
	public void setRight(float value)
	{
		setEDF(value, RIGHT);
			
	}
}
