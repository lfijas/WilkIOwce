package com.example.wilkiowce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class BluetoothService {
	
	private static final String NAME = "com.example.wilkiowce";
	private static final UUID MY_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
	private final BluetoothAdapter mBluetoothAdapter;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private final Handler mHandler;
	private Context mContext;
	private int mState;
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTIG = 2;
	public static final int STATE_CONNECTED = 3;
	
	private Board mBoard;
	
	private static BluetoothService _instance = null;
	
	public static BluetoothService getInstance(Context context, Handler handler) {
		if (_instance == null) {
			_instance = new BluetoothService(context, handler);
		}
		return _instance;
	}
	
	private BluetoothService(Context context, Handler handler) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		mState = STATE_NONE;
		mContext = context;
	}
	
	public void setBoard(Board board) {
		mBoard = board;
	}
	
	private synchronized void setState(int state) {
		mState = state;
		mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}
	
	public synchronized int getState() {
		return mState;
	}
	
	public synchronized void startServer() {
		
		setState(STATE_LISTEN);
		
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}
	
	public synchronized void connectClient(BluetoothDevice device) {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTIG);
	}
	
	public synchronized void clientConnected(BluetoothSocket socket, BluetoothDevice device) {
		
		
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
		
	}
	
	public synchronized void stop() {
		
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		
		setState(STATE_NONE);
	}
	
	public void write(byte[] out) {
		ConnectedThread connectedThread;
		synchronized (this) {
			if (mState != STATE_CONNECTED) {
				return;
			}
			connectedThread = mConnectedThread;
		}
		connectedThread.write(out);
	}
	
	
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;
		
		public AcceptThread() {
			BluetoothServerSocket temp = null;
			try {
				temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (IOException e) { }
			mServerSocket = temp;
		}
		
		public void run() {
			BluetoothSocket socket = null;
			while (true) {
				try {
					socket = mServerSocket.accept();
				} catch (IOException e) { 
					break;
				}
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTIG:
							clientConnected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							try {
								socket.close();
							}
							catch (IOException e) {
								
							}
							break;
						}
					}
				}
			}
		}
		
		public void cancel() {
			try {
				mServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class ConnectThread extends Thread {
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;
		
		public ConnectThread(BluetoothDevice device) {
			
			BluetoothSocket temp = null;
			mDevice = device;
			
			try {
				temp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				
			}
			mSocket = temp;
		}
		
		public void run() {
			mBluetoothAdapter.cancelDiscovery();
			
			try {
				mSocket.connect();
			} catch (IOException e) {
				try {
					mSocket.close();
					Message msg = mHandler.obtainMessage(MainActivity.SERVER_DOWN);
					mHandler.sendMessage(msg);
					return;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
			}
			
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			clientConnected(mSocket, mDevice);
			
		}
		
		public void cancel() {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mBluetoothSocket;
		private final InputStream mInputStream;
		private final OutputStream mOutputStream;
		
		public ConnectedThread(BluetoothSocket socket) {
			mBluetoothSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			mInputStream = tmpIn;
			mOutputStream = tmpOut;	
		}
		
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
			while (true) {
				try {
					bytes = mInputStream.read(buffer);
					//mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
					Log.i("luke", "odebrano wiadomosc");
					mBoard.mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				}
					catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
		public void write(byte[] buffer) {
			try {
				mOutputStream.write(buffer);
				//mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
				Log.i("luke", "wyslano wiadomosc");
				mBoard.mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			}
			catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		public void cancel() {
			try {
				mBluetoothSocket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
