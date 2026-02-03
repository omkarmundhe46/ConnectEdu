package com.connectedu.chat.service;

import com.connectedu.chat.client.ClubClient;
import com.connectedu.chat.client.EventClient;
import com.connectedu.chat.dto.ChatClubDto;
import com.connectedu.chat.dto.ChatEventDto;
import com.connectedu.chat.dto.ChatResponse;
import com.connectedu.chat.engine.Intent;
import com.connectedu.chat.entity.ChatMessage;
import com.connectedu.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final IntentService intentService;
    private final EventClient eventClient;
    private final ClubClient clubClient;
    private final ChatMessageRepository chatRepository;

    public ChatResponse process(String message, Long userId) {

        // 1. SAVE USER MESSAGE
        if (userId != null) {
            chatRepository.save(ChatMessage.builder()
                    .userId(userId)
                    .message(message)
                    .sender("USER")
                    .build());
        }

        Intent intent = intentService.detectIntent(message);
        String lowerMsg = message.toLowerCase();

        // Initialize response variable
        ChatResponse response;

        // 2. GENERATE RESPONSE (Use 'response =' instead of 'return')
        switch (intent) {

            case GET_MY_REGISTRATIONS:
                if (userId == null) {
                    response = new ChatResponse("🔒 Please log in to view your registrations.");
                } else {
                    List<ChatEventDto> myEvents = eventClient.getMyRegistrations(userId);
                    response = formatEvents(myEvents, "🎫 **Events you participate in:**");
                }
                break; // Exit switch, go to save logic

            case GET_CLUB_SPECIFIC_EVENTS:
                List<ChatEventDto> allEvents = eventClient.getPastEvents();
                allEvents.addAll(eventClient.getUpcomingEvents());
                List<ChatEventDto> clubEvents = allEvents.stream()
                        .filter(e -> e.getClubName() != null && lowerMsg.contains(e.getClubName().toLowerCase()))
                        .collect(Collectors.toList());
                response = formatEvents(clubEvents, "📅 Events for your requested club:");
                break;

            case SEARCH_EVENTS_BY_YEAR:
                String year = intentService.extractYear(message);
                List<ChatEventDto> yearEvents = eventClient.getPastEvents().stream()
                        .filter(e -> e.getDate().startsWith(year))
                        .collect(Collectors.toList());
                yearEvents.addAll(eventClient.getUpcomingEvents().stream()
                        .filter(e -> e.getDate().startsWith(year))
                        .collect(Collectors.toList()));
                response = formatEvents(yearEvents, "📅 Events in " + year + ":");
                break;

            case GREETING:
                response = new ChatResponse("👋 Hello! I am the ConnectEdu Assistant. Ask me about **events**, **clubs**, or **contacts**!");
                break;

            case GET_UPCOMING_EVENTS:
                List<ChatEventDto> upcoming = eventClient.getUpcomingEvents();
                List<ChatEventDto> filteredUpcoming = filterByMessage(upcoming, lowerMsg);
                if (filteredUpcoming.size() < upcoming.size()) {
                    response = formatEvents(filteredUpcoming, "📅 **Details for requested event:**");
                } else {
                    response = formatEvents(upcoming, "📅 **Upcoming Events**");
                }
                break;

            case GET_PAST_EVENTS:
                List<ChatEventDto> past = eventClient.getPastEvents();
                List<ChatEventDto> filteredPast = filterByMessage(past, lowerMsg);
                if (filteredPast.size() < past.size()) {
                    response = formatEvents(filteredPast, "📜 **Details for requested event:**");
                } else {
                    response = formatEvents(past, "📜 **Past Events**");
                }
                break;

            case GET_ALL_CLUBS:
                List<ChatClubDto> clubs = clubClient.getAllClubs();
                response = formatClubs(clubs);
                break;

            case GET_SPECIFIC_CONTACT_INFO:
                List<ChatEventDto> history = eventClient.getPastEvents();
                List<ChatEventDto> future = eventClient.getUpcomingEvents();
                history.addAll(future);
                ChatEventDto foundEvent = history.stream()
                        .filter(e -> lowerMsg.contains(e.getName().toLowerCase()))
                        .findFirst()
                        .orElse(null);

                if (foundEvent != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📞 **Contact Info for ").append(foundEvent.getName()).append("**\n\n");
                    if (foundEvent.getContactName1() != null) {
                        sb.append("👤 **").append(foundEvent.getContactName1()).append("**\n");
                        sb.append("📱 ").append(foundEvent.getContactPhone1()).append("\n\n");
                    }
                    if (foundEvent.getContactName2() != null && !foundEvent.getContactName2().isEmpty()) {
                        sb.append("👤 **").append(foundEvent.getContactName2()).append("**\n");
                        sb.append("📱 ").append(foundEvent.getContactPhone2());
                    }
                    response = new ChatResponse(sb.toString());
                } else {
                    response = new ChatResponse("⚠️ I couldn't find an event with that name. Please type the exact event name.");
                }
                break;

            default:
                response = new ChatResponse("🤔 I didn't quite catch that. Try asking:\n- 'Show upcoming events'\n- 'List active clubs'\n- 'How to contact admin?'");
                break;
        }

        // 3. SAVE BOT RESPONSE (Now reachable!)
        if (userId != null) {
            chatRepository.save(ChatMessage.builder()
                    .userId(userId)
                    .message(response.getMessage())
                    .sender("BOT")
                    .build());
        }

        // 4. Return Final Response
        return response;
    }

    public List<ChatMessage> getHistory(Long userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    // --- Formatters (Keep exactly as is) ---
    private ChatResponse formatEvents(List<ChatEventDto> events, String title) {
        if (events.isEmpty()) return new ChatResponse(title + "\n\n🚫 No events found matching your request.");

        StringBuilder sb = new StringBuilder(title + "\n\n");
        for (ChatEventDto e : events) {
            sb.append("────────────────\n")
                    .append("🔹 **").append(e.getName()).append("**\n")
                    .append("   🏢 Club: ").append(e.getClubName() != null ? e.getClubName() : "Unknown").append("\n")
                    .append("   🕒 ").append(e.getDate()).append(" • ").append(e.getTime()).append("\n")
                    .append("   📍 ").append(e.getLocation()).append("\n");
        }
        sb.append("────────────────");
        return new ChatResponse(sb.toString());
    }

    private ChatResponse formatClubs(List<ChatClubDto> clubs) {
        if (clubs.isEmpty()) return new ChatResponse("🛡️ **Clubs**\n\nNo active clubs found.");
        StringBuilder sb = new StringBuilder("🛡️ **Active Clubs**\n\n");
        for (ChatClubDto c : clubs) {
            sb.append("• **").append(c.getName()).append("**\n").append("  ").append(c.getDescription()).append("\n\n");
        }
        return new ChatResponse(sb.toString());
    }

    private List<ChatEventDto> filterByMessage(List<ChatEventDto> events, String message) {
        List<ChatEventDto> filtered = events.stream()
                .filter(e -> message.contains(e.getName().toLowerCase()))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? events : filtered;
    }
}