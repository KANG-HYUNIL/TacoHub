package com.example.TacoHub.Config.RabbitMQConfig;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 기본 설정
 * Exchange, Queue, Binding 및 메시지 컨버터 설정
 */
@Configuration
public class RabbitMqConfig {

    // YML에서 설정값 주입
    @Value("${rabbitmq.exchanges.collaboration}")
    private String collaborationExchange;
    
    @Value("${rabbitmq.exchanges.notification}")
    private String notificationExchange;

    @Value("${rabbitmq.dlq.exchange}")
    private String dlqExchange;
    
    @Value("${rabbitmq.queues.block-updates}")
    private String blockUpdatesQueue;
    
    @Value("${rabbitmq.queues.page-updates}")
    private String pageUpdatesQueue;
    
    @Value("${rabbitmq.queues.workspace-updates}")
    private String workspaceUpdatesQueue;

    @Value("${rabbitmq.queues.api-server-shared}")
    private String apiServerSharedQueue;

    @Value(("${rabbitmq.dlq.queues.api-server-shared-dlq}"))
    private String apiServerSharedDlqQueue;

    @Value("${rabbitmq.routing-keys.block}")
    private String routingKeysBlock;

    @Value("${rabbitmq.routing-keys.page}")
    private String routingKeysPage;

    @Value("${rabbitmq.routing-keys.workspace}")
    private String routingKeysWorkspace;

    @Value("${rabbitmq.routing-keys.api}")
    private String routingKeysApi;

    @Value("${rabbitmq.dlq.routing-keys.dlq-api}")
    private String routingKeysDlqApi;

    // ========================================
    // Exchange 설정
    // ========================================
    
    /**
     * 협업 관련 메시지 Exchange (Topic 타입)
     */
    @Bean
    public TopicExchange collaborationExchange() {
        return ExchangeBuilder
                .topicExchange(collaborationExchange)
                .durable(true)
                .build();
    }
    
    /**
     * 알림 관련 메시지 Exchange (Topic 타입)
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder
                .topicExchange(notificationExchange)
                .durable(true)
                .build();
    }

    /**
     * DLQ (Dead Letter Queue) Exchange
     * @return
     */
    @Bean
    public TopicExchange dlqExchange()
    {
        return ExchangeBuilder
                .topicExchange(dlqExchange)
                .durable(true)
                .build();
    }

    // ========================================
    // Queue 설정
    // ========================================
    
    /**
     * 블록 업데이트 Queue
     */
    @Bean
    public Queue blockUpdatesQueue() {
        return QueueBuilder
                .durable(blockUpdatesQueue)
                .build();
    }
    
    /**
     * 페이지 업데이트 Queue
     */
    @Bean
    public Queue pageUpdatesQueue() {
        return QueueBuilder
                .durable(pageUpdatesQueue)
                .build();
    }
    
    /**
     * 워크스페이스 업데이트 Queue
     */
    @Bean
    public Queue workspaceUpdatesQueue() {
        return QueueBuilder
                .durable(workspaceUpdatesQueue)
                .build();
    }

    


    /**
     * DLQ (Dead Letter Queue) 설정
     * 
     */
    @Bean
    public Queue dqlQueue()
    {
        return QueueBuilder
                .durable(apiServerSharedDlqQueue)
                .build();
    }

    // ========================================
    // Binding 설정 (Queue와 Exchange 연결)
    // ========================================
    
    /**
     * 블록 관련 메시지 라우팅 바인딩
     */
    @Bean
    public Binding blockUpdatesBinding() {
        return BindingBuilder
                .bind(blockUpdatesQueue())
                .to(collaborationExchange())
                .with(routingKeysBlock);
    }
    
    /**
     * 페이지 관련 메시지 라우팅 바인딩
     */
    @Bean
    public Binding pageUpdatesBinding() {
        return BindingBuilder
                .bind(pageUpdatesQueue())
                .to(collaborationExchange())
                .with(routingKeysPage);
    }
    
    /**
     * 워크스페이스 관련 메시지 라우팅 바인딩
     */
    @Bean
    public Binding workspaceUpdatesBinding() {
        return BindingBuilder
                .bind(workspaceUpdatesQueue())
                .to(collaborationExchange())
                .with(routingKeysWorkspace);
    }


    /**
     * API Server 전용 DLQ 바인딩
     * @return
     */
    @Bean
    public Binding apiServerSharedDlqBinding() {
        return BindingBuilder
                .bind(dqlQueue())
                .to(dlqExchange())
                .with(routingKeysDlqApi);
    }

    // ========================================
    // RabbitTemplate 설정
    // ========================================
    
    /**
     * JSON 메시지 컨버터
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate 설정 (메시지 전송용)
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // 기본 Exchange 설정
        template.setExchange(collaborationExchange);
        
        // Publisher Confirms 활성화 (메시지 전송 확인)
        template.setMandatory(true);
        
        return template;
    }
    
    // ========================================
    // 동적 Queue 관리 설정
    // ========================================
    
    /**
     * Queue 자동 선언을 위한 RabbitAdmin
     * 런타임에 Queue 생성/확인 담당
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
    
    /**
     * API 서버용 공용 Queue 자동 생성/연결
     * 존재하지 않으면 생성, 존재하면 연결
     */
    @Bean
    public Queue apiServerQueue(RabbitAdmin rabbitAdmin) {
        
        // Queue 선언 (존재하지 않으면 생성, 존재하면 무시)
        Queue queue = QueueBuilder
                .durable(apiServerSharedQueue)
                .withArgument("x-message-ttl", 300000) // 5분 TTL
                .withArgument("x-dead-letter-exchange", dlqExchange) // DLQ exchange 설정
                .withArgument("x-dead-letter-routing-key", routingKeysDlqApi) // DLQ routing key 설정
                .build();
                

        
        return queue;
    }
    
    /**
     * API 서버 Queue와 Collaboration Exchange 바인딩
     */
    @Bean
    public Binding apiServerBinding(RabbitAdmin rabbitAdmin) {
        Binding binding = BindingBuilder
                .bind(apiServerQueue(rabbitAdmin))
                .to(collaborationExchange())
                .with(routingKeysApi);
                

        
        return binding;
    }
}
