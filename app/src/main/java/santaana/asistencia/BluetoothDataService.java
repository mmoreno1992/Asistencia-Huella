package santaana.asistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class BluetoothDataService {

    // Debugging
    private static final String TAG = "BluetoothDataService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChat";

    // Unique UUID for this application
    // Well known SPP UUID (will *probably* map to RFCOMM channel 1 (default) if not in use); 
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int ERROR_TIMEOUT = 0;
    public static final int ERROR_INVALID_COMMAND_DATA = 1;
    public static final int ERROR_CHECKSUM_ERROR = 2;
    public static final int ERROR_UNKNOWN_COMMAND = 3;
    public static final int ERROR_IMAGE_SIZE_TOO_LARGE = 4;

    public static final int DATA_TYPE_RAWIMAGE = 0;
    public static final int DATA_TYPE_WSQIMAGE = 1;
    public static final int DATA_TYPE_FT_SAMPLE = 2;
    public static final int DATA_TYPE_ANSI_SAMPLE = 3;
    public static final int DATA_TYPE_ISO_SAMPLE = 4;
    // received data is 13 bytes command + n bytes data (wsq or raw)
    // n = command[6-9]
    // command[1] == 0x36 - start to convert wsq
    // command[1] == 0x0F - download wsq data
    // command[1] == 0x44 - download raw data
    // command[1] == 0x4D with command[10] == 0x00 - download Futronic sample data
    // command[1] == 0x4D with command[10] == 0x28 - download ANSI sample data
    // command[1] == 0x4D with command[10] == 0x38 - download ISO sample data
    public static byte[] mCommand = new byte[13];
    public static int mCommandLength = 0;
    public static long mTimeConvertWSQInFAM = 0;
    public static long mTimeDownloadImage = 0;
    public static long mTimeConvertWSQInAndroid = 0;
    public static int mErrorCode;
    //public static boolean mWsq = false;
    public static int mDataType = DATA_TYPE_RAWIMAGE;
    public static int mTotalImageSize = 0;
    public static boolean mRecvTimeOut = false;
    public static int mbytesRead = 0;
    public static int mbytesTotal = 153602;
    public static int mbytesTotalRead = 0;
    public static boolean mbFirstGet = true;
    public static int mnCurrentStep = 0;

    public static final int TIMEOUT_1 = 6000;  // 6sec timeout
    public static final int TIMEOUT_2 = 3000;  // 3sec timeout

    public static byte[] mHostSample = new byte[669];

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothDataService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(FS28DemoActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
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

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(FS28DemoActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(FS28DemoActivity.TOAST, "No se puede conectar con el lector.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(FS28DemoActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(FS28DemoActivity.TOAST, "Se ha desconectado del lector.");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public boolean ValidateCommandData(byte[] Command, int[] Size) {
        if (Command.length != 13 || Command[0] != 0x40 || Command[12] != 0x0D) {
            mErrorCode = ERROR_INVALID_COMMAND_DATA;
            return false;
        }
        int checksum = 0;
        int i;
        for (i = 0; i < 11; i++)
            checksum += Command[i];
        if (Command[11] != (byte) checksum) {
            mErrorCode = ERROR_CHECKSUM_ERROR;
            return false;
        }
        checksum = 0;
        short unsignedByte;
        for (i = 9; i > 5; i--) {
            if (Command[i] < 0)    // Java does not have unsigned type.
                unsignedByte = (short) (256 + Command[i]);
            else
                unsignedByte = Command[i];
            checksum |= unsignedByte;
            if (i > 6)
                checksum = checksum * 0x100;
        }
        Size[0] = checksum + 2;
        return true;
    }

    /*
     * add timer to monitor the data receiving progress.
     */
    private Handler mHandlerTimer = new Handler();

    private void StartTimer(long Interval) {
        mRecvTimeOut = false;
        mHandlerTimer.removeCallbacks(mUpdateTimeTask);
        mHandlerTimer.postDelayed(mUpdateTimeTask, Interval);
    }

    private void StopTimer() {
        mHandlerTimer.removeCallbacks(mUpdateTimeTask);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            mRecvTimeOut = true;
            ResetRecvVariables();
            mConnectedThread.ResponseToFS28((byte) 0x00, (byte) 0x41);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // send message to stop the connected thread and reconnect.
            mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DATA_ERROR, ERROR_TIMEOUT, -1).sendToTarget();
        }
    };

    private void ResetRecvVariables() {
        mbytesRead = 0;
        mbytesTotal = 153602;
        mbytesTotalRead = 0;
        mbFirstGet = true;
        mnCurrentStep = 0;
        mCommandLength = 0;
        mTotalImageSize = 0;
        mDataType = DATA_TYPE_RAWIMAGE;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                if (FS28DemoActivity.mStop)
                    break;
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothDataService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            /*
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            */
            Method m = null;
            try {
                m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                tmp = (BluetoothSocket) m.invoke(device, 1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                //BluetoothDataService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothDataService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
            FS28DemoActivity.mConnected = true;
        }

        @Override

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[2048];
            boolean error = false;
            long lT1 = 0;
            String strTimeMsg = null;
            ResetRecvVariables();
            while (true) {
                error = false;
                mRecvTimeOut = false;
                while (mbytesTotal > 0) {
                    if (mRecvTimeOut) {
                        error = true;
                        break;
                    }
                    if (FS28DemoActivity.mStop || (mState != STATE_CONNECTED))
                        break;
                    try {
                        if (mCommandLength < 13)//receive 13 bytes command first
                            mbytesRead = mmInStream.read(buffer, 0, 13 - mCommandLength);
                        else
                            mbytesRead = mmInStream.read(buffer, 0, Math.min(2048, mbytesTotal));
                        Log.i(TAG, "Received: " + mbytesRead);
                        StopTimer();
                    } catch (IOException e3) {
                        Log.e(TAG, "disconnected");
                        connectionLost();
                        break;
                    }
                    if (mbytesRead > 0) {
                        if (mbFirstGet) {
                            //lT1 = SystemClock.uptimeMillis();
                            mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, "Recibiendo registro...").sendToTarget();
                            mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DATA_RECEIVING).sendToTarget();
                            mbFirstGet = false;
                        }
                        //1. get and check 13 bytes command
                        if (mCommandLength < 13) {
                            if (mbytesRead > (13 - mCommandLength)) {
                                System.arraycopy(buffer, 0, mCommand, mCommandLength, 13 - mCommandLength);
                                System.arraycopy(buffer, 13 - mCommandLength, FS28DemoActivity.mImageFP, mbytesTotalRead, mbytesRead - (13 - mCommandLength));
                                mbytesTotalRead += (mbytesRead - (13 - mCommandLength));
                                mCommandLength = 13;
                            } else {
                                System.arraycopy(buffer, 0, mCommand, mCommandLength, mbytesRead);
                                mCommandLength += mbytesRead;
                            }
                            if (mCommandLength == 13) {
                                int[] Size = new int[1];
                                if (!ValidateCommandData(mCommand, Size)) {
                                    mCommandLength = 0;
                                    mbytesTotalRead = 0;
                                    mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DATA_ERROR, mErrorCode, -1).sendToTarget();
                                    error = true;
                                    ResponseToFS28((byte) 0x00, (byte) 0x41);
                                    break;
                                } else {
                                    if (mCommand[1] == 0x36) // start convert WSQ
                                    {
                                        lT1 = SystemClock.uptimeMillis();
                                        Log.i(TAG, "Got conver WSQ cmd 0x36");
                                        mCommandLength = 0;
                                        mbytesTotalRead = 0;
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, "Got convert WSQ cmd 0x36").sendToTarget();
                                        ResponseToFS28((byte) 0x00, (byte) 0x40);
                                        StartTimer(TIMEOUT_1);    // set timeout 6sec
                                    } else if (mCommand[1] == 0x0F) //download WSQ
                                    {
                                        mDataType = DATA_TYPE_WSQIMAGE;
                                        mTotalImageSize = mbytesTotal = Size[0];
                                        mTotalImageSize -= 2;
                                        mbytesTotal -= mbytesTotalRead;
                                        Log.i(TAG, "Got download WSQ cmd. Size is" + mTotalImageSize);
                                        mTimeDownloadImage = SystemClock.uptimeMillis();
                                        mTimeConvertWSQInFAM = mTimeDownloadImage - lT1;
                                        strTimeMsg = String.format("Got download WSQ cmd 0x0F. Size is: %d ", mTotalImageSize + 2);
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, strTimeMsg).sendToTarget();
                                        ResponseToFS28((byte) 0x00, (byte) 0x40);
                                        StartTimer(TIMEOUT_2);    // set timeout 3sec
                                    } else if (mCommand[1] == 0x44) //download RAW
                                    {
                                        mDataType = DATA_TYPE_RAWIMAGE;
                                        mTotalImageSize = mbytesTotal = Size[0];
                                        mTotalImageSize -= 2;
                                        mbytesTotal -= mbytesTotalRead;
                                        Log.i(TAG, "Got download RAW cmd. Size is" + mTotalImageSize);
                                        mTimeDownloadImage = SystemClock.uptimeMillis();
                                        strTimeMsg = String.format("Got download RAW cmd 0x44. Size is: %d", mTotalImageSize + 2);
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, strTimeMsg).sendToTarget();
                                        ResponseToFS28((byte) 0x00, (byte) 0x40);
                                        StartTimer(TIMEOUT_2);    // set timeout 3sec
                                    } else if (mCommand[1] == 0x4D) //0x4D command means FS28 start to download sample, the sample size is always 666 bytes
                                    {
                                        if (mCommand[10] == 0x00)//for download Futronic Sample
                                        {
                                            mDataType = DATA_TYPE_FT_SAMPLE;
                                            mTotalImageSize = mbytesTotal = 666;
                                            Log.i(TAG, "Got download Futronic Sample cmd. Size is" + mTotalImageSize);
                                            strTimeMsg = String.format("Got download Futronic Sample cmd 0x4D. Size is: %d", mTotalImageSize + 2);
                                        } else if (mCommand[10] == 0x28) //for download ANSI Sample which size is not fixed
                                        {
                                            mDataType = DATA_TYPE_ANSI_SAMPLE;
                                            mTotalImageSize = mbytesTotal = Size[0];
                                            Log.i(TAG, "Got download ANSI Sample cmd. Size is" + mTotalImageSize);
                                            strTimeMsg = String.format("Got download ANSI Sample cmd 0x4D. Size is: %d", mTotalImageSize + 2);
                                        } else if (mCommand[10] == 0x38)//for download ISO Sample which size is not fixed
                                        {
                                            mDataType = DATA_TYPE_ISO_SAMPLE;
                                            mTotalImageSize = mbytesTotal = Size[0];
                                            Log.i(TAG, "Got download ISO Sample cmd. Size is" + mTotalImageSize);
                                            strTimeMsg = String.format("Got download ISO Sample cmd 0x4D. Size is: %d", mTotalImageSize + 2);
                                        }
                                        mTotalImageSize -= 2;
                                        mbytesTotal -= mbytesTotalRead;
                                        mTimeDownloadImage = SystemClock.uptimeMillis();
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, strTimeMsg).sendToTarget();
                                        ResponseToFS28((byte) 0x00, (byte) 0x40);
                                        StartTimer(TIMEOUT_2);    // set timeout 3sec
                                    } else {
                                        Log.e(TAG, "Unknown command!");
                                        mCommandLength = 0;
                                        mbytesTotalRead = 0;
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DATA_ERROR, ERROR_UNKNOWN_COMMAND, -1).sendToTarget();
                                        error = true;
                                        ResponseToFS28((byte) 0x00, (byte) 0x41);
                                        break;
                                    }
                                    if (mbytesTotal > 153602) {
                                        Log.e(TAG, "Total data size is too large!");
                                        mCommandLength = 0;
                                        mbytesTotalRead = 0;
                                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_DATA_ERROR, ERROR_IMAGE_SIZE_TOO_LARGE, -1).sendToTarget();
                                        error = true;
                                        ResponseToFS28((byte) 0x00, (byte) 0x41);
                                        break;
                                    }
                                }
                            }
                        } else //2. get image data
                        {
                            System.arraycopy(buffer, 0, FS28DemoActivity.mImageFP, mbytesTotalRead, mbytesRead);
                            mbytesTotal -= mbytesRead;
                            mbytesTotalRead += mbytesRead;
                            mnCurrentStep = mbytesTotalRead * 100 / (mTotalImageSize + 2);
                            if (mnCurrentStep > FS28DemoActivity.mStep) {
                                FS28DemoActivity.mStep = mnCurrentStep;
                                mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_PROGRESSBAR).sendToTarget();
                            }
                            StartTimer(TIMEOUT_2);    // set timeout 3sec
                        }
                    }// if( mbytesRead>0 )                    
                } // while( bytesTotal > 0 )               
                StopTimer();
                if (FS28DemoActivity.mStop)
                    break;
                if (mState != STATE_CONNECTED) {
                    cancel();
                    break;
                }
                if (!error) {
                    FS28DemoActivity.mReceivedDataType = mDataType;
                    ResponseToFS28((byte) 0x00, (byte) 0x40);
                    mTimeDownloadImage = SystemClock.uptimeMillis() - mTimeDownloadImage;
                    if (mDataType == DATA_TYPE_WSQIMAGE) {
                        FS28DemoActivity.mWsqImageFP = new byte[mTotalImageSize];
                        System.arraycopy(FS28DemoActivity.mImageFP, 0, FS28DemoActivity.mWsqImageFP, 0, mTotalImageSize);
                        mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, "Convert WSQ to RAW...").sendToTarget();
                        mTimeConvertWSQInAndroid = SystemClock.uptimeMillis();
                        ftrwsqandroidhelper helper = new ftrwsqandroidhelper();
                        int sizeRaw = helper.GetWsqImageRawSize(FS28DemoActivity.mWsqImageFP);
                        if (sizeRaw > 0) {
                            byte[] rawImg = new byte[sizeRaw];
                            if (helper.ConvertWsqToRaw(FS28DemoActivity.mWsqImageFP, rawImg)) {
                                System.arraycopy(rawImg, 0, FS28DemoActivity.mImageFP, 0, sizeRaw);
                                mTimeConvertWSQInAndroid = SystemClock.uptimeMillis() - mTimeConvertWSQInAndroid;
                                strTimeMsg = String.format("TWsq1:%d, TDn:%d, TWsq2:%d (ms)", mTimeConvertWSQInFAM, mTimeDownloadImage, mTimeConvertWSQInAndroid);
                            } else
                                strTimeMsg = "Failed to convert wsq to raw!";
                        } else
                            strTimeMsg = "Invalid WSQ Image!";
                    } else if (mDataType == DATA_TYPE_FT_SAMPLE) {
                        ConvertToHostSample(FS28DemoActivity.mImageFP);
                        strTimeMsg = String.format("Futronic Sample TDn:%d(ms)", mTimeDownloadImage);
                    } else if (mDataType == DATA_TYPE_ANSI_SAMPLE) {
                        FS28DemoActivity.mANSISample = new byte[mTotalImageSize];
                        System.arraycopy(FS28DemoActivity.mImageFP, 0, FS28DemoActivity.mANSISample, 0, mTotalImageSize);
                        strTimeMsg = String.format("ANSI Sample TDn:%d(ms)", mTimeDownloadImage);
                    } else if (mDataType == DATA_TYPE_ISO_SAMPLE) {
                        FS28DemoActivity.mISOSample = new byte[mTotalImageSize];
                        System.arraycopy(FS28DemoActivity.mImageFP, 0, FS28DemoActivity.mISOSample, 0, mTotalImageSize);
                        strTimeMsg = String.format("ISO Sample TDn:%d(ms)", mTimeDownloadImage);
                    } else if (mDataType == DATA_TYPE_RAWIMAGE)
                        strTimeMsg = String.format("Raw TDn:%d(ms)", mTimeDownloadImage);
                    else {
                        strTimeMsg = "Unknown data type!";
                    }

                    mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_MSG, -1, -1, strTimeMsg).sendToTarget();
                    mHandler.obtainMessage(FS28DemoActivity.MESSAGE_SHOW_IMAGE).sendToTarget();
                }
                //finish receiving data, reset the variables
                ResetRecvVariables();
                FS28DemoActivity.mStep = 0;
            } // while (true)
        }

        public void ConvertToHostSample(byte[] FamSample) {
            FS28DemoActivity.mHostSample[0] = (byte) 0x9d;
            FS28DemoActivity.mHostSample[1] = 0x02;
            FS28DemoActivity.mHostSample[2] = 0x02;
            FS28DemoActivity.mHostSample[3] = 0x02;
            FS28DemoActivity.mHostSample[4] = 0x00;
            System.arraycopy(FamSample, 0, FS28DemoActivity.mHostSample, 5, 664);
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mmOutStream.flush();
                // Share the sent message back to the UI Activity
                /*mHandler.obtainMessage(FS28DemoActivity.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();*/
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        private void ResponseToFS28(byte command, byte flag) {
            byte[] Response = new byte[]{0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D};
            byte checksum = 0;
            Response[1] = command;
            Response[10] = flag;
            for (int i = 0; i < 11; i++) {
                checksum = Response[i];
            }
            Response[11] = checksum;
            write(Response);
        }
    }
}
