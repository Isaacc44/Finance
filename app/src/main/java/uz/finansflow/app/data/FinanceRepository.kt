package uz.finansflow.app.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val db: AppDatabase) {
    private val dao = db.financeDao()

    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val accounts: Flow<List<AccountEntity>> = dao.observeAccounts()
    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val workOrders: Flow<List<WorkOrderEntity>> = dao.observeWorkOrders()

    suspend fun ensureDefaults() {
        if (dao.accountCount() == 0) {
            listOf("Наличные", "Банковская карта", "Рабочие деньги", "Сбережения")
                .forEach { dao.insertAccount(AccountEntity(name = it)) }
        }
        if (dao.categoryCount() == 0) {
            val income = listOf(
                "Зарплата" to "salary", "Подработка" to "work", "Курьерский заработок" to "delivery",
                "Бонусы и чаевые" to "bonus", "Переводы от других людей" to "transfer_in",
                "Возврат долга" to "debt", "Прочие доходы" to "add"
            )
            val expense = listOf(
                "Продукты" to "food", "Транспорт" to "transport", "Аренда" to "rent",
                "Жильё и коммунальные услуги" to "home", "Связь и интернет" to "phone",
                "Покупки" to "shopping", "Развлечения" to "fun", "Образование" to "education",
                "Здоровье" to "health", "Переводы другим людям" to "transfer_out",
                "Возврат долга" to "debt", "Прочие расходы" to "more"
            )
            income.forEach { (name, icon) -> dao.insertCategory(CategoryEntity(name = name, type = "INCOME", icon = icon, isDefault = true)) }
            expense.forEach { (name, icon) -> dao.insertCategory(CategoryEntity(name = name, type = "EXPENSE", icon = icon, isDefault = true)) }
        }
    }

    suspend fun addTransaction(item: TransactionEntity) = dao.insertTransaction(item)
    suspend fun updateTransaction(item: TransactionEntity) = dao.updateTransaction(item)
    suspend fun deleteTransaction(id: Long) = db.withTransaction {
        dao.deleteWorkOrderForTransaction(id)
        dao.deleteTransaction(id)
    }

    suspend fun exportJson(): String {
        return BackupCodec.encode(BackupSnapshot(dao.getAllAccounts(), dao.getAllCategories(), dao.getAllTransactions(), dao.getAllWorkOrders()))
    }

    suspend fun exportCsv(): String {
        val accounts = dao.getAllAccounts().associateBy { it.id }
        val categories = dao.getAllCategories().associateBy { it.id }
        val rows = dao.getAllTransactions()
        fun cell(value: String): String = "\"" + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\""
        return buildString {
            append("id;date_time;type;title;amount_uzs;category;account;target_account;comment\r\n")
            rows.forEach { t ->
                append(t.id).append(';')
                append(cell(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ROOT).format(java.util.Date(t.dateTime)))).append(';')
                append(t.type).append(';').append(cell(t.title)).append(';').append(t.amount).append(';')
                append(cell(categories[t.categoryId]?.name.orEmpty())).append(';')
                append(cell(accounts[t.accountId]?.name.orEmpty())).append(';')
                append(cell(accounts[t.targetAccountId]?.name.orEmpty())).append(';')
                append(cell(t.comment)).append("\r\n")
            }
        }
    }

    suspend fun restoreBackup(json: String) {
        val snapshot = BackupCodec.decode(json)
        db.withTransaction {
            dao.clearWorkOrders()
            dao.clearTransactions()
            dao.clearCategories()
            dao.clearAccounts()
            dao.insertAccounts(snapshot.accounts)
            if (snapshot.categories.isNotEmpty()) dao.insertCategories(snapshot.categories)
            if (snapshot.transactions.isNotEmpty()) dao.insertTransactions(snapshot.transactions)
            if (snapshot.workOrders.isNotEmpty()) dao.insertWorkOrders(snapshot.workOrders)
        }
    }

    suspend fun addAccount(name: String) {
        val normalized = name.trim()
        if (normalized.isNotEmpty()) dao.insertAccount(AccountEntity(name = normalized))
    }

    suspend fun deleteAccount(id: Long): Boolean {
        if (dao.accountUsageCount(id) != 0 || dao.accountCount() <= 1) return false
        dao.deleteAccount(id)
        return true
    }

    suspend fun addCategory(name: String, type: String) {
        val normalized = name.trim()
        if (normalized.isNotEmpty()) dao.insertCategory(CategoryEntity(name = normalized, type = type, isDefault = false))
    }

    suspend fun deleteCategory(id: Long): Boolean = db.withTransaction {
        dao.detachCategoryFromHistory(id)
        dao.deleteCategory(id)
        true
    }

    suspend fun addWorkOrder(order: WorkOrderEntity, accountId: Long, categoryId: Long?) = db.withTransaction {
        val total = Math.addExact(Math.addExact(order.amount, order.tips), order.bonus)
        val transactionId = dao.insertTransaction(
            TransactionEntity(
                title = if (order.orderNumber.isBlank()) "Курьерский заказ" else "Заказ ${order.orderNumber}",
                amount = total,
                type = "INCOME",
                categoryId = categoryId,
                accountId = accountId,
                dateTime = order.dateTime,
                comment = order.comment
            )
        )
        dao.insertWorkOrder(order.copy(transactionId = transactionId))
    }
}
