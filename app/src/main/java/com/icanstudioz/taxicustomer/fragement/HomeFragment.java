package com.icanstudioz.taxicustomer.fragement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.icanstudioz.taxicustomer.Server.Server;
import com.icanstudioz.taxicustomer.custom.CheckConnection;
import com.icanstudioz.taxicustomer.pojo.NearbyData;
import com.icanstudioz.taxicustomer.pojo.Pass;
import com.icanstudioz.taxicustomer.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

import com.icanstudioz.taxicustomer.R;
import com.icanstudioz.taxicustomer.acitivities.HomeActivity;
import com.icanstudioz.taxicustomer.custom.GPSTracker;
import com.loopj.android.http.RequestParams;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * Created by android on 7/3/17.
 */

public class HomeFragment extends FragmentManagePermission implements OnMapReadyCallback, DirectionCallback, Animation.AnimationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private String driver_id;
    private String cost;
    private String unit;
    private int PLACE_PICKER_REQUEST = 7896;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1234;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Double currentLatitude;
    private Double currentLongitude;
    private View rootView;
    Boolean flag = false;
    GoogleMap myMap;
    ImageView current_location, clear;
    PlaceDetectionClient mPlaceDetectionClient;
    private RelativeLayout header, footer;
    Animation animFadeIn, animFadeOut;
    TextView pickup_location, drop_location;
    RelativeLayout relative_drop;
    RelativeLayout linear_pickup;
    TextView txt_vehicleinfo, rate, txt_info, txt_cost, txt_color, txt_address, request_ride;
    LinearLayout linear_request;
    SessionManager sessionManager;
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};
    private String drivername;
    MapView mMapView;
    Pass pass;
    Place pickup, drop;
    ProgressBar progressBar;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //  MapsInitializer.initialize(this.getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.home_fragment, container, false);
            ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.home));
            bindView(savedInstanceState);
            if (!CheckConnection.haveNetworkConnection(getActivity())) {
                Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askCompactPermissions(permissionAsk, new PermissionResult() {
                    @Override
                    public void permissionGranted() {
                        if (!GPSEnable()) {
                            tunonGps();
                        } else {
                            getcurrentlocation();
                        }
                    }

                    @Override
                    public void permissionDenied() {

                    }

                    @Override
                    public void permissionForeverDenied() {

                        openSettingsApp(getActivity());
                    }
                });

            } else {
                if (!GPSEnable()) {
                    tunonGps();
                } else {
                    getcurrentlocation();
                }

            }


            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (header.getVisibility() == View.VISIBLE && footer.getVisibility() == View.VISIBLE) {
                        header.startAnimation(animFadeOut);
                        footer.startAnimation(animFadeOut);
                        header.setVisibility(View.GONE);
                        footer.setVisibility(View.GONE);
                    }
                }
            });
            linear_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CheckConnection.haveNetworkConnection(getActivity())) {
                        if (pickup_location.getText().toString().trim().equals("") && drop_location.getText().toString().trim().equals("")) {
                            Toast.makeText(getActivity(), getString(R.string.invalid_location), Toast.LENGTH_LONG).show();
                        } else if (pickup == null && drop == null) {
                            Toast.makeText(getActivity(), getString(R.string.invalid_location), Toast.LENGTH_LONG).show();
                        } else if (driver_id == null || drivername == null) {
                            Toast.makeText(getActivity(), getString(R.string.select_driver), Toast.LENGTH_LONG).show();
                        } else if (cost == null || unit == null) {
                            Toast.makeText(getActivity(), getString(R.string.invalid_fare), Toast.LENGTH_SHORT).show();
                        } else {
                            Bundle bundle = new Bundle();
                            pass.setFromPlace(pickup);
                            pass.setToPlace(drop);
                            pass.setDriverId(driver_id);
                            pass.setFare(cost);
                            pass.setDriverName(drivername);
                            bundle.putSerializable("data", pass);
                            RequestFragment fragobj = new RequestFragment();
                            fragobj.setArguments(bundle);
                            ((HomeActivity) getActivity()).changeFragment(fragobj, getString(R.string.request_ride));
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();

                    }
                }
            });

            pickup_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .build(getActivity());
                        startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            });
            drop_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .build(getActivity());
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                }
            });

        } catch (InflateException e) {

        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("result");
                getcurrentlocation();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                pickup = PlaceAutocomplete.getPlace(getActivity(), data).freeze();
                pickup_location.setText(pickup.getAddress());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getActivity(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                drop = PlaceAutocomplete.getPlace(getActivity(), data).freeze();
                drop_location.setText(drop.getAddress());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getActivity(), status.getStatusMessage(), Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMapView != null) {

            mMapView.onStart();
            if (currentLatitude != null && !currentLatitude.equals(0.0) && currentLongitude != null && !currentLongitude.equals(0.0)) {
                NeaBy(String.valueOf(currentLatitude), String.valueOf(currentLongitude));
            }
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {

            mMapView.onResume();

        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void multipleMarker(List<NearbyData> list) {
        if (list != null) {
            for (NearbyData location : list) {
                Double latitude = null;
                Double longitude = null;
                try {
                    latitude = Double.valueOf(location.getLatitude());
                    longitude = Double.valueOf(location.getLongitude());

                    Marker marker = myMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(location.getName())
                            .snippet(location.getVehicle_info()));
                    marker.setTag(location);
                } catch (NumberFormatException e) {

                }

                CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 10);
                myMap.animateCamera(camera);
            }
        }

    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {


    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public void bindView(Bundle savedInstanceState) {
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        MapsInitializer.initialize(this.getActivity());
        current_location = (ImageView) rootView.findViewById(R.id.current_location);
        clear = (ImageView) rootView.findViewById(R.id.clear);
        txt_vehicleinfo = (TextView) rootView.findViewById(R.id.txt_vehicleinfo);
        rate = (TextView) rootView.findViewById(R.id.rate);

        txt_info = (TextView) rootView.findViewById(R.id.txt_info);
        txt_address = (TextView) rootView.findViewById(R.id.txt_addresss);
        request_ride = (TextView) rootView.findViewById(R.id.request_rides);
        txt_color = (TextView) rootView.findViewById(R.id.txt_color);
        txt_cost = (TextView) rootView.findViewById(R.id.txt_cost);
        sessionManager = new SessionManager(getActivity());

        mMapView = (MapView) rootView.findViewById(R.id.mapview);
        linear_request = (LinearLayout) rootView.findViewById(R.id.linear_request);
        header = (RelativeLayout) rootView.findViewById(R.id.header);
        footer = (RelativeLayout) rootView.findViewById(R.id.footer);
        pickup_location = (TextView) rootView.findViewById(R.id.pickup_location);
        drop_location = (TextView) rootView.findViewById(R.id.drop_location);
        linear_pickup = (RelativeLayout) rootView.findViewById(R.id.linear_pickup);
        relative_drop = (RelativeLayout) rootView.findViewById(R.id.relative_drop);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
        mMapView.getMapAsync(this);
        mMapView.onCreate(savedInstanceState);
        pass = new Pass();
        // load animations
        animFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getActivity(),
                R.anim.fade_out);
        animFadeIn.setAnimationListener(this);
        animFadeOut.setAnimationListener(this);
        applyfonts();


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drop_location.setText("");
                if (header.getVisibility() == View.VISIBLE && footer.getVisibility() == View.VISIBLE) {
                    header.startAnimation(animFadeOut);
                    footer.startAnimation(animFadeOut);
                    header.setVisibility(View.GONE);
                    footer.setVisibility(View.GONE);
                }
            }
        });

        current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    askCompactPermissions(permissionAsk, new PermissionResult() {
                        @Override
                        public void permissionGranted() {
                            if (pickup_location.getText().toString().trim().equals("")) {
                                setCurrentLocation();
                            } else {
                                pickup_location.setText("");
                                current_location.setColorFilter(ContextCompat.getColor(getActivity(), R.color.black));
                            }
                        }

                        @Override
                        public void permissionDenied() {

                        }

                        @Override
                        public void permissionForeverDenied() {
                            Snackbar.make(rootView, getString(R.string.allow_permission), Snackbar.LENGTH_LONG).show();
                            openSettingsApp(getActivity());

                        }
                    });
                } else {
                    if (!GPSEnable()) {
                        tunonGps();
                    } else {
                        if (pickup_location.getText().toString().trim().equals("")) {
                            setCurrentLocation();
                        } else {
                            pickup_location.setText("");
                            current_location.setColorFilter(ContextCompat.getColor(getActivity(), R.color.black));
                        }

                    }

                }
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                //If everything went fine lets get latitude and longitude
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                if (!currentLatitude.equals(0.0) && !currentLongitude.equals(0.0)) {
                    if (!flag) {
                        NeaBy(String.valueOf(currentLatitude), String.valueOf(currentLongitude));
                    }
                } else {

                    Toast.makeText(getActivity(), getString(R.string.couldnt_get_location), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            askCompactPermissions(permissionAsk, new PermissionResult() {
                @Override
                public void permissionGranted() {

                }

                @Override
                public void permissionDenied() {
                }

                @Override
                public void permissionForeverDenied() {
                    Snackbar.make(rootView, getString(R.string.allow_permission), Snackbar.LENGTH_LONG).show();
                    openSettingsApp(getActivity());
                }
            });

        }


    }

    private void setCurrentLocation() {
        if (!GPSEnable()) {
            tunonGps();

        } else {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                try {
                    @SuppressLint("MissingPermission") Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                    placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            try {
                                if(task.isSuccessful()){
                                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                        pickup = placeLikelihood.getPlace().freeze();
                                        pickup_location.setText(placeLikelihood.getPlace().getAddress());
                                        current_location.setColorFilter(ContextCompat.getColor(getActivity(), R.color.current_lolcation));

                                    }
                                    likelyPlaces.release();
                                }
                            } catch (Exception e) {

                            }

                        }
                    });
                } catch (Exception e) {

                }


            }
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {

                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
        }

    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }


    public void applyfonts() {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf");
        Typeface font1 = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf");
        pickup_location.setTypeface(font);
        drop_location.setTypeface(font);
        txt_vehicleinfo.setTypeface(font1);
        rate.setTypeface(font1);

        txt_color.setTypeface(font);
        txt_address.setTypeface(font);
        request_ride.setTypeface(font1);


    }

    public void NeaBy(String latitude, String longitude) {
        flag = true;
        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("long", longitude);
        Server.setHeader(sessionManager.getKEY());
        Server.get("api/user/nearby/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        Gson gson = new GsonBuilder().create();
                        List<NearbyData> list = gson.fromJson(response.getJSONArray("data").toString(), new TypeToken<List<NearbyData>>() {

                        }.getType());

                        multipleMarker(list);

                        cost = response.getJSONObject("fair").getString("cost");
                        unit = response.getJSONObject("fair").getString("unit");
                        sessionManager.setUnit(unit);
                    }
                } catch (JSONException e) {


                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Toast.makeText(getActivity(), getString(R.string.try_again), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    public void getcurrentlocation() {

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
    }

    public void tunonGps() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(30 * 1000);
            mLocationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            getcurrentlocation();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and setting the result in onActivityResult().
                                status.startResolutionForResult(getActivity(), 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }

    }

    public Boolean GPSEnable() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.canGetLocation()) {
            return true;

        } else {
            return false;
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {

                View v = getActivity().getLayoutInflater().inflate(R.layout.view_custom_marker, null);

                LatLng latLng = marker.getPosition();
                TextView title = (TextView) v.findViewById(R.id.t);
                TextView t1 = (TextView) v.findViewById(R.id.t1);
                TextView t2 = (TextView) v.findViewById(R.id.t2);
                ImageView imageView = (ImageView) v.findViewById(R.id.profile_image);
                Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf");
                t1.setTypeface(font);
                t2.setTypeface(font);
                String name = marker.getTitle();
                title.setText(name);
                String info = marker.getSnippet();
                t1.setText(info);

                NearbyData nearbyData = (NearbyData) marker.getTag();
                if (nearbyData != null) {
                    pass.setVehicleName(nearbyData.getVehicle_info());
                    txt_info.setText(nearbyData.getVehicle_info());
                    txt_address.setText("");
                    driver_id = nearbyData.getUser_id();
                    drivername = marker.getTitle();
                    t2.setVisibility(View.VISIBLE);
                } else {
                    t2.setVisibility(View.GONE);
                }
                txt_cost.setText(cost + "  " + unit);
                txt_address.setText(getAdd(Double.valueOf(nearbyData.getLatitude()), Double.valueOf(nearbyData.getLongitude())) + " ");


                return v;

            }
        });

        myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                NearbyData nearbyData = (NearbyData) marker.getTag();
                if (nearbyData != null) {
                    driver_id = nearbyData.getUser_id();
                    drivername = marker.getTitle();
                }


                if (header.getVisibility() == View.VISIBLE && footer.getVisibility() == View.VISIBLE) {
                    header.startAnimation(animFadeOut);
                    footer.startAnimation(animFadeOut);
                    header.setVisibility(View.GONE);
                    footer.setVisibility(View.GONE);
                } else {

                    header.setVisibility(View.VISIBLE);
                    footer.setVisibility(View.VISIBLE);
                    header.startAnimation(animFadeIn);
                    footer.startAnimation(animFadeIn);
                }

            }
        });

        if (myMap != null) {

            tunonGps();
        }

    }


    private String getAdd(double latitude, double longitude) {
        String finalAddress = null;
        try {

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            finalAddress = address + ", " + city + "," + state + "," + country;


        } catch (Exception e) {

        }
        return finalAddress;
    }
}


