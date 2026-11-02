package com.budgetapp.personal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.budgetapp.personal.model.TransactionType

@Composable
fun TransactionInputCard(
    onAddTransaction: (amount: Double, description: String, type: TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DAILY_EXPENSE) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val parsedAmount = amountText.toDoubleOrNull()
    val isAmountValid = parsedAmount != null && parsedAmount > 0.0

    val submitTransaction = {
        if (isAmountValid && parsedAmount != null) {
            val description = descriptionText.trim().ifEmpty {
                if (selectedType == TransactionType.DAILY_EXPENSE) "Expense" else "Monthly Addition"
            }
            onAddTransaction(parsedAmount, description, selectedType)
            amountText = ""
            descriptionText = ""
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Quick Transaction",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 1: Amount & Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Allow digits and at most one decimal point with up to 2 decimals
                        if (input.isEmpty() || input.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount (€)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1.1f)
                )

                // Dropdown for TransactionType
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { dropdownExpanded = true },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedType) {
                                    TransactionType.DAILY_EXPENSE -> "Daily Expense"
                                    TransactionType.MONTHLY_ADDITION -> "Add to Monthly"
                                    else -> selectedType.name
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select type"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Daily Expense") },
                            onClick = {
                                selectedType = TransactionType.DAILY_EXPENSE
                                dropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Monthly Budget") },
                            onClick = {
                                selectedType = TransactionType.MONTHLY_ADDITION
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Row 2: Description & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Lunch, Groceries") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitTransaction() }
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { submitTransaction() },
                    enabled = isAmountValid,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionInputCardPreview() {
    MaterialTheme {
        TransactionInputCard(
            onAddTransaction = { _, _, _ -> }
        )
    }
}
