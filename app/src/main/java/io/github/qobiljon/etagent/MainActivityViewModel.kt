package io.github.qobiljon.etagent

import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivityViewModel : ViewModel() {
    private var time = liveData {
        while (true) {
            val dateTime = DateTimeFormatter.ofPattern("EE MM.dd, KK:mm a").format(LocalDateTime.now()).split(", ")
            emit(Pair(dateTime[0], dateTime[1]))
            delay(1000)
        }
    }

    fun startDataCollection(context: Context) {
        context.startForegroundService(Intent(context, DataCollectorService::class.java))
    }

    fun stopDataCollection(context: Context) {
        context.stopService(Intent(context, DataCollectorService::class.java))
    }

    fun getTime(): LiveData<Pair<String, String>> {
        return time
    }

    companion object LiveDataVMFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel() as T
        }
    }
}
