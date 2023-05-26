package com.equationl.githubapp.di

import android.content.Context
import com.equationl.githubapp.common.database.CacheDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideIssueDataBase(@ApplicationContext app: Context) = run {
        CacheDB.create(app)
    }
}