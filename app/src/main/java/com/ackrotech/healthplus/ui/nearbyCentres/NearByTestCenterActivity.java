package com.ackrotech.healthplus.ui.nearbyCentres;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ackrotech.healthplus.R;
import com.ackrotech.healthplus.Utility.VolleyUtility;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NearByTestCenterActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_CODE = 1000;

    private double latitude, longitude;
    private Location mLastLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    private static final String TAG = NearByTestCenterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_test_center);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(checkLocationPermission()){
                nearByPlace();
            }
        }

//        mMap.setMyLocationEnabled(true);

    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{

                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{

                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE);
            }
            return false;
        } else
            return true;
    }

    private void nearByPlace() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);


        double lat = location.getLatitude();
        double lng = location.getLongitude();


        String url = getUrl(lat, lng);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            JSONArray res = response.getJSONArray("results");
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject obj = (JSONObject) res.get(i);
                                Log.d(TAG, obj.toString());
                                JSONObject location = obj.getJSONObject("geometry").getJSONObject("location");
                                Long lat = location.getLong("lat");
                                Long lng = location.getLong("lng");
                                String name = obj.getString("name");
                                MarkerOptions markerOptions = new MarkerOptions();
                                LatLng latLng = new LatLng(lat, lng);
                                markerOptions.position(latLng);
                                markerOptions.title(name);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                mMap.addMarker(markerOptions);


                            }
                            //Move Camera
                            LatLng latLng = new LatLng(lat, lng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                        } catch (JSONException e) {
                            Toast.makeText(NearByTestCenterActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.d(TAG, e.getStackTrace().toString());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(NearByTestCenterActivity.this,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });

        VolleyUtility.getInstance(NearByTestCenterActivity.this).addToRequestQueue(req);

    }

    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location="+latitude+","+longitude);
        googlePlacesUrl.append("&radius="+30000);
        googlePlacesUrl.append("&type=hospital");
        googlePlacesUrl.append("&key=AIzaSyB93h-Jh6EjvvnSrdstr3dedaGNyEAEzVw");
        Log.d("getUrl", googlePlacesUrl.toString());
        return googlePlacesUrl.toString();

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //init Google Play Services
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
            }
        }
        else{
            mMap.setMyLocationEnabled(true);
        }
    }




    @Override
    public void onLocationChanged(android.location.Location location) {
        mLastLocation = location;
        if(mMarker != null)
            mMarker.remove();

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Your position");
        mMarker = mMap.addMarker(markerOptions);

        //Move Camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }
}
