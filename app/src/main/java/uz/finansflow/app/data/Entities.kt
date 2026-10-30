package uz.finansflow.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "wallet",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // INCOME or EXPENSE
    val icon: String = "category",
    val isDefault: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long, // whole Uzbek so'm; integer storage avoids floating-point errors
    val type: String, // INCOME, EXPENSE, TRANSFER
    val categoryId: Long? = null,
    val accountId: Long,
    val targetAccountId: Long? = null,
    val dateTime: Long = System.currentTimeMillis(),
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val dateTime: Long,
    val amount: Long,
    val tips: Long = 0,
    val bonus: Long = 0,
    val comment: String = "",
    val transactionId: Long? = null
)
