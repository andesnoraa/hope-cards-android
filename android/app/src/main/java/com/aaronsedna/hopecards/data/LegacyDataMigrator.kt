package com.aaronsedna.hopecards.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/** Reads the previous Expo/React Native stores without retaining either runtime. */
class LegacyDataMigrator(private val context: Context) {
    fun read(): Map<String, String> = buildMap {
        readTable(
            file = context.getDatabasePath("RKStorage"),
            table = "catalystLocalStorage",
            keyColumn = "key",
            valueColumn = "value",
        ).forEach(::put)

        readTable(
            file = File(context.filesDir, "SQLite/ExpoSQLiteStorage"),
            table = "storage",
            keyColumn = "key",
            valueColumn = "value",
        ).forEach(::put)
    }

    private fun readTable(
        file: File,
        table: String,
        keyColumn: String,
        valueColumn: String,
    ): Map<String, String> {
        if (!file.exists()) return emptyMap()
        return runCatching {
            SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { database ->
                buildMap {
                    database.query(
                        table,
                        arrayOf(keyColumn, valueColumn),
                        null,
                        null,
                        null,
                        null,
                        null,
                    ).use { cursor ->
                        while (cursor.moveToNext()) {
                            put(cursor.getString(0), cursor.getString(1))
                        }
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }
}
