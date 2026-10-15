import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Base class for financial records
abstract class FinanceRecord {
    private String date;
    private String category;
    private double amount;
    private String description;

    // constructor
    public FinanceRecord(String date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    // getters
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

    // abstract method to get record type (Income/Expense)
    public abstract String getType();
}

// Child class extending FinanceRecord
class Transaction extends FinanceRecord {
    private int id;
    private String type; // Income or Expense

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

    @Override
    public String getType() {
        return this.type;
    }
}

// Main application class
public class PersonalFinancePortal {

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/finance_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; // update if mysql has a password

    private static Scanner sc = new Scanner(System.in);

    // Establishes connection to MySQL
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            System.out.println("Notice: MySQL JDBC Driver not found on classpath.");
            return null;
        } catch (SQLException e) {
            System.out.println("Notice: MySQL server not detected on localhost:3306.");
            return null;
        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();

        // If MySQL is offline, run demo in offline mode
        if (conn == null) {
            System.out.println("------------------------------------------------------------");
            System.out.println("Starting in Offline Mode (in-memory demo)...");
            System.out.println("------------------------------------------------------------");
            runOfflineMode();
            return;
        }

        // Main menu loop
        int choice = 0;
        while (choice != 6) {
            printDivider();
            System.out.println("          PERSONAL FINANCE MANAGEMENT PORTAL");
            System.out.println("                  (Database Connected)");
            printDivider();
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View All Transactions");
            System.out.println("4. View Balance Summary");
            System.out.println("5. Delete a Transaction");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            if (!sc.hasNextInt()) {
                System.out.println("\nInvalid input! Please enter a number between 1 and 6.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine(); // consume newline

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
                    System.out.println("\nInvalid choice! Enter 1 to 6.");
            }
        }
    }

    // Database Operations (JDBC)

    // Inserts an income or expense record into MySQL
    private static void insertRecord(Connection conn, String type) {
        printDivider();
        System.out.println("--- ADD NEW " + type.toUpperCase() + " ---");
        printDivider();

        System.out.print("Enter Date (DD-MM-YYYY): ");
        String date = sc.nextLine().trim();

        System.out.print("Enter Category (e.g., Salary, Food, Travel, Rent): ");
        String category = sc.nextLine().trim();

        double amount = readValidAmount();

        System.out.print("Enter Description: ");
        String desc = sc.nextLine().trim();

        Transaction txn = new Transaction(type, date, category, amount, desc);

        // insert query using prepared statement
        String sql = "INSERT INTO transactions (type, entry_date, category, amount, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, txn.getType());
            pstmt.setString(2, txn.getDate());
            pstmt.setString(3, txn.getCategory());
            pstmt.setDouble(4, txn.getAmount());
            pstmt.setString(5, txn.getDescription());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("\nRecord saved successfully!");
            }
        } catch (SQLException e) {
            System.out.println("\nDatabase insert error: " + e.getMessage());
        }
    }

    // Displays all transactions stored in the database
    private static void viewRecords(Connection conn) {
        printDivider();
        System.out.println("                       ALL TRANSACTIONS");
        printDivider();

        String sql = "SELECT * FROM transactions";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-5s | %-8s | %-12s | %-14s | %-10s | %-20s\n", 
                              "ID", "TYPE", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
            System.out.println("----------------------------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                int id = rs.getInt("id");
                String type = rs.getString("type");
                String date = rs.getString("entry_date");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String desc = rs.getString("description");

                System.out.printf("%-5d | %-8s | %-12s | %-14s | Rs.%-7.2f | %-20s\n",
                                  id, type, date, category, amount, desc);
            }

            if (!hasRows) {
                System.out.println("No records found.");
            }
            System.out.println("----------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }
    }

    // Calculates total income, expenses, and net balance using SQL SUM()
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
            System.out.println("                 ACCOUNT BALANCE SUMMARY");
            printDivider();
            System.out.printf("Total Income   : Rs. %.2f\n", totalIncome);
            System.out.printf("Total Expenses : Rs. %.2f\n", totalExpense);
            System.out.println("------------------------------------------");
            System.out.printf("Net Balance    : Rs. %.2f\n", balance);
            printDivider();
        } catch (SQLException e) {
            System.out.println("Error calculating summary: " + e.getMessage());
        }
    }

    // Deletes a transaction from MySQL by ID
    private static void deleteRecord(Connection conn) {
        System.out.print("\nEnter Transaction ID to delete: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid ID! Please enter a number.");
            sc.next();
            return;
        }

        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("Transaction ID " + id + " deleted successfully.");
            } else {
                System.out.println("No transaction found with ID " + id);
            }
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }

    // In-memory offline mode (used when MySQL server is not running)
    private static void runOfflineMode() {
        // sample starter records
        List<Transaction> list = new ArrayList<Transaction>();
        list.add(new Transaction(1, "Income", "01-10-2026", "Salary", 75000.0, "Monthly Internship Stipend"));
        list.add(new Transaction(2, "Expense", "03-10-2026", "Rent", 18000.0, "Hostel / Apartment Rent"));
        list.add(new Transaction(3, "Expense", "05-10-2026", "Food", 4500.0, "College Mess & Groceries"));
        list.add(new Transaction(4, "Income", "10-10-2026", "Freelance", 12000.0, "Freelance Web Project"));
        list.add(new Transaction(5, "Expense", "12-10-2026", "Travel", 2500.0, "Monthly Metro Pass"));

        int choice = 0;
        while (choice != 6) {
            printDivider();
            System.out.println("          PERSONAL FINANCE MANAGEMENT PORTAL");
            System.out.println("                    (Offline Mode)");
            printDivider();
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View All Transactions");
            System.out.println("4. View Balance Summary");
            System.out.println("5. Delete a Transaction");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");

            if (!sc.hasNextInt()) {
                System.out.println("\nInvalid input! Please enter a number between 1 and 6.");
                sc.next();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                case 2:
                    String type = (choice == 1) ? "Income" : "Expense";
                    printDivider();
                    System.out.println("--- ADD NEW " + type.toUpperCase() + " ---");
                    printDivider();

                    System.out.print("Enter Date (DD-MM-YYYY): ");
                    String date = sc.nextLine().trim();

                    System.out.print("Enter Category (e.g., Salary, Food, Travel, Rent): ");
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
                    System.out.println("\nRecord saved successfully (ID #" + nextId + ")!");
                    break;

                case 3:
                    printDivider();
                    System.out.println("                       ALL TRANSACTIONS");
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
                    System.out.print("\nEnter Transaction ID to delete: ");
                    if (!sc.hasNextInt()) {
                        System.out.println("Invalid ID! Please enter a number.");
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
                        System.out.println("Transaction ID " + delId + " deleted successfully.");
                    } else {
                        System.out.println("No transaction found with ID " + delId);
                    }
                    break;

                case 6:
                    System.out.println("\nExiting. Thank you!");
                    break;

                default:
                    System.out.println("\nInvalid choice! Please enter 1 to 6.");
            }
        }
    }

    // Validates that user enters a positive numeric amount
    private static double readValidAmount() {
        double amount = 0;
        while (true) {
            System.out.print("Enter Amount (Rs): ");
            try {
                amount = Double.parseDouble(sc.nextLine().trim());
                if (amount <= 0) {
                    System.out.println("Amount must be greater than zero. Try again.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please enter numbers only.");
            }
        }
    }

    private static void printDivider() {
        System.out.println("------------------------------------------------------------");
    }
}
