package com.anomalousmaker.hovercraftcontroller;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 1;
	
	private BluetoothConnectTask btConnectTask;

	private Set<BluetoothDevice> btPairedDevices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			// Not resuming, aka onLaunch
			getFragmentManager().beginTransaction().add(R.id.fragmentContainer, new ControllerFragment(), "ControllerUI").commit();
		}

		// Enable Bluetooth
		enableBluetooth();
		
		// Task to connect to Bluetooth device
		btConnectTask = new BluetoothConnectTask();
	}

	public void enableBluetooth()
	{
		// Make sure Bluetooth is supported
		if (BluetoothAdapter.getDefaultAdapter() == null)
		{
			exit("Bluetooth Adapter not found");
		}


		// Make sure Bluetooth is enabled
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false)
		{
			// Bluetooth enable dialog
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode)
		{
		case REQUEST_ENABLE_BT:	// Enable Bluetooth request
			if (resultCode == RESULT_CANCELED)
				// Request to enable Bluetooth denied
				//  Quitting due to rejection
				exit("Could not enable Bluetooth");
			break;
		}
	}
	
	public void createBluetoothDialog()
	{
		// Get list of paired devices
		btPairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		
		// Populate List View
		ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (BluetoothDevice device : btPairedDevices) {
			btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		}


		// Build Alert Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Connect to:");

		builder.setAdapter(btArrayAdapter, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Make sure no active connect task is active
				btConnectTask.cancel(true);
				
				// Create new task
				btConnectTask = new BluetoothConnectTask();
				btConnectTask.ui = (ControllerUI) getFragmentManager().findFragmentByTag("ControllerUI").getView();
				
				// Execute connection
				btConnectTask.execute((BluetoothDevice)(btPairedDevices.toArray())[which]);
				
				// Remove the dialog
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
		case R.id.menu_item_connect:
			createBluetoothDialog();

			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		enableBluetooth();
		
		return true;
	}



	public void exit(String msg)
	{
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		finish();
	}
}
