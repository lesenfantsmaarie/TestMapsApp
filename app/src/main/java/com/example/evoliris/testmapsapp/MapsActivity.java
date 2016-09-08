package com.example.evoliris.testmapsapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;

import android.location.LocationListener;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, LocationListener {
// La Classe LocationListener permet d'avertir l'app à intervalles voulus

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private LocationManager locationManager;
    private String provider;
    private Location location;

    LatLng currentPos;
    private int lat;
    private int lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        //sélectionne le meilleur provider, GPS ou wifi (trinagulation)
        provider = locationManager.getBestProvider(criteria, false);

//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        Location location = locationManager.getLastKnownLocation(provider);


        // Connect to Google Play Services
        // To connect to the API, you need to create an instance of the Google Play services API client.
        // using the GoogleApiClient.Builder class to add the LocationServices API, as the following code snippet shows.
        // Create an instance of GoogleAPIClient.


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    //.addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        //permissions
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);
//        } else {
//            // Show rationale and request permission.
//        }

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // permission qui s'implémente automatiquement suite à l'appel de la méthode requestLocationUpdates() ci-dessous
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //dans onResume va mettre à jour position selon criètres suivants:
        //400: temps min entre les updates
        //1: distance (m) min entre les updates
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //ci-dessous: important sinon va continuer à faire les updates quand on n'en a pas besoin
        //et bouffer de la batterie et du process
        locationManager.removeUpdates(this);
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        //ci-dessous, on ne l'utilise pas pour le moment
//        if (location != null) {
//            lat = (int) location.getLatitude();
//            lng = (int) location.getLongitude();
//        }
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
        currentPos = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(currentPos).title("User position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("testmaps", "Google Maps Connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
    //location est connu du système et ne doit pas être instancié donc
        // quand localisation change, on arrive dans cette méthode
        lat = (int) location.getLatitude();
        lng = (int) location.getLongitude();
        currentPos = new LatLng(lat, lng);

        Log.i("c3trace", "onLocationChanged: lat" +lat);
        Log.i("c3trace", "onLocationChanged: lng" +lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
