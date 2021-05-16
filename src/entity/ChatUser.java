package entity;

public class ChatUser {
    private String name;
    private String sessionId;
    private long lastInteractionTime;

    public ChatUser(String name, String sessionId, long lastInteractionTime) {
        this.name = name;
        this.sessionId = sessionId;
        this.lastInteractionTime = lastInteractionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getLastInteractionTime() {
        return lastInteractionTime;
    }

    public void setLastInteractionTime(long lastInteractionTime) {
        this.lastInteractionTime = lastInteractionTime;
    }
}