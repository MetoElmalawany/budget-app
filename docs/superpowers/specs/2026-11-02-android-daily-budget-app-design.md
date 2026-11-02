# Design Specification: Android Daily Budgeting App

## 1. Overview & Objective
A personal, offline-first Android application designed to provide disciplined daily budgeting. The user sets a monthly budget which is dynamically partitioned across the remaining days of the calendar month. Day-to-day spending impacts the daily budget immediately. At the conclusion of each day, pressing "End Day" reconciles any surplus or deficit back into the monthly pool and recalculates daily budgets for the remaining days.

All data is stored locally on the device (no cloud backend or deployment required). An automated CI/CD pipeline compiles the application into an installable `.apk` on GitHub.

## 2. Core Business Logic & Calculation Engine

### 2.1 State Variables
- `monthlyBudgetRemaining` (Double): Available funds for the rest of the current calendar month.
- `daysRemainingInMonth` (Int): Total days remaining in the active calendar month, including the current day.
- `dailyBudget` (Double): Base daily allocation, computed as:
  $$\text{dailyBudget} = \frac{\text{monthlyBudgetRemaining}}{\text{daysRemainingInMonth}}$$
- `todaySpent` (Double): Sum of all expense transactions logged during the active day.
- `todayRemaining` (Double): Immediate remaining daily spending allowance:
  $$\text{todayRemaining} = \text{dailyBudget} - \text{todaySpent}$$

### 2.2 Transaction Types
1. **Daily Expense (`DAILY_EXPENSE`):**
   - Deducted from `todayRemaining`.
   - Increases `todaySpent`.
   - If `todaySpent > dailyBudget`, `todayRemaining` turns negative (deficit state).
2. **Monthly Addition (`MONTHLY_ADDITION`):**
   - Added directly to `monthlyBudgetRemaining`.
   - Immediately recalculates `dailyBudget` for the remaining days of the month.

### 2.3 End Day Settlement Routine
When the user taps the **End Day** button:
1. Today's actual spending is subtracted from the monthly budget pool:
   $$\text{monthlyBudgetRemaining}_{\text{new}} = \text{monthlyBudgetRemaining}_{\text{old}} - \text{todaySpent}$$
2. The remaining day count decreases:
   $$\text{daysRemainingInMonth}_{\text{new}} = \text{daysRemainingInMonth}_{\text{old}} - 1$$
3. If days remain in the cycle ($\text{daysRemainingInMonth}_{\text{new}} > 0$):
   $$\text{dailyBudget}_{\text{new}} = \frac{\text{monthlyBudgetRemaining}_{\text{new}}}{\text{daysRemainingInMonth}_{\text{new}}}$$
   - Any surplus (spent < daily target) naturally increases future daily budgets.
   - Any deficit (spent > daily target) naturally decreases future daily budgets.
4. Active daily transactions are marked as archived/settled.
5. `todaySpent` resets to `0.0`.
6. Day transition is strictly manual (advances only upon tapping "End Day", keeping full user control).

### 2.4 Currency
Fixed currency formatting: **EUR (€)** (e.g. `€100.00`).

## 3. UI / UX Design (Jetpack Compose)

### 3.1 Layout Structure (Single Screen)
1. **Top Header Bar:**
   - Left: Current Date (e.g. `Nov 2, 2026`).
   - Right: Remaining monthly budget pill (e.g. `Monthly: €2,450.00` with remaining days badge).
2. **Center Dynamic Circular Gauge:**
   - Prominent central circle showing `todayRemaining` in large typography (e.g. `€75.50`).
   - Subtitle: `Target: €100.00`.
   - Color coding: Primary green/teal when positive; coral/red with warning sign when in deficit.
3. **Transaction Input Card:**
   - Input field: `Amount` (numeric decimal keypad).
   - Input field: `Description` (e.g., "Lunch", "Metro ticket").
   - Dropdown menu:
     - "Daily Expense" (Default)
     - "Add to Monthly Budget"
   - Button: `Add (+)`
4. **Today's Activity Feed:**
   - Scrollable list of transactions recorded today with timestamp, amount, and description.
   - Delete button `[X]` per row to revert accidental entries or fix typos. Reverting immediately restores the daily budget.
5. **Bottom Action:**
   - Large prominent red button: **`END DAY`**.
   - Confirmation dialog on click ("Close today and roll surplus/deficit to tomorrow? Confirm / Cancel") to prevent accidental triggers.

### 3.2 First-Launch Dialog
When the app is opened for the first time without configured state:
- A modal dialog appears: "Set your starting monthly budget for this month (€)".
- Computes initial remaining days from the current calendar date to the end of the month.
- Initializes database and populates the dashboard.

## 4. Technical Architecture & Tech Stack

### 4.1 Technologies
- **Language:** Kotlin (JVM 17/21 compatible).
- **UI Framework:** Jetpack Compose (Material3).
- **Local Persistence:** Room SQLite Database (`BudgetDatabase`) + Android DataStore / SharedPrefs for simple settings.
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with unidirectional data flow (StateFlow).
- **Build System:** Gradle (Kotlin DSL).

### 4.2 Data Models (Room)
- **`BudgetStateEntity`**:
  - `id`: Int (single row, primary key = 1)
  - `monthlyBudgetRemaining`: Double
  - `daysRemainingInMonth`: Int
  - `currentDayIndex`: Int
  - `activeMonthYear`: String (e.g., "2026-11")
  - `lastClosedDate`: String
- **`TransactionEntity`**:
  - `id`: Long (autoGenerate = true)
  - `amount`: Double
  - `description`: String
  - `timestamp`: Long
  - `type`: String (`DAILY_EXPENSE` | `MONTHLY_ADDITION`)
  - `dayId`: String (e.g. "2026-11-02")
  - `isSettled`: Boolean (false while active today, true after End Day)

## 5. GitHub & Multi-Agent PR Workflow

### 5.1 Repository & Branching Strategy
- Remote GitHub repo: `MetoElmalawany/budget-app` (linked via GitHub CLI).
- Default branch: `main`.
- Feature branches: `feature/<issue-name>`.

### 5.2 Roles & Lifecycle
1. **Tech Lead (Parent Agent):**
   - Creates GitHub issues for modular components.
   - Dispatches subagents to implement tasks.
   - Conducts formal code reviews on Pull Requests.
   - Merges approved PRs into `main` (or requests revisions).
2. **Coworkers (Subagents):**
   - Each subagent takes an assigned issue, writes implementation code and unit tests.
   - Opens a GitHub Pull Request with a clear summary of changes.
3. **CI/CD APK Generation:**
   - GitHub Actions workflow (`.github/workflows/build-apk.yml`) triggers on every push and merge to `main`.
   - Executes `./gradlew assembleDebug`.
   - Uploads `budget-app-debug.apk` as a downloadable GitHub Release / build artifact.

## 6. Verification & Quality Plan
- Unit tests for budget math calculations (`BudgetCalculatorTest`):
  - Normal daily spend.
  - Deficit spend.
  - End day surplus rollover.
  - End day deficit reduction.
  - Monthly budget additions.
- UI validation: Jetpack Compose previews and input sanitization tests.
- Build verification: Clean Gradle assemble and APK generation.
