package com.example.mada.nordicearthquake;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mada on 04.04.2017.
 */

public class ControlEarthQuake extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private SeekBar mSeekbar;

    private String mDeviceName;
    private String mDeviceAddress;
    boolean poweredOn;
    int divider = 0;
    int i = 0;

    private boolean mStartUpdatingUi = false;
    final Handler mHandler = new Handler();

    private final static String TAG = ControlEarthQuake.class.getSimpleName();
    private BluetoothLeService mBluetoothLeServiceBinder;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) //Binds the service to the javaclass and allows to send data between them.
        {
            mBluetoothLeServiceBinder = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeServiceBinder.initialize()) {
                Log.e(TAG, "UNABLE TO INITIALIZE BLUETOOTH");
                finish();
            } else {
                mBluetoothLeServiceBinder.connect(mDeviceAddress);
            }
            //automatic connect to the device upon startup initialization if successfully initialized bluetooth

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeServiceBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_control);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar); //needs to be final for some reason
        final TextView seekBarValue = (TextView) findViewById(R.id.current_frequency);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        getSupportActionBar().setTitle(mDeviceName);
        toolbar.setSubtitle(mDeviceAddress);


        Switch toggle = (Switch) findViewById(R.id.switch1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    poweredOn = true; ///////////////////////WWWWWWWWWWWWWWWWWTTTTTTTTTTTTTTTTTFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF NVm DEclared it out of scope. Powered on should work now
                    float seekbarvalue = ConvertToMachineSpecificSeekBarValue();
                    mBluetoothLeServiceBinder.writeEarthQuakeValues(seekbarvalue); //this should send the seekbarvalue that was set by the user while the machine was not powered on

                    //TODO this is also where we need to separate between the machines, as i would like to light a LED when the machine gets turned on, and so

                    //if (mDeviceName.equals("LakseFlaksern")) {
                    //    //TODO Write to LED characteristic.
                    //} else {
                    //    //TODO: write 1 to the first bit on the speakerMachine.
                   // }
                //} else {
                //    poweredOn = false;
                }
            }
        });


        Switch toggle2 = (Switch) findViewById(R.id.switch2);
        toggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (poweredOn == true) {
                        updateSeekBar();
                    } else {
                        toasttext();
                        ConvertToMachineSpecificSeekBarValue();
                        return;
                    }

                } else {
                    mStartUpdatingUi = false;
                    return;

                }
            }
        });


        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    ConvertToMachineSpecificSeekBarValue();
                } else {
                    if (poweredOn == true) {
                        float seekbarvalue = ConvertToMachineSpecificSeekBarValue();
                        mBluetoothLeServiceBinder.writeEarthQuakeValues(seekbarvalue);
                    } else {
                        toasttext();
                        ConvertToMachineSpecificSeekBarValue();

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (poweredOn == true) {
                    float seekbarvalue = ConvertToMachineSpecificSeekBarValue();
                    mBluetoothLeServiceBinder.writeEarthQuakeValues(seekbarvalue);
                } else {
                    toasttext();
                    ConvertToMachineSpecificSeekBarValue();

                }

            }
        });

    }

    final Runnable mUpdateSeekbarRunnable = new Runnable() {
        @Override
        public void run() {
            mStartUpdatingUi = true;
                for (i = 0; i <= 8000; i = i + 15) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mStartUpdatingUi) {
                                mSeekbar.setProgress(i);
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                mStartUpdatingUi = false;

        }
    };

    private void updateSeekBar() {
        if(mStartUpdatingUi) {
            return;
        }
        new Thread(mUpdateSeekbarRunnable).start();
    }



    private void toasttext() {
        Toast.makeText(this, "Please power the machine on to see it in action!", Toast.LENGTH_LONG).show();
    }

    private float ConvertToMachineSpecificSeekBarValue() {
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView seekBarValue = (TextView) findViewById(R.id.current_frequency);

        float seekbarvalue = ((float) seekBar.getProgress());
        if (mDeviceName.equals("Resonator - FC")) {
            divider = 12;
        } else if (mDeviceName.equals("LakseFlakseren")) { //TODO set real name of the DC machine controller. Should probably filter on the adress as good custom
            divider = 3333;
        }
        seekbarvalue = seekbarvalue / divider;
        if (mDeviceName.equals("Resonator - FC")) {
            seekbarvalue += 10;
        } else if (mDeviceName.equals("LakseFlakseren")) {  //TODO set real name of the DC machine controller.
            seekbarvalue += 0.1;
        }
        seekBarValue.setText("Current frequency " + String.valueOf(seekbarvalue) + " Hz");

        if(mDeviceName.equals("LakseFlakseren"))
        {
            seekbarvalue = seekbarvalue * 100;

        }
        return seekbarvalue; //seekbarvalue
    }


    private void startBleService() {
        Intent intent = new Intent(this, BluetoothLeService.class);
        startService(intent);
        bindService(intent, mServiceConnection, 0);
    }

    private void stopBleService() {
        unbindService(mServiceConnection);
        Intent intent = new Intent(this, BluetoothLeService.class);
        stopService(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        startBleService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopBleService();
        } else {
            unbindService(mServiceConnection);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        mStartUpdatingUi = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_disconnect, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_disconnect:
                mStartUpdatingUi = false;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}


