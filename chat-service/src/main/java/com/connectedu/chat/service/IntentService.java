package com.connectedu.chat.service; // Check package name matches folder structure

import com.connectedu.chat.engine.Intent;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IntentService {

    private final Pattern yearPattern = Pattern.compile("\\b(20\\d{2})\\b");

    public Intent detectIntent(String message) {
        String msg = message.toLowerCase().trim();

        // --- FIX: PRIORITY 1 - CHECK PERSONAL REGISTRATIONS FIRST ---
        // We check this first because "events I participate" contains the word "event"
        // and we don't want it to be caught by the general event filter later.
        if (containsAny(msg, "my", "i", "me") &&
                containsAny(msg, "participate", "register", "joined", "booked", "registration")) {
            return Intent.GET_MY_REGISTRATIONS;
        }

        // 2. Greetings
        if (containsAny(msg, "hi", "hello", "hey", "start")) return Intent.GREETING;

        // 3. Specific Club Event Query (e.g., "Innovators club events")
        if (msg.contains("club") && (msg.contains("event") || msg.contains("activity"))) {
            return Intent.GET_CLUB_SPECIFIC_EVENTS;
        }

        // 4. Specific Year Query (e.g., "Events in 2026")
        if (msg.contains("event") && yearPattern.matcher(msg).find()) {
            return Intent.SEARCH_EVENTS_BY_YEAR;
        }

        // 5. Contact Info
        if ((msg.contains("contact") || msg.contains("email") || msg.contains("number"))
                && (msg.contains("club") || msg.contains("event"))) {
            return Intent.GET_SPECIFIC_CONTACT_INFO;
        }

        // 6. Past Events
        if (containsAny(msg, "past", "history", "previous", "ended", "last")) return Intent.GET_PAST_EVENTS;

        // 7. Upcoming Events
        if (containsAny(msg, "upcoming", "future", "next", "events", "calendar", "schedule")) return Intent.GET_UPCOMING_EVENTS;

        // 8. Clubs
        if (containsAny(msg, "club", "society", "communities", "groups")) return Intent.GET_ALL_CLUBS;

        // Fallback checks
        if (msg.contains("event")) return Intent.GET_UPCOMING_EVENTS;
        if (msg.contains("club")) return Intent.GET_ALL_CLUBS;

        return Intent.UNKNOWN;
    }

    public String extractYear(String message) {
        Matcher m = yearPattern.matcher(message);
        if (m.find()) return m.group(1);
        return String.valueOf(LocalDate.now().getYear());
    }

    public String extractClubName(String message) {
        return message;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) if (text.contains(k)) return true;
        return false;
    }
}