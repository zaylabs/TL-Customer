package com.icanstudioz.taxicustomer.fragement;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import gun0912.tedbottompicker.TedBottomPicker;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.icanstudioz.taxicustomer.R;
import com.icanstudioz.taxicustomer.Server.Server;
import com.icanstudioz.taxicustomer.acitivities.HomeActivity;
import com.icanstudioz.taxicustomer.custom.CheckConnection;
import com.icanstudioz.taxicustomer.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.widget.Toast;
import com.thebrownarrow.permissionhelper.FragmentManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by android on 14/3/17.
 */
public class ProfileFragment extends FragmentManagePermission implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    View view;
    public File imageFile;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    SessionManager sessionManager;
    String permissionAsk[] = {PermissionUtils.Manifest_CAMERA, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_ACCESS_COARSE_LOCATION};
    ProfileUpdateListener profileUpdateListener;
    UpdateListener listener;
    EditText input_vehicle, input_name, input_password, input_mobile;
    TextView input_email;
    AppCompatButton btn_update, btn_change;
    ImageView profile_pic;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Double currentLatitude;
    ProgressBar progressBar;
    private Double currentLongitude;
    SwipeRefreshLayout swipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.setting, container, false);
        bindView();

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Server.setHeader(sessionManager.getKEY());
                UpdateUser();

            }
        });
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int MyVersion = Build.VERSION.SDK_INT;
                if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    if (!checkIfAlreadyhavePermission()) {
                        requestForSpecificPermission();
                    } else {
                        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                    @Override
                                    public void onImageSelected(Uri uri) {
                                        // here is selected uri
                                        imageFile = new File(uri.getPath());
                                        // profile_pic.setImageURI(uri);
                                        String format = getMimeType(getActivity(), uri);
                                        upload_pic(format);
                                       /* if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("png") || format.equalsIgnoreCase("gif") || format.equalsIgnoreCase("jpeg")) {

                                        } else {
                                            Toast.makeText(getActivity(), "jpg,png or gif is only accepted", Toast.LENGTH_LONG).show();
                                        }*/
                                    }
                                }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                    @Override
                                    public void onError(String message) {
                                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                                        Log.d(getTag(), message);
                                    }
                                })
                                .create();

                        tedBottomPicker.show(getActivity().getSupportFragmentManager());
                    }


                } else {
                    TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                            .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                @Override
                                public void onImageSelected(Uri uri) {
                                    // here is selected uri
                                    imageFile = new File(uri.getPath());
                                    //  profile_pic.setImageURI(uri);
                                    String format = getMimeType(getActivity(), uri);
                                    upload_pic(format);
                                }
                            }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                                    Log.d(getTag(), message);
                                }
                            })
                            .create();

                    tedBottomPicker.show(getActivity().getSupportFragmentManager());
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            profileUpdateListener = (ProfileUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement profileUpdateListener");
        }
        try {
            listener = (UpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener");
        }
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public void bindView() {
        ((HomeActivity) getActivity()).fontToTitleBar(getString(R.string.profile));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        profile_pic = (ImageView) view.findViewById(R.id.profile_pic);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        btn_change = (AppCompatButton) view.findViewById(R.id.btn_change);

        input_email = (TextView) view.findViewById(R.id.input_email);
        //input_vehicle = (EditText) view.findViewById(R.id.input_vehicle);
        input_name = (EditText) view.findViewById(R.id.input_name);
        //  input_password = (EditText) view.findViewById(R.id.input_password);
        input_mobile = (EditText) view.findViewById(R.id.input_mobile);
        btn_update = (AppCompatButton) view.findViewById(R.id.btn_update);

        MediumFont(input_email);
        //  MediumFont(input_vehicle);
        MediumFont(input_name);

        MediumFont(input_mobile);
        BookFont(btn_update);
        BookFont(btn_change);

        sessionManager = new SessionManager(getActivity());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        askCompactPermissions(permissionAsk, new PermissionResult() {
            @Override
            public void permissionGranted() {
                if (!GPSEnable()) {
                    turnonGps();
                } else {
                    getCurrentlOcation();
                }
            }

            @Override
            public void permissionDenied() {

            }

            @Override
            public void permissionForeverDenied() {

            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getActivity())) {
                    changepassword_dialog(getString(R.string.change_password));
                } else {
                    Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
                }
            }
        });
        if (CheckConnection.haveNetworkConnection(getActivity())) {
            getUserInfo();
        } else {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
            SessionManager sessionManager = new SessionManager(getActivity());
            HashMap<String, String> user = sessionManager.getUserDetails();
            if (user != null) {
                String name = user.get(SessionManager.KEY_NAME);
                String email = user.get(SessionManager.KEY_EMAIL);
                String mobile = user.get(SessionManager.KEY_MOBILE);
                String uid = user.get(SessionManager.USER_ID);
                String avatar = user.get(SessionManager.AVATAR);
                input_name.setText(name);
                input_email.setText(email);
                input_mobile.setText(mobile);
                Glide.with(getActivity()).load(avatar).error(R.mipmap.ic_account_circle_black_24dp).into(profile_pic);
            }

        }
    }

    public void BookFont(AppCompatButton view1) {
        Typeface font1 = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf");
        view1.setTypeface(font1);
    }

    public void MediumFont(TextView view) {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf");
        view.setTypeface(font);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("permisson", "granted");
                    TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(getActivity())
                            .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                                @Override
                                public void onImageSelected(Uri uri) {
                                    // here is selected uri
                                    profile_pic.setImageURI(uri);
                                }
                            }).setOnErrorListener(new TedBottomPicker.OnErrorListener() {
                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getActivity(), getString(R.string.try_again), Toast.LENGTH_LONG).show();
                                    Log.d(getTag(), message);
                                }
                            })
                            .create();

                    tedBottomPicker.show(getActivity().getSupportFragmentManager());

                } else {

                }
            }
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    private boolean checkIfAlreadyhavePermission() {
        int fine = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        int read = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (fine == PackageManager.PERMISSION_GRANTED) {
            Log.e("permission1", "fine");
            return true;

        }
        if (read == PackageManager.PERMISSION_GRANTED) {
            Log.e("permission2", "coarse");
            return true;
        }
        if (write == PackageManager.PERMISSION_GRANTED) {
            Log.e("permission2", "coarse");
            return true;
        } else {
            return false;
        }
    }

    public void UpdateUser() {
        RequestParams params = new RequestParams();
        params.put("mobile", input_mobile.getText().toString().trim());
        params.put("name", input_name.getText().toString().trim());

        Server.setHeader(sessionManager.getKEY());

        HashMap<String, String> user = sessionManager.getUserDetails();
        if (user != null) {
            String uid = user.get(SessionManager.USER_ID);
            //String email = user.get(SessionManager.KEY_EMAIL);
            params.put("user_id", uid);
            //  params.put("email", email);
        }
        Server.post("api/user/update/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("success", response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        HashMap<String, String> user = sessionManager.getUserDetails();

                        if (user != null) {
                            String oname = input_name.getText().toString().trim();
                            String oemail = input_email.getText().toString().trim();
                            String omobile = input_mobile.getText().toString().trim();

                            String uid = user.get(SessionManager.USER_ID);
                            String url = user.get(SessionManager.AVATAR);

                            sessionManager.createLoginSession(oname, oemail, uid, url, omobile);
                            SessionManager sessionManager = new SessionManager(getActivity());
                            HashMap<String, String> user1 = sessionManager.getUserDetails();

                            String name = user1.get(SessionManager.KEY_NAME);
                            String email = user1.get(SessionManager.KEY_EMAIL);
                            String mobile = user1.get(SessionManager.KEY_MOBILE);


                            input_name.setText(name);
                            input_email.setText(email);
                            input_mobile.setText(mobile);


                            Toast.makeText(getActivity(), getString(R.string.profile_updated), Toast.LENGTH_LONG).show();
                        }
                        listener.name(input_name.getText().toString().trim());

                    } else {
                        Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });


    }

    public void getCurrentlOcation() {

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

    public void turnonGps() {
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
                            getCurrentlOcation();
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

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public Boolean GPSEnable() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;

        }
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();


            //Toast.makeText(getActivity(), currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
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
                // Log the error
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                getCurrentlOcation();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void upload_pic(String type) {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams();
        if (imageFile != null) {
            try {

                if (type.equals("jpg")) {
                    params.put("avatar", imageFile, "image/jpeg");
                } else if (type.equals("jpeg")) {
                    params.put("avatar", imageFile, "image/jpeg");
                } else if (type.equals("png")) {
                    params.put("avatar", imageFile, "image/png");
                } else {
                    params.put("avatar", imageFile, "image/gif");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d("catch", e.toString());
            }
        }
        Server.setHeader(sessionManager.getKEY());

        HashMap<String, String> user = sessionManager.getUserDetails();
        if (user != null) {
            String uid = user.get(SessionManager.USER_ID);
            //String email = user.get(SessionManager.KEY_EMAIL);
            // params.put("email", email);
            params.put("user_id", uid);
        }
        Log.e("key", sessionManager.getKEY());
        Server.post("api/user/update/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e("success", response.toString());

                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        String rurl = response.getJSONObject("data").getString("avatar");
                        Glide.with(ProfileFragment.this).load(rurl).error(R.mipmap.ic_account_circle_black_24dp).into(profile_pic);
                        profileUpdateListener.update(rurl);

                        HashMap<String, String> user = sessionManager.getUserDetails();
                        if (user != null) {
                            String name = user.get(SessionManager.KEY_NAME);
                            String email = user.get(SessionManager.KEY_EMAIL);
                            String mobile = user.get(SessionManager.KEY_MOBILE);
                            String url = user.get(SessionManager.AVATAR);
                            String uid = user.get(SessionManager.USER_ID);
                            sessionManager.createLoginSession(name, email, uid, rurl, mobile);
                            input_name.setText(name);
                            input_email.setText(email);
                            input_mobile.setText(mobile);
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(getActivity(), getString(R.string.profile_uploaded), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), response.getString("data"), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    Log.e("catch", e.toString());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("fail", responseString);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getString(R.string.profile_uploaded), Toast.LENGTH_LONG).show();

            }
        });

    }

    public void getUserInfo() {
        RequestParams params = new RequestParams();
        HashMap<String, String> user = sessionManager.getUserDetails();
        if (user != null) {
            String uid = user.get(SessionManager.USER_ID);
            params.put("user_id", uid);
        }

        try {
            String url = user.get(SessionManager.AVATAR);
            String name = user.get(SessionManager.KEY_NAME);
            String email = user.get(SessionManager.KEY_EMAIL);
            String mobile = user.get(SessionManager.KEY_MOBILE);

            Glide.with(getActivity()).load(url).error(R.mipmap.ic_account_circle_black_24dp).into(profile_pic);
            input_name.setText(name);
            input_mobile.setText(mobile);
            input_email.setText(email);
        } catch (Exception e) {

        }


        Server.setHeader(sessionManager.getKEY());
        Server.get("api/user/profile/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("success", response.toString());
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        String url = response.getJSONObject("data").getString("avatar");
                        String namee = response.getJSONObject("data").getString("name");
                        Glide.with(getActivity()).load(url).error(R.mipmap.ic_account_circle_black_24dp).into(profile_pic);
                        input_name.setText(response.getJSONObject("data").getString("name"));
                        input_mobile.setText(response.getJSONObject("data").getString("mobile"));
                        input_email.setText(response.getJSONObject("data").getString("email"));
                        HashMap<String, String> user = sessionManager.getUserDetails();
                        if (user != null) {
                            String name = user.get(SessionManager.KEY_NAME);
                            String email = user.get(SessionManager.KEY_EMAIL);
                            String mobile = user.get(SessionManager.KEY_MOBILE);
                            String u = user.get(SessionManager.AVATAR);
                            String uid = user.get(SessionManager.USER_ID);

                            sessionManager.createLoginSession(name, email, uid, url, mobile);
                        }
                        profileUpdateListener.update(url);
                        listener.name(namee);


                    } else {
                        Toast.makeText(getActivity(), getString(R.string.profile_uploaded), Toast.LENGTH_LONG).show();


                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), getString(R.string.profile_uploaded), Toast.LENGTH_LONG).show();

                    Log.d("catch", e.toString());
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Toast.makeText(getActivity(), "fail", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });


    }

    public interface ProfileUpdateListener {
        void update(String url);

    }

    public interface UpdateListener {
        void name(String name);

    }


    public void changepassword_dialog(String title) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        TextView tle = (TextView) dialog.findViewById(R.id.title);
        final EditText password = (EditText) dialog.findViewById(R.id.input_Password);
        final EditText confirm_password = (EditText) dialog.findViewById(R.id.input_confirmPassword);

        AppCompatButton btn_change = (AppCompatButton) dialog.findViewById(R.id.change_password);

        MediumFont(tle);
        MediumFont(password);
        MediumFont(confirm_password);
        BookFont(btn_change);
        tle.setText(title);
        btn_change.setText(getString(R.string.change));


        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    CheckConnection.hideKeyboard(getActivity(), view);
                }
                String oldpassword = password.getText().toString().trim();
                String confirmpassword = confirm_password.getText().toString().trim();
                if (password.getText().toString().trim().equals("")) {
                    password.setError(getString(R.string.old_pws_is_required));
                } else if (!confirm_password.getText().toString().trim().equals("")) {
                    SessionManager sessionManager = new SessionManager(getActivity());
                    HashMap<String, String> user = sessionManager.getUserDetails();
                    if (user != null) {
                        String uid = user.get(SessionManager.USER_ID);
                        if (uid != null && !uid.equals("")) {
                            changepassword(dialog, uid, oldpassword, confirmpassword);
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.relogin), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    confirm_password.setError(getString(R.string.newpwd_required));
                }

            }
        });
        dialog.show();

    }

    private void overrideFonts(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideFonts(context, child);
                }
            } else if (v instanceof AppCompatButton) {
                Log.e("appcompactbutton", "called");
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            } else if (v instanceof EditText) {
                Log.e("edittext", "called");
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Medium.otf"));
            } else if (v instanceof TextView) {
                ((TextView) v).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "font/AvenirLTStd_Book.otf"));
            }

        } catch (Exception e) {
            Log.d("catch", "font settting error  " + e.toString());
        }
    }

    public void changepassword(final Dialog dialog, String id, String oldpassword, String newpassword) {
        RequestParams params = new RequestParams();
        params.put("old_password", oldpassword);
        params.put("new_password", newpassword);
        params.put("user_id", id);
        SessionManager sessionManager = new SessionManager(getActivity());
        Server.setHeader(sessionManager.getKEY());
        Server.post("api/user/change_password/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {

                        dialog.cancel();
                        Log.e("success", response.toString());
                        Toast.makeText(getActivity(), getString(R.string.password_updated), Toast.LENGTH_LONG).show();

                    } else {

                        String error = response.getString("data");
                        Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {

                    Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

                    Log.d("catch", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.e("fail", responseString);
                Toast.makeText(getActivity(), getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                if (getActivity() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }
}
