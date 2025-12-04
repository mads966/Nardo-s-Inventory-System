package product;

import enums.Category;

import java.util.regex.Pattern;

/**
 * Input Validation Utility - SRS Requirement: Data Integrity and Security
 * 
 * Validates all user inputs for the product management system according to
 * SRS business rules and security requirements. Prevents SQL injection,
 * XSS attacks, and enforces business rule constraints.
 * 
 * Security Features:
 * - SQL keyword detection and blocking
 * - Special character filtering
 * - Range validation for numeric inputs
 * - Length constraints for string inputs
 * - Pattern matching for structured data (email, phone)
 * 
 * Business Rule Enforcement (SRS 1.2 - Product Management):
 * - Product names: 1-100 characters, alphanumeric with basic punctuation
 * - Prices: Decimal format, positive, up to 999999.99
 * - Quantities: Non-negative integers, up to 999999
 * - Minimum stock: 0-10000 units (logical threshold)
 * - Categories: Enumerated list (FOOD, BEVERAGES, SNACKS, ESSENTIALS, COMBO_MEALS)
 * 
 * Usage Pattern:
 * 1. Collect user input from GUI fields
 * 2. Call appropriate validation method
 * 3. If validation fails, show error message to user
 * 4. If validation passes, proceed with database operation
 */
public class InputValidator {
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,()-]*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-+\\s()]*$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
    
    /**
     * Validate product name: non-empty, alphanumeric with spaces and basic punctuation, max 100 chars
     */
    public static boolean validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (name.length() > 100) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate product description: non-null, max 500 chars, no SQL keywords
     */
    public static boolean validateDescription(String description) {
        if (description == null) {
            return true; // Description can be null/empty
        }
        if (description.length() > 500) {
            return false;
        }
        // Check for SQL injection patterns
        String lowerDesc = description.toLowerCase();
        return !containsSQLKeywords(lowerDesc);
    }
    
    /**
     * Validate price: positive decimal, max 2 decimal places, max 999999.99
     */
    public static boolean validatePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return false;
        }
        if (!DECIMAL_PATTERN.matcher(priceStr.trim()).matches()) {
            return false;
        }
        try {
            double price = Double.parseDouble(priceStr.trim());
            return price > 0 && price <= 999999.99;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate quantity: non-negative integer, max 999999
     */
    public static boolean validateQuantity(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return false;
        }
        if (!INTEGER_PATTERN.matcher(quantityStr.trim()).matches()) {
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityStr.trim());
            return quantity >= 0 && quantity <= 999999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate minimum stock level: non-negative integer, max 10000
     */
    public static boolean validateMinStock(String minStockStr) {
        if (minStockStr == null || minStockStr.trim().isEmpty()) {
            return false;
        }
        if (!INTEGER_PATTERN.matcher(minStockStr.trim()).matches()) {
            return false;
        }
        try {
            int minStock = Integer.parseInt(minStockStr.trim());
            return minStock >= 0 && minStock <= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate category: must be one of the allowed categories
     */
    public static boolean validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        try {
            Category.valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Validate supplier ID: positive integer
     */
    public static boolean validateSupplierId(String supplierIdStr) {
        if (supplierIdStr == null || supplierIdStr.trim().isEmpty()) {
            return true; // Supplier can be optional
        }
        if (!INTEGER_PATTERN.matcher(supplierIdStr.trim()).matches()) {
            return false;
        }
        try {
            int supplierId = Integer.parseInt(supplierIdStr.trim());
            return supplierId > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate email format
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email can be optional
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone can be optional
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Removes potentially dangerous characters from user input.
     * Called before any database operations to prevent injection.
     * 
     * Removes: single quotes, double quotes, semicolons, SQL comment markers
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("['\";\\-\\-]", "").trim();
    }
    
    /**
     * Detects SQL injection attempts by checking for common SQL keywords.
     * Used in all user input validation.
     * 
     * Dangerous keywords: DROP, DELETE, INSERT, UPDATE, SELECT, EXEC, UNION, etc.
     */
    private static boolean containsSQLKeywords(String input) {
        String[] sqlKeywords = {
            "drop table", "delete from", "insert into", "update ", 
            "select ", "exec ", "execute ", "union ", "declare ", "cast("
        };
        
        for (String keyword : sqlKeywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate search criteria: max 50 chars, no SQL keywords
     */
    public static boolean validateSearchCriteria(String criteria) {
        if (criteria == null || criteria.trim().isEmpty()) {
            return true; // Empty search is allowed
        }
        if (criteria.length() > 50) {
            return false;
        }
        return !containsSQLKeywords(criteria.toLowerCase());
    }
}
