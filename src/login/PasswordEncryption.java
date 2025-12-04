package login;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password encryption and verification using SHA-256 with salt.
 * Provides secure password hashing suitable for production use.
 */
public class PasswordEncryption {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes = 128 bits
    
    /**
     * Generates a secure hash of the password with salt.
     * Format: salt_base64:hash_base64
     *
     * @param password The plain text password to encrypt
     * @return The encrypted password with salt in format: salt:hash
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Generate random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        
        // Hash password with salt
        byte[] hashedPassword = hashWithSalt(password, salt);
        
        // Combine salt and hash, encode to Base64
        byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
        System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);
        
        return Base64.getEncoder().encodeToString(saltAndHash);
    }
    
    /**
     * Verifies a plain text password against its encrypted hash.
     *
     * @param password The plain text password to verify
     * @param encryptedPassword The encrypted password hash with salt
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String encryptedPassword) {
        if (password == null || encryptedPassword == null) {
            return false;
        }
        
        try {
            // Decode the Base64 encoded salt and hash
            byte[] saltAndHash = Base64.getDecoder().decode(encryptedPassword);
            
            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(saltAndHash, 0, salt, 0, SALT_LENGTH);
            
            // Extract hash
            byte[] storedHash = new byte[saltAndHash.length - SALT_LENGTH];
            System.arraycopy(saltAndHash, SALT_LENGTH, storedHash, 0, storedHash.length);
            
            // Hash the provided password with the same salt
            byte[] computedHash = hashWithSalt(password, salt);
            
            // Compare hashes
            return MessageDigest.isEqual(computedHash, storedHash);
        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
            return false;
        }
    }
    
    /**
     * Internal method to hash a password with a given salt.
     *
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Simple hash for non-security purposes (for legacy compatibility if needed).
     * WARNING: This should NOT be used for storing passwords in production.
     * Use hashPassword() instead.
     *
     * @param input The string to hash
     * @return The SHA-256 hash as hex string
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    @Deprecated
    public static String simpleHash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
