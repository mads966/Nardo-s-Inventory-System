// File: SupplierDAO.java
package supplies;

import supplies.Supplier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private Connection connection;
    
    public SupplierDAO(Connection connection) {
        this.connection = connection;
    }
    
    public Supplier getSupplierById(int supplierId) throws SQLException {
        if (connection == null) {
            // test-mode: return a mock supplier so UI can display something
            Supplier s = new Supplier();
            s.setSupplierId(supplierId > 0 ? supplierId : (int) (Math.random() * 100000));
            s.setName("Test Supplier");
            s.setContactPerson("Test Contact");
            s.setPhone("000-000-0000");
            s.setEmail("supplier@example.com");
            s.setAddress("Test Address");
            return s;
        }

        String query = "SELECT * FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        }
        return null;
    }
    
    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        if (connection == null) return suppliers;

        String query = "SELECT * FROM suppliers ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        }
        return suppliers;
    }
    
    public boolean createSupplier(Supplier supplier) throws SQLException {
        if (connection == null) {
            // test-mode: assign mock id
            supplier.setSupplierId((int)(Math.random() * 100000));
            return true;
        }

        String query = "INSERT INTO suppliers (name, contact_person, phone, email, address) " +
                      "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getAddress());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        supplier.setSupplierId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean updateSupplier(Supplier supplier) throws SQLException {
        String query = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, " +
                      "email = ?, address = ? WHERE supplier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getPhone());
            stmt.setString(4, supplier.getEmail());
            stmt.setString(5, supplier.getAddress());
            stmt.setInt(6, supplier.getSupplierId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean supplierNameExists(String name) throws SQLException {
        String query = "SELECT COUNT(*) FROM suppliers WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        return supplier;
    }
}