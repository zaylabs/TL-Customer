package com.icanstudioz.taxicustomer.fragement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.icanstudioz.taxicustomer.R;
import com.icanstudioz.taxicustomer.Server.Server;
import com.icanstudioz.taxicustomer.acitivities.HomeActivity;
import com.icanstudioz.taxicustomer.adapter.AcceptedRequestAdapter;
import com.icanstudioz.taxicustomer.custom.CheckConnection;
import com.icanstudioz.taxicustomer.custom.SetCustomFont;
import com.icanstudioz.taxicustomer.pojo.PendingRequestPojo;
import com.icanstudioz.taxicustomer.session.SessionManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by android on 10/3/17.
 */

public class AcceptedRequestFragment extends Fragment {
    private View view;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    String userid = "";
    String key = "";
    SessionManager sessionManager;
    TextView txt_error;
    String status;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.accepted_request_fragment, container, false);
        bindView();
        if (CheckConnection.haveNetworkConnection(getActivity())) {
            getAcceptedRequest(userid, status, key);
        } else {
            Toast.makeText(getActivity(), getString(R.string.network), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    public void bindView() {

        sessionManager = new SessionManager(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        txt_error = (TextView) view.findViewById(R.id.txt_error);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        sessionManager = new SessionManager(getActivity());
        if (sessionManager != null) {
            HashMap<String, String> users = sessionManager.getUserDetails();
            if (users != null) {
                String id = users.get(SessionManager.USER_ID);
                if (id != null) {
                    userid = id;
                }
                String k = sessionManager.getKEY();
                if (k != null) {
                    key = k;
                }

            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getString("status");
            ((HomeActivity) getActivity()).fontToTitleBar(setTitle(status));
        }
        SetCustomFont setCustomFont = new SetCustomFont();
        setCustomFont.overrideFonts(getActivity(), view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }


    public void getAcceptedRequest(String id, String status, String key) {
        final RequestParams params = new RequestParams();
        params.put("id", id);
        params.put("status", status);
        Server.setHeader(key);
        Server.get("api/user/rides/format/json", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();

                swipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Gson gson = new GsonBuilder().create();

                    if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                        List<PendingRequestPojo> list = gson.fromJson(response.getJSONArray("data").toString(), new TypeToken<List<PendingRequestPojo>>() {
                        }.getType());
                        if (response.has("data") && response.getJSONArray("data").length() == 0) {
                            txt_error.setVisibility(View.VISIBLE);

                        } else {
                            AcceptedRequestAdapter acceptedRequestAdapter = new AcceptedRequestAdapter(list);
                            recyclerView.setAdapter(acceptedRequestAdapter);
                            acceptedRequestAdapter.notifyDataSetChanged();
                        }


                    } else {

                        Toast.makeText(getActivity(), getString(R.string.contact_admin), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {

                    Toast.makeText(getActivity(), getString(R.string.contact_admin), Toast.LENGTH_LONG).show();


                }
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

    private String setTitle(String s) {
        String title = null;
        switch (s) {
            case "ACCEPTED":
                title = getString(R.string.accepted_request);
                break;
            case "PENDING":
                title = getString(R.string.pending_request);
                break;
            case "CANCELLED":
                title = getString(R.string.cancelled_request);
                break;
            case "COMPLETED":
                title = getString(R.string.completed_request);
                break;

        }
        return title;
    }

}
