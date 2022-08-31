package org.ericgha.dbtransactions.service;

import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;

public interface DbInterface {


    Mono<Tuple2<ATableEntity, BTableEntity>> createPair(ATableEntity tableA, BTableEntity tableB);

    Mono<Void> deletePair(UUID a, UUID b);

    Mono<ATableEntity> fetch(ATableEntity tableA);

    Mono<BTableEntity> fetch(BTableEntity tableB);

}
