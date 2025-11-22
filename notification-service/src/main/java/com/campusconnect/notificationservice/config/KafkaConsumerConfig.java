package com.campusconnect.notificationservice.config;

import com.campusconnect.notificationservice.dto.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.campusconnect.notificationservice.dto.ParticipantRegisteredEvent; // Import new DTO

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // A generic configuration method for our consumers
    private <T> Map<String, Object> consumerConfigs(Class<T> trustedClass) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, trustedClass);
        return props;
    }

    // --- Factory for User Registered Messages ---
    @Bean
    public ConsumerFactory<String, UserRegisteredRequest> userRegisteredConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(UserRegisteredRequest.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredRequest> userRegisteredListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userRegisteredConsumerFactory());
        return factory;
    }

    // --- Factory for Certificate Messages ---
    @Bean
    public ConsumerFactory<String, CertificateNotificationRequest> certificateConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(CertificateNotificationRequest.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CertificateNotificationRequest> certificateListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CertificateNotificationRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(certificateConsumerFactory());
        return factory;
    }

    // --- ADD NEW FACTORY for Event Created Messages ---
    @Bean
    public ConsumerFactory<String, EventResponseDto> eventCreatedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(EventResponseDto.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventResponseDto> eventCreatedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventResponseDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventCreatedConsumerFactory());
        return factory;
    }

    // --- ADD NEW FACTORY for Club Member Added Messages ---
    @Bean
    public ConsumerFactory<String, ClubMemberAddedRequest> clubMemberAddedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(ClubMemberAddedRequest.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClubMemberAddedRequest> clubMemberAddedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClubMemberAddedRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clubMemberAddedConsumerFactory());
        return factory;
    }

    // --- ADD NEW FACTORY for Event Participation Messages ---
    @Bean
    public ConsumerFactory<String, EventParticipationDTO> eventParticipationConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(EventParticipationDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventParticipationDTO> eventParticipationListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventParticipationDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventParticipationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ParticipantRegisteredEvent> participantRegisteredConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(ParticipantRegisteredEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ParticipantRegisteredEvent> participantRegisteredListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ParticipantRegisteredEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(participantRegisteredConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, EmailVerificationRequest> emailVerificationConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(EmailVerificationRequest.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailVerificationRequest> emailVerificationListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmailVerificationRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailVerificationConsumerFactory());
        return factory;
    }
   //  For Forget Password
    @Bean
    public ConsumerFactory<String, PasswordResetEmailRequest> passwordResetConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(PasswordResetEmailRequest.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PasswordResetEmailRequest> passwordResetListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PasswordResetEmailRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(passwordResetConsumerFactory());
        return factory;
    }
}