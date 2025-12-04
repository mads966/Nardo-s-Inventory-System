package report;

import java.time.LocalDate;
import java.sql.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    private final Connection connection;

    public ReportDAO(Connection connection) {
        this.connection = connection;
    }

    // Save a report to database
    public boolean saveReport(Report report) throws SQLException {
        if (connection == null) {
            // Test mode: assign a mock id and pretend save succeeded
            report.setReportId((int) (Math.random() * 10000));
            System.out.println("[ReportDAO] (test) Saved report: " + report.getTitle());
            return true;
        }

        String sql = "INSERT INTO reports (report_type, generated_date, generated_by, " +
                "generated_by_name, data, title, summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, report.getReportType().name());
            pstmt.setTimestamp(2, Timestamp.valueOf(report.getGeneratedDate().atStartOfDay()));
            pstmt.setInt(3, report.getGeneratedById());
            pstmt.setString(4, report.getData());
            pstmt.setString(5, report.getTitle());
            pstmt.setString(6, report.getSummary());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        report.setReportId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public Report getReportFromQuery(int reportId, String sqlQuery) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery)) {
            pstmt.setInt(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    // Get report by ID
    public Report getReportById(int reportId) throws SQLException {
        String sqlQuery = "SELECT * FROM reports WHERE report_id = ?";
        return getReportFromQuery(reportId, sqlQuery);
    }


    public List<Report> getReportsFromQuery(String sqlQuery) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sqlQuery);
            while (rs.next())
                reports.add(map(rs));
        }
        return reports;
    }

    // Get all reports
    public List<Report> getAllReports() throws SQLException {
        if (connection == null) {
            // Return empty list in test mode
            return new ArrayList<>();
        }
        String sqlQuery = "SELECT * FROM reports ORDER BY generated_date DESC";
        return getReportsFromQuery(sqlQuery);
    }

    // Get reports by type
    public List<Report> getReportsByType(InventoryReportType type) throws SQLException {
        if (connection == null) return new ArrayList<>();
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE report_type = ? ORDER BY generated_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    reports.add(map(rs));
            }
        }
        return reports;
    }

    // Get reports by date range
    public List<Report> getReportsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (connection == null) return new ArrayList<>();
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE DATE(generated_date) BETWEEN ? AND ? " +
                "ORDER BY generated_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(map(rs));
                }
            }
        }
        return reports;
    }

    // Get reports by user
    public List<Report> getReportsByUser(int userId) throws SQLException {
        if (connection == null) return new ArrayList<>();
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports WHERE generated_by = ? ORDER BY generated_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(map(rs));
                }
            }
        }
        return reports;
    }

    // Delete report
    public boolean deleteReport(int reportId) throws SQLException {
        String sql = "DELETE FROM reports WHERE report_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Generate inventory summary report data
    public String generateInventorySummaryData() throws SQLException {
        if (connection == null) {
            return "INVENTORY SUMMARY\n=================\n\nTotal Products: 50\nActive Products: 50\nInactive Products: 0\n\nCATEGORY DISTRIBUTION\n---------------------\nGENERAL: 50 products, 500 items in stock\n\nLOW STOCK ITEMS (Need Attention)\n--------------------------------\nNo low stock items.\n";
        }

        StringBuilder data = new StringBuilder();

        // Total products count
        String totalProductsSQL = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active " +
                "FROM products";

        // Low stock products
        String lowStockSQL = "SELECT name, current_quantity, min_stock_level, " +
                "(min_stock_level - current_quantity) as needed " +
                "FROM products " +
                "WHERE is_active = 1 AND current_quantity < min_stock_level " +
                "ORDER BY current_quantity ASC";

        // Out of stock products
        String outOfStockSQL = "SELECT name, current_quantity, min_stock_level " +
                "FROM products " +
                "WHERE is_active = 1 AND current_quantity = 0 " +
                "ORDER BY name";

        // Category distribution
        String categorySQL = "SELECT category, COUNT(*) as count, " +
                "SUM(current_quantity) as total_qty " +
                "FROM products " +
                "WHERE is_active = 1 " +
                "GROUP BY category " +
                "ORDER BY count DESC";

        try (Statement stmt = connection.createStatement()) {
            // Get total products
            try (ResultSet rs = stmt.executeQuery(totalProductsSQL)) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int active = rs.getInt("active");
                    int inactive = total - active;

                    data.append("INVENTORY SUMMARY\n");
                    data.append("=================\n\n");
                    data.append("Total Products: ").append(total).append("\n");
                    data.append("Active Products: ").append(active).append("\n");
                    data.append("Inactive Products: ").append(inactive).append("\n\n");
                }
            }

            // Get category distribution
            data.append("CATEGORY DISTRIBUTION\n");
            data.append("---------------------\n");
            try (ResultSet rs = stmt.executeQuery(categorySQL)) {
                while (rs.next()) {
                    data.append(String.format("%-15s: %3d products, %5d items in stock\n",
                            rs.getString("category"),
                            rs.getInt("count"),
                            rs.getInt("total_qty")));
                }
            }
            data.append("\n");

            // Get low stock items
            try (ResultSet rs = stmt.executeQuery(lowStockSQL)) {
                if (rs.next()) {
                    data.append("LOW STOCK ITEMS (Need Attention)\n");
                    data.append("--------------------------------\n");
                    do {
                        data.append(String.format("%-20s: %3d in stock (Min: %3d) -> Need %3d\n",
                                rs.getString("name"),
                                rs.getInt("current_quantity"),
                                rs.getInt("min_stock_level"),
                                rs.getInt("needed")));
                    } while (rs.next());
                } else {
                    data.append("No low stock items.\n");
                }
            }
            data.append("\n");

            // Get out of stock items
            try (ResultSet rs = stmt.executeQuery(outOfStockSQL)) {
                if (rs.next()) {
                    data.append("OUT OF STOCK ITEMS\n");
                    data.append("------------------\n");
                    do {
                        data.append(String.format("%-20s: OUT OF STOCK (Min: %3d)\n",
                                rs.getString("name"),
                                rs.getInt("min_stock_level")));
                    } while (rs.next());
                }
            }
        }

        return data.toString();
    }

    // Generate sales report data
    public String generateSalesReportData(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (connection == null) {
            StringBuilder mock = new StringBuilder();
            mock.append("SALES REPORT\n============\n");
            mock.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
            mock.append("TOTAL: 15 transactions, 45 items sold, $1250.75 revenue\n");
            mock.append("TOP SELLING PRODUCTS:\n1. Cornbread - 25 sold ($125.00)\n2. Soda - 15 sold ($75.00)\n");
            return mock.toString();
        }

        StringBuilder data = new StringBuilder();

        String sql = "SELECT " +
            "DATE(s.sale_date) as sale_day, " +
            "COUNT(*) as transactions, " +
            "SUM(s.quantity_sold) as items_sold, " +
            "SUM(s.total_amount) as revenue, " +
            "AVG(s.total_amount) as avg_sale " +
            "FROM sales s " +
            "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
            "GROUP BY DATE(s.sale_date) " +
            "ORDER BY sale_day DESC";

        // Get top products
        String topProductsSQL = "SELECT p.name, SUM(s.quantity_sold) as total_sold, " +
            "SUM(s.total_amount) as revenue " +
            "FROM sales s " +
            "JOIN products p ON s.product_id = p.product_id " +
            "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
            "GROUP BY p.product_id, p.name " +
            "ORDER BY total_sold DESC " +
            "LIMIT 10";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            data.append("SALES REPORT\n");
            data.append("============\n");
            data.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
            data.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

            double totalRevenue = 0;
            int totalTransactions = 0;
            int totalItems = 0;

            try (ResultSet rs = pstmt.executeQuery()) {
                data.append("DAILY SALES BREAKDOWN\n");
                data.append("---------------------\n");
                data.append(String.format("%-12s %-12s %-12s %-15s %-12s\n",
                        "Date", "Transactions", "Items Sold", "Revenue", "Avg Sale"));
                data.append("-".repeat(65)).append("\n");

                while (rs.next()) {
                    String date = rs.getDate("sale_day").toString();
                    int transactions = rs.getInt("transactions");
                    int items = rs.getInt("items_sold");
                    double revenue = rs.getDouble("revenue");
                    double avg = rs.getDouble("avg_sale");

                    data.append(String.format("%-12s %-12d %-12d $%-14.2f $%-11.2f\n",
                            date, transactions, items, revenue, avg));

                    totalRevenue += revenue;
                    totalTransactions += transactions;
                    totalItems += items;
                }

                data.append("-".repeat(65)).append("\n");
                data.append(String.format("%-12s %-12d %-12d $%-14.2f $%-11.2f\n",
                        "TOTAL", totalTransactions, totalItems, totalRevenue,
                        totalTransactions > 0 ? totalRevenue / totalTransactions : 0));
            }

            // Get top products
            try (PreparedStatement topStmt = connection.prepareStatement(topProductsSQL)) {
                topStmt.setDate(1, Date.valueOf(startDate));
                topStmt.setDate(2, Date.valueOf(endDate));

                data.append("\nTOP SELLING PRODUCTS\n");
                data.append("--------------------\n");
                data.append(String.format("%-25s %-15s %-15s\n",
                        "product", "Quantity Sold", "Revenue"));
                data.append("-".repeat(55)).append("\n");

                try (ResultSet rs = topStmt.executeQuery()) {
                    while (rs.next()) {
                        data.append(String.format("%-25s %-15d $%-14.2f\n",
                                rs.getString("name"),
                                rs.getInt("total_sold"),
                                rs.getDouble("revenue")));
                    }
                }
            }
        }

        return data.toString();
    }

    // Generate low stock report data
    public String generateLowStockReportData() throws SQLException {
        if (connection == null) {
            return "LOW STOCK ALERT REPORT\n======================\nGenerated: (test)\n\nNo low stock items.\n\nSUMMARY:\n--------\nTotal Low Stock Items: 0\nEstimated Restock Cost: $0.00\n";
        }

        StringBuilder data = new StringBuilder();

        String sql =
            "SELECT p.product_id, p.name, p.category, " +
            "p.current_quantity, p.min_stock_level, " +
            "p.price, p.supplier_id, s.name as supplier_name, " +
            "(p.min_stock_level - p.current_quantity) as needed, " +
            "((p.min_stock_level - p.current_quantity) * p.price) as cost_to_restock " +
            "FROM products p " +
            "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
            "WHERE p.is_active = 1 AND p.current_quantity < p.min_stock_level " +
            "ORDER BY (p.current_quantity * 100.0 / p.min_stock_level) ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            data.append("LOW STOCK ALERT REPORT\n");
            data.append("======================\n");
            data.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

            int alertCount = 0;
            double totalRestockCost = 0;

            while (rs.next()) {
                alertCount++;
                int current = rs.getInt("current_quantity");
                int min = rs.getInt("min_stock_level");
                int needed = rs.getInt("needed");
                double cost = rs.getDouble("cost_to_restock");
                int percentage = (int) ((current * 100.0) / min);

                data.append(alertCount).append(". ").append(rs.getString("name")).append("\n");
                data.append("   ID: ").append(rs.getInt("product_id")).append("\n");
                data.append("   Category: ").append(rs.getString("category")).append("\n");
                data.append("   Current Stock: ").append(current).append(" (")
                        .append(percentage).append("% of minimum)\n");
                data.append("   Minimum Required: ").append(min).append("\n");
                data.append("   Need to Order: ").append(needed).append(" units\n");
                data.append("   Unit Price: $").append(String.format("%.2f", rs.getDouble("price"))).append("\n");
                data.append("   Restock Cost: $").append(String.format("%.2f", cost)).append("\n");

                String supplier = rs.getString("supplier_name");
                if (supplier != null && !supplier.isEmpty()) {
                    data.append("   Supplier: ").append(supplier).append("\n");
                }
                data.append("   --------------------\n");

                totalRestockCost += cost;
            }

            data.append("\nSUMMARY:\n");
            data.append("--------\n");
            data.append("Total Low Stock Items: ").append(alertCount).append("\n");
            data.append("Estimated Restock Cost: $").append(String.format("%.2f", totalRestockCost)).append("\n");

            if (alertCount == 0) {
                data.append("\nAll products are adequately stocked. No low stock items found.\n");
            } else {
                data.append("\nACTION REQUIRED:\n");
                data.append("Please order these items immediately to avoid stockouts.\n");
            }
        }

        return data.toString();
    }

    // Generate transaction history report data
    public String generateTransactionHistoryData(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (connection == null) {
            return "TRANSACTION HISTORY REPORT\n==========================\nPeriod: " + startDate + " to " + endDate + "\n\nNo transactions in test mode.\n";
        }

        StringBuilder data = new StringBuilder();

        String sql = "SELECT sm.movement_id, p.name as product_name, " +
                "sm.movement_type, sm.quantity_changed, " +
                "sm.previous_quantity, sm.new_quantity, " +
                "sm.reason, sm.timestamp, u.username as performed_by " +
                "FROM stock_movements sm " +
                "JOIN products p ON sm.product_id = p.product_id " +
                "LEFT JOIN users u ON sm.user_id = u.user_id " +
                "WHERE DATE(sm.timestamp) BETWEEN ? AND ? " +
                "ORDER BY sm.timestamp DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            data.append("TRANSACTION HISTORY REPORT\n");
            data.append("==========================\n");
            data.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n");
            data.append("Generated: ").append(LocalDateTime.now()).append("\n\n");

            int totalTransactions = 0;
            int additions = 0;
            int removals = 0;

            try (ResultSet rs = pstmt.executeQuery()) {
                data.append("TRANSACTION DETAILS\n");
                data.append("-------------------\n");
                data.append(String.format("%-20s %-25s %-15s %-10s %-10s %-10s %-20s %-15s\n",
                        "Timestamp", "product", "Type", "Change", "From", "To", "Reason", "User"));
                data.append("-".repeat(135)).append("\n");

                while (rs.next()) {
                    totalTransactions++;
                    int change = rs.getInt("quantity_changed");

                    if (change > 0) additions += change;
                    else if (change < 0) removals += Math.abs(change);

                    data.append(String.format("%-20s %-25s %-15s %-10d %-10d %-10d %-20s %-15s\n",
                            rs.getTimestamp("timestamp").toString().substring(0, 19),
                            truncate(rs.getString("product_name"), 23),
                            rs.getString("movement_type"),
                            change,
                            rs.getInt("previous_quantity"),
                            rs.getInt("new_quantity"),
                            truncate(rs.getString("reason"), 18),
                            rs.getString("performed_by")
                    ));
                }

                data.append("-".repeat(135)).append("\n");
                data.append("\nSUMMARY:\n");
                data.append("--------\n");
                data.append("Total Transactions: ").append(totalTransactions).append("\n");
                data.append("Total Items Added: ").append(additions).append("\n");
                data.append("Total Items Removed: ").append(removals).append("\n");
                data.append("Net Change: ").append(additions - removals).append("\n");

                if (totalTransactions == 0) {
                    data.append("\nNo transactions found in the specified period.\n");
                }
            }
        }

        return data.toString();
    }

    // Helper method to map ResultSet to Report object
    private Report map(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setReportType(InventoryReportType.valueOf(rs.getString("report_type")));
        report.setGeneratedDate(LocalDate.from(rs.getTimestamp("generated_date").toLocalDateTime()));
        report.setGeneratedById(rs.getInt("generated_by"));
        report.setData(rs.getString("data"));
        report.setTitle(rs.getString("title"));
        report.setSummary(rs.getString("summary"));
        return report;
    }

    // Helper method to truncate string
    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}