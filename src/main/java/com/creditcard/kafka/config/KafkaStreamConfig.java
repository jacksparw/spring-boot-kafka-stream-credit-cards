package com.creditcard.kafka.config;

import com.creditcard.kafka.aggregate.CreditCard;
import com.creditcard.kafka.events.DomainEvent;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {

    @Value("${kafka.credit-card.snapshot.topic}")
    private String SNAPSHOTS_FOR_CARDS_TOPIC;

    @Value("${kafka.bootstrap.server}")
    private String BROKER_URL;

    @Value("${spring.application.name}")
    private String APPLICATION_NAME;

    @Value("${kafka.credit-card.input.topic}")
    private String CREDIT_CARDS_EVENTS_TOPIC;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration streamsConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        return new KafkaStreamsConfiguration(config);
    }

    @Bean
    KTable<String, CreditCard> kTable(StreamsBuilder builder) {
        Serde<DomainEvent> domainEventSerde = new JsonSerde<>(DomainEvent.class);
        Serde<CreditCard> creditCardSerde = new JsonSerde<>(CreditCard.class);

        return
                builder
                        .stream(CREDIT_CARDS_EVENTS_TOPIC, Consumed.with(Serdes.String(), domainEventSerde))
                        .groupBy((uuid , event) -> uuid, Grouped.with(Serdes.String(), domainEventSerde))
                        .aggregate(CreditCard::new,
                                (uuid, domainEvent, aggregateCreditCard) -> aggregateCreditCard.handle(domainEvent),
                                Materialized.<String, CreditCard, KeyValueStore<Bytes, byte[]>>
                                        as(SNAPSHOTS_FOR_CARDS_TOPIC)
                                        .withValueSerde(creditCardSerde)
                                        .withKeySerde(Serdes.String()));
    }
}
