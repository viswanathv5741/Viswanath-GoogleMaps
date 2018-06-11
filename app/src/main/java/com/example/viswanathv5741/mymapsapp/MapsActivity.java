package com.example.viswanathv5741.mymapsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private Location myLocation;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean gotMyLocationOneTime;
    private boolean notTrackingMyLocation = true;
    private boolean canGetLocation = false;
    private double latitude, longitude;
    private String[] results;
    private LatLng searchQ;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private static final long MIN_TIME_BW_UPDATES = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Add a marker at your place of birth and add a marker
        LatLng banglore = new LatLng(13, 78);
        mMap.addMarker(new MarkerOptions().position(banglore).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(banglore));

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMapsApp", "Failed Permission Check");
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMapsApp", "Failed Permission Check");
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
//        }
//        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
//                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            mMap.setMyLocationEnabled(true);
//        }

        locationSearch = (EditText) findViewById(R.id.editText_name);

        gotMyLocationOneTime = false;
        getLocation();
    }

    //Add a View button and method to switch between satellite an map views

    public void changeView(View view){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    public void onSearch(View v){

        String location= locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use LocationManager for user location
        //Implement the LocationListener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria,false);

        Log.d("MyMapsApp", "onSearch: location = " + location);
        Log.d("MyMapsApp","onSearch: provider " + provider);

        LatLng userLocation = null;

        //Check the last known location, need to specifically list the provder (network or gps)

        try {
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: locationManager is not null");

                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null from getLastKnownLocation");
                }
            }
        } catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "onSearch: Exception getLastKnownLocation");
            Toast.makeText(this, "onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT);

        }

        //Get the location if it exists
        if (!location.matches("")){
            Log.d("MyMapsApp", "onSearch: location field is populated");
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try{
                //Get a list of the addresses
                addressList = geocoder.getFromLocationName(location,1000, userLocation.latitude - (10.0/60),userLocation.longitude - (10.0/60),
                        userLocation.latitude + (10.0/60),userLocation.longitude + (10.0/60));

                Log.d("MyMapsApp", "onSearch, addressList is created");
            } catch (IOException e){
                e.printStackTrace();
            }

            if (!addressList.isEmpty()){
                Log.d("MyMapsApp", "onSearch: AddressList size is: " + addressList.size());
                for (int i=0; i<addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                    // Place a marker on the map
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(i+ ": " + address.getSubThoroughfare() + address.getSubThoroughfare()));
                    Log.d("MyMapsApp", "onSearch: getting address for searched location");
                    mMap.addMarker(new MarkerOptions().position(latLng).title((i+1) + ": " + address.getAddressLine(0)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }//end onSearch

    public void getLocation(){

        try{
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            //Get GPS status, isProviderEnabled returns true if user has enabled GPS
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled)Log.d("MyMapsApp", "getLocation: GPS is enabled");
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled)Log.d("MyMapsApp", "getLocation: Network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: no provider enabled");
            }
            else {
                if (isNetworkEnabled){
                    //Request location updates
                    if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerNetwork);
                    Log.d("MyMapsApp", "getLocation: Requested location updates Network");
                }
                if (isGPSEnabled){
                    //location manager request for GPS Provider
                    Toast.makeText(this, "getLocation: GPS enabled", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerGPS);
                    Log.d("MyMapsApp", "getLocation: Requested location updates GPS");
                }
            }
        } catch (Exception e){
            Log.d("MyMapsApp", "getLocation: Exception in getLocation");
            e.printStackTrace();
        }
    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);
            Toast.makeText(MapsActivity.this, "coordinate from Network", Toast.LENGTH_SHORT);

            //Check if doing one time, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;
            }
            else {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                Log.d("MyMapsApp", "LocationListenerNetwork: requesting location again");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.GPS_PROVIDER);
            Toast.makeText(MapsActivity.this, "coordinate from GPS", Toast.LENGTH_SHORT);
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }
            else {

            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerGPS: status change");

            switch (status){
                case (LocationProvider.AVAILABLE):
                    Log.d("MyMapsApp","locationListenerGPS: location provider is available");
                    break;
                case (LocationProvider.OUT_OF_SERVICE):
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    Log.d("MyMapsApp", "LocationListenerGPS: requesting location again");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerGPS);
                    break;
                case (LocationProvider.TEMPORARILY_UNAVAILABLE):
                    getLocation();
                    break;
                default:
                    getLocation();
                    break;
            }
            //switch (status)
                //case LocationProvider.AVAILABLE:
                //print Log.d or toast message
                //break;
                //case LocationProvider.OUT_OF_SERVICE:
                //enable network updates
                //break;
                //case LocationProvider.TEMPPORARILY_UNAVAILABLE:
                //enable both network and gps
                //break;
                //default:
                //enable both network and gps
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAmarker(String provider){
        if (locationManager != null){
            if (ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
        }
        myLocation = locationManager.getLastKnownLocation(provider);
        LatLng userLocation = null;
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
        if (myLocation == null){
            Log.d("MyMapsActivity","myLocation not found");
        }
        else{
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
            update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
        }
        if (provider == LocationManager.GPS_PROVIDER){
            mMap.addCircle(new CircleOptions().center(userLocation).radius(1.0).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
            mMap.addCircle(new CircleOptions().center(userLocation).radius(1.5).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.TRANSPARENT));
        }
        else{
            mMap.addCircle(new CircleOptions().center(userLocation).radius(1.0).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
            mMap.addCircle(new CircleOptions().center(userLocation).radius(1.5).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.TRANSPARENT));
        }
        mMap.animateCamera(update);

        //mMap.animateCamera(camera);
    }

    public void trackMyLocation(View view){
        if (notTrackingMyLocation){
            getLocation();
            Toast.makeText(this, "tracking my location", Toast.LENGTH_SHORT).show();
            notTrackingMyLocation = false;
        }
        else {
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            Toast.makeText(this, "not tracking my location", Toast.LENGTH_SHORT).show();

            notTrackingMyLocation = true;
        }
        //kick off the location tracker using getLocation to start the LocationListener
        //if(notTrackingMyLocation) getLocation(); notTrackingMyLocation=false;
        //else removeUpdates for both network and gps; noTrackingMyLocation=true;
    }

    public void clearMarkers(View view){
        mMap.clear();
    }

    public void onSearch2(View v) {

        //This loop is a hack since the returned string has format inconsistencies which result in errors when parsing
        //Doing it 10x is a hack to get one set of lat/lons for each location that work
        for (int i = 0; i<10;i++){
            Log.d("MyMaps", "Search activated");

            //Get the POI url, eg.  https://maps.googleapis.com/maps/api/place/radarsearch/json?keyword=Starbucks&location=32.959076,-117.189433&radius=9000&key=AIzaSyAO_vWgA5nwAC-KAV_en4p-1GXPYpbg__M
            String siteUrl = "https://maps.googleapis.com/maps/api/place/radarsearch/json?keyword=" +
                    locationSearch.getText().toString() + "&location=" +
                    latitude + "," + longitude + "&radius=9000&key=AIzaSyAO_vWgA5nwAC-KAV_en4p-1GXPYpbg__M";

            Log.d("MyMaps", "POI url: " + siteUrl);
            (new ParseURL()).execute(new String[]{siteUrl});
        }
    }


    private class ParseURL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String buffer = new String();
            try {
                Log.d("MyMaps", "Connecting to [" + strings[0] + "]");
                Document doc = Jsoup.connect(strings[0]).ignoreContentType(true).get();
                Log.d("MyMaps", "Connected to [" + strings[0] + "]");
                // Get document (HTML page) title

                Element bod = doc.body();
                buffer = ("BOD TEXT  " + bod.text() );
                Log.d("MyMaps", ""+buffer.toString());
                results = new String[201];
                int j = 0;
                while (buffer.indexOf("location")>-1){
                    mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
                    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                    results[j] = buffer.substring(buffer.indexOf("lat")+7,buffer.indexOf("lng")-3 ) + " " + buffer.substring(buffer.indexOf("lng")+7, buffer.indexOf("lng")+16);
                    buffer = buffer.substring(buffer.indexOf("lng")+20);
                    j++;
                }


                Log.d("MyMaps", ""+buffer.toString());
                for (String str: results){
                    Log.d("MyMaps", ""+str);
                }
                markMaker();



            } catch (Throwable t) {
                Log.d("MyMaps", "ERROR");
                t.printStackTrace();
            }

            return buffer.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //respText.setText(s);
        }
    }

    public void markMaker(){

        for (String str: results){


            Log.d("MyMaps", "Start Marking");
            searchQ = new LatLng(Double.parseDouble(str.substring(0,str.indexOf(" ")-1)), Double.parseDouble(str.substring(str.indexOf(" ")+1)));

            Log.d("MyMaps", ""+searchQ.latitude + " "+searchQ.longitude);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.addMarker(new MarkerOptions().position(searchQ).title(locationSearch.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                }
            });
            Log.d("MyMaps", "Stop Marking");
        }
    }

    public class Compass implements SensorEventListener {
        private static final String TAG = "Compass";

        private SensorManager sensorManager;
        private Sensor gsensor;
        private Sensor msensor;
        private float[] mGravity = new float[3];
        private float[] mGeomagnetic = new float[3];
        private float azimuth = 0f;
        private float currectAzimuth = 0;

        private boolean bearing = false;
        private float bearingDegrees = -1;

        // compass arrow to rotate
        public ImageView arrowView = null;

        FragmentActivity activity;

        public Compass(FragmentActivity activity) {

            this.activity = activity;

            sensorManager = (SensorManager) activity.getApplicationContext()
                    .getSystemService(Context.SENSOR_SERVICE);
            gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        public void start() {

            boolean deviceSensorCompatible = true;

            if(!sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME))
                deviceSensorCompatible = false;

            if(!sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME))
                deviceSensorCompatible = false;

            if(!deviceSensorCompatible) {
                Utility.ShowMessage(activity, activity.getString(R.string.erroroccured), activity.getString(R.string.deviceIncompatible),  1);
                stop();
            }
        }

        public void startBearing()
        {
            bearing = true;
            start();
        }

        public void setBearingDegrees(float bearingDegrees)
        {
            this.bearingDegrees = bearingDegrees;
        }

        public void stop() {
            sensorManager.unregisterListener(this);
        }

        public void stopBearing()
        {
            bearing = false;
            stop();
        }

        private void adjustArrow() {
            if (arrowView == null) {
                Log.i(TAG, "arrow view is not set");
                return;
            }

            Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            currectAzimuth = azimuth;

            an.setDuration(250);
            an.setRepeatCount(0);
            an.setFillAfter(true);

            arrowView.startAnimation(an);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.97f;

            synchronized (this) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                            * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                            * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                            * event.values[2];

                    // mGravity = event.values;

                    // Log.e(TAG, Float.toString(mGravity[0]));
                }

                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    // mGeomagnetic = event.values;

                    mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                            * event.values[0];
                    mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                            * event.values[1];
                    mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                            * event.values[2];
                    // Log.e(TAG, Float.toString(event.values[0]));

                }

                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                        mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    // Log.d(TAG, "azimuth (rad): " + azimuth);
                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                    azimuth = (azimuth + 360) % 360;

                    if(bearing) {
                        if(bearingDegrees != -1) {
                            azimuth -= bearingDegrees;
                            adjustArrow();
                        }
                    }
                    else
                        adjustArrow();

                    // Log.d(TAG, "azimuth (deg): " + azimuth);

                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

}
