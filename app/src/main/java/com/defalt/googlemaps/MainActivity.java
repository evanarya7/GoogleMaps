package com.defalt.googlemaps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity {
    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private List<MyLocation> locationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMaps();
    }

    private void setMaps() {
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                findViewById(R.id.loading).setVisibility(View.GONE);
                findViewById(R.id.maps).setVisibility(View.VISIBLE);

                while(locationList.size() == 0) {
                    try {
                        locationList = new MapCallback().execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                boolean runOnce = true;
                for (int i = 0; i < locationList.size(); i++) {
                    LatLng mLatLng = new LatLng(locationList.get(i).getLatitude(), locationList.get(i).getLongitude());
                    mGoogleMap.addMarker(new MarkerOptions().position(mLatLng).title(locationList.get(i).getName()).draggable(false));

                    if(runOnce) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 8));
                        runOnce = false;
                    }
                }

                mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        double latitude = marker.getPosition().latitude;
                        double longitude = marker.getPosition().longitude;
                        Toast.makeText(MainActivity.this, latitude + ";" + longitude, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private static class MapCallback extends AsyncTask<Void, Void, List<MyLocation>> {
        private List<MyLocation> locationList = new ArrayList<>();

        @Override
        protected List doInBackground(Void... args) {
            String url = "https://dev.projectlab.co.id/mit/1317003/get_location.php";
            JSONParser jsonParser = new JSONParser();
            JSONObject json = jsonParser.makeHttpRequest(url, "GET", new ArrayList<NameValuePair>());

            if (json != null) {
                try {
                    int success = json.getInt("success");

                    if (success == 1) {
                        JSONArray locations = json.getJSONArray("locations");

                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject c = locations.getJSONObject(i);

                            MyLocation loc = new MyLocation(c.getString("name"), c.getDouble("latitude"), c.getDouble("longitude"));
                            locationList.add(loc);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return locationList;
        }
    }

}
