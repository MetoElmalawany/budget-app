# Design Specification: Luxury Budget & Direct Budget Modification

## 1. Overview & Objective
Enhance the Android Daily Budget application with two core features:
1. **Direct Budget Modification via Header Badges**: Tap either the Monthly Budget badge or the Luxury Budget badge to directly update its total balance.
2. **Dedicated Luxury Budget**: An independent, optional side budget for clothes, expensive items, and non-daily discretionary purchases. Unlike the monthly budget, the luxury budget is never subdivided into daily allowances and is unaffected by the "End Day" rollover.

## 2. Business Logic & Calculations

### 2.1 State Variables
- `monthlyBudgetRemaining` (Double): Funds remaining for the calendar month, divided across remaining days.
- `daysRemainingInMonth` (Int): Active days left in the calendar month.
- `dailyBudget` (Double): Computed as $\frac{\text{monthlyBudgetRemaining}}{\text{daysRemainingInMonth}}$.
- `todaySpent` (Double): Sum of `DAILY_EXPENSE` transactions recorded today.
- `todayRemaining` (Double): $\text{dailyBudget} - \text{todaySpent}$.
- `luxuryBudget` (Double): Current balance in the dedicated luxury pool (default `0.0`, optional).

### 2.2 Transaction Types & Impact
1. **`DAILY_EXPENSE`**:
   - Deducted from `todayRemaining`.
   - Reconciled against `monthlyBudgetRemaining` only when "End Day" is pressed.
2. **`MONTHLY_EXPENSE`**:
   - Deducted directly from `monthlyBudgetRemaining`.
   - Immediately recalculates `dailyBudget` for the remaining days.
   - Does NOT affect `todaySpent` or today's center circle gauge.
3. **`LUXURY_EXPENSE`**:
   - Deducted directly from `luxuryBudget`.
   - Has zero effect on `dailyBudget`, `todayRemaining`, or "End Day" settlement.
4. **`MONTHLY_ADDITION`**:
   - Added directly to `monthlyBudgetRemaining`.
   - Immediately recalculates `dailyBudget`.
5. **`LUXURY_ADDITION`**:
   - Added directly to `luxuryBudget`.

### 2.3 Rollback on Delete
- Deleting a `DAILY_EXPENSE`: restores `todayRemaining`.
- Deleting a `MONTHLY_EXPENSE`: refunds amount into `monthlyBudgetRemaining` and recomputes `dailyBudget`.
- Deleting a `LUXURY_EXPENSE`: refunds amount into `luxuryBudget`.
- Deleting a `MONTHLY_ADDITION`: deducts amount from `monthlyBudgetRemaining` and recomputes `dailyBudget`.
- Deleting a `LUXURY_ADDITION`: deducts amount from `luxuryBudget`.

## 3. UI / UX Design (Jetpack Compose)

### 3.1 Top Header Bar
- **Left**: Current date display (e.g. `Nov 2, 2026`).
- **Right**: Two interactive badges:
  - **Monthly Badge**: `Monthly: €2,450.00 (29d) ✎`. Tapping opens `EditBudgetDialog` for monthly funds.
  - **Luxury Badge**: `Luxury: €500.00 ✎` (or `Luxury: €0.00 ✎` if not yet configured). Tapping opens `EditBudgetDialog` for luxury funds.

### 3.2 Edit Budget Dialog (`EditBudgetDialog.kt`)
- Title: "Edit Monthly Budget" or "Edit Luxury Budget".
- Pre-filled with current balance.
- Decimal numeric keyboard.
- Confirm button: "Save" (disabled if negative or invalid format).
- Dismiss button: "Cancel".

### 3.3 Transaction Input Row
- `Amount` text field (decimal number).
- `Description` text field.
- Dropdown selector with 5 options:
  1. `Daily Expense` (Default)
  2. `Monthly Expense`
  3. `Luxury Expense`
  4. `Add to Monthly Budget`
  5. `Add to Luxury Budget`
- `Add (+)` action button.

### 3.4 Transaction List
- Each transaction displays a badge tag:
  - `Daily` (green tint)
  - `Monthly` (blue tint)
  - `Luxury` (purple/gold tint)
- Delete `[X]` button per item.

## 4. Room Database Schema Updates
- `BudgetStateEntity`: Add `val luxuryBudget: Double = 0.0`.
- `TransactionType`: Add `MONTHLY_EXPENSE` and `LUXURY_ADDITION`.
- Database version incremented from 1 to 2 with `fallbackToDestructiveMigration()`.

## 5. Verification Plan
- Unit tests:
  - `BudgetEngineTest`: verify luxury balance adjustments and direct monthly expense calculations.
  - `BudgetViewModelTest`: verify editing monthly budget, editing luxury budget, logging luxury expenses, logging monthly expenses, and transaction rollback behavior.
- CI/CD build: Automated GitHub Actions run compiling the debug APK.
