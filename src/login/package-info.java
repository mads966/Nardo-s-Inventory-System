/**
 * Authentication and Login Layer - SRS Component: 1.1 User Authentication
 * 
 * This package handles user authentication, session management, and password
 * security for role-based access control (RBAC).
 * 
 * SRS Requirement 1.1 Coverage:
 * - Valid login with username and password
 * - Encrypted password storage (SHA-256 with salt)
 * - Password strength validation (8+ chars, uppercase, lowercase, numbers)
 * - Session timeout (30 minutes inactivity)
 * - Three failed login attempts locks account temporarily
 * - Password reset with security questions
 * - Role-based access: OWNER and STAFF
 * 
 * Architecture: MVC Pattern with Security Layer
 * - LoginPage: UI for user authentication (View)
 * - AuthenticationService: Validates credentials (Business Logic)
 * - SessionManager: Maintains user sessions and permissions (State Management)
 * - PasswordStrengthChecker: Enforces password policy
 * - PasswordEncryption: Implements SHA-256 with salt
 * 
 * Key Components:
 * - Requirement 1.1.1: Valid Login
 *   Authentication checks user credentials against encrypted database records
 * - Requirement 1.1.2: Password Strength
 *   Minimum 8 characters with uppercase, lowercase, and numeric characters
 * - Requirement 1.1.3: Session Management
 *   Sessions expire after 30 minutes of inactivity
 * - Requirement 1.1.4: Account Lockout
 *   Temporary lockout after 3 consecutive failed login attempts
 * 
 * Security Notes:
 * - Passwords never stored in plaintext
 * - All credentials validated server-side
 * - Session tokens are transient (not persistent)
 * - Logout clears all session data
 */
package login;
