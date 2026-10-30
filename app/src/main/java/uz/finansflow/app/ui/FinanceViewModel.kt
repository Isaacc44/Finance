package uz.finansflow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.finansflow.app.data.AccountEntity
import uz.finansflow.app.data.AppDatabase
import uz.finansflow.app.data.CategoryEntity
import uz.finansflow.app.data.FinanceRepository
import uz.finansflow.app.data.TransactionEntity
import uz.finansflow.app.data.WorkOrderEntity

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(AppDatabase.get(application))

    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val workOrders = repository.workOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { viewModelScope.launch { repository.ensureDefaults() } }

    fun saveTransaction(item: TransactionEntity, done: () -> Unit = {}) = viewModelScope.launch {
        if (item.id == 0L) repository.addTransaction(item) else repository.updateTransaction(item)
        done()
    }

    fun deleteTransaction(id: Long) = viewModelScope.launch { repository.deleteTransaction(id) }
    fun exportJson(done: (String) -> Unit) = viewModelScope.launch { done(repository.exportJson()) }
    fun exportCsv(done: (String) -> Unit) = viewModelScope.launch { done(repository.exportCsv()) }
    fun restoreBackup(json: String, done: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            repository.restoreBackup(json)
            done(true, "Резервная копия восстановлена.")
        } catch (e: Exception) {
            done(false, e.message ?: "Не удалось восстановить резервную копию.")
        }
    }
    fun addAccount(name: String) = viewModelScope.launch { repository.addAccount(name) }
    fun deleteAccount(id: Long, result: (Boolean) -> Unit = {}) = viewModelScope.launch { result(repository.deleteAccount(id)) }
    fun addCategory(name: String, type: String) = viewModelScope.launch { repository.addCategory(name, type) }
    fun deleteCategory(id: Long) = viewModelScope.launch { repository.deleteCategory(id) }
    fun addWorkOrder(order: WorkOrderEntity, accountId: Long, categoryId: Long?, done: () -> Unit = {}) = viewModelScope.launch {
        repository.addWorkOrder(order, accountId, categoryId)
        done()
    }
}
