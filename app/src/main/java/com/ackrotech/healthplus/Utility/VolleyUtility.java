package com.ackrotech.healthplus.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyUtility {
    public static final String TAG = VolleyUtility.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static Context ctx;
    private static VolleyUtility mInstance;


    private VolleyUtility(Context context) {
        ctx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyUtility getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyUtility(context);
        }
        return mInstance;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(ctx);
        }

        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}

