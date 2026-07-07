package com.passgo.app.feature.autofill.dataset

import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.Presentations
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.passgo.app.feature.autofill.model.AutofillCredential
import com.passgo.app.feature.autofill.model.AutofillField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatasetBuilder @Inject constructor() {

    private val packageName = "com.passgo.app"

    fun buildFillDataset(
        credential: AutofillCredential,
        usernameField: AutofillField?,
        emailField: AutofillField?,
        passwordField: AutofillField?
    ): Dataset? {
        if (passwordField == null) return null

        val menuPresentation = createPresentation(credential)

        val dataset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val builder = Presentations.Builder()
            builder.setMenuPresentation(menuPresentation)
            Dataset.Builder(builder.build())
        } else {
            @Suppress("DEPRECATION")
            Dataset.Builder(menuPresentation)
        }

        usernameField?.let { field ->
            if (credential.username.isNotEmpty()) {
                setDatasetValue(dataset, field.autofillId, AutofillValue.forText(credential.username))
            }
        }

        emailField?.let { field ->
            if (credential.email.isNotEmpty()) {
                setDatasetValue(dataset, field.autofillId, AutofillValue.forText(credential.email))
            }
        }

        setDatasetValue(dataset, passwordField.autofillId, AutofillValue.forText(credential.password))

        return dataset.build()
    }

    @Suppress("DEPRECATION")
    private fun setDatasetValue(dataset: Dataset.Builder, id: AutofillId, value: AutofillValue) {
        dataset.setValue(id, value)
    }

    fun buildSaveDataset(
        usernameField: AutofillField?,
        emailField: AutofillField?,
        passwordField: AutofillField?
    ): SaveInfo {
        val requiredIds = mutableListOf<AutofillId>()
        usernameField?.let { requiredIds.add(it.autofillId) }
        passwordField?.let { requiredIds.add(it.autofillId) }

        val optionalIds = mutableListOf<AutofillId>()
        emailField?.let { optionalIds.add(it.autofillId) }

        return SaveInfo.Builder(
            SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
            requiredIds.toTypedArray()
        ).apply {
            if (optionalIds.isNotEmpty()) {
                setOptionalIds(optionalIds.toTypedArray())
            }
        }.build()
    }

    private fun createPresentation(credential: AutofillCredential): RemoteViews {
        val displayName = credential.name.ifEmpty { credential.username.ifEmpty { credential.email } }
        val displaySub = credential.username.ifEmpty { credential.email }
        val primaryText = if (displaySub.isNotEmpty() && displaySub != displayName) {
            "$displayName ($displaySub)"
        } else {
            displayName
        }

        val layoutId = android.R.layout.simple_list_item_2
        val presentation = RemoteViews(packageName, layoutId)
        presentation.setTextViewText(android.R.id.text1, primaryText)
        presentation.setTextViewText(android.R.id.text2, credential.url.ifEmpty { packageName })
        presentation.setContentDescription(android.R.id.text1, "Autofill credential for $displayName")
        presentation.setContentDescription(android.R.id.text2, "Website: ${credential.url.ifEmpty { packageName }}")
        return presentation
    }
}
