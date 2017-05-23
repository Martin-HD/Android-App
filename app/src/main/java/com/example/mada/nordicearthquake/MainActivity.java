package com.example.mada.nordicearthquake;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    //private static final String UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; //old UUID for freqcontrol
    private static final String UART_SERVICE = "49f89999-edd1-4c81-8702-585449ba92a8";
    private static final int REQUEST_PERMISSION_REQ_CODE = 1023;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private Handler mHandler;
    private ProgressBar mProgressBar;

    private static final int REQUEST_ENABLE_BT = 1;
    //scan interval (set to 10 000 ms)
    private static final long SCAN_PERIOD = 10000;
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();


        //check if BLE is supported on device, if not exit application after toast.

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "Bluetooth low energy is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }

        //initializes the bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mListView = (ListView) findViewById(R.id.list_devices); //list_devices


        //initializes the list view adapter
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);

        //checks for the permisson and asks to have the permission if it is not already granted.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(mListView, view, position, id);
                return;
            }
        });

    }

    @Override
    public void onBackPressed()
    {

        //handles the backbutton presses.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //this sets the scan visible when not scanning, and stop visible when scanning, also viewing the progressbar when scanning

        if(!mScanning)
        {
            getMenuInflater().inflate(R.menu.start_scan, menu);
            //menu.findItem(R.id.menu_stop).setVisible(false);
            //menu.findItem(R.id.menu_scan).setVisible(true);  //this is apparently not the way to do it.
            //menu.findItem(R.id.menu_refresh).setActionView(null);
            //mProgressBar.setVisibility(View.INVISIBLE);
        }
        else
        {
            getMenuInflater().inflate(R.menu.stop_scan, menu);
            //menu.findItem(R.id.menu_stop).setVisible(true);
            //menu.findItem(R.id.menu_scan).setVisible(false);
            //menu.findItem(R.id.menu_refresh).setActionView(R.layout.app_bar_main);
            //mProgressBar.setVisibility(View.VISIBLE);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_start_scan:

                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();

                if(!mBluetoothAdapter.isEnabled()) //Checks that the user didnt use the dropdown menu to disable bluetooth.
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else
                {
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                    return true;
                }


               // return true;
            case R.id.action_stop_scan:
                scanLeDevice(false);
                return true;
        }
        //return super.onOptionsItemSelected(item);
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_control)
        {
            Intent controlActivitynav = new Intent(this, MainActivity.class);
            startActivity(controlActivitynav);

        }
        else if (id == R.id.nav_aboutme)
        {
            Intent aboutMeIntent = new Intent(this, aboutMe.class);
            startActivity(aboutMeIntent);

        }
        else if (id == R.id.nav_aboutnordic)
        {
            Intent aboutnordicIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.nordicsemi.com/eng/About-us"));
            startActivity(aboutnordicIntent);

            //webview.loadUrl("https://www.nordicsemi.com/eng/About-us"); //Need to implement webview in the code for the xml in the activity.
            // TODO: Possible solution, more streamlined, but takes more implementation, as we need to add to backstack and design the activity and link it, also the navigation drawer and overflow menu, should be copy and paste, but errors may occur.


        }
        else if (id == R.id.aboutntnu)
        {
            Intent aboutntnuIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.ntnu.edu/about"));
            startActivity(aboutntnuIntent);

        }
        else if (id == R.id.nav_devzone)
        {
            Intent devzoneIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://devzone.nordicsemi.com"));
            startActivity(devzoneIntent);
        }
        else if (id == R.id.nav_github)
        {
            Intent githubIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.github.com/NordicSemiconductor"));
            startActivity(githubIntent);
        }
        else if(id == R.id.nav_physweb)
        {
            Intent physwebIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://andreln.github.io"));
            startActivity(physwebIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume()
    {
        //checks that the user didnt disable bluetooth while beeing out of the app, and if the user did, asks to re-enable it.
        super.onResume();

            if(!mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            //scanLeDevice(true);  //uncomment this is you want scanning on startup, its more efficient but not common practice in applications for android.
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        //user decided not to enable the bluetooth

        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)
        {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now we may proceed with scanning.
                    scanLeDevice(true);
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
        }
    }

    //@Override
    protected void onListItemClick(ListView l, View v, int position, long id) // SAY WHAT hehehe l ikke 1 :P
    {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if(device == null)
        {
            return;
        }

        if (mScanning)
        {
            scanLeDevice(false);
        }

       // final Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.tek.no")); //so this works and gets called. Somehow something in the activity shift crashes the application
        //startActivity(intent);

        //here we pass the device name and device adress over to the control earthquake activity so that we can use them.
        final Intent intent = new Intent(this, ControlEarthQuake.class);
        intent.putExtra(ControlEarthQuake.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(ControlEarthQuake.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        startActivity(intent);


        mLeDeviceListAdapter.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
    }


    private void scanLeDevice(final boolean enable) // Filter the device returned results to only include the ones we are interested in. call startLeScan(UUID[], BluetoothAdapter.LeScanCallback), providing an array of UUID objects that specify the GATT services your app supports.
    {                                                  //Filter implemented in its own class
        // Since Android 6.0 we need to obtain either Manifest.permission.ACCESS_COARSE_LOCATION or Manifest.permission.ACCESS_FINE_LOCATION to be able to scan for
        // Bluetooth LE devices. This is related to beacons as proximity devices.
        // On API older than Marshmallow the following code does nothing.

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            return;
        }


        final BluetoothLeScannerCompat mScanner = BluetoothLeScannerCompat.getScanner();
        if(enable)
        {
            final ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).setUseHardwareBatchingIfSupported(false).setUseHardwareFilteringIfSupported(false).build();
            final List<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(UART_SERVICE)).build()); //filters out everything but this and adds the UUID to the filterlist.
            mScanner.startScan(filters, settings, scanCallback); //set filters as first parameter to filter the results, null if not.

            //stops scanning after a predefined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mScanner.stopScan(scanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            mScanning = true;
        }
        else
        {
            mScanning = false;
            mProgressBar.setVisibility(View.GONE);
            mScanner.stopScan(scanCallback);
        }
        invalidateOptionsMenu();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            Log.v("TAG", "Scan result: " + result.getDevice().getAddress());
            final BluetoothDevice device = result.getDevice();
            mLeDeviceListAdapter.addDevice(device);
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

    //adapter for holding the new devices that we find when scanning

    private class LeDeviceListAdapter extends BaseAdapter
    {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter()
        {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflater = MainActivity.this.getLayoutInflater();
        }

        public void addDevice (BluetoothDevice device)
        {
            if(!mLeDevices.contains(device))
            {
                mLeDevices.add(device);
                notifyDataSetChanged();
            }
        }

        public BluetoothDevice getDevice (int position)
        {
            return mLeDevices.get(position);
        }

        public void clear()
        {
            mLeDevices.clear();
        }

        @Override
        public int getCount()
        {
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i)
        {
            return i;
        }
        @Override
        public long getItemId (int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            ViewHolder viewHolder;

            //optimization code

            if(view == null)
            {
                view = mInflater.inflate(R.layout.listitem_device, null); // XML FORM TO SHOW LISTED DEVICES //MUCHTODO
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if(deviceName != null && deviceName.length() > 0)
            {
                viewHolder.deviceName.setText(deviceName);
            }
            else
            {
                viewHolder.deviceName.setText("Unknown device");
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {


        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();

                }
            });
        }
    };

    static class ViewHolder
    {
        TextView deviceName;
        TextView deviceAddress;
    }
}
