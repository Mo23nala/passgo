package com.passgo.app.core.database

import android.content.Context
import android.net.Uri
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: PassGoLogger
) {

    suspend fun exportDatabase(targetUri: Uri): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dbFile = context.getDatabasePath("passgo_vault.db")
            if (!dbFile.exists()) {
                throw AppException.UnknownException("Database file does not exist")
            }

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw AppException.UnknownException("Failed to open target URI for writing")

            logger.info("BackupManager", "Database exported successfully")
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }

    suspend fun importDatabase(sourceUri: Uri): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dbFile = context.getDatabasePath("passgo_vault.db")

            // Check if the source is a valid SQLite DB file
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                // First read to a temporary file
                val tempFile = File(context.cacheDir, "temp_import.db")
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // Backup current database just in case
                if (dbFile.exists()) {
                    val backupFile = File(context.cacheDir, "db_backup.db")
                    dbFile.copyTo(backupFile, overwrite = true)
                }

                // Overwrite the database
                tempFile.copyTo(dbFile, overwrite = true)
                tempFile.delete()
            } ?: throw AppException.UnknownException("Failed to open source URI for reading")

            logger.info("BackupManager", "Database imported successfully")
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Error(AppException.fromThrowable(it)) }
        )
    }
}
