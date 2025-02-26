package com.app.backend.global.config;

import com.app.backend.domain.notification.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {
    // 어드민 - Producer
    @Bean
    public ProducerFactory<String, NotificationMessage> producerFactory() {
        // JSON Deserializer 설정
        JsonDeserializer<NotificationMessage> jsonDeserializer = new JsonDeserializer<>(NotificationMessage.class, false);
        jsonDeserializer.addTrustedPackages("*"); // 모든 패키지 신뢰 설정

    	// Producer 속성 설정
        Map<String, Object> props = new HashMap<>();
        // Kafka 서버 주소 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 메시지 키의 직렬화 방식 설정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);     
        // 메시지 값의 직렬화 방식 설정 (JSON)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);       

        return new DefaultKafkaProducerFactory<>(props);
    }

    // 회원 - Consumer
    // KafkaTemplate 설정 - Producer가 메시지를 보내는데 사용
    @Bean
    public KafkaTemplate<String, NotificationMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, NotificationMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Kafka 서버 주소
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); 
        // Consumer 그룹 ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        // Deserializer 설정
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 키 역직렬화
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // 값 역직렬화
        // 오프셋 설정 - 새로운 Consumer가 시작할 때 가장 최근 메시지부터 읽기
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // NotificationMessage 클래스의 패키지를 신뢰하도록 설정
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.app.backend.domain.notification.dto");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationMessage> kafkaListenerContainerFactory() {
        // Kafka Listener 컨테이너 팩토리 생성
        ConcurrentKafkaListenerContainerFactory<String, NotificationMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // Consumer 팩토리 설정
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
