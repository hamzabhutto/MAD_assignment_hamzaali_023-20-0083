package com.example.madassignment_hamzaali

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.madassignment_hamzaali.ui.theme.MADAssignment_hamzaaliTheme
import java.sql.SQLException



class JokeContentProvider : ContentProvider() {

    private lateinit var dbHelper: JokeDbHelper

    companion object {
        private const val AUTHORITY = "com.example.jokelist.provider"
        private const val PATH_JOKES = "jokes"

        private const val JOKE = 100
        private const val JOKE_ID = 101

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(AUTHORITY, PATH_JOKES, JOKE)
            uriMatcher.addURI(AUTHORITY, "$PATH_JOKES/#", JOKE_ID)
        }
    }

    override fun onCreate(): Boolean {
        dbHelper = JokeDbHelper(context!!)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert(JokeContract.JokeEntry.TABLE_NAME, null, values)
        if (id > 0) {
            context?.contentResolver?.notifyChange(uri, null)
            return ContentUris.withAppendedId(uri, id)
        }
        throw SQLException("Failed to insert row into $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        val match = uriMatcher.match(uri)
        return when (match) {
            JOKE -> db.query(
                JokeContract.JokeEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            JOKE_ID -> {
                val jokeId = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid URI: $uri")
                val newSelection = "${BaseColumns._ID}=?"
                val newSelectionArgs = arrayOf(jokeId)
                db.query(
                    JokeContract.JokeEntry.TABLE_NAME,
                    projection,
                    newSelection,
                    newSelectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val db = dbHelper.writableDatabase
        val match = uriMatcher.match(uri)
        return when (match) {
            JOKE -> db.update(JokeContract.JokeEntry.TABLE_NAME, values, selection, selectionArgs)
            JOKE_ID -> {
                val jokeId = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid URI: $uri")
                val newSelection = "${BaseColumns._ID}=?"
                val newSelectionArgs = arrayOf(jokeId)
                db.update(JokeContract.JokeEntry.TABLE_NAME, values, newSelection, newSelectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        val match = uriMatcher.match(uri)
        return when (match) {
            JOKE -> db.delete(JokeContract.JokeEntry.TABLE_NAME, selection, selectionArgs)
            JOKE_ID -> {
                val jokeId = uri.lastPathSegment ?: throw IllegalArgumentException("Invalid URI: $uri")
                val newSelection = "${BaseColumns._ID}=?"
                val newSelectionArgs = arrayOf(jokeId)
                db.delete(JokeContract.JokeEntry.TABLE_NAME, newSelection, newSelectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? = when (uriMatcher.match(uri)) {
        JOKE -> "vnd.android.cursor.dir/$AUTHORITY.$PATH_JOKES"
        JOKE_ID -> "vnd.android.cursor.item/$AUTHORITY.$PATH_JOKES"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    // Inner class for SQLite database helper
    private class JokeDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_JOKES_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS ${JokeContract.JokeEntry.TABLE_NAME}")
            onCreate(db)
        }

        companion object {
            private const val DATABASE_NAME = "jokes.db"
            private const val DATABASE_VERSION = 1

            private const val SQL_CREATE_JOKES_TABLE = "CREATE TABLE ${JokeContract.JokeEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${JokeContract.JokeEntry.COLUMN_JOKE} TEXT NOT NULL)"
        }
    }
}

// Object for contract containing table and column names
object JokeContract {
    object JokeEntry : BaseColumns {
        const val TABLE_NAME = "jokes"
        const val COLUMN_JOKE = "joke"
    }
}
