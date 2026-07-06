package com.passgo.app.di

import android.content.Context
import com.passgo.app.core.database.PassGoDatabase
import com.passgo.app.core.database.dao.AttachmentDao
import com.passgo.app.core.database.dao.FolderDao
import com.passgo.app.core.database.dao.TagDao
import com.passgo.app.core.database.dao.VaultDao
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.security.MasterKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePassGoDatabase(
        @ApplicationContext context: Context,
        masterKeyManager: MasterKeyManager
    ): PassGoDatabase {
        val masterKey = masterKeyManager.getOrCreateMasterKey()
        return PassGoDatabase.build(context, masterKey)
    }

    @Provides
    fun provideVaultDao(database: PassGoDatabase): VaultDao = database.vaultDao()

    @Provides
    fun provideVaultItemDao(database: PassGoDatabase): VaultItemDao = database.vaultItemDao()

    @Provides
    fun provideFolderDao(database: PassGoDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideTagDao(database: PassGoDatabase): TagDao = database.tagDao()

    @Provides
    fun provideAttachmentDao(database: PassGoDatabase): AttachmentDao = database.attachmentDao()
}
