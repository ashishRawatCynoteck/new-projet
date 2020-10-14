package com.cynoteck.newproject;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoveOnMap extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    List<LatLng> polylineList;
    Marker marker;
    float v;
    double lat, lng, end_lat, end_log;
    Handler handler;
    LatLng startPostion, endPostion;
    int index, next;
    Button btnGo;
    EditText editPlace;
    String destination;
    PolylineOptions polylineOptions, blackPolylineOptions;
    Polyline blackPolyline, greyPolyline;
    LatLng myLocation;

    RetrofitApi myService;


    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Button button;
    String destination_address = "", destination_latlng = "";
    Double dest_latitude = 0.0, dest_longitude = 0.0, src_latitude = 0.0, src_longitude = 0.0;
    String CAMERA="location";
    private AutocompleteSupportFragment autocompleteFragment;
    LinearLayout maoLL;
    Context context;
    public static String mapclear = "";
    String addresss = "", city ="", state ="";
    Polyline polyline = null;

    LatLng latLng;
    String mapready ="";
    Marker mCurrLocationMarker;
    LatLng latlngNew;
    ArrayList directionList;


    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> listPoints;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragmentactivity);
        button = findViewById(R.id.findMap);
        maoLL = findViewById(R.id.mapLL);
        Places.initialize(this, "AIzaSyClXYwahInayLuwd5sQpm5k2jVW2Oc8490", Locale.US);

        polylineList = new ArrayList<>();
        mapFragment();
        autoComp();
        autocompClick();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapReady(mMap);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(end_lat,end_log));
                markerOptions.title("leangth");
                float results[] = new float[10];
                Location.distanceBetween(src_latitude,src_longitude,dest_latitude,dest_longitude,results);
                markerOptions.snippet("distance"+results[0]);
                mMap.addMarker(markerOptions);
                Toast.makeText(MoveOnMap.this, "distance"+results[0], Toast.LENGTH_SHORT).show();
            }

        });

        myService = CommonBaseUrl.getGoogleApi();

    }


    private void autocompClick() {

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.e("TAG", "Place: " + place.getName() + ", " + place.getId()+", ");
                Log.e("componet",place.getAddressComponents().toString());
                if (place.getAddressComponents().asList().size()<=1){
                    destination_address = place.getAddressComponents().asList().get(0).getName();

                }else {
                    city = place.getAddressComponents().asList().get(0).getName();
                    state = place.getAddressComponents().asList().get(2).getName();
                    destination_address=place.getAddress();

                }

                mapclear = "map";
                destination_latlng = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                destination_address = place.getAddress().trim();
                destination_latlng = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                dest_longitude = place.getLatLng().longitude;
                dest_latitude = place.getLatLng().latitude;
                Log.e("dest_address", destination_address + "\n" + destination_latlng);
                CAMERA = "dest";
                mMap.clear();
                latlngNew = new LatLng(dest_latitude, dest_longitude);
                MarkerOptions markerOptionx = new MarkerOptions();
                markerOptionx.position(latlngNew);
                mMap.addMarker(markerOptionx);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                mCurrLocationMarker = mMap.addMarker(markerOptions);


            }

            @Override
            public void onError(Status status) {
                Log.e("TAG", "An error occurred: " + status);
            }
        });

    }

    private void autoComp() {

        autocompleteFragment = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS,Place.Field.ADDRESS_COMPONENTS));
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void mapFragment() {
        context = this;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        directionList = new ArrayList();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        src_latitude = location.getLatitude();
        src_longitude = location.getLongitude();
        latLng = new LatLng(src_latitude, src_longitude);
        //listPoints.add(latLng);

        mMap.addMarker(new MarkerOptions()
                .position(latLng));
        if (CAMERA.equals("location")) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        String requestUrl  =null;

        try {

            String str_org = "origin=" + latLng.latitude +","+latLng.longitude;
            //Value of destination
            String str_dest = "destination=" + latlngNew.latitude+","+latlngNew.longitude;
            //Set value enable the sensor
            String sensor = "sensor=false";
            //Mode for find direction
            String mode = "mode=driving";
            //Build the full param
            final String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
            //Output format
            String output = "json";
            //Create url to request
            requestUrl = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param + "&key=" + "AIzaSyClXYwahInayLuwd5sQpm5k2jVW2Oc8490";

            myService.getDataFromGoogleApi(requestUrl).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = decodePoly(polyline);
                        }


                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng:polylineList)
                            builder.include(latLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                        mMap.animateCamera(mCameraUpdate);


                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GREEN);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.RED);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.endCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        mMap.addMarker(new MarkerOptions().position(polylineList.get(polylineList.size()-1)));
                        Log.e("size", String.valueOf(polylineList.size()));
                        Log.e("size-1", String.valueOf(polylineList.size()-1));
                        Log.e("polyinelist", String.valueOf(polylineList));

//                        ValueAnimator polylineAnimator = ValueAnimator.ofInt(0,100);
//                        polylineAnimator.setDuration(2000);
//                        polylineAnimator.setInterpolator(new LinearInterpolator());
//                        polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator animation) {
//
//                                List<LatLng> points = greyPolyline.getPoints();
//                                Log.e("points", String.valueOf(points));
//                                int percentValue = (int) animation.getAnimatedValue();
//                                Log.e("percentValue", String.valueOf(percentValue));
//                                int size = points.size();
//                                int newpoints = (int) (size * (percentValue/100.0f));
//                                Log.e("newpoints", String.valueOf(newpoints));
//                                List<LatLng> P = points.subList(0,newpoints);
//                                Log.e("pointsP", String.valueOf(P));
//                                greyPolyline.setPoints(P);
//
//
//                            }
//                        });
//
//                        polylineAnimator.start();
//                        int height = 80;
//                        int width = 60;
//                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.google);
//                        Bitmap b=bitmapdraw.getBitmap();
//                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
//
//                        marker = mMap.addMarker(new MarkerOptions().position(latLng).flat(true).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
//
//
//                        handler = new Handler();
//
//                        index = -1;
//                        next = 1;
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                if (index < polylineList.size()-1){
//                                    index++;
//                                    next = index+1;
//                                }
//                                if (index<polylineList.size()-1){
//                                    startPostion = polylineList.get(index);
//                                    endPostion = polylineList.get(next);
//
//                                    Log.e("startpostion",startPostion.toString());
//                                    Log.e("endpostion",endPostion.toString());
//                                }
//
//                                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
//                                valueAnimator.setDuration(2000);
//                                valueAnimator.setInterpolator(new LinearInterpolator());
//                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                    @Override
//                                    public void onAnimationUpdate(ValueAnimator animation) {
//                                        v = animation .getAnimatedFraction();
//                                        Log.e("v", String.valueOf(v));
//
//                                        lng = v*endPostion.longitude+(1-v)*startPostion.longitude;
//                                        lat = v*endPostion.latitude+(1-v)*startPostion.latitude;
//                                        Log.e("lng", String.valueOf(lng));
//                                        Log.e("lat", String.valueOf(lat));
//                                        LatLng newPos = new LatLng(lat,lng);
//                                        Log.e("newPos",newPos.toString());
//                                        marker.setPosition(newPos);
//                                        marker.setAnchor(0.5f,0.5f);
//                                        marker.setRotation(-1);
//                                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                                                .target(newPos).zoom(15.5f).build()));
//
//                                    }
//                                });
//
//                                valueAnimator.start();
//                                handler.postDelayed(this,3000);
//
//                            }
//                        },3000);


                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }




    }

    private float getBearing(LatLng startPostion, LatLng newPos) {
        double lat = Math.abs(startPostion.latitude - newPos.latitude);
        double lng = Math.abs(startPostion.longitude - newPos.longitude);

        if (startPostion.latitude<newPos.latitude&&startPostion.longitude<newPos.longitude){
            return (float) Math.toDegrees(Math.atan(lng/lat));
        }else  if (startPostion.latitude>=newPos.latitude&&startPostion.longitude<newPos.longitude){
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        }else  if (startPostion.latitude>=newPos.latitude&&startPostion.longitude>=newPos.longitude){
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        }else  if (startPostion.latitude<newPos.latitude&&startPostion.longitude>=newPos.longitude){
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        }
        return -1;

    }

    private List<LatLng> decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        dest_latitude = marker.getPosition().latitude;
        dest_longitude = marker.getPosition().longitude;
    }
}
