# Luxury Budget & Direct Budget Modification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a standalone Luxury Budget and direct budget modification capabilities via top header badges, with 5 transaction types and automatic budget restoration upon transaction deletion.

**Architecture:** MVVM with Room SQLite persistence. `BudgetEngine` manages pure calculation adjustments, `BudgetStateEntity` persists the luxury balance, `BudgetViewModel` exposes the extended state via StateFlow, and Jetpack Compose displays clickable header badges, an edit dialog, and a 5-option transaction entry dropdown.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, AndroidX Room, StateFlow, JUnit.

## Global Constraints
- Currency display: EUR (`€`) with 2 decimal places (e.g. `€500.00`).
- Tapping either the Monthly or Luxury badge opens `EditBudgetDialog` to modify that budget directly.
- The Luxury budget is completely independent: it is NEVER divided into daily allowances and is unaffected by the "End Day" button.
- 5 Dropdown options: `Daily Expense`, `Monthly Expense`, `Luxury Expense`, `Add to Monthly Budget`, `Add to Luxury Budget`.
- Deleting any transaction restores funds to its respective budget pool.
- 100% offline, local storage via Room SQLite.

---

### Task 1: Domain Logic & Transaction Types Extension

**Files:**
- Modify: `app/src/main/java/com/budgetapp/personal/model/TransactionType.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/domain/BudgetEngine.kt`
- Test: `app/src/test/java/com/budgetapp/personal/domain/BudgetEngineTest.kt`

**Interfaces:**
- Produces: `TransactionType` with 5 enum values: `DAILY_EXPENSE`, `MONTHLY_EXPENSE`, `LUXURY_EXPENSE`, `MONTHLY_ADDITION`, `LUXURY_ADDITION`.
- Produces: `BudgetEngine.subtractMonthlyExpense(monthlyRemaining: Double, amount: Double): Double`
- Produces: `BudgetEngine.adjustLuxuryBudget(currentLuxury: Double, delta: Double): Double`

- [ ] **Step 1: Write failing unit tests in `BudgetEngineTest.kt`**
Test `subtractMonthlyExpense` (€2,500 - €200 = €2,300) and `adjustLuxuryBudget` (€500 - €120 = €380, €500 + €100 = €600).

- [ ] **Step 2: Update `TransactionType.kt`**
Add `MONTHLY_EXPENSE` and `LUXURY_ADDITION`.

- [ ] **Step 3: Implement new methods in `BudgetEngine.kt`**
Add `subtractMonthlyExpense` and `adjustLuxuryBudget`.

- [ ] **Step 4: Run unit tests and verify pass**
Run `.\gradlew.bat testDebugUnitTest --tests com.budgetapp.personal.domain.BudgetEngineTest`.

- [ ] **Step 5: Commit**
`git add app/src/... && git commit -m "feat(domain): extend TransactionType and BudgetEngine for luxury and monthly expenses"`

---

### Task 2: Room Schema & Storage Layer Update

**Files:**
- Modify: `app/src/main/java/com/budgetapp/personal/model/BudgetStateEntity.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/data/BudgetDatabase.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/data/BudgetRepository.kt`
- Test: `app/src/test/java/com/budgetapp/personal/data/BudgetRepositoryTest.kt`

**Interfaces:**
- Produces: `BudgetStateEntity(..., luxuryBudget: Double = 0.0)`
- Produces: `BudgetDatabase` version 2 with destructive migration fallback.
- Produces: `BudgetRepository` methods to update luxury budget and monthly budget directly.

- [ ] **Step 1: Add `luxuryBudget` field to `BudgetStateEntity.kt`**
Default `0.0`.

- [ ] **Step 2: Increment `BudgetDatabase` version to 2**
Set `version = 2` in `@Database`.

- [ ] **Step 3: Add update methods in `BudgetRepository.kt`**
`suspend fun updateMonthlyBudget(newMonthlyBudget: Double)` and `suspend fun updateLuxuryBudget(newLuxuryBudget: Double)`.

- [ ] **Step 4: Run unit tests in `BudgetRepositoryTest.kt`**
Verify schema and repository operations pass.

- [ ] **Step 5: Commit**
`git add app/src/... && git commit -m "feat(data): add luxuryBudget to Room schema and repository"`

---

### Task 3: ViewModel State & Business Logic

**Files:**
- Modify: `app/src/main/java/com/budgetapp/personal/ui/BudgetUiState.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/ui/BudgetViewModel.kt`
- Test: `app/src/test/java/com/budgetapp/personal/ui/BudgetViewModelTest.kt`

**Interfaces:**
- Produces: `BudgetUiState(..., luxuryBudget: Double = 0.0)`
- Produces: `BudgetViewModel` methods:
  - `updateMonthlyBudget(newAmount: Double)`
  - `updateLuxuryBudget(newAmount: Double)`
  - Updated `addTransaction(...)` supporting all 5 transaction types.
  - Updated `deleteTransaction(...)` restoring respective budget pools.

- [ ] **Step 1: Write failing unit tests in `BudgetViewModelTest.kt`**
Cover:
1. `updateMonthlyBudget`: recalculates daily budget.
2. `updateLuxuryBudget`: sets luxury balance.
3. `addTransaction(MONTHLY_EXPENSE)`: subtracts from monthly and recomputes daily target; daily circle remains unspent.
4. `addTransaction(LUXURY_EXPENSE)`: subtracts from luxury; daily and monthly budgets unchanged.
5. `addTransaction(LUXURY_ADDITION)`: adds to luxury.
6. `deleteTransaction`: restores respective pools for monthly expense, luxury expense, and additions.

- [ ] **Step 2: Update `BudgetUiState.kt`**
Add `val luxuryBudget: Double = 0.0`.

- [ ] **Step 3: Update `BudgetViewModel.kt`**
Implement `updateMonthlyBudget`, `updateLuxuryBudget`, extend `addTransaction` and `deleteTransaction`.

- [ ] **Step 4: Run all unit tests**
Run `.\gradlew.bat testDebugUnitTest` and verify all tests pass.

- [ ] **Step 5: Commit**
`git add app/src/... && git commit -m "feat(ui): update BudgetViewModel with luxury tracking and budget editing"`

---

### Task 4: Jetpack Compose UI Components, Dialogs & Screen

**Files:**
- Create: `app/src/main/java/com/budgetapp/personal/ui/dialogs/EditBudgetDialog.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/ui/components/HeaderBar.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/ui/components/TransactionInputCard.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/ui/components/TransactionList.kt`
- Modify: `app/src/main/java/com/budgetapp/personal/ui/BudgetScreen.kt`

**Interfaces:**
- Produces: Clickable `Monthly` and `Luxury` header pills opening `EditBudgetDialog`.
- Produces: 5-option dropdown in `TransactionInputCard`.
- Produces: Tagged transaction items (`[Daily]`, `[Monthly]`, `[Luxury]`) in `TransactionList`.

- [ ] **Step 1: Implement `EditBudgetDialog.kt`**
Modal dialog with title, pre-filled amount text field, "Save", and "Cancel".

- [ ] **Step 2: Update `HeaderBar.kt`**
Display Monthly badge and Luxury badge with edit icon, clickable with callbacks `onEditMonthlyBudget` and `onEditLuxuryBudget`.

- [ ] **Step 3: Update `TransactionInputCard.kt`**
Update dropdown options to include all 5 types with clear labels:
1. "Daily Expense"
2. "Monthly Expense"
3. "Luxury Expense"
4. "Add to Monthly Budget"
5. "Add to Luxury Budget"

- [ ] **Step 4: Update `TransactionList.kt`**
Add badge chip on each transaction row indicating its type (`Daily`, `Monthly`, `Luxury`).

- [ ] **Step 5: Update `BudgetScreen.kt`**
Connect `EditBudgetDialog` state for both monthly and luxury budgets to `viewModel.updateMonthlyBudget` and `viewModel.updateLuxuryBudget`.

- [ ] **Step 6: Run build and tests**
Run `.\gradlew.bat testDebugUnitTest` and `.\gradlew.bat assembleDebug`.

- [ ] **Step 7: Commit**
`git add app/src/... && git commit -m "feat(ui): implement luxury badges, edit dialog, 5-option dropdown, and transaction tags"`

---

### Task 5: End-to-End Build, Verification & Release v1.1.0

**Files:**
- Release APK `budget-app.apk`
- GitHub Release `v1.1.0`

- [ ] **Step 1: Run full unit test suite**
Ensure all tests pass.

- [ ] **Step 2: Push changes to GitHub and monitor CI**
Verify GitHub Actions passes.

- [ ] **Step 3: Download generated APK and create GitHub Release `v1.1.0`**
Update `budget-app.apk` in workspace and create GitHub release.
