package com.nardos.inventory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchFilterService {

    private ProductDAO productDAO;

    public SearchFilterService() {
        this.productDAO = new ProductDAO();
    }

    // --- SEARCH BY NAME ---
    public List<Product> searchByName(String productName) {
        try {
            if (productName == null || productName.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return productDAO.searchByName(productName.trim());
        } catch (SQLException e) {
            System.out.println("Error searching by name: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- FILTER BY CATEGORY ---
    public List<Product> filterByCategory(String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return productDAO.filterByCategory(category.trim());
        } catch (SQLException e) {
            System.out.println("Error filtering by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- FILTER BY SUPPLIER ---
    public List<Product> filterBySupplier(int supplierID) {
        try {
            return productDAO.filterBySupplier(supplierID);
        } catch (SQLException e) {
            System.out.println("Error filtering by supplier: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- FILTER BY STOCK LEVEL ---
    public List<Product> filterByStock(String comparison, int quantity) {
        try {
            if (comparison == null) return new ArrayList<>();

            switch (comparison.toUpperCase()) {
                case "BELOW":
                    return productDAO.filterByStock(ProductDAO.StockComparison.BELOW, quantity, null);

                case "EQUAL":
                    return productDAO.filterByStock(ProductDAO.StockComparison.EQUAL, quantity, null);

                case "ABOVE":
                    return productDAO.filterByStock(ProductDAO.StockComparison.ABOVE, quantity, null);

                default:
                    System.out.println("Invalid stock comparison: " + comparison);
                    return new ArrayList<>();
            }
        } catch (SQLException e) {
            System.out.println("Error filtering by stock: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- COMBINE SEARCH + FILTER ---
    public List<Product> combineSearchAndFilter(
            String productName,
            String category,
            Integer supplierID,
            String comparison,
            Integer quantity
    ) {
        try {
            ProductDAO.StockComparison cmp = null;

            if (comparison != null) {
                switch (comparison.toUpperCase()) {
                    case "BELOW": cmp = ProductDAO.StockComparison.BELOW; break;
                    case "EQUAL": cmp = ProductDAO.StockComparison.EQUAL; break;
                    case "ABOVE": cmp = ProductDAO.StockComparison.ABOVE; break;
                }
            }

            return productDAO.searchAndFilter(
                    productName,
                    category,
                    supplierID,
                    cmp,
                    quantity,
                    null
            );
        } catch (SQLException e) {
            System.out.println("Error combining search/filter: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
