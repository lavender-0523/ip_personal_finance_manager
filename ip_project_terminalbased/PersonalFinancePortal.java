import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ========================================================
// 1. BASE CLASS: Demonstrates OOP Abstraction & Encapsulation
// ========================================================
abstract class FinanceRecord {
    // Encapsulation: Private variables protected from direct outside modification
    private String date;
    private String category;
    private double amount;
    private String description;

    // Parameterized constructor
    public FinanceRecord(String date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    // Public Getters (Encapsulation)
    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    // Abstract method: forces child classes to specify type
    public abstract String getType();
}

// ========================================================
// 2. DERIVED CLASS: Demonstrates OOP Inheritance & Polymorphism
// ========================================================
class Transaction extends FinanceRecord {
    private int id;
    private String type; // "Income" or "Expense"

    public Transaction(String type, String date, String category, double amount, String description) {
        super(date, category, amount, description);
        this.type = type;
        this.id = 0;
    }

    public Transaction(int id, String type, String date, String category, double amount, String description) {
        super(date, category, amount, description);
        this.type = type;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Implementing the abstract method
    @Override
    public String getType() {
        return this.type;
    }
}

// ========================================================
// 3. MAIN APPLICATION CLASS: Menu-driven Console with JDBC & Auto-Fallback
// ========================================================
public class PersonalFinancePortal {

    // Database connection credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // Change if your MySQL has a password

    private static Scanner sc = new Scanner(System.in);

    // Method to establish database connection
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            System.out.println("[Notice] MySQL JDBC Driver not found on classpath.");
            return null;
        } catch (SQLException e) {
            System.out.println("[Notice] MySQL server not running on localhost:3306 (" + e.getMessage() + ")");
            return null;
        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();

        // If MySQL is not detected, gracefully switch to built-in Offline Mode
        if (conn == null) {
            System.out.println("==================================================================================");
            System.out.println("[INFO] Activating Built-in Offline Mode so you can run and test all features live!");
            System.out.println("==================================================================================");
            runOfflineMode();
            return;
        }

        // MySQL Mode
        int choice = 0;
        while (choice != 6) {
            printDivider();
            System.out.println("      PERSONAL FINANCE MANAGEMENT PORTAL (MySQL Connected)");
            printDivider();
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View All Transactions (SELECT)");
            System.out.println("4. View Balance Summary (SUM/Aggregation)");
            System.out.println("5. Delete a Transaction (DELETE)");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            if (!sc.hasNextInt()) {
                System.out.println("\n[Error] Invalid input! Please enter a number between 1 and 6.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine(); // clear newline

            switch (choice) {
                case 1:
                    insertRecord(conn, "Income");
                    break;
                case 2:
                    insertRecord(conn, "Expense");
                    break;
                case 3:
                    viewRecords(conn);
                    break;
                case 4:
                    viewSummary(conn);
                    break;
                case 5:
                    deleteRecord(conn);
                    break;
                case 6:
                    System.out.println("\nClosing connection. Thank you!");
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                default:
                    System.out.println("\n[Error] Invalid choice! Enter 1 to 6.");
            }
        }
    }

    // =========================================================================
    // MYSQL OPERATIONS (JDBC)
    // =========================================================================

    private static void insertRecord(Connection conn, String type) {
        printDivider();
        System.out.println(">>> ADD NEW " + type.toUpperCase());
        printDivider();

        System.out.print("Enter Date (DD-MM-YYYY): ");
        String date = sc.nextLine().trim();

        System.out.print("Enter Category (e.g. Salary, Food, Travel, Rent): ");
        String category = sc.nextLine().trim();

        double amount = readValidAmount();

        System.out.print("Enter Description: ");
        String desc = sc.nextLine().trim();

        Transaction txn = new Transaction(type, date, category, amount, desc);

        String sql = "INSERT INTO transactions (type, entry_date, category, amount, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txn.getType());
            pstmt.setString(2, txn.getDate());
            pstmt.setString(3, txn.getCategory());
            pstmt.setDouble(4, txn.getAmount());
            pstmt.setString(5, txn.getDescription());

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("\n[Success] Record saved to MySQL database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("\n[Error] Database Insert failed: " + e.getMessage());
        }
    }

    private static void viewRecords(Connection conn) {
        printDivider();
        System.out.println("                  ALL TRANSACTIONS (FROM MySQL)");
        printDivider();

        String sql = "SELECT * FROM transactions";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-5s | %-8s | %-12s | %-14s | %-10s | %-20s\n", 
                              "ID", "TYPE", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
            System.out.println("----------------------------------------------------------------------------------");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String date = rs.getString("entry_date");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String desc = rs.getString("description");

                System.out.printf("%-5d | %-8s | %-12s | %-14s | Rs.%-7.2f | %-20s\n",
                                  id, type, date, category, amount, desc);
            }

            if (!hasData) {
                System.out.println("No records found in database.");
            }
            System.out.println("----------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[Error] Retrieval failed: " + e.getMessage());
        }
    }

    private static void viewSummary(Connection conn) {
        String sqlIncome = "SELECT SUM(amount) AS total FROM transactions WHERE type = 'Income'";
        String sqlExpense = "SELECT SUM(amount) AS total FROM transactions WHERE type = 'Expense'";

        try (Statement stmt = conn.createStatement()) {
            double totalIncome = 0;
            double totalExpense = 0;

            ResultSet rsIncome = stmt.executeQuery(sqlIncome);
            if (rsIncome.next()) totalIncome = rsIncome.getDouble("total");

            ResultSet rsExpense = stmt.executeQuery(sqlExpense);
            if (rsExpense.next()) totalExpense = rsExpense.getDouble("total");

            double balance = totalIncome - totalExpense;

            printDivider();
            System.out.println("                 ACCOUNT BALANCE SUMMARY (MySQL)");
            printDivider();
            System.out.printf("Total Income   : Rs. %.2f\n", totalIncome);
            System.out.printf("Total Expenses : Rs. %.2f\n", totalExpense);
            System.out.println("------------------------------------------");
            System.out.printf("Net Balance    : Rs. %.2f\n", balance);
            printDivider();
        } catch (SQLException e) {
            System.out.println("[Error] Summary failed: " + e.getMessage());
        }
    }

    private static void deleteRecord(Connection conn) {
        System.out.print("\nEnter the Transaction ID to delete: ");
        if (!sc.hasNextInt()) {
            System.out.println("[Error] Invalid ID!");
            sc.next();
            return;
        }

        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("[Success] Transaction ID " + id + " deleted successfully!");
            } else {
                System.out.println("[Notice] No transaction found with ID " + id);
            }
        } catch (SQLException e) {
            System.out.println("[Error] Delete failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // BUILT-IN OFFLINE MODE (RUNS ANYWHERE WITHOUT MYSQL SERVICE)
    // =========================================================================

    private static void runOfflineMode() {
        List<Transaction> list = new ArrayList<Transaction>();
        list.add(new Transaction(1, "Income", "01-10-2026", "Salary", 75000.0, "Monthly Internship Stipend"));
        list.add(new Transaction(2, "Expense", "03-10-2026", "Rent", 18000.0, "Hostel / Apartment Rent"));
        list.add(new Transaction(3, "Expense", "05-10-2026", "Food", 4500.0, "College Mess & Groceries"));
        list.add(new Transaction(4, "Income", "10-10-2026", "Freelance", 12000.0, "Freelance Web Project"));
        list.add(new Transaction(5, "Expense", "12-10-2026", "Travel", 2500.0, "Monthly Metro Pass"));

        int choice = 0;
        while (choice != 6) {
            printDivider();
            System.out.println("      PERSONAL FINANCE MANAGEMENT PORTAL (Offline Demo Mode)");
            printDivider();
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View All Transactions (SELECT)");
            System.out.println("4. View Balance Summary (SUM/Aggregation)");
            System.out.println("5. Delete a Transaction (DELETE)");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            if (!sc.hasNextInt()) {
                System.out.println("\n[Error] Invalid input! Please enter a number between 1 and 6.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                case 2:
                    String type = (choice == 1) ? "Income" : "Expense";
                    printDivider();
                    System.out.println(">>> ADD NEW " + type.toUpperCase());
                    printDivider();

                    System.out.print("Enter Date (DD-MM-YYYY): ");
                    String date = sc.nextLine().trim();

                    System.out.print("Enter Category (e.g. Salary, Food, Travel, Rent): ");
                    String category = sc.nextLine().trim();

                    double amount = readValidAmount();

                    System.out.print("Enter Description: ");
                    String desc = sc.nextLine().trim();

                    int nextId = 1;
                    for (Transaction t : list) {
                        if (t.getId() >= nextId) nextId = t.getId() + 1;
                    }

                    Transaction newTxn = new Transaction(nextId, type, date, category, amount, desc);
                    list.add(newTxn);
                    System.out.println("\n[Success] Record saved successfully (ID #" + nextId + ")!");
                    break;

                case 3:
                    printDivider();
                    System.out.println("                  ALL TRANSACTIONS (CURRENT RECORDS)");
                    printDivider();
                    System.out.printf("%-5s | %-8s | %-12s | %-14s | %-10s | %-20s\n", 
                                      "ID", "TYPE", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
                    System.out.println("----------------------------------------------------------------------------------");
                    for (Transaction t : list) {
                        System.out.printf("%-5d | %-8s | %-12s | %-14s | Rs.%-7.2f | %-20s\n",
                                          t.getId(), t.getType(), t.getDate(), t.getCategory(), t.getAmount(), t.getDescription());
                    }
                    System.out.println("----------------------------------------------------------------------------------");
                    break;

                case 4:
                    double inc = 0, exp = 0;
                    for (Transaction t : list) {
                        if ("Income".equalsIgnoreCase(t.getType())) inc += t.getAmount();
                        else exp += t.getAmount();
                    }
                    printDivider();
                    System.out.println("                 ACCOUNT BALANCE SUMMARY");
                    printDivider();
                    System.out.printf("Total Income   : Rs. %.2f\n", inc);
                    System.out.printf("Total Expenses : Rs. %.2f\n", exp);
                    System.out.println("------------------------------------------");
                    System.out.printf("Net Balance    : Rs. %.2f\n", (inc - exp));
                    printDivider();
                    break;

                case 5:
                    System.out.print("\nEnter the Transaction ID to delete: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("[Error] Invalid ID!");
                        sc.next();
                        break;
                    }
                    int delId = sc.nextInt();
                    sc.nextLine();

                    boolean found = false;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getId() == delId) {
                            list.remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        System.out.println("[Success] Transaction ID " + delId + " deleted successfully!");
                    } else {
                        System.out.println("[Notice] No transaction found with ID " + delId);
                    }
                    break;

                case 6:
                    System.out.println("\nThank you for using Personal Finance Management Portal!");
                    break;

                default:
                    System.out.println("\n[Error] Invalid choice! Enter 1 to 6.");
            }
        }
    }

    // Helper for Amount Validation
    private static double readValidAmount() {
        double amount = 0;
        while (true) {
            System.out.print("Enter Amount (Rs): ");
            try {
                amount = Double.parseDouble(sc.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("[Warning] Amount must be greater than zero! Try again.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid input! Please enter numeric values only.");
            }
        }
    }

    private static void printDivider() {
        System.out.println("==================================================================================");
    }
}
