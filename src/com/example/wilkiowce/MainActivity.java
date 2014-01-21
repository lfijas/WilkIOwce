package com.example.wilkiowce;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothService mBluetoothService = null;
	private String mConnectedDeviceName = null;
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_DEVICE_NAME = 2;
	public static final int MESSAGE_READ = 3;
	public static final int MESSAGE_WRITE = 4;
	public static final int SERVER_DOWN = 5;
	
	public static final String DEVICE_NAME = "device_name";
	
	private int player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		Button serverButton = (Button) findViewById(R.id.serverButton);
		serverButton.setOnClickListener(new OnClickListener() { 			
			@Override
			public void onClick(View v) {
				player = Board.SHEEP;
				mBluetoothService.startServer();
				Button serverBut = (Button) findViewById(R.id.serverButton);
				serverBut.setEnabled(false);
				Button clientBut = (Button) findViewById(R.id.clientButton);
				clientBut.setEnabled(false);
				Button helpBut = (Button) findViewById(R.id.infoButton);
				helpBut.setEnabled(false);
				Toast.makeText(getApplicationContext(), "Serwer bluetooth uruchomiony. Oczekiwanie na połączenie.", Toast.LENGTH_LONG).show();
			}
		});
		
		Button clientButton = (Button) findViewById(R.id.clientButton);
		clientButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				player = Board.WOLF;
				Intent serverIntent = new Intent(MainActivity.this, DiscoverActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				
				//mBluetoothService.connectClient(device);
				//Toast.makeText(getApplicationContext(), "Client clicked!", Toast.LENGTH_LONG).show();
			}
		});
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			return;
		}
		//if (!mBluetoothAdapter.isEnabled()) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);
		//}
	}

	@Override
	public void onStart() {
		super.onStart();
		/*Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);*/
		if (mBluetoothService == null) {
			//mBluetoothService = new BluetoothService(this, mHandler);
			mBluetoothService = BluetoothService.getInstance(this, mHandler);
			}
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mBluetoothService != null) {
			mBluetoothService.stop();
		}
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)  {
		switch(requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		}
	}
	
	private void connectDevice(Intent data) {
		String address = data.getExtras().getString(DiscoverActivity.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothService.connectClient(device);
	}
	
	private final void setStatus(int resourceId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resourceId);
	}
	
	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					setStatus("Połączony z " + mConnectedDeviceName);
					Intent intent = new Intent(MainActivity.this, Board.class);
					Bundle bundle = new Bundle();
					bundle.putInt("player", player);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				case BluetoothService.STATE_CONNECTIG:
					setStatus(R.string.connecting);
				}
			case MESSAGE_DEVICE_NAME:
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Połączono z " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case SERVER_DOWN:
				setStatus("Nieudana próba połącznia z serwerem");
				Toast.makeText(getApplicationContext(), "Nie udało się połączyć z serwerem bluetooth. Upewnij się, że jest on włączony na wybranym urządzeniu i spróbuj ponownie.", Toast.LENGTH_LONG).show();	
				break;
			}
			
		}
	};
}
