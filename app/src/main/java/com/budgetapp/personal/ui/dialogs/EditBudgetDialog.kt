package com.budgetapp.personal.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.util.Locale

@Composable
fun EditBudgetDialog(
    title: String,
    initialAmount: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialText = remember(initialAmount) {
        if (initialAmount > 0.0) {
            String.format(Locale.US, "%.2f", initialAmount)
        } else {
            ""
        }
    }
    var amountText by remember(initialAmount) { mutableStateOf(initialText) }
    val parsedAmount = amountText.toDoubleOrNull()
    val isValid = parsedAmount != null && parsedAmount >= 0.0

    val submit = {
        if (isValid && parsedAmount != null) {
            onSave(parsedAmount)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter new budget total in euros (€):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*(\.\d{0,2})?$"""))) {
                            amountText = input
                        }
                    },
                    label = { Text("Budget (€)") },
                    placeholder = { Text("0.00") },
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
                Text(text = "Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun EditBudgetDialogPreview() {
    MaterialTheme {
        EditBudgetDialog(
            title = "Edit Monthly Budget",
            initialAmount = 2450.0,
            onDismiss = {},
            onSave = {}
        )
    }
}
