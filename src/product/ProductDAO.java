package product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import database.DBManager;

public class ProductDAO {
    Connection connection;
    public ProductDAO() {}

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean updateProductQuantity(int productId, int quantityDelta) {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantityDelta);
            ps.setInt(2, productId);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product quantity: " + e.getMessage());
            throw new RuntimeException("Database error updating product quantity", e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));

        Integer supplierId = rs.getInt("supplier_id");
        if (rs.wasNull()) {
            p.setSupplierId(null);
        } else {
            p.setSupplierId(supplierId);
        }

        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinStock(rs.getInt("min_stock"));
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }

    public List<Product> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM products WHERE name LIKE ? AND is_active = TRUE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE ORDER BY name";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Product> getLowStockProducts() throws SQLException {
        String sql = "SELECT * FROM products WHERE quantity <= min_stock AND is_active = TRUE ORDER BY name";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public int getTotalProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = TRUE";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= min_stock AND is_active = TRUE";
        try (Connection c = DBManager.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public void saveProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products(name, category, supplier_id, price, quantity, min_stock, is_active) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());

            if (product.getSupplierId() != null) {
                ps.setInt(3, product.getSupplierId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setInt(6, product.getMinStock());
            ps.setBoolean(7, true);

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setProductId(rs.getInt(1));
                    }
                }
                System.out.println("Product saved successfully with ID: " + product.getProductId());
            }
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, category = ?, supplier_id = ?, price = ?, quantity = ?, min_stock = ? WHERE product_id = ?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());

            if (product.getSupplierId() != null) {
                ps.setInt(3, product.getSupplierId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setInt(6, product.getMinStock());
            ps.setInt(7, product.getProductId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Product Updated successfully");
            } else {
                throw new SQLException("Product not found with ID: " + product.getProductId());
            }
        }
    }

    public void deleteProduct(int productID) throws SQLException {
        String sql = "UPDATE products SET is_active = FALSE WHERE product_id = ?";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productID);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Product deactivated successfully");
            } else {
                throw new SQLException("Product not found with ID: " + productID);
            }
        }
    }

    public List<Product> filterByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? AND is_active = TRUE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public int getProductStockLevel(int productId) throws SQLException {
        String sql = "SELECT quantity FROM products WHERE product_id = ? AND is_active = TRUE";
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        return 0;
    }
}