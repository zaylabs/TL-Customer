package com.icanstudioz.taxicustomer.custom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


/**
 * Created by android on 21/3/17.
 */

public class CheckConnection {

    public static boolean haveNetworkConnection(Context context) {
        boolean conntected = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                conntected = true;
            } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                conntected = true;
            } else {
                conntected = false;
            }
        }
        return conntected;
    }

    public static void hideKeyboard(Context context, View view) {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


    }
}
