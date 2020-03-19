package ua.turskyi.sqliteinnerjoin

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        const val LOG_TAG = "myLogs"
    }

    // данные для таблицы должностей
    var positionId = intArrayOf(1, 2, 3, 4)
    var positionName = arrayOf("Директор", "Программер", "Бухгалтер", "Охранник")
    var positionSalary = intArrayOf(15000, 13000, 10000, 8000)

    // данные для таблицы людей
    var peopleName = arrayOf("Иван", "Марья", "Петр", "Антон", "Даша", "Борис",
            "Костя", "Игорь")
    var peoplePosId = intArrayOf(2, 3, 2, 2, 3, 1, 2, 4)

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Подключаемся к БД
        val dbHelper = DBHelper(this)
        val db = dbHelper.writableDatabase

        // Описание курсора
        var cursor: Cursor

        // выводим в лог данные по должностям
        Log.d(LOG_TAG, "--- Table position ---")
        cursor = db.query("position", null, null, null,
                null, null, null)
        logCursor(cursor)
        cursor.close()
        Log.d(LOG_TAG, "--- ---")

        // выводим в лог данные по людям
        Log.d(LOG_TAG, "--- Table people ---")
        cursor = db.query("people", null, null, null,
                null, null, null)
        logCursor(cursor)
        cursor.close()
        Log.d(LOG_TAG, "--- ---")

        // выводим результат объединения
        // используем rawQuery
        Log.d(LOG_TAG, "--- INNER JOIN with rawQuery---")
        val sqlQuery = ("select PL.name as Name, PS.name as Position, salary as Salary "
                + "from people as PL "
                + "inner join position as PS "
                + "on PL.posId = PS.id "
                + "where salary > ?")
        cursor = db.rawQuery(sqlQuery, arrayOf("12000"))
        logCursor(cursor)
        cursor.close()
        Log.d(LOG_TAG, "--- ---")

        // выводим результат объединения
        // используем query
        Log.d(LOG_TAG, "--- INNER JOIN with query---")
        val table = "people as PL inner join position as PS on PL.posId = PS.id"
        val columns = arrayOf("PL.name as Name", "PS.name as Position", "salary as " +
                "Salary")
        val selection = "salary < ?"
        val selectionArgs = arrayOf("12000")
        cursor = db.query(table, columns, selection, selectionArgs, null, null,
                null)
        logCursor(cursor)
        cursor.close()
        Log.d(LOG_TAG, "--- ---")

        // закрываем БД
        dbHelper.close()
    }

    // вывод в лог данных из курсора
    private fun logCursor(cursor: Cursor?) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                var str: String
                do {
                    str = ""
                    for (cn in cursor.columnNames) {
                        str = str + cn + " = " + cursor.getString(cursor.getColumnIndex(cn)) + "; "
                    }
                    Log.d(LOG_TAG, str)
                } while (cursor.moveToNext())
            }
        } else Log.d(LOG_TAG, "Cursor is null")
    }

    // класс для работы с БД
    internal inner class DBHelper(context: Context?) : SQLiteOpenHelper(context, "myDB",
            null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            Log.d(LOG_TAG, "--- onCreate database ---")
            val values = ContentValues()

            // создаем таблицу должностей
            db.execSQL("create table position ("
                    + "id integer primary key,"
                    + "name text,"
                    + "salary integer"
                    + ");")

            // заполняем ее
            for (id in positionId.indices) {
                values.clear()
                values.put("id", positionId[id])
                values.put("name", positionName[id])
                values.put("salary", positionSalary[id])
                db.insert("position", null, values)
            }

            // создаем таблицу людей
            db.execSQL("create table people ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "posId integer"
                    + ");")

            // заполняем ее
            for (nameNum in peopleName.indices) {
                values.clear()
                values.put("name", peopleName[nameNum])
                values.put("posId", peoplePosId[nameNum])
                db.insert("people", null, values)
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }
}