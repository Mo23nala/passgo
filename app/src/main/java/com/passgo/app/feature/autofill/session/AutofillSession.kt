package com.passgo.app.feature.autofill.session

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.View
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.data.session.SessionManager
import com.passgo.app.feature.autofill.auth.BiometricAuthManager
import com.passgo.app.feature.autofill.domain.DomainHandler
import com.passgo.app.feature.autofill.matcher.CredentialMatcher
import com.passgo.app.feature.autofill.matcher.FieldMatcher
import com.passgo.app.feature.autofill.model.FieldType
import com.passgo.app.feature.autofill.model.SessionState
import com.passgo.app.feature.autofill.parser.RequestParser
import com.passgo.app.feature.autofill.repository.AutofillRepository
import com.passgo.app.feature.autofill.response.ResponseBuilder
import javax.inject.Inject

class AutofillSession @Inject constructor(
    private val requestParser: RequestParser,
    private val responseBuilder: ResponseBuilder,
    private val domainHandler: DomainHandler,
    private val fieldMatcher: FieldMatcher,
    private val credentialMatcher: CredentialMatcher,
    private val autofillRepository: AutofillRepository,
    private val biometricAuthManager: BiometricAuthManager,
    private val sessionManager: SessionManager,
    private val logger: PassGoLogger
) {
    private var state: SessionState = SessionState.CREATED
        private set

    private var currentPackageName: String = ""
        private set

    private var currentDomain: String? = null
        private set

    fun needsAuthentication(): Boolean {
        if (sessionManager.hasAutofillAuthBeenAttempted()) return false
        val biometricStatus = biometricAuthManager.isBiometricAvailable()
        return state == SessionState.CREATED &&
            !autofillRepository.isVaultUnlocked() &&
            biometricStatus == BiometricAuthManager.BiometricAvailability.AVAILABLE
    }

    fun onFillRequest(request: FillRequest, callback: FillCallback) {
        val startTime = System.currentTimeMillis()
        logger.info("AutofillSession", "Matching started for request: ${request.id}")
        state = SessionState.PARSING

        val parsedRequest = try {
            requestParser.parse(request)
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to parse request: ${e.message}")
            completeWithEmpty(callback)
            return
        }

        currentPackageName = parsedRequest.packageName
        currentDomain = parsedRequest.domain

        if (currentPackageName.isEmpty()) {
            logger.info("AutofillSession", "Invalid request — no package name")
            completeWithEmpty(callback)
            return
        }

        if (parsedRequest.detectedFields.isEmpty()) {
            logger.info("AutofillSession", "No autofillable fields detected")
            completeWithEmpty(callback)
            return
        }

        if (!fieldMatcher.hasLoginFields(parsedRequest.detectedFields)) {
            logger.info("AutofillSession", "Unsupported screen — no login fields detected")
            completeWithEmpty(callback)
            return
        }

        state = SessionState.PARSED

        if (!autofillRepository.isVaultUnlocked()) {
            logger.info("AutofillSession", "Authentication required — vault locked, skipping autofill")
            completeWithEmpty(callback)
            return
        }

        state = SessionState.RESPONDING

        val usernameField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.USERNAME }
        val emailField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.EMAIL }
        val passwordField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.PASSWORD }

        logger.info("AutofillSession", "Matching attempt for package: ${parsedRequest.packageName}")

        val allCredentials = try {
            autofillRepository.getAllAvailableCredentials()
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to retrieve credentials: ${e.message}")
            completeWithEmpty(callback)
            return
        }

        val matchedCredentials = credentialMatcher.findMatchingCredentials(
            credentials = allCredentials,
            packageName = currentPackageName,
            domain = currentDomain
        )

        logger.info("AutofillSession", "Matching completed — ${matchedCredentials.size} datasets")

        val fillResponse = responseBuilder.buildResponse(
            credentials = matchedCredentials,
            usernameField = usernameField,
            emailField = emailField,
            passwordField = passwordField
        )

        state = SessionState.RESPONDED
        callback.onSuccess(fillResponse)
        finishSession(request.id)

        val elapsed = System.currentTimeMillis() - startTime
        logger.info("AutofillSession", "Session fill completed in ${elapsed}ms")
    }

    fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        logger.info("AutofillSession", "SaveRequest received")

        val fillContexts = request.fillContexts
        if (fillContexts.isEmpty()) {
            callback.onSuccess()
            return
        }

        val lastContext = fillContexts.lastOrNull()
        if (lastContext == null) {
            callback.onSuccess()
            return
        }

        val structure = lastContext.structure
        val requestPackageName = structure.activityComponent?.packageName ?: currentPackageName
        val requestDomain = try {
            extractDomainFromStructure(structure) ?: currentDomain
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to extract domain from structure: ${e.message}")
            currentDomain
        }

        if (!autofillRepository.isVaultUnlocked()) {
            logger.info("AutofillSession", "SaveRequest — vault locked, skipping save")
            callback.onSuccess()
            return
        }

        val usernameValue = try {
            extractFieldValue(structure, FieldType.USERNAME) ?: ""
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to extract username: ${e.message}")
            ""
        }

        val emailValue = try {
            extractFieldValue(structure, FieldType.EMAIL) ?: ""
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to extract email: ${e.message}")
            ""
        }

        val passwordValue = try {
            extractFieldValue(structure, FieldType.PASSWORD)
        } catch (e: Exception) {
            logger.warn("AutofillSession", "Failed to extract password: ${e.message}")
            null
        }

        if (passwordValue != null && passwordValue.isNotEmpty()) {
            try {
                autofillRepository.performSave(
                    packageName = requestPackageName,
                    domain = requestDomain,
                    username = usernameValue,
                    email = emailValue,
                    password = passwordValue
                )
            } catch (e: Exception) {
                logger.warn("AutofillSession", "Failed to save credential: ${e.message}")
            }
        } else {
            logger.info("AutofillSession", "SaveRequest — no password value found")
        }

        callback.onSuccess()
        logger.info("AutofillSession", "SaveRequest completed")
    }

    fun cancel() {
        if (state == SessionState.CANCELLED || state == SessionState.FINISHED) return
        state = SessionState.CANCELLED
        logger.info("AutofillSession", "Session cancelled")
        sessionManager.lockIfAutofillOnly()
    }

    fun getState(): SessionState = state

    private fun completeWithEmpty(callback: FillCallback) {
        state = SessionState.FINISHED
        sessionManager.lockIfAutofillOnly()
        callback.onSuccess(responseBuilder.buildEmptyResponse())
    }

    private fun finishSession(requestId: Int) {
        state = SessionState.FINISHED
        logger.info("AutofillSession", "Session finished for request: $requestId")
        sessionManager.lockIfAutofillOnly()
    }

    private fun extractFieldValue(structure: AssistStructure, fieldType: FieldType): String? {
        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val value = findFieldInNode(windowNode.rootViewNode, fieldType)
            if (value != null) return value
        }
        return null
    }

    private fun findFieldInNode(node: AssistStructure.ViewNode, fieldType: FieldType): String? {
        if (isMatchingField(node, fieldType)) {
            val value = node.autofillValue
            if (value != null && value.isText) {
                return value.textValue.toString()
            }
        }
        for (i in 0 until node.childCount) {
            val value = findFieldInNode(node.getChildAt(i), fieldType)
            if (value != null) return value
        }
        return null
    }

    private fun isMatchingField(node: AssistStructure.ViewNode, fieldType: FieldType): Boolean {
        val autofillId = node.autofillId ?: return false
        if (node.autofillType == View.AUTOFILL_TYPE_NONE) return false
        val field = fieldMatcher.classifyField(
            autofillId = autofillId,
            hints = node.autofillHints,
            inputType = node.inputType,
            text = node.text?.toString() ?: "",
            htmlAutofillType = node.autofillType
        )
        return field.fieldType == fieldType
    }

    private fun extractDomainFromStructure(structure: AssistStructure): String? {
        val packageName = structure.activityComponent?.packageName ?: return null
        val fromPackage = domainHandler.extractDomainFromPackageName(packageName)

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val title = windowNode.title?.toString() ?: continue
            domainHandler.extractDomain(title)?.let { return it }
        }

        return fromPackage ?: packageName
    }
}
