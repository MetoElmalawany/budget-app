package com.budgetapp.personal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetapp.personal.ui.util.FormatUtils

@Composable
fun BudgetCircleGauge(
    remainingToday: Double,
    dailyBudget: Double,
    isDeficit: Boolean,
    modifier: Modifier = Modifier
) {
    val ringColor = if (isDeficit) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val containerColor = if (isDeficit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val textColor = if (isDeficit) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    val subtextColor = if (isDeficit) Color(0xFFC62828) else Color(0xFF2E7D32)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circular gauge
        Surface(
            modifier = Modifier
                .size(210.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .border(BorderStroke(6.dp, ringColor), CircleShape),
            shape = CircleShape,
            color = containerColor
        ) {
            Column(
                modifier = Modifier
                    .size(210.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top label
                Text(
                    text = "REMAINING TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = subtextColor.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Big amount text
                Text(
                    text = FormatUtils.formatCurrency(remainingToday),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Target subtitle
                Text(
                    text = "Target: ${FormatUtils.formatCurrency(dailyBudget)} / day",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = subtextColor.copy(alpha = 0.9f)
                )

                // Warning indicator if deficit
                if (isDeficit) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Deficit warning",
                            tint = textColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Deficit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetCircleGaugeSurplusPreview() {
    MaterialTheme {
        BudgetCircleGauge(
            remainingToday = 75.50,
            dailyBudget = 100.0,
            isDeficit = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetCircleGaugeDeficitPreview() {
    MaterialTheme {
        BudgetCircleGauge(
            remainingToday = -15.00,
            dailyBudget = 100.0,
            isDeficit = true
        )
    }
}
