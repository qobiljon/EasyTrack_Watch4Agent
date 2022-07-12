package io.github.qobiljon.etagent

import android.view.View
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import io.github.qobiljon.etagent.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "EasyTrackAgent"
    }

    private val viewModel: MainActivityViewModel by viewModels { MainActivityViewModel.LiveDataVMFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onclick
        binding.btnStart.setOnClickListener {
            viewModel.startDataCollection(applicationContext)

            binding.btnStart.visibility = View.GONE
            binding.btnStop.visibility = View.VISIBLE
            binding.root.setBackgroundResource(R.drawable.green_circle)
        }
        binding.btnStop.setOnClickListener {
            viewModel.stopDataCollection(applicationContext)

            binding.btnStart.visibility = View.VISIBLE
            binding.btnStop.visibility = View.GONE
            binding.root.setBackgroundResource(R.drawable.orange_circle)
        }

        // livedata
        viewModel.getTime().observe(this) {
            binding.tvDate.text = it.first
            binding.tvTime.text = it.second
        }

        // hrm
        val healthClient = HealthServices.getClient(applicationContext)
        val measureClient = healthClient.measureClient
        lifecycleScope.launch {
            val capabilities = measureClient.capabilities.await()
            val supportsHeartRate = DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
            print(supportsHeartRate)
        }
    }
}
