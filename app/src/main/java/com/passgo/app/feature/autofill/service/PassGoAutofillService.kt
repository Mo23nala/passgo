package com.passgo.app.feature.autofill.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.data.session.SessionManager
import com.passgo.app.feature.autofill.auth.AutofillAuthActivity
import com.passgo.app.feature.autofill.response.ResponseBuilder
import com.passgo.app.feature.autofill.session.AutofillSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class PassGoAutofillService : AutofillService() {

    @Inject
    lateinit var sessionProvider: Provider<AutofillSession>

    @Inject
    lateinit var responseBuilder: ResponseBuilder

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var logger: PassGoLogger

    override fun onConnected() {
        super.onConnected()
        logger.info("PassGoAutofillService", "Autofill service connected")
    }

    override fun onDisconnected() {
        super.onDisconnected()
        logger.info("PassGoAutofillService", "Autofill service disconnected")
    }

    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        val session = sessionProvider.get()

        if (session.needsAuthentication() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            sessionManager.markAutofillAuthAttempted()
            val intent = Intent(this, AutofillAuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                request.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            callback.onSuccess(responseBuilder.buildAuthResponse(pendingIntent))
            logger.info("PassGoAutofillService", "Authentication response sent for request: ${request.id}")
            return
        }

        cancellationSignal.setOnCancelListener { session.cancel() }
        session.onFillRequest(request, callback)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val session = sessionProvider.get()
        session.onSaveRequest(request, callback)
    }
}
