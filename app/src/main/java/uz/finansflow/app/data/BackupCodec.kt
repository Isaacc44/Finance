package uz.finansflow.app.data

import org.json.JSONArray
import org.json.JSONObject

 data class BackupSnapshot(
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val workOrders: List<WorkOrderEntity>
)

object BackupCodec {
    fun encode(snapshot: BackupSnapshot): String {
        val root = JSONObject().put("format", "FinansFlowBackup").put("schemaVersion", 1).put("exportedAt", System.currentTimeMillis())
        root.put("accounts", JSONArray().apply { snapshot.accounts.forEach { a -> put(JSONObject().put("id", a.id).put("name", a.name).put("icon", a.icon).put("createdAt", a.createdAt)) } })
        root.put("categories", JSONArray().apply { snapshot.categories.forEach { c -> put(JSONObject().put("id", c.id).put("name", c.name).put("type", c.type).put("icon", c.icon).put("isDefault", c.isDefault)) } })
        root.put("transactions", JSONArray().apply { snapshot.transactions.forEach { t ->
            put(JSONObject().put("id", t.id).put("title", t.title).put("amount", t.amount).put("type", t.type)
                .put("categoryId", t.categoryId ?: JSONObject.NULL).put("accountId", t.accountId)
                .put("targetAccountId", t.targetAccountId ?: JSONObject.NULL).put("dateTime", t.dateTime)
                .put("comment", t.comment).put("createdAt", t.createdAt))
        } })
        root.put("workOrders", JSONArray().apply { snapshot.workOrders.forEach { w ->
            put(JSONObject().put("id", w.id).put("orderNumber", w.orderNumber).put("dateTime", w.dateTime)
                .put("amount", w.amount).put("tips", w.tips).put("bonus", w.bonus).put("comment", w.comment)
                .put("transactionId", w.transactionId ?: JSONObject.NULL))
        } })
        return root.toString(2)
    }

    fun decode(json: String): BackupSnapshot {
        val root = JSONObject(json)
        require(root.optString("format") == "FinansFlowBackup") { "Это не резервная копия FinansFlow." }
        require(root.optInt("schemaVersion", -1) == 1) { "Версия резервной копии не поддерживается." }
        val accountJson = root.getJSONArray("accounts")
        val categoryJson = root.getJSONArray("categories")
        val transactionJson = root.getJSONArray("transactions")
        val orderJson = root.optJSONArray("workOrders") ?: JSONArray()

        val accounts = (0 until accountJson.length()).map { i -> accountJson.getJSONObject(i).let { a ->
            AccountEntity(id = a.getLong("id"), name = a.getString("name").also { require(it.isNotBlank()) }, icon = a.optString("icon", "wallet"), createdAt = a.optLong("createdAt", System.currentTimeMillis()))
        } }
        val categories = (0 until categoryJson.length()).map { i -> categoryJson.getJSONObject(i).let { c ->
            val type = c.getString("type")
            require(type == "INCOME" || type == "EXPENSE") { "В резервной копии есть неизвестный тип категории." }
            CategoryEntity(id = c.getLong("id"), name = c.getString("name").also { require(it.isNotBlank()) }, type = type, icon = c.optString("icon", "category"), isDefault = c.optBoolean("isDefault", false))
        } }
        val transactions = (0 until transactionJson.length()).map { i -> transactionJson.getJSONObject(i).let { t ->
            val type = t.getString("type")
            require(type == "INCOME" || type == "EXPENSE" || type == "TRANSFER") { "В резервной копии есть неизвестный тип операции." }
            val amount = t.getLong("amount")
            require(amount > 0L) { "Сумма операции должна быть больше нуля." }
            TransactionEntity(
                id = t.getLong("id"), title = t.getString("title"), amount = amount, type = type,
                categoryId = if (t.isNull("categoryId")) null else t.getLong("categoryId"),
                accountId = t.getLong("accountId"), targetAccountId = if (t.isNull("targetAccountId")) null else t.getLong("targetAccountId"),
                dateTime = t.getLong("dateTime"), comment = t.optString("comment", ""), createdAt = t.optLong("createdAt", System.currentTimeMillis())
            )
        } }
        val orders = (0 until orderJson.length()).map { i -> orderJson.getJSONObject(i).let { w ->
            val amount = w.getLong("amount"); val tips = w.optLong("tips", 0L); val bonus = w.optLong("bonus", 0L)
            require(amount >= 0L && tips >= 0L && bonus >= 0L) { "В заказе найдена отрицательная сумма." }
            WorkOrderEntity(id = w.getLong("id"), orderNumber = w.optString("orderNumber", ""), dateTime = w.getLong("dateTime"), amount = amount, tips = tips, bonus = bonus, comment = w.optString("comment", ""), transactionId = if (w.isNull("transactionId")) null else w.getLong("transactionId"))
        } }

        require(accounts.isNotEmpty()) { "В резервной копии должен быть хотя бы один счёт." }
        require(accounts.map { it.id }.toSet().size == accounts.size) { "В резервной копии повторяются ID счетов." }
        require(categories.map { it.id }.toSet().size == categories.size) { "В резервной копии повторяются ID категорий." }
        require(transactions.map { it.id }.toSet().size == transactions.size) { "В резервной копии повторяются ID операций." }
        val accountIds = accounts.map { it.id }.toSet()
        val categoryIds = categories.map { it.id }.toSet()
        val transactionIds = transactions.map { it.id }.toSet()
        transactions.forEach { tx ->
            require(tx.accountId in accountIds) { "У операции «${tx.title}» не найден счёт." }
            if (tx.type == "TRANSFER") require(tx.targetAccountId != null && tx.targetAccountId in accountIds && tx.targetAccountId != tx.accountId) { "Некорректный перевод между счетами." }
            if (tx.categoryId != null) require(tx.categoryId in categoryIds) { "У операции «${tx.title}» не найдена категория." }
        }
        orders.forEach { order -> if (order.transactionId != null) require(order.transactionId in transactionIds) { "У заказа не найдена связанная операция." } }
        return BackupSnapshot(accounts, categories, transactions, orders)
    }
}
