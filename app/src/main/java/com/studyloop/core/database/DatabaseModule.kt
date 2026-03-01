package com.studyloop.core.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx)

    @Provides @Singleton
    fun provideReminderDao(db: AppDatabase) = db.reminderDao()

    @Provides @Singleton
    fun provideReviewDao(db: AppDatabase) = db.reviewDao()

    @Provides @Singleton
    fun provideNoteDao(db: AppDatabase) = db.noteDao()

    @Provides @Singleton
    fun provideTodoDao(db: AppDatabase) = db.todoDao()

    @Provides @Singleton
    fun provideAlarmDao(db: AppDatabase) = db.alarmDao()
}
