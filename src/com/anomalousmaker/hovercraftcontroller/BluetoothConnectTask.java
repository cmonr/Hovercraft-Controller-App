package com.anomalousmaker.hovercraftcontroller;

import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

public class BluetoothConnectTask extends AsyncTask<BluetoothDevice, String, Boolean> {
	// UUID for RFCOMM Bluetooth Connection
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	
	public ControllerUI ui;
	
	
	@Override
	protected void onPreExecute() { }

	@Override
	protected Boolean doInBackground(BluetoothDevice... params) {
		
		BluetoothSocket btSocket;
		try {
			// Create socket
			btSocket = params[0].createRfcommSocketToServiceRecord(MY_UUID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// Make several attempts to connect
		publishProgress("Connecting...");


		for(int i=0; i<3; i++)
		{
			try {
				btSocket.connect();

				// Setup the Output Stream
				OutputStream btOutputStream = btSocket.getOutputStream();
				ui.robot = new Hovercraft(btOutputStream);
				return true;

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (i == 1)
					publishProgress("Connecting... (2nd Attempt)");
				else if (i == 2)
					publishProgress("Connecting... (3rd Attempt)");
			}
		}
		return false;
	}

	@Override protected void onProgressUpdate(String... status) {
		super.onProgressUpdate(status);

		ui.setStatus(status[0]);
	}

	@Override protected void onPostExecute(Boolean connected) {
		super.onPostExecute(connected);

		if (connected) {
			ui.setStatus("Connected");
		}else{
			ui.setStatus("Disconnected");
		}
		
		ui.enableUI(connected);

	}
}
