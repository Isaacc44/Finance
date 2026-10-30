package uz.finansflow.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.finansflow.app.data.AccountEntity
import uz.finansflow.app.data.TransactionEntity
import java.time.DayOfWeek
import java.time.LocalDate

class FinanceCalculationsTest {
    @Test fun incomeExpenseAndTransferProduceCorrectAccountBalances() {
        val accounts = listOf(AccountEntity(id = 1, name = "Наличные"), AccountEntity(id = 2, name = "Карта"))
        val operations = listOf(
            TransactionEntity(id = 1, title = "Зарплата", amount = 1_000_000, type = "INCOME", accountId = 1),
            TransactionEntity(id = 2, title = "Продукты", amount = 150_000, type = "EXPENSE", accountId = 1),
            TransactionEntity(id = 3, title = "На карту", amount = 200_000, type = "TRANSFER", accountId = 1, targetAccountId = 2)
        )
        assertEquals(650_000L, FinanceCalculations.accountBalance(1, operations))
        assertEquals(200_000L, FinanceCalculations.accountBalance(2, operations))
        assertEquals(850_000L, FinanceCalculations.totalBalance(accounts, operations))
        assertEquals(1_000_000L, FinanceCalculations.income(operations))
        assertEquals(150_000L, FinanceCalculations.expenses(operations))
    }

    @Test fun weekStartsOnMonday() {
        val wednesday = LocalDate.of(2026, 10, 28)
        assertEquals(DayOfWeek.MONDAY, FinanceCalculations.startOfPeriod("Неделя", wednesday).dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 26), FinanceCalculations.startOfPeriod("Неделя", wednesday))
    }
}
