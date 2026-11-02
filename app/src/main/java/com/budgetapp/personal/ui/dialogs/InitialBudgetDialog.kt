package com.budgetapp.personal.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InitialBudgetDialog(
    onSaveBudget: (startingMonthlyBudget: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetText by remember { mutableStateOf("") }
    val parsedBudget = budgetText.toDoubleOrNull()
    val isValid = parsedBudget != null && parsedBudget > 0.0

    val submit = {
        if (isValid && parsedBudget != null) {
            onSaveBudget(parsedBudget)
        }
    }

    AlertDialog(
        onDismissRequest = { /* Modal: cannot dismiss without setting budget */ },
        icon = {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Welcome to Daily Budget",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set your starting monthly budget. Your daily spending allowance will be calculated automatically based on the days left in the month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
                            budgetText = input
                        }
                    },
                    label = { Text("Starting Monthly Budget (€)") },
                    placeholder = { Text("e.g. 3000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = isValid
            ) {
                Text(text = "Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun InitialBudgetDialogPreview() {
    MaterialTheme {
        InitialBudgetDialog(onSaveBudget = {})
    }
}
