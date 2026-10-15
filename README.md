# Personal Finance Management Portal

A lightweight, modern cashflow and expense management application with both an attractive **Minimalist Monochrome Web Interface** and a **Core Java Terminal Application**.

---

## Project Structure

```text
├── index.html                 # Modern Monochrome Web Portal UI
├── style.css                  # Clean vanilla CSS styling
├── app.js                     # Client-side state & localStorage persistence
├── WebServer.java             # Zero-dependency embedded local HTTP server
├── PersonalFinancePortal.java # Core Java menu-driven accounting utility
├── schema.sql                 # MySQL schema setup script
├── ASSESSMENT_DOCUMENTATION.md# Complete technical documentation & test cases
└── ip_project_terminalbased/  # Untouched backup of the terminal-based suite
```

---

## 1. Web Application (Minimalist Monochrome)

### Features
- **Summary Cards:** Real-time computation of Total Income, Total Expenses, and Net Balance with deficit alerts.
- **Transaction Entry:** Expense / Income segmented toggle with dynamic category switching.
- **Interactive Ledger:** Search filtering, type filtering (All, Income, Expense), and row deletion.
- **Export to CSV:** Direct browser download of all recorded transactions.
- **Data Persistence:** Automatically synced to browser `localStorage`.

### How to Run
#### Option A: Direct Browser Launch
Simply open `index.html` in any modern web browser.

#### Option B: Via Local Server
```bash
javac WebServer.java
java WebServer
```
Then visit: `http://localhost:8080`

---

## 2. Terminal-Based Application (`PersonalFinancePortal.java`)

### Features
- Menu-driven console interface with 6 standard accounting operations.
- Demonstrates Core Java Object-Oriented Programming (Abstraction, Encapsulation, Inheritance, Polymorphism).
- **Dual Persistence Mode:**
  - Connects to MySQL using parameterized `PreparedStatement` queries.
  - Automatically activates **Built-in Offline Demo Mode** if MySQL is not detected on `localhost:3306`.

### How to Run
```bash
javac PersonalFinancePortal.java
java PersonalFinancePortal
```
