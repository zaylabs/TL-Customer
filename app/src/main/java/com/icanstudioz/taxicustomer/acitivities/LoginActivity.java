package com.icanstudioz.taxicustomer.acitivities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.icanstudioz.taxicustomer.R;

import com.icanstudioz.taxicustomer.Server.Server;
import com.icanstudioz.taxicustomer.custom.CheckConnection;
import com.icanstudioz.taxicustomer.custom.SetCustomFont;
import com.icanstudioz.taxicustomer.session.SessionManager;
import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.widget.Toast;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;



public class LoginActivity extends ActivityManagePermission {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private static final String TAG = "login";
    RelativeLayout relative_register;
    EditText input_email, input_password;
    AppCompatButton login;
    SessionManager sessionManager;
    TextView as, txt_createaccount, forgot_password;
    private String token;
    Context context;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        bindView();
        applyfonts();


        relative_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {

                    if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                        login(input_email.getText().toString().trim(), input_password.getText().toString().trim());
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.network), Toast.LENGTH_LONG).show();
                    }

                } else {
                    // do nothing
                }
            }
        });

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                token = FirebaseInstanceId.getInstance().getToken();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                sessionManager.setGcmToken(token);
            }
        }.execute();

    }

    public void bindView() {
        context = LoginActivity.this;
        as = (TextView) findViewById(R.id.as);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        txt_createaccount = (TextView) findViewById(R.id.txt_createaccount);
        input_email = (EditText) findViewById(R.id.input_email);
        input_password = (EditText) findViewById(R.id.input_password);
        relative_register = (RelativeLayout) findViewById(R.id.relative_register);
        login = (AppCompatButton) findViewById(R.id.login);
        sessionManager = new SessionManager(getApplicationContext());
        forgot_password = (TextView) findViewById(R.id.txt_forgotpassword);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckConnection.haveNetworkConnection(getApplicationContext())) {
                    changepassword_dialog();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.network), Toast.LENGTH_LONG).show();
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    public Boolean validate() {
        Boolean value = true;
        if (input_email.getText().toString().equals("") && !android.util.Patterns.EMAIL_ADDRESS.matcher(input_email.getText().toString().trim()).matches()) {
            value = false;
            input_email.setError(getString(R.string.email_invalid));
        } else {
            input_email.setError(null);
        }

        if (input_password.getText().toString().trim().equalsIgnoreCase("")) {
            value = false;
            input_password.setError(getString(R.string.fiels_is_required));
        } else {
            input_password.setError(null);
        }
        return value;
    }

    public void applyfonts() {
        if (getCurrentFocus() != null) {
            SetCustomFont setCustomFont = new SetCustomFont();
            setCustomFont.overrideFonts(getApplicationContext(), getCurrentFocus());
        } else {
            Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
            Typeface font1 = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
            input_email.setTypeface(font1);
            input_password.setTypeface(font1);
            login.setTypeface(font);
            txt_createaccount.setTypeface(font);
            forgot_password.setTypeface(font);
        }


    }

    public void login(String email, String password) {

        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        params.put("utype", "0");
        params.put("gcm_token", token);

        Server.post("user/login/format/json", params, new JsonHttpResponseHandler() {
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
                        SessionManager sessionManager = new SessionManager(LoginActivity.this);

                        String key = response.getJSONObject("data").getString("key");
                        sessionManager.setKEY(key);

                        String name = response.getJSONObject("data").getString("name");
                        String email = response.getJSONObject("data").getString("email");
                        String user_id = response.getJSONObject("data").getString("user_id");
                        String avatar = response.getJSONObject("data").getString("avatar");
                        String mobile = response.getJSONObject("data").getString("mobile");

                        sessionManager.createLoginSession(name, email, user_id, avatar, mobile);
                        if (currentUser == null) {
                            mAuth.signInAnonymously()
                                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "signInAnonymously:success");


                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "signInAnonymously:failure", task.getException());

                                            }

                                            // ...
                                        }
                                    });

                        }

                        Toast.makeText(LoginActivity.this, getString(R.string.success_login), Toast.LENGTH_LONG);
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {

                        Toast.makeText(LoginActivity.this, response.getString("data"), Toast.LENGTH_LONG);

                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, getString(R.string.contact_admin), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();

                    swipeRefreshLayout.setRefreshing(false);

            }
        });


    }

    public void resetPassword(String email, final Dialog dialog) {

        RequestParams params = new RequestParams();
        params.put("email", email);
        Server.post("user/forgot_password/format/json", params, new JsonHttpResponseHandler() {
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
                        String data = response.getString("data");

                        Toast.makeText(LoginActivity.this, data, Toast.LENGTH_LONG).show();

                    } else {
                        String data = response.getString("data");
                        Toast.makeText(LoginActivity.this, data, Toast.LENGTH_LONG).show();


                    }
                } catch (JSONException e) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Toast.makeText(LoginActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                swipeRefreshLayout.setRefreshing(false);

            }
        });


    }

    public void changepassword_dialog() {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_reset);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        final EditText email = (EditText) dialog.findViewById(R.id.input_email);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView message = (TextView) dialog.findViewById(R.id.message);

        AppCompatButton btn_change = (AppCompatButton) dialog.findViewById(R.id.btn_reset);
        AppCompatButton btn_cancel = (AppCompatButton) dialog.findViewById(R.id.btn_cancel);

        Typeface font = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Medium.otf");
        Typeface font1 = Typeface.createFromAsset(getAssets(), "font/AvenirLTStd_Book.otf");
        btn_change.setTypeface(font1);
        btn_cancel.setTypeface(font1);
        email.setTypeface(font);
        title.setTypeface(font);
        message.setTypeface(font);


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LoginActivity.this.getCurrentFocus();
                if (view != null) {
                    CheckConnection.hideKeyboard(LoginActivity.this, view);
                }
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) {
                    //dialog.cancel();
                    resetPassword(email.getText().toString().trim(), dialog);

                } else {
                    email.setError(getString(R.string.email_invalid));
                    // Toast.makeText(LoginActivity.this, "email is invalid", Toast.LENGTH_LONG).show();
                }


            }
        });
        dialog.show();

    }
}
