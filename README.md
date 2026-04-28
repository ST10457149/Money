# MoneyGoat – Personal Budgeting App
## Technical Report & Project Documentation

---

## 1. Executive Summary
**MoneyGoat** is a robust Android application designed to empower users with comprehensive personal finance management tools. By integrating structured expense tracking, categorical analysis, and goal-oriented budgeting, the application provides a holistic view of a user's financial health. The primary objective of MoneyGoat is to transform passive expense recording into active financial engagement.

---

## 2. Core Feature Set
The application offers a specialized suite of features tailored for modern financial tracking:
*   **Secure Authentication**: Personal user accounts with persistent session management.
*   **Dynamic Category Management**: User-defined spending categories for personalized organization.
*   **Granular Expense Tracking**: Detailed transaction logging including timestamps, descriptions, and amounts.
*   **Digital Receipt Archiving**: Integrated camera support for attaching photo evidence to transactions.
*   **Spending Analytics**: Categorical data aggregation to identify spending patterns.
*   **Goal-Based Budgeting**: Monthly threshold setting (Minimum/Maximum) to regulate cash flow.
*   **Temporal Filtering**: Advanced history search functionality based on custom date ranges.

---

## 3. Functional Architecture

### 3.1 Authentication & Security
The entry point of the application utilizes a login/registration system. User credentials are encrypted and managed via a local **Room Database**, while session persistence is handled through **SharedPreferences**, ensuring a seamless user experience upon app relaunch.

### 3.2 Categorization Logic
The "Categories" module allows users to define the "labels" of their financial life (e.g., Groceries, Utilities, Leisure). This forms the foundation for all subsequent analytics, ensuring that data is organized according to the user's specific needs.

### 3.3 Expense Entry & Multimedia Support
The "Add Expense" interface utilizes a multi-modal input strategy:
*   **Date/Time Pickers**: Standardized temporal data entry.
*   **Camera API Integration**: Captures receipt images, storing them in the app's private external storage directory and linking the URI to the database record.
*   **Validation Logic**: Ensures data integrity by verifying amounts and required fields before persistence.

### 3.4 Data Visualization & Analytics
The "Analytics" engine performs real-time SQL aggregations to calculate totals per category. This provides immediate visual feedback on where capital is being allocated, highlighting potential areas for cost-saving.

### 3.5 Budgetary Goals
Users can establish monthly financial boundaries. By setting a minimum "savings" goal and a maximum "spending" ceiling, the app provides a framework for disciplined financial behavior.

---

## 4. Technical Specifications
*   **Language**: Kotlin (JVM 17)
*   **Minimum SDK**: API 26 (Android 8.0)
*   **Target SDK**: API 35 (Android 15)
*   **Architecture**: MVVM (Model-View-ViewModel) for separation of concerns and testability.
*   **Database**: Room Persistence Library (SQLite abstraction).
*   **UI Framework**: Material Design components, RecyclerView for optimized list rendering, and View Binding.
*   **Concurrency**: Kotlin Coroutines for non-blocking database and IO operations.
*   **Logging**: Comprehensive Logcat instrumentation across UI and Data layers for auditability.

---

## 5. Conclusion
MoneyGoat serves as a comprehensive solution for users seeking to improve their financial literacy. By combining ease of use with detailed data entry and analytical insights, the application bridges the gap between simple ledger keeping and proactive wealth management.
