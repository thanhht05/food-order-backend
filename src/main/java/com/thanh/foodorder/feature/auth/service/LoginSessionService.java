package com.thanh.foodorder.feature.auth.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.thanh.foodorder.feature.user.domain.User;

import lombok.extern.log4j.Log4j2;

/**
 * Manages single active session:
 * each account has at most one active session. New session supersedes old,
 * and previous session is notified in real time via private WebSocket destination.
 *
 * Separated from tokenVersion (which is reserved for password resets).
 */
@Service
@Log4j2
public class LoginSessionService {

    /** Client subscribe: /user/queue/session */
    public static final String SESSION_QUEUE = "/queue/session";
    public static final String EVENT_SESSION_REPLACED = "SESSION_REPLACED";

    private final SimpMessagingTemplate messagingTemplate;

    public LoginSessionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Creates new session for user (assigned to entity, caller persists)
     * and dispatches termination alert to previous session (if any).
     *
     * @return new sessionId
     */
    public String startNewSession(User user) {
        String newSessionId = UUID.randomUUID().toString();
        boolean hadSession = user.getActiveSessionId() != null;
        user.setActiveSessionId(newSessionId);

        if (hadSession) {
            notifySessionReplaced(user.getEmail());
        }
        return newSessionId;
    }

    private void notifySessionReplaced(String email) {
        try {
            this.messagingTemplate.convertAndSendToUser(
                    email,
                    SESSION_QUEUE,
                    Map.of(
                            "type", EVENT_SESSION_REPLACED,
                            "timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            // Prevent WebSocket delivery failure from breaking login; old session remains blocked at JwtFilter.
            log.warn("Cannot send SESSION_REPLACED to {}: {}", email, e.getMessage());
        }
    }
}

