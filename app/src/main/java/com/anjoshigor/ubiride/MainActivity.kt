package com.anjoshigor.ubiride

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.awareness.Awareness
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.awareness.fence.*
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.fence.AwarenessFence
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.location.places.ui.PlaceAutocomplete.getStatus
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import com.google.android.gms.awareness.SnapshotClient
import com.google.android.gms.awareness.snapshot.DetectedActivityResult
import com.google.android.gms.location.DetectedActivity


class MainActivity : AppCompatActivity() {

    private val FENCE_RECEIVER_ACTION = "FENCE_RECEIVE"
    private lateinit var mFencePendingIntent: PendingIntent
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val TAG = "Awareness"
    private lateinit var mMyFenceReceiver: InternalLocationBroadCastReceiver
    lateinit var headphoneButton: Button
    lateinit var locationButton: Button
    lateinit var activityButton: Button
    lateinit var timeButton: Button
    lateinit var mainText: TextView
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 940


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .addApi(Nearby.CONNECTIONS_API)
                .addApi(Nearby.MESSAGES_API)
                .useDefaultAccount()
                .build()

        mGoogleApiClient.connect()

        mainText = findViewById(R.id.maintext)
        headphoneButton = findViewById(R.id.headphone)
        headphoneButton.setOnClickListener {
            var snapshotClient = Awareness.getSnapshotClient(this)
            snapshotClient.headphoneState.addOnSuccessListener { headphoneStateResponse ->
                if (headphoneStateResponse.headphoneState.state == HeadphoneState.PLUGGED_IN) {
                    mainText.text = "Headphone Plugado"
                } else {
                    mainText.text = "HeadPhone Desplugado"
                }

            }.addOnFailureListener { status ->
                Log.e(TAG, status.message)
            }
        }

        locationButton = findViewById(R.id.location)
        locationButton.setOnClickListener {
            getLocation()
        }

        activityButton = findViewById(R.id.activity)
        activityButton.setOnClickListener {
            var snapshotClient = Awareness.getSnapshotClient(this)
            snapshotClient.detectedActivity.addOnSuccessListener { activityResponse ->
                var activity = activityResponse.activityRecognitionResult.mostProbableActivity
                mainText.text = getActivityString(activity.type)
            }.addOnFailureListener { status ->
                mainText.text = status.message
            }
        }

        timeButton = findViewById(R.id.time)
        timeButton.setOnClickListener {
            var snapshotClient = Awareness.getSnapshotClient(this)
            try {
                snapshotClient.timeIntervals.addOnSuccessListener { timeResponse ->
                    var time = timeResponse.timeIntervals.timeIntervals
                    mainText.text = time[0].toString()
                }.addOnFailureListener { status ->
                    mainText.text = status.message
                }
            } catch (ex: SecurityException) {
                Log.e(TAG, ex.message)
            }
        }


        val intent = Intent(FENCE_RECEIVER_ACTION)
        mFencePendingIntent = PendingIntent.getBroadcast(this,
                10001,
                intent,
                0)


        val walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING)
        val headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)
        val timeFence = TimeFence.inDailyInterval(null, 0L, 70080000)

        handleLocationFence()

        registerFence("timefencekey", timeFence)
        registerFence("walkingkey", walkingFence)
        registerFence("headphonekey", headphoneFence)

        mMyFenceReceiver = InternalLocationBroadCastReceiver()
        registerReceiver(mMyFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))


    }

    fun handleLocationFence() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    Array(1, { Manifest.permission.ACCESS_FINE_LOCATION }),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        } else {
            var homeFence = LocationFence.`in`(-7.155978, -34.830320, 50.0, 10)
            homeFence = AwarenessFence.or(homeFence, LocationFence.entering(-7.155978, -34.830320, 50.0))
            homeFence = AwarenessFence.or(homeFence, LocationFence.exiting(-7.155978, -34.830320, 50.0))
            registerFence("homekey", homeFence)
        }
    }

    fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    Array(1, { Manifest.permission.ACCESS_FINE_LOCATION }),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
        } else {
            var snapshotClient = Awareness.getSnapshotClient(this)

            snapshotClient.location.addOnSuccessListener { locationResponse ->
                var location = locationResponse.location
                mainText.text = "Latitude: {${location.latitude}} \n Longitude: {${location.longitude}}"
            }.addOnFailureListener { status ->
                mainText.text = status.message
            }
        }
    }


    fun getActivityString(type: Int): String {
        when (type) {
            DetectedActivity.IN_VEHICLE ->
                return "Em veículo"
            DetectedActivity.ON_BICYCLE ->
                return "Em bicicleta"
            DetectedActivity.ON_FOOT ->
                return "À pé"
            DetectedActivity.RUNNING ->
                return "Correndo"
            DetectedActivity.STILL ->
                return "Parado"
            DetectedActivity.TILTING ->
                return "Inclinado"
            DetectedActivity.WALKING ->
                return "Caminhando"
            else -> {
                return "Desconhecido"
            }
        }
    }

    protected fun registerFence(fenceKey: String, fence: AwarenessFence) {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                FenceUpdateRequest.Builder()
                        .addFence(fenceKey, fence, mFencePendingIntent)
                        .build())
                .setResultCallback(object : ResultCallback<Status> {
                    override fun onResult(status: Status) {
                        if (status.isSuccess) {
                            Log.i(TAG, "Fence {$fenceKey} was successfully registered.")
                        } else {
                            Log.e(TAG, "Fence {$fenceKey} could not be registered: " + status)
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


    inner class InternalLocationBroadCastReceiver : BroadcastReceiver() {


        fun handleFalse(context: Context?, key: String) {
            //TODO("Atualizar o banco de dados")
            //LANCAR NOTIFICACAO
            when (key) {
                "headphonekey" -> {
                    headphoneButton.text = "Headphone não está plugado"
                    Toast.makeText(context, "Não está mais com o fone", Toast.LENGTH_LONG).show()
                }
                "walkingkey" -> {
                    activityButton.text = "Parado"
                    Toast.makeText(context, "Não está mais andando", Toast.LENGTH_LONG).show()
                }
                "homekey" -> {
                    locationButton.text = "Não está em um local conhecido"
                    Toast.makeText(context, "Não está mais em casa", Toast.LENGTH_LONG).show()
                }
                "timefencekey" -> {
                    timeButton.text = "Não está no horário determinado"
                    Toast.makeText(context, "Não está no horário determinado", Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        }

        fun handleTrue(context: Context?, key: String) {
            //TODO("Atualizar o banco de dados")
            //LANCAR NOTIFICACAO
            when (key) {
                "headphonekey" -> {
                    headphoneButton.text = "Headphone está plugado"
                    Toast.makeText(context, "Está com o fone", Toast.LENGTH_LONG).show()
                }
                "walkingkey" -> {
                    activityButton.text = "Andando"
                    Toast.makeText(context, "Está andando", Toast.LENGTH_LONG).show()
                }
                "homekey" -> {
                    locationButton.text = "Está em casa"
                    Toast.makeText(context, "Está em casa", Toast.LENGTH_LONG).show()
                }
                "timefencekey" -> {
                    timeButton.text = "Está no horário determinado"
                    Toast.makeText(context, "Está no horário determinado", Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            var state: FenceState = FenceState.extract(intent)
            Log.d("Awareness", "Fence Receiver Received")


            when (state.currentState) {
                FenceState.TRUE -> handleTrue(context, state.fenceKey)
                FenceState.FALSE -> handleFalse(context, state.fenceKey)
                else -> {
                    Toast.makeText(context, "NADA", Toast.LENGTH_LONG).show()
                }


            }
        }
    }

}



