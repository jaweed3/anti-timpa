package com.factlens

import android.app.Application
import com.factlens.network.VerdictEngine
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FactLensApp : Application() {

    override fun onCreate() {
        super.onCreate()
        VerdictEngine.USE_MOCK = false

        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(thread, throwable)
            currentHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun logCrash(thread: Thread, throwable: Throwable) {
        try {
            val dir = File(filesDir, "crashes")
            dir.mkdirs()
            val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val file = File(dir, "crash_$date.txt")
            FileWriter(file).use { writer ->
                writer.write("Thread: ${thread.name}\n")
                writer.write("Time: $date\n")
                writer.write("------------------------------------------------\n")
                throwable.printStackTrace(PrintWriter(writer))
            }
        } catch (_: Exception) {}
    }
}
