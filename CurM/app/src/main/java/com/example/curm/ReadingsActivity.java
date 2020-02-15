package com.example.curm;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ReadingsActivity extends AppCompatActivity {
    TextView mSensor1TextView;
    Button mDisconnectButton;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        setContentView(R.layout.activity_readings);

        mSensor1TextView = (TextView)findViewById(R.id.sensor_1_value);
        mDisconnectButton = (Button)findViewById(R.id.button_disconnect);

        new ConnectBT().execute(); //Call the class to connect

        mDisconnectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        //while(btSocket == null);
        //readSensor1();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }
    private void readSensor1() {
        //while (true){
        if (btSocket!=null)
        {
            try
            {
                InputStream socketInputStream =  btSocket.getInputStream();
                byte[] buffer = new byte[256];
                int bytes;

                // Keep looping to listen for received messages
                //while (true) {
                try {
                    bytes = socketInputStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    mSensor1TextView.setText(readMessage);
                    //Log.i("logging", readMessage + "");
                } catch (IOException e) {
                    //break;
                }
                //}
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        //}
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ReadingsActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
                progress.dismiss();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                /*Runnable r1 = new SingletonClass();
                Thread t1 =new Thread(r1);
                // this will call run() method
                t1.start();*/
                progress.dismiss();
                final Handler handler=new Handler();
                handler.post(new Runnable(){
                    @Override
                    public void run() {
                        readSensor1();
                        // upadte textView here
                        handler.postDelayed(this,50); // set time here to refresh textView
                    }
                });
            }
            //progress.dismiss();
        }
    }
}
