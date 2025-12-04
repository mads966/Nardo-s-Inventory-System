package login;

public class User {
    private int userId;
    private String username;
    private String password;
    private UserRole role;
    private String email;
    private boolean isActive;
    
    public User(int userId, String username, String password, 
                UserRole role, String email, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.isActive = isActive;
    }
    
    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public String getEmail() { return email; }
    public boolean isActive() { return isActive; }
    
    // Setters
    public void setPassword(String password) { this.password = password; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setUserId(int userId) { this.userId = userId; }
}