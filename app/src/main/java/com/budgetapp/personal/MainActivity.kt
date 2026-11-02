package com.budgetapp.personal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.budgetapp.personal.data.BudgetDatabase
import com.budgetapp.personal.data.BudgetRepository
import com.budgetapp.personal.ui.BudgetScreen
import com.budgetapp.personal.ui.BudgetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BudgetViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = BudgetDatabase.getDatabase(applicationContext)
                val repository = BudgetRepository(db.budgetDao())
                return BudgetViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                BudgetScreen(viewModel = viewModel)
            }
        }
    }
}
