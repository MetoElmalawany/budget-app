# Android Daily Budgeting App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a fully functional, offline-first Android budgeting application in Kotlin and Jetpack Compose that dynamically calculates daily budgets from a monthly pool, reconciles surplus/deficit on "End Day", and compiles into an installable APK via GitHub Actions.

**Architecture:** Clean MVVM architecture (Model-View-ViewModel) with unidirectional data flow. Pure Kotlin calculation engine for deterministic math; Room SQLite database for persistent offline storage; Jetpack Compose Material 3 for the single-screen UI.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, AndroidX Room, StateFlow/Coroutines, Gradle Kotlin DSL, GitHub Actions.

## Global Constraints
- Currency display: EUR (`€`) with 2 decimal places (e.g. `€100.00`).
- Strictly manual day transitions triggered by the "End Day" button.
- 100% offline; no network permissions or external backend required.
- Single-screen interface with prominent center circle, top-right monthly badge, quick entry row, today's transactions list, and red End Day button.
- Git repository managed via GitHub with automated CI/CD compiling the debug APK.

---

### Task 1: Project Scaffolding, Gradle Setup & GitHub Actions CI/CD

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `.github/workflows/build-apk.yml`
- Create: `.gitignore`

**Interfaces:**
- Produces: Gradle build system capable of compiling Android application and running tests via `./gradlew test` and `./gradlew assembleDebug`.
- Produces: GitHub Actions workflow generating `budget-app-debug.apk` as a build artifact on every push/merge to `main`.

- [ ] **Step 1: Create `.gitignore` for Android and Gradle**
Include `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.apk`.

- [ ] **Step 2: Configure root Gradle files (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`)**
Configure Android Gradle Plugin (8.5.2 or compatible), Kotlin (1.9.24 or 2.0.0), Maven repositories (Google, MavenCentral).

- [ ] **Step 3: Configure `app/build.gradle.kts`**
Configure Android SDK 34 (compileSdk 34, minSdk 26, targetSdk 34), Compose enabled (Compose Compiler), Room dependencies (`androidx.room:room-runtime:2.6.1`, `androidx.room:room-ktx:2.6.1`, `androidx.room:room-compiler:2.6.1` via KSP or annotationProcessor), JUnit 4 test dependencies.

- [ ] **Step 4: Create `AndroidManifest.xml` and base resources**
Define `BudgetApp` application and `MainActivity` as Launcher.

- [ ] **Step 5: Create `.github/workflows/build-apk.yml`**
GitHub Actions workflow on `push` and `pull_request` to `main`:
1. Check out repository.
2. Set up JDK 17.
3. Grant execute permission for gradlew.
4. Run `./gradlew testDebugUnitTest`.
5. Run `./gradlew assembleDebug`.
6. Upload APK artifact `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 6: Commit and push base scaffolding to GitHub**
Initialize remote repo via `gh repo create` if not already linked, and push initial commit.

---

### Task 2: Core Domain Calculation Engine & Unit Tests

**Files:**
- Create: `app/src/main/java/com/budgetapp/personal/domain/BudgetEngine.kt`
- Test: `app/src/test/java/com/budgetapp/personal/domain/BudgetEngineTest.kt`

**Interfaces:**
- Produces: `BudgetEngine` class with methods:
  - `calculateDailyBudget(monthlyRemaining: Double, daysRemaining: Int): Double`
  - `calculateTodayRemaining(dailyBudget: Double, todaySpent: Double): Double`
  - `endDaySettlement(monthlyRemaining: Double, todaySpent: Double, daysRemaining: Int): EndDayResult`
  - `addMonthlyIncome(monthlyRemaining: Double, additionAmount: Double, daysRemaining: Int): Double`
- Produces: `EndDayResult(newMonthlyRemaining: Double, newDaysRemaining: Int, newDailyBudget: Double, surplusOrDeficit: Double)`

- [ ] **Step 1: Write failing unit tests in `BudgetEngineTest.kt`**
Cover:
1. Normal daily split: €3,000 / 30 days = €100.
2. Daily remaining when spending €35.50 = €64.50.
3. Daily remaining in deficit when spending €120.00 = -€20.00.
4. End Day surplus: €100 budget, €60 spent -> surplus €40 remains in monthly (€2,940 / 29 days = €101.38).
5. End Day deficit: €100 budget, €150 spent -> deficit €50 deducted from monthly (€2,850 / 29 days = €98.28).
6. Monthly addition: adding €300 mid-month recalculates daily target.

- [ ] **Step 2: Implement `BudgetEngine.kt`**
Implement pure functions with round-to-2-decimal-places precision.

- [ ] **Step 3: Run unit tests and verify they pass**
Verify all test cases pass.

- [ ] **Step 4: Commit**
`git add app/src/... && git commit -m "feat(domain): implement BudgetEngine with comprehensive unit tests"`

---

### Task 3: Local Storage Layer (Room Database & Repository)

**Files:**
- Create: `app/src/main/java/com/budgetapp/personal/model/TransactionType.kt`
- Create: `app/src/main/java/com/budgetapp/personal/model/BudgetStateEntity.kt`
- Create: `app/src/main/java/com/budgetapp/personal/model/TransactionEntity.kt`
- Create: `app/src/main/java/com/budgetapp/personal/data/BudgetDao.kt`
- Create: `app/src/main/java/com/budgetapp/personal/data/BudgetDatabase.kt`
- Create: `app/src/main/java/com/budgetapp/personal/data/BudgetRepository.kt`

**Interfaces:**
- Consumes: `TransactionType` (`DAILY_EXPENSE`, `MONTHLY_ADDITION`).
- Produces: `BudgetRepository` providing:
  - `getBudgetState(): Flow<BudgetStateEntity?>`
  - `saveBudgetState(state: BudgetStateEntity)`
  - `getTodayTransactions(date: String): Flow<List<TransactionEntity>>`
  - `addTransaction(transaction: TransactionEntity)`
  - `deleteTransaction(id: Long)`
  - `settleActiveTransactions(date: String)`

- [ ] **Step 1: Define Entities & Enums**
`TransactionType` enum (`DAILY_EXPENSE`, `MONTHLY_ADDITION`).
`BudgetStateEntity` (@Entity with id=1, monthlyBudgetRemaining, daysRemainingInMonth, activeMonthYear, lastClosedDate).
`TransactionEntity` (@Entity with id, amount, description, timestamp, type, date, isSettled).

- [ ] **Step 2: Implement `BudgetDao`**
Room DAO with reactive Flow queries for state and active today's transactions, insert/delete operations.

- [ ] **Step 3: Implement `BudgetDatabase` & `BudgetRepository`**
RoomDatabase builder (fallbackToDestructiveMigration for simplicity) and Repository class encapsulating DAO operations.

- [ ] **Step 4: Commit**
`git add app/src/... && git commit -m "feat(data): implement Room database, entities, and repository"`

---

### Task 4: UI State Management (ViewModel)

**Files:**
- Create: `app/src/main/java/com/budgetapp/personal/ui/BudgetUiState.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/BudgetViewModel.kt`
- Test: `app/src/test/java/com/budgetapp/personal/ui/BudgetViewModelTest.kt`

**Interfaces:**
- Consumes: `BudgetEngine`, `BudgetRepository`.
- Produces: `BudgetViewModel` exposing `uiState: StateFlow<BudgetUiState>` and event handlers:
  - `onInitializeBudget(startingMonthlyBudget: Double)`
  - `onAddTransaction(amount: Double, description: String, type: TransactionType)`
  - `onDeleteTransaction(transactionId: Long)`
  - `onEndDay()`

- [ ] **Step 1: Define `BudgetUiState` data class**
Fields: `isInitialized: Boolean`, `monthlyBudgetRemaining: Double`, `daysRemainingInMonth: Int`, `dailyBudget: Double`, `todaySpent: Double`, `todayRemaining: Double`, `isDeficit: Boolean`, `todayTransactions: List<TransactionEntity>`, `currentDateFormatted: String`.

- [ ] **Step 2: Implement `BudgetViewModel`**
Combine flows from repository and compute UI state via `BudgetEngine`. Implement action methods:
- `onInitializeBudget`: computes days left in month and saves initial state.
- `onAddTransaction`: validates positive amount, inserts `TransactionEntity`, updates state if `MONTHLY_ADDITION`.
- `onDeleteTransaction`: deletes by ID.
- `onEndDay`: executes `BudgetEngine.endDaySettlement`, archives today's transactions, updates repository state.

- [ ] **Step 3: Write ViewModel unit tests**
Test state transitions, expense logging, deletion, and end-day rollover.

- [ ] **Step 4: Commit**
`git add app/src/... && git commit -m "feat(ui): implement BudgetViewModel and UI state management"`

---

### Task 5: Jetpack Compose UI Components & Single-Screen Interface

**Files:**
- Create: `app/src/main/java/com/budgetapp/personal/ui/components/HeaderBar.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/components/BudgetCircleGauge.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/components/TransactionInputCard.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/components/TransactionList.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/dialogs/InitialBudgetDialog.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/dialogs/EndDayConfirmDialog.kt`
- Create: `app/src/main/java/com/budgetapp/personal/ui/BudgetScreen.kt`
- Create: `app/src/main/java/com/budgetapp/personal/MainActivity.kt`

**Interfaces:**
- Consumes: `BudgetViewModel`, `BudgetUiState`.
- Produces: Complete Jetpack Compose UI matching wireframe and UX specifications.

- [ ] **Step 1: Implement `HeaderBar.kt`**
Renders formatted date on left and monthly budget badge (`Monthly: €X,XXX.XX` • `X days left`) on top right.

- [ ] **Step 2: Implement `BudgetCircleGauge.kt`**
Draws centered circle (Canvas or Box with border/surface) showing `todayRemaining` in bold EUR format, target label beneath, and shifts from emerald green to alert red/coral when `isDeficit` is true.

- [ ] **Step 3: Implement `TransactionInputCard.kt`**
Amount text field (decimal number keypad), description text field, dropdown menu to choose between "Daily Expense" and "Add to Monthly Budget", and "Add (+)" button.

- [ ] **Step 4: Implement `TransactionList.kt`**
LazyColumn displaying today's transactions with timestamp, description, formatted amount, and an icon button to delete.

- [ ] **Step 5: Implement `InitialBudgetDialog.kt` & `EndDayConfirmDialog.kt`**
Dialogs for first-time onboarding setup and End Day confirmation safety prompt.

- [ ] **Step 6: Implement `BudgetScreen.kt` & `MainActivity.kt`**
Assembles all components with scaffold, bottom prominent red "End Day" button, and wires to `BudgetViewModel`.

- [ ] **Step 7: Commit**
`git add app/src/... && git commit -m "feat(ui): implement Jetpack Compose UI components and screen"`

---

### Task 6: GitHub Workflow Verification, Build & APK Release

**Files:**
- Verify: `.github/workflows/build-apk.yml`
- Test: Build verification and test runs

**Interfaces:**
- Produces: Validated, installable `app-debug.apk` ready for download from the GitHub repository.

- [ ] **Step 1: Run local test suite**
Execute unit tests to confirm all domain and UI logic pass.

- [ ] **Step 2: Push all commits to GitHub repository**
Push to remote `main` branch.

- [ ] **Step 3: Monitor GitHub Actions build run**
Use `gh run watch` / `gh run list` to verify the Android build finishes successfully.

- [ ] **Step 4: Download and verify generated APK**
Download `app-debug.apk` artifact and provide direct install link/instructions to the Product Owner.
