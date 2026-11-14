package com.wa.beastclient.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        MessageFtsEntity::class,
        MessageEditEntity::class,
        StatusEntity::class,
        PresenceLogEntity::class,
        ProfileHistoryEntity::class,
        PrivacyRuleEntity::class,
        GroupReceiptEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class WaUiDatabase : RoomDatabase() {
    abstract fun waDao(): WaDao

    companion object {
        @Volatile
        private var INSTANCE: WaUiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chats ADD COLUMN is_community_parent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE chats ADD COLUMN parent_group_jid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE chats ADD COLUMN is_default_subgroup INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): WaUiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaUiDatabase::class.java,
                    File(context.applicationContext.filesDir, "wa_ui.db").absolutePath
                )
                    .addMigrations(MIGRATION_1_2)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            runCatching { db.query("PRAGMA busy_timeout = 10000;").use { it.moveToFirst() } }
                            runCatching { db.query("PRAGMA synchronous = NORMAL;").use { it.moveToFirst() } }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
