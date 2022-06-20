package io.github.qobiljon.etagent

import android.view.View
import android.os.Bundle
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import java.time.format.DateTimeFormatter
import io.github.qobiljon.etagent.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {
    companion object {
        const val TAG = "EasyTrackAgent"
    }

    private var svc: DataCollectorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onclick
        btnStart.setOnClickListener {
            startForegroundService(Intent(applicationContext, DataCollectorService::class.java))

            btnStart.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
            binding.root.background = getDrawable(R.drawable.green_circle)
        }
        btnStop.setOnClickListener {
            stopService(Intent(applicationContext, DataCollectorService::class.java))

            btnStart.visibility = View.VISIBLE
            btnStop.visibility = View.GONE
            binding.root.background = getDrawable(R.drawable.orange_circle)
        }
    }

    override fun onStart() {
        super.onStart()

        val dateTime = DateTimeFormatter.ofPattern("EE MM.dd, KK:mm a").format(LocalDateTime.now()).split(", ")
        tvDate.text = dateTime[0]
        tvTime.text = dateTime[1]
    }

    override fun onDestroy() {
        super.onDestroy()

        TODO("NOT IMPLEMENTED YET")
    }
}
