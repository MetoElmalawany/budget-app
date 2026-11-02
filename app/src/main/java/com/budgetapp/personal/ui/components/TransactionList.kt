package com.budgetapp.personal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetapp.personal.model.TransactionEntity
import com.budgetapp.personal.model.TransactionType
import com.budgetapp.personal.ui.util.FormatUtils

@Composable
fun TransactionList(
    transactions: List<TransactionEntity>,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (transactions.isNotEmpty()) {
                Text(
                    text = "${transactions.size} transaction${if (transactions.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transactions list or empty state
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No expenses recorded today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onDelete = { onDeleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCategoryBadge(
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (type) {
        TransactionType.DAILY_EXPENSE -> Triple(
            "Daily",
            Color(0xFFE8F5E9), // Emerald/Green tint
            Color(0xFF1B5E20)
        )
        TransactionType.MONTHLY_EXPENSE -> Triple(
            "Monthly",
            Color(0xFFE3F2FD), // Blue tint
            Color(0xFF0D47A1)
        )
        TransactionType.LUXURY_EXPENSE -> Triple(
            "Luxury",
            Color(0xFFEDE7F6), // Purple/Indigo tint
            Color(0xFF4A148C)
        )
        TransactionType.MONTHLY_ADDITION -> Triple(
            "+ Monthly",
            Color(0xFFE0F2F1), // Teal tint
            Color(0xFF004D40)
        )
        TransactionType.LUXURY_ADDITION -> Triple(
            "+ Luxury",
            Color(0xFFFFF8E1), // Amber/Gold tint
            Color(0xFFE65100)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = when (transaction.type) {
        TransactionType.DAILY_EXPENSE,
        TransactionType.MONTHLY_EXPENSE,
        TransactionType.LUXURY_EXPENSE -> true
        TransactionType.MONTHLY_ADDITION,
        TransactionType.LUXURY_ADDITION -> false
    }

    val amountColor = if (isExpense) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color(0xFF2E7D32)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Description, Category Badge & Timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TransactionCategoryBadge(type = transaction.type)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = FormatUtils.formatTime(transaction.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Formatted amount & Delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FormatUtils.formatTransactionAmount(transaction.amount, isExpense),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListPreview() {
    MaterialTheme {
        TransactionList(
            transactions = listOf(
                TransactionEntity(
                    id = 1,
                    amount = 15.0,
                    description = "Lunch",
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.DAILY_EXPENSE,
                    date = "2026-11-02"
                ),
                TransactionEntity(
                    id = 2,
                    amount = 120.0,
                    description = "New Coat",
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.LUXURY_EXPENSE,
                    date = "2026-11-02"
                ),
                TransactionEntity(
                    id = 3,
                    amount = 50.0,
                    description = "Bonus",
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.MONTHLY_ADDITION,
                    date = "2026-11-02"
                )
            ),
            onDeleteTransaction = {}
        )
    }
}
