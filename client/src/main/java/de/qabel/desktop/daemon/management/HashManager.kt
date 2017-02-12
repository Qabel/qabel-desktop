package de.qabel.desktop.daemon.management

import java.nio.file.Path

class HashManager {
    data class DataHashManager(val destination: Path, val path: Path)

    fun filesSet(transactions: List<Transaction<Path, Path>>): Set<HashManager.DataHashManager> {
        return transactions.filter { !it.isDir && isRemote(it) }.map { DataHashManager(it.destination, it.source) }.toSet()
    }

    fun totalFiles(transactions: List<Transaction<Path, Path>>): Int {
        return filesSet(transactions).count()
    }

    fun finishedFiles(transactions: List<Transaction<Path, Path>>): Int {
        return filesSet(transactions.filter { it.isDone }).count()
    }

    fun isRemote(transaction: Transaction<*, *>): Boolean {
        if (transaction is Upload) {
            return true
        }
        return false
    }
}
