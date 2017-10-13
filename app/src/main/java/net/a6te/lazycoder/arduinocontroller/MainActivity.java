package net.a6te.lazycoder.arduinocontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    ArrayList mBAddress = new ArrayList();


    private boolean isBtConnected = false;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;


    Button mLeft,mRight,mUp,mDown;
    ProgressBar mProgressbar;
    TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv = (ListView) findViewById(R.id.list_view);
        mUp = (Button) findViewById(R.id.btn_up);
        mDown = (Button) findViewById(R.id.btn_down);
        mLeft = (Button) findViewById(R.id.btn_left);
        mRight = (Button) findViewById(R.id.btn_right);
        mProgressbar = (ProgressBar) findViewById(R.id.progressbar);
        mTv = (TextView) findViewById(R.id.text_connecting);
    }

    //connect new bluetooth
    public void connect(View v){
        if (isBluetoothOn()){

            lv.setVisibility(View.VISIBLE);

            pairedDevices = BA.getBondedDevices();
            final ArrayList list = new ArrayList();
            for (BluetoothDevice bt: pairedDevices) {
                list.add(bt.getName());
                mBAddress.add(bt.getAddress());

            }
            final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    address = mBAddress.get(position)+"";

                    isBtConnected = false;
                    btSocket = null;
                    progressOn();
                    ConnectBT bluetoothConnect = new ConnectBT();
                    bluetoothConnect.execute();
                }
            });
        }else {
            showToast("Please turn on your bluetooth connection first");
        }
    }

    public void up(View v){
        sendMessageToDevice("up");
    }
    public void down(View v){
        sendMessageToDevice("down");
    }
    public void left(View v){
        sendMessageToDevice("mLeft");
    }
    public void right(View v){
        sendMessageToDevice("right");
    }


    //send message to bluetooth device
    public void sendMessageToDevice(String message){
        if (isBluetoothOn()){
            if (btSocket!=null)
            {
                try
                {
                    btSocket.getOutputStream().write(message.getBytes());
                }
                catch (IOException e)
                {
                    showToast("!Opps message sending failed ");
                }
            }else showToast("you are not connected with device");
        }else showToast("Please turn your bluetooth first");
    }
    //show toast massage
    public void showToast(String str){
        Toast.makeText(MainActivity.this,str+"",Toast.LENGTH_SHORT).show();
    }
    public boolean isBluetoothOn(){
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA.isEnabled()) return true;
        else return false;
    }



    //coonection with bluetooth device
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

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
            progressOf();
            if (!ConnectSuccess)
            {
                showToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
                isBtConnected = false;
                btSocket = null;
            }
            else
            {
                lv.setVisibility(View.GONE);
                showToast("Connected.");
                isBtConnected = true;
            }
        }
    }

    // set visible progressbar and set unvisible button
    public void progressOn(){
        mProgressbar.setVisibility(View.VISIBLE);
        mTv.setVisibility(View.VISIBLE);
        mLeft.setVisibility(View.GONE);
        mRight.setVisibility(View.GONE);
        mUp.setVisibility(View.GONE);
        mDown.setVisibility(View.GONE);
    }

    //set visible button and set unvisible progressbar
    public void progressOf(){
        mProgressbar.setVisibility(View.GONE);
        mTv.setVisibility(View.GONE);
        mLeft.setVisibility(View.VISIBLE);
        mRight.setVisibility(View.VISIBLE);
        mUp.setVisibility(View.VISIBLE);
        mDown.setVisibility(View.VISIBLE);
    }
}
