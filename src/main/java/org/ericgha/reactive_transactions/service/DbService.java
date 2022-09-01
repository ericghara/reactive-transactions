package org.ericgha.reactive_transactions.service;

import org.ericgha.reactive_transactions.entity.ATableEntity;
import org.ericgha.reactive_transactions.entity.BTableEntity;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;

public interface DbService {


    Mono<Tuple2<ATableEntity, BTableEntity>> insertPair(ATableEntity tableA, BTableEntity tableB);

    Mono<Void> deletePair(UUID a, UUID b);

    Mono<ATableEntity> fetch(ATableEntity tableA);

    Mono<BTableEntity> fetch(BTableEntity tableB);

}
