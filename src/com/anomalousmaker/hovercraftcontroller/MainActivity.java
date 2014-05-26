package com.anomalousmaker.hovercraftcontroller;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {
	// UUID for RFCOMM Bluetooth Connection
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

	private ControllerUI ui;
	
	private static Set<BluetoothDevice> btPairedDevices;
	private static BluetoothDevice btDevice;
	private static BluetoothSocket btSocket;
	private static OutputStream btOutputStream;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new ControllerFragment()).commit();
		}
		
		ui = (ControllerUI) findViewById(R.id.controllerUI);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
		case R.id.menu_item_connect:

			// Enable Bluetooth
			//if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false)


			// Get list of paired Bluetooth Devices
			ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

			btPairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

			for (BluetoothDevice device : btPairedDevices) {
				btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}


			// Build Alert Dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Connect to:");

			builder.setAdapter(btArrayAdapter, new OnClickListener() {
				@SuppressWarnings("static-access")
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Set target device to connect to
					btDevice = (BluetoothDevice)(btPairedDevices.toArray())[which];

					new AsyncTask<Object, String, Boolean>(){
						@Override
						protected Boolean doInBackground(Object... params) {

							try {
								// Create socket
								btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
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
									btOutputStream = btSocket.getOutputStream();
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
							ui.connection_status = status[0];

							ui.invalidate();
							ui.requestLayout();
						}

						@Override protected void onPostExecute(Boolean connected) {
							super.onPostExecute(connected);
							ui.enableUI(connected);

							if (connected) {
								// TODO: Change Menu Item from -Connect- to -Disconnect-
								
								// TODO: Create/enable BT Serial Interface/Communications
							}
						}

					}.execute();

					dialog.dismiss();		
				}
			});

			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			builder.setCancelable(true);

			builder.create().show();

			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
