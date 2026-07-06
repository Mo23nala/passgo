package com.passgo.app.core.logging

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassGoLogger @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _logEntries = Channel<LogEntry>(Channel.UNLIMITED)

    private data class LogEntry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )

    init {
        scope.launch {
            for (entry in _logEntries) {
                writeToLogcat(entry)
            }
        }
    }

    fun info(tag: String, message: String) {
        _logEntries.trySend(LogEntry(LogLevel.INFO, tag, message))
    }

    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        _logEntries.trySend(LogEntry(LogLevel.WARN, tag, message, throwable))
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        _logEntries.trySend(LogEntry(LogLevel.ERROR, tag, message, throwable))
    }

    private fun writeToLogcat(entry: LogEntry) {
        val logMessage = "${entry.tag}: ${entry.message}"
        when (entry.level) {
            LogLevel.INFO -> Log.i("PassGo", logMessage)
            LogLevel.WARN -> {
                if (entry.throwable != null) {
                    Log.w("PassGo", logMessage, entry.throwable)
                } else {
                    Log.w("PassGo", logMessage)
                }
            }
            LogLevel.ERROR -> {
                if (entry.throwable != null) {
                    Log.e("PassGo", logMessage, entry.throwable)
                } else {
                    Log.e("PassGo", logMessage)
                }
            }
        }
    }
}
