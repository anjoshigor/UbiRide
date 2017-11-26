package com.anjoshigor.ubiride

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.awareness.Awareness
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.awareness.fence.*
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.fence.AwarenessFence
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status


class MainActivity : AppCompatActivity() {

    private val FENCE_RECEIVER_ACTION = "FENCE_RECEIVE"
    private lateinit var mFencePendingIntent: PendingIntent
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val TAG = "Awareness"
    private lateinit var txtView: TextView
    private lateinit var mMyFenceReceiver: LocationBroadCastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(FENCE_RECEIVER_ACTION)
        mFencePendingIntent = PendingIntent.getBroadcast(this,
                10001,
                intent,
                0)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build()

        mGoogleApiClient.connect()

        val walkingFence = DetectedActivityFence.during(DetectedActivityFence.STILL)
        val headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)
        val walkingWithHeadphones = AwarenessFence.and(walkingFence, headphoneFence)
        registerFence("myKey", walkingWithHeadphones)
        mMyFenceReceiver = LocationBroadCastReceiver()
        registerReceiver(mMyFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))
    }


    protected fun registerFence(fenceKey: String, fence: AwarenessFence) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, mFencePendingIntent)
                        .build())
                .setResultCallback(object : ResultCallback<Status> {
                    override fun onResult(status: Status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.")
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status)
                        }
                    }
                })
    }

    protected fun unregisterFence(fenceKey: String, fence: AwarenessFence) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                FenceUpdateRequest.Builder()
                        .removeFence(fenceKey)
                        .build())
                .setResultCallback(object : ResultCallback<Status> {
                    override fun onResult(status: Status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully unregistered.")
                        } else {
                            Log.e(TAG, "Fence could not be unregistered: " + status)
                        }
                    }
                })
    }


}
