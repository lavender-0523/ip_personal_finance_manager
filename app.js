// Personal Finance Management Portal - Client logic and UI handling

// Initial pre-loaded sample data
const DEFAULT_TRANSACTIONS = [
  { id: 1, type: "Income", date: "2026-10-01", category: "Salary", amount: 75000.0, description: "Monthly Internship Stipend" },
  { id: 2, type: "Expense", date: "2026-10-03", category: "Rent & Housing", amount: 18000.0, description: "Hostel / Apartment Rent" },
  { id: 3, type: "Expense", date: "2026-10-05", category: "Food & Dining", amount: 4500.0, description: "College Mess & Groceries" },
  { id: 4, type: "Income", date: "2026-10-10", category: "Freelance", amount: 12000.0, description: "Freelance Web Project" },
  { id: 5, type: "Expense", date: "2026-10-12", category: "Travel & Transit", amount: 2500.0, description: "Monthly Metro Pass" }
];

const CATEGORIES = {
  Expense: [
    "Food & Dining",
    "Travel & Transit",
    "Rent & Housing",
    "Utilities",
    "Healthcare",
    "Entertainment",
    "Education",
    "Miscellaneous"
  ],
  Income: [
    "Salary",
    "Allowance",
    "Freelance",
    "Investments",
    "Business",
    "Bonus",
    "Other Inflow"
  ]
};

const STORAGE_KEY = "finance_portal_records";

// State
let transactions = [];
let currentFilter = "ALL";
let searchQuery = "";

// Currency Formatter Helper
const formatCurrency = (num) => {
  return "Rs. " + Number(num).toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};

// Initialize Application
document.addEventListener("DOMContentLoaded", () => {
  loadData();
  setupEventListeners();
  setDefaultDate();
  updateCategoryDropdown("Expense");
  renderUI();
});

// Load records from LocalStorage or initialize with sample records
function loadData() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    try {
      transactions = JSON.parse(saved);
    } catch (e) {
      transactions = [...DEFAULT_TRANSACTIONS];
    }
  } else {
    transactions = [...DEFAULT_TRANSACTIONS];
    saveData();
  }
}

// Persist records to LocalStorage
function saveData() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(transactions));
}

// Set default date to today YYYY-MM-DD
function setDefaultDate() {
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("txDate").value = today;
}

// Event Listeners Setup
function setupEventListeners() {
  // Segmented Type Selector buttons
  const segExpense = document.getElementById("segExpense");
  const segIncome = document.getElementById("segIncome");
  const hiddenType = document.getElementById("txType");

  segExpense.addEventListener("click", () => {
    segExpense.classList.add("active");
    segIncome.classList.remove("active");
    hiddenType.value = "Expense";
    updateCategoryDropdown("Expense");
  });

  segIncome.addEventListener("click", () => {
    segIncome.classList.add("active");
    segExpense.classList.remove("active");
    hiddenType.value = "Income";
    updateCategoryDropdown("Income");
  });

  // Form submission
  document.getElementById("transactionForm").addEventListener("submit", handleFormSubmit);

  // Clear Form
  document.getElementById("btnClearForm").addEventListener("click", resetForm);

  // Filter Buttons
  document.querySelectorAll(".pill-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      document.querySelectorAll(".pill-btn").forEach((b) => b.classList.remove("active"));
      e.target.classList.add("active");
      currentFilter = e.target.dataset.filter;
      renderTable();
    });
  });

  // Search Input
  document.getElementById("searchInput").addEventListener("input", (e) => {
    searchQuery = e.target.value.toLowerCase().trim();
    renderTable();
  });

  // Export CSV
  document.getElementById("btnExportCsv").addEventListener("click", exportToCsv);

  // Reset Demo Data
  document.getElementById("btnResetData").addEventListener("click", resetDemoData);
}

// Update Category dropdown dynamically based on Type selection
function updateCategoryDropdown(type) {
  const select = document.getElementById("txCategory");
  select.innerHTML = "";
  const list = CATEGORIES[type] || CATEGORIES.Expense;
  list.forEach((cat) => {
    const opt = document.createElement("option");
    opt.value = cat;
    opt.textContent = cat;
    select.appendChild(opt);
  });
}

// Handle Form Submit
function handleFormSubmit(e) {
  e.preventDefault();

  const type = document.getElementById("txType").value;
  const category = document.getElementById("txCategory").value;
  const amountInput = document.getElementById("txAmount");
  const dateInput = document.getElementById("txDate");
  const descInput = document.getElementById("txDescription");

  const amountVal = parseFloat(amountInput.value);

  // Input Validations
  if (isNaN(amountVal) || amountVal <= 0) {
    showToast("Please enter a valid amount greater than zero.");
    amountInput.focus();
    return;
  }

  if (!dateInput.value) {
    showToast("Please select a transaction date.");
    dateInput.focus();
    return;
  }

  // Generate unique auto-incrementing ID
  const nextId = transactions.length > 0 
    ? Math.max(...transactions.map(t => t.id || 0)) + 1 
    : 1;

  const newTransaction = {
    id: nextId,
    type: type,
    date: dateInput.value,
    category: category,
    amount: amountVal,
    description: descInput.value.trim() || "-"
  };

  // Prepend to top of list
  transactions.unshift(newTransaction);
  saveData();

  renderUI();
  resetForm();
  showToast(`Transaction #${nextId} recorded successfully.`);
}

// Delete Record
function deleteTransaction(id) {
  if (!confirm(`Are you sure you want to delete transaction #${id}?`)) {
    return;
  }

  transactions = transactions.filter(t => t.id !== id);
  saveData();
  renderUI();
  showToast(`Transaction #${id} deleted.`);
}

// Reset Form fields
function resetForm() {
  document.getElementById("txAmount").value = "";
  document.getElementById("txDescription").value = "";
  setDefaultDate();

  const segExpense = document.getElementById("segExpense");
  const segIncome = document.getElementById("segIncome");
  const hiddenType = document.getElementById("txType");

  segExpense.classList.add("active");
  segIncome.classList.remove("active");
  hiddenType.value = "Expense";
  updateCategoryDropdown("Expense");

  document.getElementById("txAmount").focus();
}

// Restore default demo transactions
function resetDemoData() {
  if (confirm("Restore original demo transactions? Current entries will be replaced.")) {
    transactions = [...DEFAULT_TRANSACTIONS];
    saveData();
    renderUI();
    showToast("Sample transactions restored.");
  }
}

// Export Transactions to CSV file
function exportToCsv() {
  if (transactions.length === 0) {
    showToast("No records available to export.");
    return;
  }

  const headers = ["ID", "Type", "Date", "Category", "Amount", "Description"];
  const rows = transactions.map(t => [
    t.id,
    t.type,
    t.date,
    `"${t.category}"`,
    t.amount.toFixed(2),
    `"${(t.description || "").replace(/"/g, '""')}"`
  ]);

  const csvContent = "data:text/csv;charset=utf-8," 
    + [headers.join(","), ...rows.map(e => e.join(","))].join("\n");

  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `finance_records_${new Date().toISOString().split("T")[0]}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  showToast("CSV file exported successfully.");
}

// Render Complete UI (Metrics & Table)
function renderUI() {
  renderMetrics();
  renderTable();
}

// Calculate and render Top Metric Cards
function renderMetrics() {
  let totalIncome = 0;
  let totalExpense = 0;
  let countIncome = 0;
  let countExpense = 0;

  transactions.forEach(t => {
    if (t.type.toUpperCase() === "INCOME") {
      totalIncome += t.amount;
      countIncome++;
    } else {
      totalExpense += t.amount;
      countExpense++;
    }
  });

  const netBalance = totalIncome - totalExpense;

  document.getElementById("valTotalIncome").textContent = formatCurrency(totalIncome);
  document.getElementById("valTotalExpense").textContent = formatCurrency(totalExpense);
  document.getElementById("valNetBalance").textContent = formatCurrency(netBalance);

  document.getElementById("countIncome").textContent = `${countIncome} incoming transactions`;
  document.getElementById("countExpense").textContent = `${countExpense} outgoing transactions`;

  const cardBalance = document.getElementById("cardNetBalance");
  const headingBalance = document.getElementById("labelBalanceHeading");
  const badgeStatus = document.getElementById("badgeBalanceStatus");

  if (netBalance < 0) {
    cardBalance.classList.add("is-deficit");
    headingBalance.textContent = "Net Deficit";
    badgeStatus.textContent = "Deficit";
    badgeStatus.classList.add("deficit");
  } else {
    cardBalance.classList.remove("is-deficit");
    headingBalance.textContent = "Net Balance";
    badgeStatus.textContent = "Surplus";
    badgeStatus.classList.remove("deficit");
  }
}

// Render Transactions Table with filtering & search
function renderTable() {
  const tbody = document.getElementById("tableBody");
  const emptyState = document.getElementById("emptyState");
  const countBadge = document.getElementById("txCountBadge");

  tbody.innerHTML = "";

  // Apply Filter & Search
  const filtered = transactions.filter(t => {
    const matchesFilter = currentFilter === "ALL" || t.type.toUpperCase() === currentFilter;
    const matchesSearch = searchQuery === "" 
      || t.category.toLowerCase().includes(searchQuery)
      || (t.description && t.description.toLowerCase().includes(searchQuery))
      || t.date.includes(searchQuery);

    return matchesFilter && matchesSearch;
  });

  countBadge.textContent = `${filtered.length} of ${transactions.length} records`;

  if (filtered.length === 0) {
    emptyState.style.display = "block";
    return;
  }

  emptyState.style.display = "none";

  filtered.forEach(t => {
    const tr = document.createElement("tr");

    const isIncome = t.type.toUpperCase() === "INCOME";
    const tagClass = isIncome ? "type-income" : "type-expense";

    tr.innerHTML = `
      <td class="col-center"><span class="id-badge">#${t.id}</span></td>
      <td class="col-center">
        <span class="type-tag ${tagClass}">${t.type}</span>
      </td>
      <td class="col-center" style="font-family: var(--font-mono); font-size: 12px; color: var(--text-secondary);">${t.date}</td>
      <td><span class="category-name">${escapeHtml(t.category)}</span></td>
      <td class="col-right">
        <span class="amount-text">${formatCurrency(t.amount)}</span>
      </td>
      <td><span class="memo-text">${escapeHtml(t.description || "-")}</span></td>
      <td class="col-center">
        <button class="btn-delete-row" title="Delete record #${t.id}" onclick="deleteTransaction(${t.id})">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </td>
    `;

    tbody.appendChild(tr);
  });
}

// Utility: Escape HTML strings to prevent XSS
function escapeHtml(str) {
  if (!str) return "";
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// Toast Notification
let toastTimer;
function showToast(message) {
  const toast = document.getElementById("toastNotification");
  const msgEl = document.getElementById("toastMessage");

  msgEl.textContent = message;
  toast.style.display = "block";

  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.style.display = "none";
  }, 2800);
}
