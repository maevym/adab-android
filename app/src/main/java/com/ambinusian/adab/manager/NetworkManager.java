package com.ambinusian.adab.manager;

import android.content.Context;
import android.util.Log;

import com.ambinusian.adab.preferences.UserPreferences;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    private static NetworkManager instance = null;
    private static final String TAG = "NetworkManager";
    private static final String SERVER_URL = "https://adabtest.ashm999.fr/";

    private RequestQueue requestQueue;

    public NetworkManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (null == instance)
            instance = new NetworkManager(context);
        return instance;
    }

    public static synchronized NetworkManager getInstance() {
        if (null == instance) {
            throw new IllegalStateException(NetworkManager.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first");
        }
        return instance;
    }

    public interface postCallback {
        void onResponse(JSONObject response) throws JSONException;
    }

    public interface GetCallback {
        void onResponse(JSONObject response) throws JSONException;
    }

    public void doPostRequest(Context context, String userToken, JSONObject jsonBody, String apiPath, postCallback callback) {
        String url = SERVER_URL + apiPath;
        Log.d(TAG, jsonBody.toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Log.d(TAG, response.toString());
                    try {
                        callback.onResponse(response);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 404) {
                            Log.e(TAG, "Not found????");

                        } else if (error.networkResponse.statusCode == 401) {
                            Log.e(TAG, "Unauthorized");
                            try {
                                callback.onResponse(new JSONObject(new String(error.networkResponse.data)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                if (!userToken.isEmpty()) {
                    headers.put("authorization", "Bearer " + userToken);
                }
                Log.d("Headers", String.valueOf(headers));
                return headers;
            }
        };

        requestQueue.add(jsonObjReq);
    }

    public void doGetRequest(Context context, String userToken, String apiEndPoint, GetCallback callback) {
        String url = SERVER_URL + apiEndPoint;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, response.toString());
                    try {
                        callback.onResponse(response);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 404) {
                            Log.e(TAG, "Not found????");

                        } else if (error.networkResponse.statusCode == 401) {
                            Log.e(TAG, "Unauthorized");
                            try {
                                callback.onResponse(new JSONObject(new String(error.networkResponse.data)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                if (!userToken.isEmpty()) {
                    headers.put("authorization", "Bearer " + userToken);
                }
                Log.d("Headers", String.valueOf(headers));
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}
