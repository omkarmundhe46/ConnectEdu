package com.campusconnect.notificationservice.service;

import com.campusconnect.notificationservice.dto.*;
import com.campusconnect.notificationservice.entity.NotificationLog;
import com.campusconnect.notificationservice.entity.NotificationRequest;
import com.campusconnect.notificationservice.exception.UserNotFoundException;
import com.campusconnect.notificationservice.feign.UserClient;
import com.campusconnect.notificationservice.feign.ClubClient;
import com.campusconnect.notificationservice.feign.EventClient;
import com.campusconnect.notificationservice.repository.NotificationLogRepository;
import com.campusconnect.notificationservice.repository.NotificationRequestRepository;
import feign.FeignException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final EmailSenderService emailSenderService;
	private final NotificationLogRepository logRepository;
	private final NotificationRequestRepository requestRepository;
	private final UserClient userClient;
	private final ClubClient clubClient;
	private final EventClient eventClient;

	private final JavaMailSender mailSender;
	/**
	 * Notify when a user registers
	 */
	public NotificationResponse notifyUserRegistered(UserRegisteredRequest request) {
		if (request.getRequestId() != null && requestRepository.existsByRequestId(request.getRequestId())) {
			NotificationRequest existing = requestRepository.findByRequestId(request.getRequestId()).get();
			NotificationResponse response = new NotificationResponse();
			response.setDetails("notifications already processed");
			response.setEnqueueCount(existing.getProcessedCount());
			return response;
		}

		try {

			Map<String, Object> variables = new HashMap<>();
			variables.put("name", request.getName());   // Use name from the request
			variables.put("email", request.getEmail()); // Use email from the request

			sendNotificationAsync("WELCOME_USER", request.getEmail(), variables, request.getUserId());


			if (request.getRequestId() != null) {
				NotificationRequest notificationRequest = new NotificationRequest();
				notificationRequest.setRequestId(request.getRequestId());
				notificationRequest.setNotificationType("USER_REGISTERED");
				notificationRequest.setProcessedCount(1);
				notificationRequest.setUserId(request.getUserId());
				notificationRequest.setCreatedAt(LocalDateTime.now());
				requestRepository.save(notificationRequest);
			}

			NotificationResponse response = new NotificationResponse();
			response.setDetails("notifications enqueued");
			response.setEnqueueCount(1);
			return response;

		} catch (FeignException.NotFound e) {
            // The FeignException will no longer happen, but we can keep a generic catch block.
			log.error("Failed to process user registration notification for user {}: {}", request.getUserId(), e.getMessage());
			throw new RuntimeException("Failed to process notification", e);		}
	}

	public NotificationResponse notifyEventCreated(EventResponseDto eventDto) {
		String requestId = "event-created-" + eventDto.getId();

		if (requestRepository.existsByRequestId(requestId)) {
			NotificationRequest existing = requestRepository.findByRequestId(requestId).get();
			NotificationResponse response = new NotificationResponse();
			response.setDetails("notifications already processed");
			response.setEnqueueCount(existing.getProcessedCount());
			return response;
		}

		try {
			List<ClubMemberDto> members = clubClient.getClubMembers(eventDto.getClubId());
			int count = 0;

			for (ClubMemberDto member : members) {
				try {
					UserDto user = userClient.getUser(member.getUserId());
					Map<String, Object> variables = new HashMap<>();
					variables.put("name", user.getName());
					variables.put("eventTitle", eventDto.getName());
					variables.put("eventDate", eventDto.getDate() != null ? eventDto.getDate().toString() : "TBD");
					variables.put("location", eventDto.getLocation());
					variables.put("clubName", "Club");

					sendNotificationAsync("EVENT_CREATED", user.getEmail(), variables, user.getId());
					count++;
				} catch (FeignException.NotFound e) {
					log.warn("User not found for member: {}", member.getUserId());
				}
			}

			NotificationRequest notificationRequest = new NotificationRequest();
			notificationRequest.setRequestId(requestId);
			notificationRequest.setNotificationType("EVENT_CREATED");
			notificationRequest.setProcessedCount(count);
			notificationRequest.setCreatedAt(LocalDateTime.now());
			requestRepository.save(notificationRequest);

			NotificationResponse response = new NotificationResponse();
			response.setDetails("notifications enqueued");
			response.setEnqueueCount(count);
			return response;

		} catch (FeignException.NotFound e) {
			throw new RuntimeException("Club not found with id: " + eventDto.getClubId());
		}
	}

	/**
	 * Notify a user when they participate in an event
	 */

	public NotificationResponse notifyEventParticipation(ParticipantRegisteredEvent request) {

		log.info("Received participation notification request: userId={}, eventId={}", request.getUserId(),
				request.getEventId());
		// Check if notification already sent
		boolean alreadySent = requestRepository.existsByNotificationTypeAndUserIdAndEventId("EVENT_PARTICIPATION",
				request.getUserId(), request.getEventId());

		if (alreadySent) {
			NotificationResponse response = new NotificationResponse();
			response.setDetails("participation notification already sent");
			response.setEnqueueCount(1);
			return response;
		}

		try {
			// Fetch user
			UserDto user = userClient.getUser(request.getUserId());
			log.info("Preparing participation email for user: {}", user.getEmail());

			EventResponseDto eventDetails = eventClient.getEventById(request.getEventId());
			log.info("Fetched event details: {}", eventDetails.getName());

			String subject = "You are registered for " + eventDetails.getName();

			String body = "<h1>Hello " + user.getName() + ",</h1>" + "<p>You have successfully registered for <b>"
					+ eventDetails.getName() + "</b>.</p>" + "<p><b>Event Details:</b></p>" + "<ul>"
					+ "<li><b>Date:</b> "
					+ (eventDetails.getDate() != null
							? eventDetails.getDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
							: "TBD")
					+ "</li>" + "<li><b>Location:</b> " + eventDetails.getLocation() + "</li>" + "</ul>"
					+ "<p>We look forward to seeing you 🎉</p>";

			// Save email log
			NotificationLog logEntry = new NotificationLog();
			logEntry.setUserId(user.getId());
			logEntry.setToEmail(user.getEmail());
			logEntry.setSubject(subject);
			logEntry.setBody(body);

			NotificationLog savedLog = logRepository.save(logEntry);


			emailSenderService.sendEmailAsync(savedLog);
			log.info("✅ Participation email sent to {}", user.getEmail());

			// Save notification record
			NotificationRequest notificationRequest = new NotificationRequest();
			notificationRequest.setUserId(user.getId());
			notificationRequest.setEventId(request.getEventId());
			notificationRequest.setCreatedAt(LocalDateTime.now());
			notificationRequest.setNotificationType("EVENT_PARTICIPATION");
			notificationRequest.setRequestId(UUID.randomUUID().toString()); // unique for this user+event
			notificationRequest.setProcessedCount(1);

			requestRepository.save(notificationRequest);

			NotificationResponse response = new NotificationResponse();
			response.setDetails("participation notification enqueued");
			response.setEnqueueCount(1);
			return response;

		} catch (FeignException.NotFound e) {
			throw new UserNotFoundException("User not found with id: " + request.getUserId());
		}
	}

	public NotificationResponse notifyClubMemberAdded(ClubMemberAddedRequest request) {
		String requestId = "club-member-added-" + request.getUserId() + "-" + request.getClubId();

		if (requestRepository.existsByRequestId(requestId)) {
			NotificationResponse response = new NotificationResponse();
			response.setDetails("club member notification already sent");
			response.setEnqueueCount(1);
			return response;
		}

		try {

			String subject = "🎉 Welcome to " + request.getClubName();
			String body = "<h2>Hello " + request.getUserName() + ",</h2>"
					+ "<p>Congratulations! You are now a member of <b>" + request.getClubName() + "</b>.</p>"
					+ "<p>Your role: <b>" + request.getRole() + "</b></p>"
					+ "<p>We’re excited to have you onboard 🚀</p>";

			NotificationLog logEntry = new NotificationLog();
			logEntry.setUserId(request.getUserId());

			logEntry.setToEmail(request.getUserEmail());
			logEntry.setSubject(subject);
			logEntry.setBody(body);
			NotificationLog savedLog = logRepository.save(logEntry);

			emailSenderService.sendEmailAsync(savedLog);

			NotificationRequest notificationRequest = new NotificationRequest();
			notificationRequest.setRequestId(requestId);
			notificationRequest.setNotificationType("CLUB_MEMBER_ADDED");
			notificationRequest.setUserId(request.getUserId());
			notificationRequest.setProcessedCount(1);
			notificationRequest.setCreatedAt(LocalDateTime.now());
			requestRepository.save(notificationRequest);

			NotificationResponse response = new NotificationResponse();
			response.setDetails("club member notification enqueued from kafka message");
			response.setEnqueueCount(1);
			return response;

		} catch (Exception e) {
			log.error("Failed to process club member notification for user {}: {}", request.getUserId(), e.getMessage());
			throw new RuntimeException("Failed to process notification", e);
		}
	}
	
	
	
	public void sendCertificateEmail(CertificateNotificationRequest request) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(request.getUserEmail());
            helper.setSubject("Your Certificate — " + request.getEventName());
            String text = "Dear " + request.getUserName() + ",\n\n" +
                    "Please find attached your certificate for the event \"" + request.getEventName() + "\" held on " + request.getEventDate() + ".\n\n" +
                    "Regards,\nCampus Club";
            helper.setText(text);

            // Attach the PDF
            ByteArrayResource pdfResource = new ByteArrayResource(request.getPdfBytes());
            helper.addAttachment("Certificate_" + request.getEventName().replaceAll("\\s+", "_") + ".pdf", pdfResource);

            mailSender.send(mimeMessage);
            log.info("Certificate email sent to {}", request.getUserEmail());
        } catch (MessagingException e) {
            log.error("Failed to send certificate email to {}: {}", request.getUserEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

	@Async
	public CompletableFuture<Void> sendNotificationAsync(String templateCode, String toEmail,
			Map<String, Object> variables, Long userId) {
		log.info("Sending notification to email: {}", toEmail);
		try {

			String subject;
			String body;

			switch (templateCode) {
				case "WELCOME_USER":
					subject = "Welcome to ConnectEdu, " + variables.getOrDefault("name", "User") + "!";
					body = "<h1>Hello " + variables.getOrDefault("name", "User") + ",</h1>"
							+ "<p>You have successfully registered for ConnectEdu. Welcome! 🎉</p>";
					break;

			case "EVENT_CREATED":
				subject = "🎉 Congratulations! " + variables.getOrDefault("eventTitle", "An event")
						+ " has been created under " + variables.getOrDefault("clubName", "your club");
				body = "<h2>Hello " + variables.getOrDefault("name", "Member") + ",</h2>"
						+ "<p>A new event has been created under " + variables.getOrDefault("clubName", "your club")
						+ ".</p>" + "<ul>" + "<li><b>Event:</b> " + variables.getOrDefault("eventTitle", "N/A")
						+ "</li>" + "<li><b>Date:</b> " + variables.getOrDefault("eventDate", "TBD") + "</li>"
						+ "<li><b>Location:</b> " + variables.getOrDefault("location", "TBD") + "</li>" + "</ul>"
						+ "<p>Don’t miss out!</p>";
				break;

			default:
				subject = "CampusConnect Notification";
				body = "<p>Hello " + variables.getOrDefault("name", "User") + ",</p>"
						+ "<p>You have a new notification from CampusConnect.</p>";
			}

			NotificationLog logEntry = new NotificationLog();
			logEntry.setTemplateId(null);
			logEntry.setUserId(userId);
			logEntry.setToEmail(toEmail);
			logEntry.setSubject(subject);
			logEntry.setBody(body);

			NotificationLog savedLog = logRepository.save(logEntry);

			emailSenderService.sendEmailAsync(savedLog);
			log.info("✅ Email enqueued for {}", toEmail);

		} catch (Exception e) {
			log.error("❌ Failed to send notification: {}", e.getMessage(), e);
		}

		return CompletableFuture.completedFuture(null);
	}

	public void sendVerificationEmail(EmailVerificationRequest request) {
		log.info("Preparing verification email for: {}", request.getEmail());

		try {
			String subject = "Your ConnectEdu Verification Code";
			String body = "<h1>Hello " + request.getName() + ",</h1>"
					+ "<p>Thank you for registering with ConnectEdu. Your verification code is:</p>"
					+ "<h2 style='color: #4A55A2;'>" + request.getOtp() + "</h2>"
					+ "<p>This code is valid for 10 minutes.</p>"
					+ "<p>If you did not request this, please ignore this email.</p>";

			NotificationLog logEntry = new NotificationLog();
			logEntry.setToEmail(request.getEmail());
			logEntry.setSubject(subject);
			logEntry.setBody(body);

			NotificationLog savedLog = logRepository.save(logEntry);

			emailSenderService.sendEmailAsync(savedLog);
			log.info("✅ Verification email enqueued for {}", request.getEmail());

		} catch (Exception e) {
			log.error("❌ Failed to send verification email: {}", e.getMessage(), e);
		}
	}

	public List<NotificationLogResponseDto> getNotificationsForUser(Long userId) {
		log.info("Fetching notifications for user {}", userId);
		return logRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	private NotificationLogResponseDto mapToDto(NotificationLog log) {
		return NotificationLogResponseDto.builder()
				.id(log.getId())
				.subject(log.getSubject())
				.body(log.getBody())
				.createdAt(log.getCreatedAt())
				.build();
	}
}
