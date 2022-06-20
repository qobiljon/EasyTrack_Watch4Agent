package io.github.qobiljon.etagent

import java.io.File
import android.util.Log
import android.os.Binder
import android.os.IBinder
import android.app.Service
import android.graphics.Color
import android.content.Intent
import android.hardware.Sensor
import android.content.Context
import android.app.Notification
import kotlin.concurrent.thread
import android.app.PendingIntent
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.hardware.SensorEventListener


class DataCollectorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var files: MutableMap<Sensor, File>
    private val samplingRate = SensorManager.SENSOR_DELAY_UI

    override fun onCreate() {
        Log.e(MainActivity.TAG, "DataCollectorService.onCreate()")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        files = mutableMapOf()
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        listOf<Any>(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_GYROSCOPE
        ).forEach {
            when (it) {
                is String -> allSensors.find { s -> s.stringType.equals(it) }
                else -> sensorManager.getDefaultSensor(it as Int)
            }?.let { sensor ->
                val file = File(filesDir, "${sensor.name}.csv")
                if (!file.exists()) {
                    file.createNewFile()
                    file.writeText("timestamp\tvalue\n")
                }
                files[sensor] = file
            }
        }

        // region setup foreground service (notification)
        val notificationId = 98765
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notificationChannelId = javaClass.name
        val notificationChannelName = "EasyTrack Data Collection Service"
        val notificationChannel = NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        val notification = Notification.Builder(this, notificationChannelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Data Collection service is running now...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(notificationId, notification)
        // endregion

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.e(MainActivity.TAG, "DataCollectorService.onStartCommand()")
        files.forEach { sensorManager.registerListener(this, it.key, samplingRate) }
        setUpDataSubmissionThread()
        setUpHeartbeatSubmissionThread()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e(MainActivity.TAG, "DataCollectorService.onDestroy()")
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun setUpDataSubmissionThread() {
        /*thread {
            while (isRunning) {
                if (Tools.isNetworkAvailable) {
                    val cursor = DbMgr.sensorData
                    if (cursor.moveToFirst()) {
                        val channel = ManagedChannelBuilder.forAddress(
                            getString(R.string.grpc_host),
                            getString(R.string.grpc_port).toInt()
                        ).usePlaintext().build()
                        val stub = ETServiceGrpc.newBlockingStub(channel)
                        val prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
                        val userId = prefs.getInt("userId", -1)
                        val sessionKey = prefs.getString("sessionKey", null)
                        try {
                            do {
                                val submitDataRecordRequestMessage =
                                    EtService.SubmitDataRecord.Request.newBuilder()
                                        .setUserId(userId)
                                        .setSessionKey(sessionKey)
                                        .setCampaignId(getString(R.string.easytrack_campaign_id).toInt())
                                        .setDataSource(cursor.getInt(1))
                                        .setTimestamp(cursor.getLong(2))
                                        .setValue(
                                            ByteString.copyFrom(
                                                cursor.getString(4),
                                                Charsets.UTF_8
                                            )
                                        )
                                        .build()
                                val responseMessage =
                                    stub.submitDataRecord(submitDataRecordRequestMessage)
                                if (responseMessage.success) DbMgr.deleteRecord(cursor.getInt(0))
                            } while (cursor.moveToNext())
                        } catch (e: StatusRuntimeException) {
                            Log.e(
                                MainActivity.TAG,
                                "DataCollectorService.setUpDataSubmissionThread() exception: " + e.message
                            )
                            e.printStackTrace()
                        } finally {
                            channel.shutdown()
                        }
                    }
                    cursor.close()
                }

                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()*/
    }

    private fun setUpHeartbeatSubmissionThread() {
        /*Thread {
            while (isRunning) {
                val channel = ManagedChannelBuilder.forAddress(
                    getString(R.string.grpc_host),
                    getString(R.string.grpc_port).toInt()
                ).usePlaintext().build()
                val prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
                val stub = ETServiceGrpc.newBlockingStub(channel)
                val submitHeartbeatRequestMessage = EtService.SubmitHeartbeat.Request.newBuilder()
                    .setUserId(prefs.getInt("userId", -1))
                    .setSessionKey(prefs.getString("sessionKey", null))
                    .build()
                try {
                    stub.submitHeartbeat(submitHeartbeatRequestMessage)
                } catch (e: StatusRuntimeException) {
                    Log.e(
                        MainActivity.TAG,
                        "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.message
                    )
                    e.printStackTrace()
                } finally {
                    channel.shutdown()
                }
                try {
                    Thread.sleep(5000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()*/
    }

    override fun onSensorChanged(event: SensorEvent) {
        val timestamp: Long = System.currentTimeMillis() + (event.timestamp - System.nanoTime()) / 1000000L
        val values: String = event.values.joinToString(",")
        files[event.sensor]?.appendText("$timestamp\t$values\n")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("Not yet implemented")
    }

    // region binder
    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        @Suppress("unused")
        val getService: DataCollectorService
            get() = this@DataCollectorService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
    // endregion
}
