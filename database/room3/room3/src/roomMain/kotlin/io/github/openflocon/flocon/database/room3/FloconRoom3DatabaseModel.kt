package io.github.openflocon.flocon.database.room3

import androidx.room3.PooledConnection
import androidx.room3.RoomDatabase
import androidx.room3.executeSQL
import androidx.room3.immediateTransaction
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.sqlite.async.step
import io.github.openflocon.flocon.database.core.FloconDatabasePlugin
import io.github.openflocon.flocon.database.core.model.FloconDatabaseModel
import io.github.openflocon.flocon.database.core.model.fromdevice.DatabaseExecuteResponse
import io.github.openflocon.flocon.database.core.model.fromdevice.DatabaseExecuteSqlResponse

/**
 * Registers a Room database, to browse it from the desktop app
 *
 * @param id unique and stable, also the one to give to the query logs of this database
 */
fun FloconDatabasePlugin.register(id: String, displayName: String, database: RoomDatabase) {
    register(
        FloconRoom3DatabaseModel(
            id = id,
            displayName = displayName,
            database = database
        )
    )
}

internal data class FloconRoom3DatabaseModel(
    override val id: String,
    override val displayName: String,
    val database: RoomDatabase
) : FloconDatabaseModel {

    override suspend fun executeQuery(query: String): DatabaseExecuteResponse = try {
        when (getFirstWord(query).uppercase()) {
            "SELECT",
            "PRAGMA",
            "EXPLAIN" -> database.useReaderConnection { executeSelect(connection = it, query = query) }

            "INSERT" -> database.write { executeInsert(connection = it, query = query) }

            "UPDATE",
            "DELETE" -> database.write { executeUpdateDelete(connection = it, query = query) }

            // not in a transaction: the query may be BEGIN, VACUUM...
            else -> database.useWriterConnection { executeRawQuery(connection = it, query = query) }
        }
    } catch (t: Throwable) {
        DatabaseExecuteSqlResponse.Error(
            message = t.message ?: "error on executeSQL",
            originalSql = query,
        )
    }
}

/**
 * Runs a write and the read of its result (`changes()`, `last_insert_rowid()`) in one transaction, so that both
 * use the same connection: outside a transaction, a driver with its own pool (AndroidSQLiteDriver, the framework
 * SQLiteDatabase) may run them on different connections, and `changes()` then returns 0.
 */
private suspend fun <R> RoomDatabase.write(block: suspend (PooledConnection) -> R): R = useWriterConnection { transactor -> transactor.immediateTransaction { block(this) } }

private suspend fun executeSelect(
    connection: PooledConnection,
    query: String,
): DatabaseExecuteSqlResponse = connection.usePrepared(query) { statement ->
    val columnNames = mutableListOf<String>()
    val columnCount = statement.getColumnCount()
    for (i in 0 until columnCount) {
        columnNames.add(statement.getColumnName(i))
    }

    val rows = mutableListOf<List<String?>>()
    while (statement.step()) {
        val values = mutableListOf<String?>()
        for (i in 0 until columnCount) {
            values.add(if (statement.isNull(i)) null else statement.getText(i))
        }
        rows.add(values)
    }

    DatabaseExecuteSqlResponse.Select(
        columns = columnNames,
        values = rows
    )
}

private suspend fun executeInsert(
    connection: PooledConnection,
    query: String,
): DatabaseExecuteSqlResponse {
    connection.executeSQL(query)
    val id = connection.usePrepared("SELECT last_insert_rowid()") {
        it.step()
        it.getLong(0)
    }
    return DatabaseExecuteSqlResponse.Insert(id)
}

private suspend fun executeUpdateDelete(
    connection: PooledConnection,
    query: String,
): DatabaseExecuteSqlResponse {
    connection.executeSQL(query)
    val count = connection.usePrepared("SELECT changes()") {
        it.step()
        it.getLong(0).toInt()
    }
    return DatabaseExecuteSqlResponse.UpdateDelete(count)
}

private suspend fun executeRawQuery(
    connection: PooledConnection,
    query: String,
): DatabaseExecuteSqlResponse {
    connection.executeSQL(query)
    return DatabaseExecuteSqlResponse.RawSuccess
}

private fun getFirstWord(s: String): String {
    val trimmed = s.trim()
    val firstSpace = trimmed.indexOf(' ')
    return if (firstSpace >= 0) trimmed.substring(0, firstSpace) else trimmed
}
