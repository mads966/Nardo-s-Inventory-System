package com.nardos.inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    enum StockComparison { BELOW, EQUAL, ABOVE }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setPrice(rs.getDouble("price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setMinStock(rs.getInt("min_stock"));
        return p;
    }

    public List<Product> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setString(1, "%" + name + "%");

        ResultSet rs = ps.executeQuery();
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Product> filterByCategory(String cat) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ?";
        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setString(1, cat);

        ResultSet rs = ps.executeQuery();
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Product> filterBySupplier(int supplierID) throws SQLException {
        String sql = "SELECT * FROM products WHERE supplier_id = ?";
        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, supplierID);

        ResultSet rs = ps.executeQuery();
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Product> filterByStock(StockComparison cmp, int qty, Integer unused) throws SQLException {
        String sql = "";

        switch (cmp) {
            case BELOW: sql = "SELECT * FROM products WHERE quantity < ?"; break;
            case EQUAL: sql = "SELECT * FROM products WHERE quantity = ?"; break;
            case ABOVE: sql = "SELECT * FROM products WHERE quantity > ?"; break;
        }

        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, qty);

        ResultSet rs = ps.executeQuery();
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    public List<Product> searchAndFilter(
            String name,
            String category,
            Integer supplierID,
            StockComparison cmp,
            Integer qty,
            Integer unused
    ) throws SQLException {

        StringBuilder sb = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sb.append("AND name LIKE ? ");
            params.add("%" + name + "%");
        }

        if (category != null && !category.isBlank()) {
            sb.append("AND category = ? ");
            params.add(category);
        }

        if (supplierID != null) {
            sb.append("AND supplier_id = ? ");
            params.add(supplierID);
        }

        if (cmp != null && qty != null) {
            switch (cmp) {
                case BELOW: sb.append("AND quantity < ? "); break;
                case EQUAL: sb.append("AND quantity = ? "); break;
                case ABOVE: sb.append("AND quantity > ? "); break;
            }
            params.add(qty);
        }

        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sb.toString());

        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }

        ResultSet rs = ps.executeQuery();
        List<Product> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }
public boolean isDuplicateInList(String productName, int supplierId) throws SQLException {
    String sql = "SELECT * FROM products WHERE name = ? AND supplier_id = ?";
        Connection c = DBManager.getConnection();
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setString(1, productName);
        ps.setInt(2, supplierId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
    
    
public void saveProduct(Product product) throws SQLException{
    if (isDuplicateInList(product.getName(), product.getSupplierId())) {
    System.out.print("Product Already exists!");
    return;
}
    String sql = "INSERT INTO products(Name,category,supplier_id,price,quantity,min_stock) VALUES(?,?,?,?,?,?)";
    Connection c = DBManager.getConnection();
    PreparedStatement ps = c.prepareStatement(sql);
   ps.setString(1, product.getName());
   ps.setString(2,product.getCategory());
   ps.setInt(3, product.getSupplierId());
   ps.setDouble(4,product.getPrice());
   ps.setInt(5,product.getQuantity());
   ps.setInt(6,product.getMinStock());
   int rowsInserted = ps.executeUpdate();
if (rowsInserted > 0) {
    System.out.println("Product saved successfully");
}
}
   
public void updateProduct(Product product) throws SQLException{
    String sql = "UPDATE products SET Name = ?,category = ?,supplier_id = ?,price = ?,quantity = ?,min_stock = ?  WHERE product_id = ?";
    Connection c = DBManager.getConnection();
    PreparedStatement ps = c.prepareStatement(sql);
    ps.setString(1, product.getName());
    ps.setString(2,product.getCategory());
    ps.setInt(3, product.getSupplierId());
    ps.setDouble(4,product.getPrice());
    ps.setInt(5,product.getQuantity());
    ps.setInt(6,product.getMinStock());
    ps.setInt(7, product.getProductId());
    int rowsUpdated = ps.executeUpdate();
if (rowsUpdated> 0) {
    System.out.println("Product Updated successfully");
}
}
public void deleteProduct(int productID) throws SQLException{
    String sql = "DELETE FROM products WHERE product_id =?";
    Connection c = DBManager.getConnection();
    PreparedStatement ps = c.prepareStatement(sql);
    ps.setInt(1,productID);
    int rowsDeleted = ps.executeUpdate();
if (rowsDeleted > 0){
    System.out.println("Product Deleted Successfully");
}
else{
    System.out.println("Product does not exist!");
}
    
}


}


