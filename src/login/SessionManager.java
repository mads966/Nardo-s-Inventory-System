package login;

import java.util.*;
import java.util.concurrent.*;

public class SessionManager {
    private static SessionManager instance;
    private Map<String, UserSession> activeSessions;
    private ScheduledExecutorService scheduler;
    
    private SessionManager() {
        activeSessions = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
        startSessionCleanup();
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public String createSession(User user) {
        String sessionId = generateSessionId();
        UserSession session = new UserSession(user, sessionId);
        activeSessions.put(sessionId, session);
        return sessionId;
    }
    
    public UserSession getSession(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null && session.isValid()) {
            session.updateLastAccess();
            return session;
        } else {
            activeSessions.remove(sessionId);
            return null;
        }
    }
    
    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    public void invalidateAllSessionsForUser(String username) {
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getUser().getUsername().equals(username));
    }
    
    private String generateSessionId() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    private void startSessionCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            activeSessions.entrySet().removeIf(entry -> 
                !entry.getValue().isValid(currentTime));
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    public class UserSession {
        private User user;
        private String sessionId;
        private long creationTime;
        private long lastAccessTime;
        private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
        
        public UserSession(User user, String sessionId) {
            this.user = user;
            this.sessionId = sessionId;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = creationTime;
        }
        
        public User getUser() { return user; }
        public String getSessionId() { return sessionId; }
        public long getCreationTime() { return creationTime; }
        
        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public boolean isValid() {
            return isValid(System.currentTimeMillis());
        }
        
        public boolean isValid(long currentTime) {
            return (currentTime - lastAccessTime) <= SESSION_TIMEOUT;
        }
        
        public long getRemainingTime() {
            long elapsed = System.currentTimeMillis() - lastAccessTime;
            return Math.max(0, SESSION_TIMEOUT - elapsed);
        }
    }
}