package com.grosso.chat.config;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.filters.SetCharacterEncodingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Configuration class for WebSocket messaging
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${frontend.url}")
    private String callerHost;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .setAllowedOrigins(callerHost)
                .withSockJS();
    }

    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
                .setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic")
                .setTaskScheduler(heartBeatScheduler())
                .setHeartbeatValue(new long[] {10000L, 10000L});
    }

    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.initialize();

        return scheduler;
    }
}
