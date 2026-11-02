package com.budgetapp.personal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetapp.personal.model.TransactionEntity
import com.budgetapp.personal.model.TransactionType
import com.budgetapp.personal.ui.components.BudgetCircleGauge
import com.budgetapp.personal.ui.components.HeaderBar
import com.budgetapp.personal.ui.components.TransactionInputCard
import com.budgetapp.personal.ui.components.TransactionList
import com.budgetapp.personal.ui.dialogs.EditBudgetDialog
import com.budgetapp.personal.ui.dialogs.EndDayConfirmDialog
import com.budgetapp.personal.ui.dialogs.InitialBudgetDialog

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    BudgetScreen(
        uiState = uiState,
        onInitializeBudget = { startingBudget -> viewModel.initializeBudget(startingBudget) },
        onAddTransaction = { amount, description, type -> viewModel.addTransaction(amount, description, type) },
        onDeleteTransaction = { transaction -> viewModel.deleteTransaction(transaction) },
        onEndDay = { viewModel.endDay() },
        onUpdateMonthlyBudget = { amount -> viewModel.updateMonthlyBudget(amount) },
        onUpdateLuxuryBudget = { amount -> viewModel.updateLuxuryBudget(amount) },
        modifier = modifier
    )
}

@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onInitializeBudget: (Double) -> Unit,
    onAddTransaction: (Double, String, TransactionType) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onEndDay: () -> Unit,
    onUpdateMonthlyBudget: (Double) -> Unit = {},
    onUpdateLuxuryBudget: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEndDayDialog by remember { mutableStateOf(false) }
    var showEditMonthlyDialog by remember { mutableStateOf(false) }
    var showEditLuxuryDialog by remember { mutableStateOf(false) }

    // First-launch initial budget dialog
    if (!uiState.isInitialized) {
        InitialBudgetDialog(
            onSaveBudget = onInitializeBudget
        )
    }

    // End Day confirmation dialog
    if (showEndDayDialog) {
        EndDayConfirmDialog(
            onConfirm = {
                showEndDayDialog = false
                onEndDay()
            },
            onDismiss = {
                showEndDayDialog = false
            }
        )
    }

    // Edit Monthly Budget dialog
    if (showEditMonthlyDialog) {
        EditBudgetDialog(
            title = "Edit Monthly Budget",
            initialAmount = uiState.monthlyBudgetRemaining,
            onDismiss = { showEditMonthlyDialog = false },
            onSave = { newAmount ->
                onUpdateMonthlyBudget(newAmount)
                showEditMonthlyDialog = false
            }
        )
    }

    // Edit Luxury Budget dialog
    if (showEditLuxuryDialog) {
        EditBudgetDialog(
            title = "Edit Luxury Budget",
            initialAmount = uiState.luxuryBudget,
            onDismiss = { showEditLuxuryDialog = false },
            onSave = { newAmount ->
                onUpdateLuxuryBudget(newAmount)
                showEditLuxuryDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HeaderBar(
                currentDate = uiState.currentDateFormatted,
                monthlyBudgetRemaining = uiState.monthlyBudgetRemaining,
                daysRemainingInMonth = uiState.daysRemainingInMonth,
                luxuryBudget = uiState.luxuryBudget,
                onEditMonthlyBudget = { showEditMonthlyDialog = true },
                onEditLuxuryBudget = { showEditLuxuryDialog = true }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = { showEndDayDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "END DAY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Circular Budget Gauge
            BudgetCircleGauge(
                remainingToday = uiState.todayRemaining,
                dailyBudget = uiState.dailyBudget,
                isDeficit = uiState.isDeficit
            )

            // Transaction Input Card
            TransactionInputCard(
                onAddTransaction = onAddTransaction
            )

            // Scrollable List of Today's Transactions
            TransactionList(
                transactions = uiState.todayTransactions,
                onDeleteTransaction = onDeleteTransaction,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    MaterialTheme {
        BudgetScreen(
            uiState = BudgetUiState(
                isInitialized = true,
                monthlyBudgetRemaining = 2450.0,
                daysRemainingInMonth = 29,
                dailyBudget = 84.48,
                todaySpent = 15.0,
                todayRemaining = 69.48,
                luxuryBudget = 500.0,
                isDeficit = false,
                currentDateFormatted = "Monday, Nov 2",
                todayTransactions = listOf(
                    TransactionEntity(
                        id = 1,
                        amount = 15.0,
                        description = "Lunch",
                        timestamp = System.currentTimeMillis(),
                        type = TransactionType.DAILY_EXPENSE,
                        date = "2026-11-02"
                    )
                )
            ),
            onInitializeBudget = {},
            onAddTransaction = { _, _, _ -> },
            onDeleteTransaction = {},
            onEndDay = {},
            onUpdateMonthlyBudget = {},
            onUpdateLuxuryBudget = {}
        )
    }
}
