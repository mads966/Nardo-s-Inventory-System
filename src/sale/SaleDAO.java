package sale;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {
    private Connection connection;
    
    public SaleDAO(Connection connection) {
        this.connection = connection;
    }
    
    // Save a new sale
    public int saveSale(Sale sale) throws SQLException {
        if (connection == null) {
            // Test-mode: simulate save
            int mockId = (int) (Math.random() * 100000);
            sale.setSaleId(mockId);
            // assign saleId to items
            if (sale.getItems() != null) {
                int idx = 1;
                for (SaleItem it : sale.getItems()) {
                    it.setSaleId(mockId);
                    it.setSaleItemId(idx++);
                }
            }
            System.out.println("[SaleDAO] (test) Saved sale mock id=" + mockId);
            return mockId;
        }

        String sql = "INSERT INTO sales (sale_datetime, user_id, user_name, subtotal, " +
                    "tax_amount, discount_amount, total_amount, payment_method, " +
                    "payment_status, notes, receipt_number, is_completed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(sale.getSaleDateTime()));
            pstmt.setInt(2, sale.getUserId());
            pstmt.setString(3, sale.getUserName());
            pstmt.setDouble(4, sale.getSubtotal());
            pstmt.setDouble(5, sale.getTaxAmount());
            pstmt.setDouble(6, sale.getDiscountAmount());
            pstmt.setDouble(7, sale.getTotalAmount());
            pstmt.setString(8, sale.getPaymentMethod());
            pstmt.setString(9, sale.getPaymentStatus());
            pstmt.setString(10, sale.getNotes());
            pstmt.setString(11, sale.getReceiptNumber());
            pstmt.setBoolean(12, sale.isCompleted());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int saleId = rs.getInt(1);
                        sale.setSaleId(saleId);

                        // Save sale items
                        saveSaleItems(saleId, sale.getItems());

                        return saleId;
                    }
                }
            }
            throw new SQLException("Failed to save sale, no ID obtained");
        }
    }
    
    // Save sale items
    private void saveSaleItems(int saleId, List<SaleItem> items) throws SQLException {
        if (connection == null) {
            // test-mode: assign mock item IDs
            if (items != null) {
                int idx = 1;
                for (SaleItem it : items) {
                    it.setSaleItemId(idx++);
                    it.setSaleId(saleId);
                }
            }
            return;
        }

        String sql = "INSERT INTO sale_items (sale_id, product_id, product_name, " +
                    "product_category, quantity, unit_price, line_total) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (items != null) {
                for (SaleItem item : items) {
                    pstmt.setInt(1, saleId);
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setString(3, item.getProductName());
                    pstmt.setString(4, item.getProductCategory());
                    pstmt.setInt(5, item.getQuantity());
                    pstmt.setDouble(6, item.getUnitPrice());
                    pstmt.setDouble(7, item.getLineTotal());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        }
    }
    
    // Get sale by ID
    public Sale getSaleById(int saleId) throws SQLException {
        if (connection == null) {
            // test-mode: return mock sale
            Sale s = new Sale();
            s.setSaleId(saleId);
            s.setUserId(1);
            s.setUserName("TestUser");
            return s;
        }

        String sql = "SELECT * FROM sales WHERE sale_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);

                    // Load sale items
                    List<SaleItem> items = getSaleItems(saleId);
                    sale.setItems(items);

                    return sale;
                }
            }
        }
        return null;
    }
    
    // Get sale items for a sale
    private List<SaleItem> getSaleItems(int saleId) throws SQLException {
        List<SaleItem> items = new ArrayList<>();
        if (connection == null) return items;

        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductCategory(rs.getString("product_category"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setLineTotal(rs.getDouble("line_total"));

                    items.add(item);
                }
            }
        }
        return items;
    }
    
    // Get all sales
    public List<Sale> getAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        if (connection == null) return sales;

        String sql = "SELECT * FROM sales ORDER BY sale_datetime DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }
        return sales;
    }
    
    // Get sales by date range
    public List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        if (connection == null) return sales;

        String sql = "SELECT * FROM sales WHERE DATE(sale_datetime) BETWEEN ? AND ? " +
                    "ORDER BY sale_datetime DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        return sales;
    }
    
    // Get today's sales
    public List<Sale> getTodaySales() throws SQLException {
        if (connection == null) return new ArrayList<>();
        LocalDate today = LocalDate.now();
        return getSalesByDateRange(today, today);
    }
    
    // Get sales by user
    public List<Sale> getSalesByUser(int userId) throws SQLException {
        List<Sale> sales = new ArrayList<>();
        if (connection == null) return sales;

        String sql = "SELECT * FROM sales WHERE user_id = ? ORDER BY sale_datetime DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        return sales;
    }
    
    // Get total sales amount by date range
    public double getTotalSalesAmount(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (connection == null) return 0;

        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM sales " +
                    "WHERE DATE(sale_datetime) BETWEEN ? AND ? AND is_completed = true";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }
    
    // Get today's total sales
    public double getTodayTotalSales() throws SQLException {
        if (connection == null) return 0;
        LocalDate today = LocalDate.now();
        return getTotalSalesAmount(today, today);
    }
    
    // Get sales statistics
    public SalesStatistics getSalesStatistics(LocalDate startDate, LocalDate endDate) throws SQLException {
        SalesStatistics stats = new SalesStatistics();
        if (connection == null) {
            // return mocked stats in test-mode
            stats.setTotalSales(0);
            stats.setTotalRevenue(0);
            stats.setAverageSale(0);
            stats.setTotalItems(0);
            stats.setTopProducts(new ArrayList<>());
            return stats;
        }

        String sql = "SELECT " +
                    "COUNT(*) as total_sales, " +
                    "COALESCE(SUM(total_amount), 0) as total_revenue, " +
                    "COALESCE(AVG(total_amount), 0) as avg_sale, " +
                    "COALESCE(SUM(total_items), 0) as total_items " +
                    "FROM (SELECT s.sale_id, s.total_amount, " +
                    "      (SELECT SUM(quantity) FROM sale_items si WHERE si.sale_id = s.sale_id) as total_items " +
                    "      FROM sales s " +
                    "      WHERE DATE(s.sale_datetime) BETWEEN ? AND ? AND s.is_completed = true) as sales_data";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalSales(rs.getInt("total_sales"));
                    stats.setTotalRevenue(rs.getDouble("total_revenue"));
                    stats.setAverageSale(rs.getDouble("avg_sale"));
                    stats.setTotalItems(rs.getInt("total_items"));
                }
            }
        }

        // Get top products
        stats.setTopProducts(getTopProducts(startDate, endDate, 5));

        return stats;
    }
    
    // Get top products
    private List<TopProduct> getTopProducts(LocalDate startDate, LocalDate endDate, int limit) throws SQLException {
        List<TopProduct> topProducts = new ArrayList<>();
        if (connection == null) return topProducts;

        String sql = "SELECT p.product_id, p.name, p.category, " +
                    "SUM(si.quantity) as total_sold, " +
                    "SUM(si.line_total) as total_revenue " +
                    "FROM sale_items si " +
                    "JOIN sales s ON si.sale_id = s.sale_id " +
                    "JOIN products p ON si.product_id = p.product_id " +
                    "WHERE DATE(s.sale_datetime) BETWEEN ? AND ? AND s.is_completed = true " +
                    "GROUP BY p.product_id, p.name, p.category " +
                    "ORDER BY total_sold DESC " +
                    "LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            pstmt.setInt(3, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TopProduct product = new TopProduct();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("name"));
                    product.setCategory(rs.getString("category"));
                    product.setTotalSold(rs.getInt("total_sold"));
                    product.setTotalRevenue(rs.getDouble("total_revenue"));

                    topProducts.add(product);
                }
            }
        }
        return topProducts;
    }
    
    // Update sale status
    public boolean updateSaleStatus(int saleId, String status, boolean isCompleted) throws SQLException {
        if (connection == null) {
            // test-mode: pretend update succeeded
            System.out.println("[SaleDAO] (test) updateSaleStatus: saleId=" + saleId + " status=" + status);
            return true;
        }

        String sql = "UPDATE sales SET payment_status = ?, is_completed = ? WHERE sale_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setBoolean(2, isCompleted);
            pstmt.setInt(3, saleId);

            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Delete sale (for cancellations)
    public boolean deleteSale(int saleId) throws SQLException {
        // First delete sale items
        String deleteItemsSql = "DELETE FROM sale_items WHERE sale_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteItemsSql)) {
            pstmt.setInt(1, saleId);
            pstmt.executeUpdate();
        }
        
        // Then delete sale
        String deleteSaleSql = "DELETE FROM sales WHERE sale_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSaleSql)) {
            pstmt.setInt(1, saleId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Helper method to map ResultSet to Sale
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setSaleDateTime(rs.getTimestamp("sale_datetime").toLocalDateTime());
        sale.setUserId(rs.getInt("user_id"));
        sale.setUserName(rs.getString("user_name"));
        // sale.setSubtotal(rs.getDouble("subtotal"));
        sale.setTaxAmount(rs.getDouble("tax_amount"));
        sale.setDiscountAmount(rs.getDouble("discount_amount"));
        // sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setPaymentStatus(rs.getString("payment_status"));
        sale.setNotes(rs.getString("notes"));
        sale.setReceiptNumber(rs.getString("receipt_number"));
        sale.setCompleted(rs.getBoolean("is_completed"));
        
        return sale;
    }
    
    // Inner classes for statistics
    public static class SalesStatistics {
        private int totalSales;
        private double totalRevenue;
        private double averageSale;
        private int totalItems;
        private List<TopProduct> topProducts;
        
        // Getters and Setters
        public int getTotalSales() { return totalSales; }
        public void setTotalSales(int totalSales) { this.totalSales = totalSales; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public double getAverageSale() { return averageSale; }
        public void setAverageSale(double averageSale) { this.averageSale = averageSale; }
        
        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        
        public List<TopProduct> getTopProducts() { return topProducts; }
        public void setTopProducts(List<TopProduct> topProducts) { this.topProducts = topProducts; }
        
        @Override
        public String toString() {
            return String.format("Sales: %d, Revenue: $%.2f, Avg: $%.2f, Items: %d", 
                totalSales, totalRevenue, averageSale, totalItems);
        }
    }
    
    public static class TopProduct {
        private int productId;
        private String name;
        private String category;
        private int totalSold;
        private double totalRevenue;
        
        // Getters and Setters
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public int getTotalSold() { return totalSold; }
        public void setTotalSold(int totalSold) { this.totalSold = totalSold; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        @Override
        public String toString() {
            return String.format("%s: %d sold, $%.2f revenue", name, totalSold, totalRevenue);
        }
    }
}