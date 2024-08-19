package org.mycompany.hris.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// I assume that we may need to wrap several DB operations in a single transaction.
// So I decided to create a transaction wrapper to attach a transaction to the Dispatchers.IO in one place.
suspend fun <T> inTx(callback: suspend () -> T): T =
    withContext(Dispatchers.IO) {
        return@withContext newSuspendedTransaction {
            callback()
        }
    }
