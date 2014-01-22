package com.example.wilkiowce;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DiscoverActivity extends Activity {
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doDiscovery();
				v.setVisibility(View.GONE);
				
			}
		});
		
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		
		//Paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
		if (pairedDevices.size() > 0) {	
			for(BluetoothDevice device : pairedDevices) {
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		else {
			String noPairedDevice = getResources().getText(R.string.no_paired_devices).toString();
			mPairedDevicesArrayAdapter.add(noPairedDevice);
			
		}
		
		//Search new available devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);
		
		IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mBroadcastReceiver, intentFilter);
		
		intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mBroadcastReceiver, intentFilter);
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		this.unregisterReceiver(mBroadcastReceiver);
	}
	
	//Scanning in order to find new devices
	private void doDiscovery() {
		setProgressBarIndeterminate(true);
		//setTitle(R.string.scanning);
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.startDiscovery();
	}
	
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener()  {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			mBluetoothAdapter.cancelDiscovery();
			
			String info = ((TextView) arg1).getText().toString();
			String address = info.substring(info.length() - 17);
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			
			setResult(Activity.RESULT_OK, intent);
			finish();
			
		}
		
	};
	
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					String deviceName = device.getName() + "\n" + device.getAddress();
					for (int i = 0; i < mNewDevicesArrayAdapter.getCount(); i++) {
						if (mNewDevicesArrayAdapter.getItem(i).equals(deviceName)) {
							return;
						}
					}
					mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
				else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					setProgressBarIndeterminateVisibility(false);
					//setTitle(R.string.found_devices);
					if (mNewDevicesArrayAdapter.getCount() == 0) {
						String noDevicesFound = getResources().getText(R.string.no_devices_found).toString();
						mNewDevicesArrayAdapter.add(noDevicesFound);
					}
				}
			}
			
		}
	};
	
}
