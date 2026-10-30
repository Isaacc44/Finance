package uz.finansflow.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.menuAnchor
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.finansflow.app.data.AccountEntity
import uz.finansflow.app.data.CategoryEntity
import uz.finansflow.app.data.TransactionEntity
import uz.finansflow.app.data.WorkOrderEntity
import uz.finansflow.app.domain.FinanceCalculations
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Positive = Color(0xFF16856B)
private val Negative = Color(0xFFD65353)
private val Accent = Color(0xFF6455D8)

@Composable
fun FinanceApp(vm: FinanceViewModel = viewModel()) {
    val context = LocalContext.current
    var exportContent by remember { mutableStateOf("") }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var backupMessage by remember { mutableStateOf("") }
    val jsonExportLauncher = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        if (uri != null) try { context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(exportContent) }; backupMessage = "JSON-резервная копия сохранена." } catch (e: Exception) { backupMessage = "Ошибка сохранения: ${e.message}" }
    }
    val csvExportLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        if (uri != null) try { context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(exportContent) }; backupMessage = "CSV-файл сохранён." } catch (e: Exception) { backupMessage = "Ошибка сохранения: ${e.message}" }
    }
    val importLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) try { pendingImportJson = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "" } catch (e: Exception) { backupMessage = "Не удалось прочитать файл: ${e.message}" }
    }
    val transactions by vm.transactions.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val categories by vm.categories.collectAsState()
    val orders by vm.workOrders.collectAsState()
    var tab by rememberSaveable { mutableStateOf("home") }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingType by rememberSaveable { mutableStateOf("EXPENSE") }

    Scaffold(
        bottomBar = {
            if (tab != "manage") NavigationBar {
                NavigationBarItem(selected = tab == "home", onClick = { tab = "home" }, icon = { androidx.compose.material3.Icon(Icons.Default.Home, null) }, label = { Text("Главная") })
                NavigationBarItem(selected = tab == "history", onClick = { tab = "history" }, icon = { androidx.compose.material3.Icon(Icons.Default.History, null) }, label = { Text("История") })
                NavigationBarItem(selected = tab == "add", onClick = { editingId = null; pendingType = "EXPENSE"; tab = "add" }, icon = { androidx.compose.material3.Icon(Icons.Default.Add, null) }, label = { Text("Добавить") })
                NavigationBarItem(selected = tab == "work", onClick = { tab = "work" }, icon = { androidx.compose.material3.Icon(Icons.Default.Work, null) }, label = { Text("Работа") })
                NavigationBarItem(selected = tab == "stats", onClick = { tab = "stats" }, icon = { androidx.compose.material3.Icon(Icons.Default.Analytics, null) }, label = { Text("Статистика") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                "home" -> HomeScreen(
                    transactions, accounts, categories,
                    onAddIncome = { editingId = null; pendingType = "INCOME"; tab = "add" },
                    onAddExpense = { editingId = null; pendingType = "EXPENSE"; tab = "add" },
                    onEdit = { editingId = it.id; tab = "add" },
                    onManage = { tab = "manage" },
                    onOpenHistory = { tab = "history" }
                )
                "history" -> HistoryScreen(transactions, accounts, categories,
                    onEdit = { editingId = it.id; tab = "add" },
                    onDelete = vm::deleteTransaction
                )
                "add" -> {
                    val current = transactions.firstOrNull { it.id == editingId }
                    keyTransactionEditor(editingId, pendingType, current, accounts, categories, vm) { tab = "history"; editingId = null }
                }
                "work" -> WorkScreen(orders, accounts, categories, vm)
                "stats" -> StatisticsScreen(transactions, categories)
                "manage" -> ManageScreen(
                    accounts, categories, transactions, vm,
                    backupMessage = backupMessage,
                    onBack = { tab = "home" },
                    onExportJson = { vm.exportJson { payload -> exportContent = payload; jsonExportLauncher.launch("FinansFlow_backup.json") } },
                    onExportCsv = { vm.exportCsv { payload -> exportContent = payload; csvExportLauncher.launch("FinansFlow_transactions.csv") } },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/json", "text/*")) }
                )
            }
            if (pendingImportJson != null) {
                AlertDialog(
                    onDismissRequest = { pendingImportJson = null },
                    title = { Text("Восстановить резервную копию?") },
                    text = { Text("Восстановление заменит текущие счета, категории, операции и заказы данными из файла. Перед продолжением убедись, что текущие данные сохранены отдельно.") },
                    confirmButton = { TextButton(onClick = { val json = pendingImportJson ?: return@TextButton; vm.restoreBackup(json) { success, message -> backupMessage = message; pendingImportJson = null; if (success) tab = "home" } }) { Text("Заменить данные", color = MaterialTheme.colorScheme.error) } },
                    dismissButton = { TextButton(onClick = { pendingImportJson = null }) { Text("Отмена") } }
                )
            }
        }
    }
}

@Composable
private fun keyTransactionEditor(
    editId: Long?, defaultType: String, current: TransactionEntity?, accounts: List<AccountEntity>, categories: List<CategoryEntity>,
    vm: FinanceViewModel, onSaved: () -> Unit
) {
    androidx.compose.runtime.key(editId, defaultType) {
        OperationForm(current, defaultType, accounts, categories, vm, onSaved)
    }
}

@Composable
private fun HomeScreen(
    transactions: List<TransactionEntity>, accounts: List<AccountEntity>, categories: List<CategoryEntity>,
    onAddIncome: () -> Unit, onAddExpense: () -> Unit, onEdit: (TransactionEntity) -> Unit, onManage: () -> Unit, onOpenHistory: () -> Unit
) {
    var period by rememberSaveable { mutableStateOf("Месяц") }
    val today = LocalDate.now()
    val periodTransactions = FinanceCalculations.periodTransactions(transactions, period, today)
    val balance = FinanceCalculations.totalBalance(accounts, transactions)
    val income = FinanceCalculations.income(periodTransactions)
    val expenses = FinanceCalculations.expenses(periodTransactions)

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("FinansFlow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Твои деньги под контролем", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(46.dp).clickable { onManage() }) {
                    Box(contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(Icons.Default.MoreHoriz, contentDescription = "Счета и категории") }
                }
            }
        }
        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Accent), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Общий баланс", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(money(balance), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Text("Все счета · с учётом переводов между ними", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("Сегодня", "Неделя", "Месяц", "Год").forEach { value ->
                    PeriodChip(value, period == value) { period = value }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Доходы", income, Positive, Icons.Default.ArrowDownward, Modifier.weight(1f))
                MetricCard("Расходы", expenses, Negative, Icons.Default.ArrowUpward, Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Результат за период", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(money(income - expenses), fontWeight = FontWeight.Bold, fontSize = 21.sp, color = if (income >= expenses) Positive else Negative)
                    }
                    Text("${if (income - expenses >= 0) "+" else "−"}${money(kotlin.math.abs(income - expenses))}", color = if (income >= expenses) Positive else Negative)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onAddIncome, modifier = Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Доход") }
                OutlinedButton(onClick = onAddExpense, modifier = Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Default.ArrowUpward, null); Spacer(Modifier.width(6.dp)); Text("Расход") }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Последние операции", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onOpenHistory) { Text("Все") }
            }
        }
        if (transactions.isEmpty()) item { EmptyState("Пока нет операций", "Добавь доход или расход — здесь появится история.") }
        items(transactions.take(6), key = { it.id }) { tx -> OperationRow(tx, categories, accounts, onClick = { onEdit(tx) }) }
        item {
            Text("Мои счета", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            accounts.forEach { account ->
                val amount = FinanceCalculations.accountBalance(account.id, transactions)
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(Icons.Default.Category, null) }
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(account.name, fontWeight = FontWeight.Medium); Text("Счёт", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(money(amount), fontWeight = FontWeight.SemiBold)
                }
            }
            OutlinedButton(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text("Управлять счетами и категориями") }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: Long, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color.copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(icon, null, tint = color, modifier = Modifier.size(17.dp)) }
                }
                Spacer(Modifier.width(7.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(9.dp))
            Text(money(value), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) Button(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text(label, fontSize = 12.sp) }
    else OutlinedButton(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text(label, fontSize = 12.sp) }
}

@Composable
private fun HistoryScreen(
    transactions: List<TransactionEntity>, accounts: List<AccountEntity>, categories: List<CategoryEntity>,
    onEdit: (TransactionEntity) -> Unit, onDelete: (Long) -> Unit
) {
    var filter by rememberSaveable { mutableStateOf("Все") }
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val filtered = transactions.filter { tx ->
        (filter == "Все" || (filter == "Доходы" && tx.type == "INCOME") || (filter == "Расходы" && tx.type == "EXPENSE") || (filter == "Переводы" && tx.type == "TRANSFER")) &&
            (query.isBlank() || tx.title.contains(query, true) || tx.comment.contains(query, true))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("История", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Поиск по названию и комментарию") }, singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 10.dp)) {
            listOf("Все", "Доходы", "Расходы", "Переводы").forEach { PeriodChip(it, filter == it) { filter = it } }
        }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EmptyState("Ничего не найдено", "Попробуй изменить фильтр или поисковый запрос.") }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                val grouped = filtered.groupBy { dayLabel(it.dateTime) }
                grouped.forEach { (day, dayItems) ->
                    item(key = "date_$day") { Text(day, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(dayItems, key = { it.id }) { tx ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { OperationRow(tx, categories, accounts) { onEdit(tx) } }
                            androidx.compose.material3.IconButton(onClick = { pendingDelete = tx }) { androidx.compose.material3.Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
    pendingDelete?.let { tx ->
        AlertDialog(onDismissRequest = { pendingDelete = null }, title = { Text("Удалить операцию?") },
            text = { Text("«${tx.title}» будет удалена из истории. Баланс пересчитается автоматически.") },
            confirmButton = { TextButton(onClick = { onDelete(tx.id); pendingDelete = null }) { Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Отмена") } })
    }
}

@Composable
private fun OperationRow(tx: TransactionEntity, categories: List<CategoryEntity>, accounts: List<AccountEntity>, onClick: () -> Unit) {
    val color = when (tx.type) { "INCOME" -> Positive; "EXPENSE" -> Negative; else -> Accent }
    val symbol = when (tx.type) { "INCOME" -> "+"; "EXPENSE" -> "−"; else -> "↔" }
    val category = categories.firstOrNull { it.id == tx.categoryId }?.name ?: if (tx.type == "TRANSFER") "Между своими счетами" else "Без категории"
    val accountName = accounts.firstOrNull { it.id == tx.accountId }?.name ?: "Счёт удалён"
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(symbol, color = color, fontSize = 23.sp, fontWeight = FontWeight.Bold) }
        }
        Column(Modifier.weight(1f).padding(start = 11.dp, end = 5.dp)) {
            Text(tx.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$category · $accountName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(timeLabel(tx.dateTime), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("$symbol${money(tx.amount)}", color = color, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 13.sp)
    }
    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperationForm(
    existing: TransactionEntity?, defaultType: String, accounts: List<AccountEntity>, categories: List<CategoryEntity>, vm: FinanceViewModel, onSaved: () -> Unit
) {
    val context = LocalContext.current
    var type by remember(existing?.id, defaultType) { mutableStateOf(existing?.type ?: defaultType) }
    var amountText by remember(existing?.id) { mutableStateOf(existing?.amount?.toString() ?: "") }
    var title by remember(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var comment by remember(existing?.id) { mutableStateOf(existing?.comment ?: "") }
    var selectedCategory by remember(existing?.id) { mutableStateOf(existing?.categoryId) }
    var sourceAccount by remember(existing?.id) { mutableStateOf(existing?.accountId ?: 0L) }
    var targetAccount by remember(existing?.id) { mutableStateOf(existing?.targetAccountId ?: 0L) }
    var duplicate by remember(existing?.id) { mutableStateOf(false) }
    var dateTime by remember(existing?.id) { mutableLongStateOf(existing?.dateTime ?: System.currentTimeMillis()) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val categoryList = categories.filter { it.type == if (type == "INCOME") "INCOME" else "EXPENSE" }
    val currentCategory = categoryList.firstOrNull { it.id == selectedCategory }
    val effectiveSource = if (sourceAccount != 0L) sourceAccount else accounts.firstOrNull()?.id ?: 0L
    val effectiveTarget = if (targetAccount != 0L) targetAccount else accounts.firstOrNull { it.id != effectiveSource }?.id ?: 0L
    val currentAccount = accounts.firstOrNull { it.id == effectiveSource }
    val currentTarget = accounts.firstOrNull { it.id == effectiveTarget }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (existing == null) "Новая операция" else "Редактирование", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (existing != null) TextButton(onClick = { duplicate = true; title = "${existing.title} (копия)" }) { Text("Копировать") }
        }
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("INCOME" to "Доход", "EXPENSE" to "Расход", "TRANSFER" to "Перевод").forEach { (value, label) ->
                    if (type == value) Button(onClick = { type = value; selectedCategory = null }, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1) }
                    else OutlinedButton(onClick = { type = value; selectedCategory = null }, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1) }
                }
            }
            OutlinedTextField(amountText, { amountText = it.filter(Char::isDigit).take(15); error = "" }, modifier = Modifier.fillMaxWidth(), label = { Text("Сумма, сум") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = MaterialTheme.typography.headlineSmall)
            if (type != "TRANSFER") {
                OutlinedTextField(title, { title = it; error = "" }, modifier = Modifier.fillMaxWidth(), label = { Text("Название операции") }, singleLine = true)
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(value = currentCategory?.name ?: "Выбрать категорию", onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text("Категория") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) })
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categoryList.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category.id; if (title.isBlank()) title = category.name; categoryExpanded = false }) }
                        DropdownMenuItem(text = { Text("+ Создать категорию") }, onClick = { categoryExpanded = false; showCategoryDialog = true })
                    }
                }
            } else {
                OutlinedTextField("Перевод между своими счетами", {}, modifier = Modifier.fillMaxWidth(), readOnly = true, label = { Text("Операция") })
            }
            ExposedDropdownMenuBox(expanded = sourceExpanded, onExpandedChange = { sourceExpanded = !sourceExpanded }) {
                OutlinedTextField(currentAccount?.name ?: "Выбрать счёт", {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text(if (type == "TRANSFER") "Откуда" else "Счёт") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sourceExpanded) })
                ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                    accounts.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { sourceAccount = item.id; sourceExpanded = false }) }
                    DropdownMenuItem(text = { Text("+ Новый счёт") }, onClick = { sourceExpanded = false; error = "Создать счёт можно на главном экране → управление счетами." })
                }
            }
            if (type == "TRANSFER") {
                ExposedDropdownMenuBox(expanded = targetExpanded, onExpandedChange = { targetExpanded = !targetExpanded }) {
                    OutlinedTextField(currentTarget?.name ?: "Выбрать счёт", {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text("Куда") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(targetExpanded) })
                    ExposedDropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                        accounts.filter { it.id != sourceAccount }.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { targetAccount = item.id; targetExpanded = false }) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker(context, dateTime) { dateTime = it } }, modifier = Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(5.dp)); Text(SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(dateTime))) }
                OutlinedButton(onClick = { showTimePicker(context, dateTime) { dateTime = it } }, modifier = Modifier.weight(1f)) { Text(SimpleDateFormat("HH:mm", Locale("ru")).format(Date(dateTime))) }
            }
            OutlinedTextField(comment, { comment = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Комментарий (необязательно)") }, minLines = 1, maxLines = 3)
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Button(onClick = {
                val amount = amountText.toLongOrNull()
                when {
                    accounts.isEmpty() -> error = "Сначала создай хотя бы один счёт."
                    amount == null || amount <= 0L -> error = "Укажи сумму больше нуля."
                    type != "TRANSFER" && title.isBlank() -> error = "Укажи название операции."
                    type == "TRANSFER" && (effectiveTarget == effectiveSource || currentTarget == null) -> error = "Выбери другой счёт для перевода."
                    else -> {
                        saving = true
                        val item = TransactionEntity(
                            id = if (duplicate) 0L else existing?.id ?: 0L,
                            title = if (type == "TRANSFER") "Перевод: ${currentAccount?.name ?: "счёт"} → ${currentTarget?.name ?: "счёт"}" else title.trim(),
                            amount = amount,
                            type = type,
                            categoryId = if (type == "TRANSFER") null else selectedCategory,
                            accountId = effectiveSource,
                            targetAccountId = if (type == "TRANSFER") effectiveTarget else null,
                            dateTime = dateTime,
                            comment = comment.trim(),
                            createdAt = if (duplicate) System.currentTimeMillis() else existing?.createdAt ?: System.currentTimeMillis()
                        )
                        vm.saveTransaction(item) { saving = false; onSaved() }
                    }
                }
            }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (saving) "Сохранение…" else if (existing == null || duplicate) "Сохранить операцию" else "Сохранить изменения") }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showCategoryDialog) {
        AlertDialog(onDismissRequest = { showCategoryDialog = false }, title = { Text("Новая категория") },
            text = { OutlinedTextField(newCategoryName, { newCategoryName = it }, label = { Text("Название") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { if (newCategoryName.isNotBlank()) { vm.addCategory(newCategoryName, if (type == "INCOME") "INCOME" else "EXPENSE"); newCategoryName = ""; showCategoryDialog = false } }) { Text("Создать") } },
            dismissButton = { TextButton(onClick = { showCategoryDialog = false }) { Text("Отмена") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkScreen(orders: List<WorkOrderEntity>, accounts: List<AccountEntity>, categories: List<CategoryEntity>, vm: FinanceViewModel) {
    val context = LocalContext.current
    var orderNumber by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var tips by rememberSaveable { mutableStateOf("0") }
    var bonus by rememberSaveable { mutableStateOf("0") }
    var comment by rememberSaveable { mutableStateOf("") }
    var selectedAccount by rememberSaveable { mutableStateOf<Long?>(null) }
    var dateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var period by rememberSaveable { mutableStateOf("Сегодня") }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    val account = accounts.firstOrNull { it.id == (selectedAccount ?: accounts.firstOrNull()?.id) }
    val filtered = orders.filter {
        val start = FinanceCalculations.startOfPeriod(period)
        FinanceCalculations.inDateRange(TransactionEntity(title = "", amount = 0, type = "INCOME", accountId = 0, dateTime = it.dateTime), start, LocalDate.now())
    }
    val base = filtered.sumOf { it.amount }
    val tipsTotal = filtered.sumOf { it.tips }
    val bonusTotal = filtered.sumOf { it.bonus }
    val total = base + tipsTotal + bonusTotal

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("Работа", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Курьерский заработок и заказы", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Заработок · $period", fontWeight = FontWeight.SemiBold)
                    Text(money(total), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Positive)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Заказы: ${filtered.size}"); Text("Чаевые: ${money(tipsTotal)}") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Бонусы: ${money(bonusTotal)}"); Text("В среднем: ${money(if (filtered.isEmpty()) 0 else total / filtered.size)}") }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Сегодня", "Неделя", "Месяц").forEach { PeriodChip(it, period == it) { period = it } } }
                }
            }
        }
        item {
            Text("Добавить заказ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(orderNumber, { orderNumber = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Номер заказа (необязательно)") }, singleLine = true)
            OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, modifier = Modifier.fillMaxWidth(), label = { Text("Заработок за заказ, сум") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(tips, { tips = it.filter(Char::isDigit) }, modifier = Modifier.weight(1f), label = { Text("Чаевые") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(bonus, { bonus = it.filter(Char::isDigit) }, modifier = Modifier.weight(1f), label = { Text("Бонус") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
            ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = !accountExpanded }) {
                OutlinedTextField(account?.name ?: "Выбрать счёт", {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text("Куда зачислить заработок") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(accountExpanded) })
                ExposedDropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                    accounts.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { selectedAccount = item.id; accountExpanded = false }) }
                }
            }
            OutlinedButton(onClick = { showDatePicker(context, dateTime) { dateTime = it } }, modifier = Modifier.fillMaxWidth()) { Text("Дата заказа: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(Date(dateTime))}") }
            OutlinedTextField(comment, { comment = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Комментарий") }, singleLine = true)
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val earning = amount.toLongOrNull()
                val tipsValue = tips.toLongOrNull() ?: 0L
                val bonusValue = bonus.toLongOrNull() ?: 0L
                if (earning == null || earning <= 0L) error = "Введи сумму заработка больше нуля."
                else if (account == null) error = "Сначала создай счёт на главной странице."
                else {
                    saving = true
                    val categoryId = categories.firstOrNull { it.name == "Курьерский заработок" && it.type == "INCOME" }?.id
                    vm.addWorkOrder(WorkOrderEntity(orderNumber = orderNumber.trim(), dateTime = dateTime, amount = earning, tips = tipsValue, bonus = bonusValue, comment = comment.trim()), account.id, categoryId) {
                        orderNumber = ""; amount = ""; tips = "0"; bonus = "0"; comment = ""; dateTime = System.currentTimeMillis(); saving = false; error = ""
                    }
                }
            }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text(if (saving) "Сохранение…" else "Сохранить заработок") }
        }
        item { Text("История заказов", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (filtered.isEmpty()) item { EmptyState("Заказов пока нет", "Добавленные заказы появятся здесь.") }
        items(filtered, key = { it.id }) { item ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (item.orderNumber.isBlank()) "Курьерский заказ" else "Заказ ${item.orderNumber}", fontWeight = FontWeight.SemiBold)
                        Text(timeLabel(item.dateTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Чаевые ${money(item.tips)} · бонус ${money(item.bonus)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(money(item.amount + item.tips + item.bonus), color = Positive, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
    }
}

@Composable
private fun StatisticsScreen(transactions: List<TransactionEntity>, categories: List<CategoryEntity>) {
    var period by rememberSaveable { mutableStateOf("Месяц") }
    val today = LocalDate.now()
    val current = FinanceCalculations.periodTransactions(transactions, period, today).filter { it.type != "TRANSFER" }
    val income = FinanceCalculations.income(current)
    val expenses = FinanceCalculations.expenses(current)
    val expenseGroups = current.filter { it.type == "EXPENSE" }.groupBy { it.categoryId }
        .map { (id, list) -> (categories.firstOrNull { it.id == id }?.name ?: "Без категории") to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
    val incomeGroups = current.filter { it.type == "INCOME" }.groupBy { it.categoryId }
        .map { (id, list) -> (categories.firstOrNull { it.id == id }?.name ?: "Без категории") to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
    val dayCount = maxOf(1, java.time.temporal.ChronoUnit.DAYS.between(FinanceCalculations.startOfPeriod(period, today), today).toInt() + 1)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(Modifier.height(12.dp)); Text("Статистика", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Сегодня", "Неделя", "Месяц", "Год").forEach { PeriodChip(it, period == it) { period = it } } } }
        item {
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(17.dp)) {
                    Text("Финансовый результат", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(money(income - expenses), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = if (income >= expenses) Positive else Negative)
                    Spacer(Modifier.height(12.dp)); Divider()
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Доходы"); Text(money(income), fontWeight = FontWeight.SemiBold, color = Positive) }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Расходы"); Text(money(expenses), fontWeight = FontWeight.SemiBold, color = Negative) }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Расходы в день, в среднем"); Text(money(expenses / dayCount)) }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Доходы в день, в среднем"); Text(money(income / dayCount)) }
                }
            }
        }
        item { Text("Расходы по категориям", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (expenseGroups.isEmpty()) item { Text("За выбранный период расходов нет.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(expenseGroups.take(10), key = { "e_${it.first}" }) { (name, amount) ->
            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name); Text(money(amount), fontWeight = FontWeight.SemiBold) }
                LinearProgressIndicator(progress = { if (expenses <= 0L) 0f else (amount.toDouble() / expenses).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(7.dp).clip(CircleShape))
            }
        }
        item { Text("Доходы по категориям", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        if (incomeGroups.isEmpty()) item { Text("За выбранный период доходов нет.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(incomeGroups.take(10), key = { "i_${it.first}" }) { (name, amount) ->
            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name); Text(money(amount), fontWeight = FontWeight.SemiBold) }
                LinearProgressIndicator(progress = { if (income <= 0L) 0f else (amount.toDouble() / income).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(7.dp).clip(CircleShape), color = Positive)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ManageScreen(accounts: List<AccountEntity>, categories: List<CategoryEntity>, transactions: List<TransactionEntity>, vm: FinanceViewModel, backupMessage: String, onBack: () -> Unit, onExportJson: () -> Unit, onExportCsv: () -> Unit, onImport: () -> Unit) {
    var showAddAccount by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("") }
    var showAddCategory by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("EXPENSE") }
    var message by remember { mutableStateOf("") }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.Default.Close, "Назад") }
            Text("Счета и категории", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Счета", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); TextButton(onClick = { showAddAccount = true }) { Text("+ Добавить") } }
            }
            items(accounts, key = { "a_${it.id}" }) { account ->
                val balance = FinanceCalculations.accountBalance(account.id, transactions)
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(account.name, fontWeight = FontWeight.SemiBold); Text(money(balance), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        androidx.compose.material3.IconButton(onClick = { vm.deleteAccount(account.id) { deleted -> message = if (deleted) "Счёт удалён." else "Нельзя удалить счёт с операциями или последний счёт." } }) { androidx.compose.material3.Icon(Icons.Default.Delete, "Удалить счёт") }
                    }
                }
            }
            item {
                Spacer(Modifier.height(10.dp))
                Text("Резервная копия", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("JSON содержит счета, категории, операции и заказы. Импорт заменяет текущие данные после подтверждения.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onExportJson, modifier = Modifier.weight(1f)) { Text("Экспорт JSON") }
                    OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text("Импорт JSON") }
                }
                OutlinedButton(onClick = onExportCsv, modifier = Modifier.fillMaxWidth()) { Text("Экспорт операций CSV") }
                if (backupMessage.isNotBlank()) Text(backupMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("Категории", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); TextButton(onClick = { showAddCategory = true }) { Text("+ Добавить") } }
                Text("Удаление категории не удаляет операции из истории.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            item { Text("Доходы", fontWeight = FontWeight.SemiBold, color = Positive, modifier = Modifier.padding(top = 5.dp)) }
            items(categories.filter { it.type == "INCOME" }, key = { "ci_${it.id}" }) { category -> CategoryManageRow(category, onDelete = { deletingCategory = category }) }
            item { Text("Расходы", fontWeight = FontWeight.SemiBold, color = Negative, modifier = Modifier.padding(top = 5.dp)) }
            items(categories.filter { it.type == "EXPENSE" }, key = { "ce_${it.id}" }) { category -> CategoryManageRow(category, onDelete = { deletingCategory = category }) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
    if (showAddAccount) AlertDialog(onDismissRequest = { showAddAccount = false }, title = { Text("Новый счёт") }, text = { OutlinedTextField(accountName, { accountName = it }, label = { Text("Название счёта") }, singleLine = true) }, confirmButton = { TextButton(onClick = { if (accountName.isNotBlank()) { vm.addAccount(accountName); accountName = ""; showAddAccount = false } }) { Text("Добавить") } }, dismissButton = { TextButton(onClick = { showAddAccount = false }) { Text("Отмена") } })
    if (showAddCategory) AlertDialog(onDismissRequest = { showAddCategory = false }, title = { Text("Новая категория") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(categoryName, { categoryName = it }, label = { Text("Название") }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PeriodChip("Расход", categoryType == "EXPENSE") { categoryType = "EXPENSE" }; PeriodChip("Доход", categoryType == "INCOME") { categoryType = "INCOME" } }
        }
    }, confirmButton = { TextButton(onClick = { if (categoryName.isNotBlank()) { vm.addCategory(categoryName, categoryType); categoryName = ""; showAddCategory = false } }) { Text("Создать") } }, dismissButton = { TextButton(onClick = { showAddCategory = false }) { Text("Отмена") } })
    deletingCategory?.let { cat -> AlertDialog(onDismissRequest = { deletingCategory = null }, title = { Text("Удалить категорию?") }, text = { Text("Категория «${cat.name}» будет удалена. Связанные операции сохранятся без категории.") }, confirmButton = { TextButton(onClick = { vm.deleteCategory(cat.id); deletingCategory = null }) { Text("Удалить") } }, dismissButton = { TextButton(onClick = { deletingCategory = null }) { Text("Отмена") } }) }
}

@Composable
private fun CategoryManageRow(category: CategoryEntity, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(Icons.Default.Category, null) } }
        Text(category.name, Modifier.weight(1f).padding(start = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!category.isDefault) androidx.compose.material3.IconButton(onClick = onDelete) { androidx.compose.material3.Icon(Icons.Default.Delete, "Удалить категорию") }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(58.dp)) { Box(contentAlignment = Alignment.Center) { androidx.compose.material3.Icon(Icons.Default.MoreHoriz, null, modifier = Modifier.size(28.dp)) } }
        Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

private fun showDatePicker(context: android.content.Context, current: Long, onSet: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = current }
    DatePickerDialog(context, { _, year, month, day ->
        val newCal = Calendar.getInstance().apply { timeInMillis = current; set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day) }
        onSet(newCal.timeInMillis)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}

private fun showTimePicker(context: android.content.Context, current: Long, onSet: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = current }
    TimePickerDialog(context, { _, hour, minute ->
        val newCal = Calendar.getInstance().apply { timeInMillis = current; set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        onSet(newCal.timeInMillis)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
}

private fun money(amount: Long): String = NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(amount) + " сум"
private fun timeLabel(millis: Long): String = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(millis))
private fun dayLabel(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
    }
}
