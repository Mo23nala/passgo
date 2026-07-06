package com.passgo.app.di

import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.data.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePassGoLogger(): PassGoLogger {
        return PassGoLogger()
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        logger: PassGoLogger,
        passwordStore: MasterPasswordStore
    ): SessionManager {
        return SessionManager(logger, passwordStore)
    }
}
