package com.pedidos360.notificaciones.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración centralizada de RabbitMQ para Pedidos360 Notificaciones API.
 * Declara la infraestructura de mensajería (Colas, DLQ, Exchanges y Bindings)
 * con soporte para confirmaciones manuales (ACK/NACK) y conversor JSON.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.rabbitmq.exchange.pedidos}")
    private String pedidosExchangeName;

    @Value("${pedidos360.rabbitmq.exchange.dlx}")
    private String dlxExchangeName;

    @Value("${pedidos360.rabbitmq.queue.notificaciones}")
    private String notificacionesQueueName;

    @Value("${pedidos360.rabbitmq.queue.notificaciones-dlq}")
    private String notificacionesDlqName;

    @Value("${pedidos360.rabbitmq.routing-key.creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${pedidos360.rabbitmq.routing-key.notificaciones-dlq}")
    private String notificacionesDlqRoutingKey;

    // 1. Exchange Principal (TopicExchange para eventos de pedidos)
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(pedidosExchangeName, true, false);
    }

    // 2. Exchange DLX (DirectExchange para direccionamiento a DLQ)
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // 3. Cola de Notificaciones configurada con Dead Letter Exchange
    @Bean
    public Queue notificacionesQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", notificacionesDlqRoutingKey);

        return QueueBuilder.durable(notificacionesQueueName)
                .withArguments(args)
                .build();
    }

    // 4. Cola DLQ para Notificaciones con mensajes fallidos
    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(notificacionesDlqName).build();
    }

    // 5. Binding entre TopicExchange y Cola de Notificaciones
    @Bean
    public Binding bindingNotificaciones() {
        return BindingBuilder
                .bind(notificacionesQueue())
                .to(pedidosExchange())
                .with(pedidoCreadoRoutingKey);
    }

    // 6. Binding entre Dead Letter Exchange y Cola DLQ de Notificaciones
    @Bean
    public Binding bindingNotificacionesDlq() {
        return BindingBuilder
                .bind(notificacionesDlq())
                .to(deadLetterExchange())
                .with(notificacionesDlqRoutingKey);
    }

    // 7. Conversor de mensajes JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // 8. Contenedor de listeners con soporte para confirmación MANUAL (ACK/NACK explícito)
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
