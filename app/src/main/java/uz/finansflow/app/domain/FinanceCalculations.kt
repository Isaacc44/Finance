package uz.finansflow.app.domain

import uz.finansflow.app.data.AccountEntity
import uz.finansflow.app.data.TransactionEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object FinanceCalculations {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
    const val TRANSFER = "TRANSFER"

    fun signedChange(transaction: TransactionEntity, accountId: Long): Long = when {
        transaction.type == INCOME && transaction.accountId == accountId -> transaction.amount
        transaction.type == EXPENSE && transaction.accountId == accountId -> -transaction.amount
        transaction.type == TRANSFER && transaction.accountId == accountId -> -transaction.amount
        transaction.type == TRANSFER && transaction.targetAccountId == accountId -> transaction.amount
        else -> 0L
    }

    fun accountBalance(accountId: Long, transactions: List<TransactionEntity>): Long =
        transactions.fold(0L) { total, tx -> Math.addExact(total, signedChange(tx, accountId)) }

    fun totalBalance(accounts: List<AccountEntity>, transactions: List<TransactionEntity>): Long =
        accounts.fold(0L) { total, account -> Math.addExact(total, accountBalance(account.id, transactions)) }

    fun income(transactions: List<TransactionEntity>): Long = transactions
        .asSequence().filter { it.type == INCOME }.fold(0L) { s, t -> Math.addExact(s, t.amount) }

    fun expenses(transactions: List<TransactionEntity>): Long = transactions
        .asSequence().filter { it.type == EXPENSE }.fold(0L) { s, t -> Math.addExact(s, t.amount) }

    fun startOfPeriod(period: String, now: LocalDate = LocalDate.now()): LocalDate = when (period) {
        "Сегодня" -> now
        "Неделя" -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        "Месяц" -> now.withDayOfMonth(1)
        "Год" -> now.withDayOfYear(1)
        else -> LocalDate.of(1970, 1, 1)
    }

    fun inDateRange(transaction: TransactionEntity, start: LocalDate, endInclusive: LocalDate): Boolean {
        val date = Instant.ofEpochMilli(transaction.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
        return !date.isBefore(start) && !date.isAfter(endInclusive)
    }

    fun periodTransactions(
        transactions: List<TransactionEntity>, period: String, today: LocalDate = LocalDate.now()
    ): List<TransactionEntity> = transactions.filter { inDateRange(it, startOfPeriod(period, today), today) }
}
