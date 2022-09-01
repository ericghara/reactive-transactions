package org.ericgha.dbtransactions.service;

import io.r2dbc.spi.ConnectionFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.entity.LinkEntity;
import org.ericgha.dbtransactions.repository.jooq.ATableJooq;
import org.ericgha.dbtransactions.repository.jooq.BTableJooq;
import org.ericgha.dbtransactions.repository.jooq.LinkTableJooq;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JooqService implements DbService {

    private final ATableJooq aTable;
    private final BTableJooq bTable;
    private final LinkTableJooq linkTable;
    private final TransactionalOperator transactionalOperator;

    @Override
    @Transactional
    public Mono<Tuple2<ATableEntity, BTableEntity>> insertPair(ATableEntity tableA, BTableEntity tableB) {
        var aIns = aTable.insert( tableA );
        var bIns = bTable.insert( tableB );
        return Mono.zip(aIns, bIns);
    }

    @Override
    @Transactional
    public Mono<Void> deletePair(UUID a, UUID b) {
        var aDel = aTable.deleteById( a )
                .doOnNext( this::throwIfZero );
        var bDel = bTable.deleteById( b )
                .doOnNext( this::throwIfZero );
        return Mono.zip(aDel, bDel).then();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ATableEntity> fetch(ATableEntity tableA) {
        return aTable.fetchById( tableA.getId() );
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BTableEntity> fetch(BTableEntity tableB) {
        return bTable.fetchById( tableB.getId() );
    }

    @Transactional(readOnly = true)
    public Mono<LinkEntity> fetchLink(UUID idA, UUID idB) {
        return linkTable.fetchBy( idA, idB);
    }

    @Transactional
    public Mono<Long> deleteLink(UUID idA, UUID idB) throws IllegalStateException {
        return linkTable.delete( idA, idB).doOnNext( this::throwIfZero );
    }

    @Transactional
    public Mono<LinkEntity> insertLink(UUID idA, UUID idB) throws IllegalStateException {
        return linkTable.insert( idA, idB ).doOnNext( this::throwIfNull );
    }

    private void throwIfZero(Long num) throws IllegalStateException {
        if (num.equals(0L) ) {
            throw new IllegalStateException("Failure to modify");
        }
    }

    private void throwIfNull(Object o) throws IllegalStateException {
        if (Objects.isNull(o) ) {
            throw new IllegalStateException("Failure to modify");
        }
    }
}
