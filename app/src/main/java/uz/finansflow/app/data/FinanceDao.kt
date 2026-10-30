package uz.finansflow.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM transactions ORDER BY dateTime DESC, id DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransaction(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC, id DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(item: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun accountCount(): Int

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(item: AccountEntity): Long

    @Update
    suspend fun updateAccount(item: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :id OR targetAccountId = :id")
    suspend fun accountUsageCount(id: Long): Int

    @Query("SELECT * FROM categories ORDER BY type, name COLLATE NOCASE")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(item: CategoryEntity): Long

    @Update
    suspend fun updateCategory(item: CategoryEntity)

    @Query("UPDATE transactions SET categoryId = NULL WHERE categoryId = :id")
    suspend fun detachCategoryFromHistory(id: Long)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    @Query("SELECT * FROM work_orders ORDER BY dateTime DESC, id DESC")
    fun observeWorkOrders(): Flow<List<WorkOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkOrder(item: WorkOrderEntity): Long

    @Query("SELECT * FROM work_orders ORDER BY dateTime DESC, id DESC")
    suspend fun getAllWorkOrders(): List<WorkOrderEntity>

    @Query("DELETE FROM work_orders")
    suspend fun clearWorkOrders()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccounts(items: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategories(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransactions(items: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkOrders(items: List<WorkOrderEntity>)

    @Query("DELETE FROM work_orders WHERE transactionId = :transactionId")
    suspend fun deleteWorkOrderForTransaction(transactionId: Long)
}
