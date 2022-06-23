package io.github.qobiljon.etagent

import android.view.View
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.github.qobiljon.etagent.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class MainActivity : Activity() {
    companion object {
        const val TAG = "EasyTrackAgent"
    }

    @OptIn(DelicateCoroutinesApi::class)
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

        GlobalScope.launch {
            while (true) {
                runOnUiThread {
                    val dateTime = DateTimeFormatter.ofPattern("EE MM.dd, KK:mm a").format(LocalDateTime.now()).split(", ")
                    tvDate.text = dateTime[0]
                    tvTime.text = dateTime[1]
                }
                delay(1000)
            }
        }
    }
}
