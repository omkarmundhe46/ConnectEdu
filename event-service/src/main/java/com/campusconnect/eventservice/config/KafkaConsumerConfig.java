package com.campusconnect.eventservice.config;

import com.campusconnect.eventservice.dto.PaymentSuccessfulEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:event-group}")
    private String groupId;

    private <T> Map<String, Object> consumerConfigs(Class<T> trustedClass) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, trustedClass.getName());
        return props;
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessfulEvent> paymentSuccessConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(PaymentSuccessfulEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessfulEvent> paymentSuccessListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessfulEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentSuccessConsumerFactory());
        return factory;
    }
}