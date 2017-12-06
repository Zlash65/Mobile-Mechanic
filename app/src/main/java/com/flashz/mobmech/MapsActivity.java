package com.flashz.mobmech;

import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button btnShowLocation;
    private static  final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnShowLocation = (Button) findViewById(R.id.locate);


        try {
            if(ActivityCompat.checkSelfPermission(this,mPermission) != MockPackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[] {mPermission},REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//         Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        // Add markers for all the mechanics

        // Toast "message"

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
        mMap.setMyLocationEnabled(true);
        final GPSTracker gps = new GPSTracker(MapsActivity.this);

        if (gps.canGetLocation()) {
            LatLng latlng = new LatLng(gps.getLatitude(), gps.getLongitude());
//            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
//                    + gps.getLatitude() + "\nLong: " + gps.getLongitude(), Toast.LENGTH_LONG).show();
            mMap.addMarker(new MarkerOptions().position(latlng).title("You are here!"));
            float zoomLevel = (float) 7;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoomLevel));
        } else {
            gps.showSettingsAlert();
        }

        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!gps.canGetLocation()) {
                    Toast.makeText(getApplicationContext(), "Cannot get GPS location.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Notify loading..
                Toast.makeText(getApplicationContext(), "Fetching mechanics nearby..", Toast.LENGTH_SHORT).show();

                // Send Request
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String IP = "192.168.43.174"; // Change the IP to the IP of laptop. If you use another hotspot then take care to edit here and rebuild.
                int kms = 3; // How far can the mechanics be. Change this parameter to search for more mechanics that would be farther
//                String url ="http://" + IP + "/Projects/mobile/find.php?lat=18.5302414&long=73.9137111&dist=2";
                String url ="http://" + IP + "/Projects/mobile/find.php?lat=" + gps.getLatitude() + "&long=" + gps.getLongitude() + "&dist=" + kms;

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String jsonStr) {
                                try {
                                    JSONObject response = new JSONObject(jsonStr);
                                    String status = response.getString("status");
                                    String message = response.getString("message");

                                    if (!status.equals("success")) {
                                        // Failure handliing code
                                        // Toast "message" or something..
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                    JSONArray mechanics = response.getJSONArray("mechanics");
                                    ArrayList<Mechanic> mechanicsList = new ArrayList<Mechanic>();
                                    for (int i = 0; i < mechanics.length(); i++) {
                                        JSONObject m = mechanics.getJSONObject(i);
                                        mechanicsList.add(new Mechanic(
                                                m.getInt("id"),
                                                m.getString("name"),
                                                m.getString("contact"),
                                                m.getDouble("latitude"),
                                                m.getDouble("longitude"),
                                                m.getInt("distance")
                                        ));
                                    }

//                    Toast.makeText(getApplicationContext(), mechanicsList.size()+"", Toast.LENGTH_LONG).show();

                                    String str;
                                    LatLng latlng;
                                    for (int i = 0; i < mechanicsList.size(); i++) {
                                        Mechanic m = mechanicsList.get(i);
                                        // Add marker
                                        str = m.name;
//                                        Toast.makeText(getApplicationContext(), str+"", Toast.LENGTH_LONG).show();
                                        latlng = new LatLng(m.latitude, m.longitude);
                                        mMap.addMarker(new MarkerOptions().position(latlng).title(str));
                                        float zoomLevel = (float) 7;
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoomLevel));

                                    }

//                    JSONObject m = mechanics.getJSONObject(0);
//                    String str = m.getString("name");
//                    LatLng latlng = new LatLng(m.getLong("latitude"), m.getLong("longitude"));
//                    mMap.addMarker(new MarkerOptions().position(latlng).title(str));
//                    float zoomLevel = (float) 6;
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,zoomLevel));
//                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();

                                }
                                catch(Exception ex){
                                    Toast.makeText(getApplicationContext(), "Something went wrong while parsing the response.", Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast that it didn't work
                        Toast.makeText(getApplicationContext(), "Something went wrong while fetching the data. Please try again.", Toast.LENGTH_SHORT).show();

                    }
                });
                // Add the request to the RequestQueue.
                queue.add(stringRequest);

                // Process JSON



                //create class object

//                String jsonStr = "{\"status\":\"success\",\"message\":\"2 mechanics found nearby!\",\"mechanics\":[{\"id\":\"1\",\"name\":\"Umang Galaiya\",\"contact\":\"9974768388\",\"latitude\":\"18.5302414\",\"longitude\":\"73.9137111\",\"distance\":0},{\"id\":\"4\",\"name\":\"John Doe\",\"contact\":\"9974768388\",\"latitude\":\"18.532741\",\"longitude\":\"73.914505\",\"distance\":1039}]}";

            }
        });


    }
}
