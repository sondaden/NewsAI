package com.example.myapplication.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.NewsArticleDao
import com.example.myapplication.data.model.NewsArticleEntity

@Database(entities = [NewsArticleEntity::class], version = 8, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsArticleDao(): NewsArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("PRAGMA table_info(news_articles)")
                var aiCategoryExists = false
                cursor.use {
                    while (it.moveToNext()) {
                        val columnNameIndex = it.getColumnIndex("name")
                        if (columnNameIndex >= 0) {
                            val columnName = it.getString(columnNameIndex)
                            if (columnName == "aiCategory") {
                                aiCategoryExists = true
                                break
                            }
                        }
                    }
                }
                if (!aiCategoryExists) {
                    database.execSQL("ALTER TABLE news_articles ADD COLUMN aiCategory TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Không có thay đổi schema
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("PRAGMA table_info(news_articles)")
                var aiCategoryExists = false
                var aiCategoryNotNull = false
                cursor.use {
                    while (it.moveToNext()) {
                        val columnNameIndex = it.getColumnIndex("name")
                        val notNullIndex = it.getColumnIndex("notnull")
                        if (columnNameIndex >= 0) {
                            val columnName = it.getString(columnNameIndex)
                            if (columnName == "aiCategory") {
                                aiCategoryExists = true
                                aiCategoryNotNull = notNullIndex >= 0 && it.getInt(notNullIndex) == 1
                                break
                            }
                        }
                    }
                }
                if (aiCategoryExists) {
                    if (!aiCategoryNotNull) {
                        database.execSQL("UPDATE news_articles SET aiCategory = '' WHERE aiCategory IS NULL")
                        database.execSQL("""
                            CREATE TABLE news_articles_temp (
                                id TEXT NOT NULL,
                                title TEXT NOT NULL,
                                content TEXT,
                                source TEXT,
                                author TEXT,
                                publishedAt TEXT,
                                imageUrl TEXT,
                                category TEXT NOT NULL,
                                categories TEXT NOT NULL,
                                articleUrl TEXT,
                                keywords TEXT,
                                language TEXT,
                                timestamp INTEGER NOT NULL,
                                reliabilityScore REAL,
                                aiCategory TEXT NOT NULL DEFAULT '',
                                PRIMARY KEY(id)
                            )
                        """.trimIndent())
                        database.execSQL("""
                            INSERT INTO news_articles_temp (
                                id, title, content, source, author, publishedAt, imageUrl,
                                category, categories, articleUrl, keywords, language, timestamp,
                                reliabilityScore, aiCategory
                            )
                            SELECT 
                                id, title, content, source, author, publishedAt, imageUrl,
                                category, categories, articleUrl, keywords, language, timestamp,
                                reliabilityScore, aiCategory
                            FROM news_articles
                        """.trimIndent())
                        database.execSQL("DROP TABLE news_articles")
                        database.execSQL("ALTER TABLE news_articles_temp RENAME TO news_articles")
                    }
                } else {
                    database.execSQL("ALTER TABLE news_articles ADD COLUMN aiCategory TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE news_articles ADD COLUMN isSaved INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "news_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}