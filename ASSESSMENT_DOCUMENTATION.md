# FORMATIVE ASSESSMENT DOCUMENTATION (FA-1 & FA-2)
## Subject: Industrial Practice / Core Java Lab Evaluation
## Project: Personal Finance Management Portal (Java + MySQL JDBC)

---

# PART 1: FA-1 DOCUMENTATION

### 1.1 Problem Statement
Many students and young professionals struggle with monitoring their daily cash flow, leading to unplanned expenditures and financial deficits. Existing commercial budgeting apps are often bloated, ad-supported, or require cloud subscriptions. There is a need for a lightweight, secure, and reliable desktop accounting utility to record incomes and expenses, persist records in a relational database, compute real-time balances, and prevent accidental data loss.

### 1.2 Objectives
- Develop an offline-capable, menu-driven Core Java console application.
- Apply foundational Object-Oriented Programming (OOP) concepts: Abstraction, Encapsulation, Inheritance, and Polymorphism.
- Establish a database interface with MySQL using JDBC (`java.sql.*`).
- Implement parameterized `PreparedStatement` queries to prevent SQL Injection attacks.
- Perform robust input validation for financial figures and menu navigation.

### 1.3 Scope of the System
- **In Scope:**
  - Recording Income and Expense transactions with Date, Category, Amount, and Description.
  - Viewing tabular lists of all past records directly from the MySQL database.
  - Computing aggregate financial statistics: Total Income, Total Expenses, and Net Balance.
  - Deleting records by unique Transaction ID.
- **Out of Scope (Future Enhancements):**
  - Multi-user authentication, cloud synchronization, and automated monthly PDF statement exports.

---

### 1.4 Mapping of Core Java OOP Principles

| OOP Principle | Where It Is Used in Code | Technical Explanation |
|---|---|---|
| **Encapsulation** | `FinanceRecord` base class | Fields `date`, `category`, `amount`, and `description` are marked `private` to protect them from external manipulation. Access is strictly controlled through public getters (`getDate()`, `getAmount()`, etc.). |
| **Abstraction** | `abstract class FinanceRecord` | Declares the abstract method `public abstract String getType();`. It defines the template of what every financial record must have without exposing concrete type logic. |
| **Inheritance** | `class Transaction extends FinanceRecord` | The derived class `Transaction` inherits common attributes and behavior from `FinanceRecord` via `super(date, category, amount, description)`, maximizing code reuse. |
| **Polymorphism** | Overriding `getType()` in `Transaction` | `Transaction` provides concrete runtime method implementation for `getType()`, fulfilling the abstract contract declared by the base class. |

---

### 1.5 Text-Based UML Diagrams

#### A. UML Class Diagram
```text
+-----------------------------------------------------------+
|               <<abstract>> FinanceRecord                 |
+-----------------------------------------------------------+
| - date: String                                            |
| - category: String                                        |
| - amount: double                                          |
| - description: String                                     |
+-----------------------------------------------------------+
| + FinanceRecord(date: String, category: String,           |
|                 amount: double, description: String)      |
| + getDate(): String                                       |
| + getCategory(): String                                   |
| + getAmount(): double                                     |
| + getDescription(): String                                |
| + getType(): String <<abstract>>                          |
+-----------------------------------------------------------+
                            ^
                            | (Inherits / Extends)
+-----------------------------------------------------------+
|                       Transaction                         |
+-----------------------------------------------------------+
| - type: String                                            |
+-----------------------------------------------------------+
| + Transaction(type: String, date: String,                 |
|               category: String, amount: double,           |
|               description: String)                        |
| + getType(): String                                       |
+-----------------------------------------------------------+
                            ^
                            | (Instantiates & Uses)
+-----------------------------------------------------------+
|                  PersonalFinancePortal                    |
+-----------------------------------------------------------+
| - DB_URL: String                                          |
| - DB_USER: String                                         |
| - DB_PASS: String                                         |
| - sc: Scanner                                             |
+-----------------------------------------------------------+
| + getConnection(): Connection                             |
| + main(args: String[]): void                              |
| - insertRecord(conn: Connection, type: String): void      |
| - viewRecords(conn: Connection): void                     |
| - viewSummary(conn: Connection): void                     |
| - deleteRecord(conn: Connection): void                    |
| - printDivider(): void                                    |
+-----------------------------------------------------------+
```

#### B. UML Use Case Diagram
```text
                       +---------------------------------------+
                       | Personal Finance Management Portal    |
                       +---------------------------------------+
                       |                                       |
                       |   (1) Add Income Transaction          |
                       |            ^                          |
                       |            |                          |
    +--------+         |   (2) Add Expense Transaction         |
    |  User  | --------+            |                          |
    | (Actor)|         |   (3) View All Records (SELECT)       |
    +--------+         |            |                          |
                       |   (4) View Balance Summary (SUM)      |
                       |            |                          |
                       |   (5) Delete Record by ID             |
                       |                                       |
                       +---------------------------------------+
                                    |
                                    v (JDBC Connection)
                       +---------------------------------------+
                       |         MySQL Database                |
                       |         (finance_db)                  |
                       +---------------------------------------+
```

---

# PART 2: FA-2 DOCUMENTATION

### 2.1 Implementation Details & Exception Handling
- **Database Driver Management:** Dynamically loads MySQL driver via `Class.forName("com.mysql.cj.jdbc.Driver")` inside `getConnection()`. Catches `ClassNotFoundException` and provides actionable diagnostic messages.
- **Resource Management:** Employs standard `try-with-resources` (`PreparedStatement`, `Statement`, `ResultSet`) ensuring memory and cursor resources are closed automatically even when queries fail.
- **SQL Injection Prevention:** Uses parameterized SQL templates with placeholder `?` marks:
  ```java
  String sql = "INSERT INTO transactions (type, entry_date, category, amount, description) VALUES (?, ?, ?, ?, ?)";
  ```
- **Robust Scanner Handling:** Trailing newlines from `nextInt()` are cleared with `sc.nextLine()` to prevent string reading skips.
- **Numeric Validation Loop:** Uses `Double.parseDouble()` inside a `while(true)` try-catch block to reject alphanumeric strings and values `<= 0`.

---

### 2.2 Test Cases Table

| Test Case ID | Test Scenario | Input Data | Expected Output | Actual Output | Status |
|---|---|---|---|---|---|
| **TC-01** | Non-integer Main Menu selection | User enters string `"abc"` at main menu | System prints `"[Error] Invalid input! Please enter a number between 1 and 6."` and re-prompts menu | Error message displayed; re-prompted cleanly | **PASS** |
| **TC-02** | Non-numeric Amount entry | User enters `"five hundred"` for amount | System catches `NumberFormatException`, displays `"[Error] Invalid input! Please enter numeric values only."` | Rejects input; prompts again | **PASS** |
| **TC-03** | Zero or Negative Amount entry | User enters `-250` or `0` for amount | System rejects value with `"[Warning] Amount must be greater than zero! Try again."` | Warning displayed; loop continues | **PASS** |
| **TC-04** | Valid Transaction Addition | Type: `Income`, Date: `15-10-2026`, Category: `Salary`, Amount: `50000`, Desc: `Stipend` | SQL executes; displays `"[Success] Record saved to MySQL database successfully!"` | Record inserted in MySQL table | **PASS** |
| **TC-05** | Deleting Non-existent ID | User enters ID `9999` (not in database) | Query executes; returns `0` affected rows; displays `"[Notice] No transaction found with ID 9999"` | Handled cleanly; database unchanged | **PASS** |

---

### 2.3 Bug Log & Troubleshooting Table

| Bug ID | Problem Description | Root Cause | Technical Resolution |
|---|---|---|---|
| **BUG-01** | `InputMismatchException` crashes program when string is entered at menu | `sc.nextInt()` expects numeric tokens and throws runtime exception on characters | Guarded with `if (!sc.hasNextInt()) { sc.next(); continue; }` before consuming token. |
| **BUG-02** | Description prompt gets skipped after reading numeric Amount | `sc.nextInt()` / `sc.nextDouble()` leaves a dangling newline `\n` in the `Scanner` buffer | Added explicit `sc.nextLine()` immediately following numeric reads to purge buffer. |
| **BUG-03** | `CommunicationsException` (Connection Refused) on startup | MySQL server (XAMPP / MySQL service) is stopped, or port `3306` is unreachable | `getConnection()` returns `null` safely and displays an alert instead of throwing an unhandled `SQLException`. |

---

# PART 3: VIVA DEFENSE CHEAT SHEET

| # | Examiner Question | 2-Sentence Technical Answer to State in Viva |
|---|---|---|
| **1** | *"Why did you use `PreparedStatement` instead of `Statement` for inserts and deletes?"* | "We use `PreparedStatement` because it pre-compiles the SQL query template and binds input values as strict literals, which completely prevents SQL Injection attacks. Additionally, it improves execution performance through database query-plan caching." |
| **2** | *"Where did you use Abstraction and Inheritance in this program?"* | "`FinanceRecord` is an abstract base class containing common transaction attributes and an abstract method `getType()`. The concrete class `Transaction` extends `FinanceRecord` and polymorphically overrides `getType()` to return whether the record is Income or Expense." |
| **3** | *"How does your program compute the account balance summary?"* | "The program executes SQL aggregation queries `SELECT SUM(amount) AS total FROM transactions WHERE type = ?` for both Income and Expense categories. It then computes the net balance directly as `totalIncome - totalExpense` and displays it in standard currency format." |
| **4** | *"Why is `try-with-resources` used in database operations?"* | "`try-with-resources` automatically closes any object implementing `AutoCloseable`—such as `PreparedStatement`, `Statement`, and `ResultSet`—at the end of the block. This prevents connection leaks, cursor exhaustion, and database memory overhead." |
| **5** | *"What happens if the user enters letters for the transaction amount?"* | "The program captures the input as a string and wraps `Double.parseDouble()` inside a `try-catch` block handling `NumberFormatException`. If parsing fails or the amount is `<= 0`, it displays an informative warning and re-prompts the user without crashing." |
