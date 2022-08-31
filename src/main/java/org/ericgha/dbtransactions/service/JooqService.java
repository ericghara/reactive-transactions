package org.ericgha.dbtransactions.service;

import io.r2dbc.spi.ConnectionFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.repository.ATableJooq;
import org.ericgha.dbtransactions.repository.BTableJooq;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JooqService implements DbInterface {

    private final ATableJooq aTable;
    private final BTableJooq bTable;
    private final DSLContext dsl;
    private final ConnectionFactory cfi;
    private final ReactiveTransactionManager transactionManager;

    @Override
    @Transactional
    public Mono<Tuple2<ATableEntity, BTableEntity>> createPair(ATableEntity tableA, BTableEntity tableB) {
        Function<DSLContext, Mono<Tuple2<ATableEntity, BTableEntity>>> ins = trxDsl -> {
            var aIns = aTable.insert( tableA, trxDsl );
            var bIns = bTable.insert( tableB, trxDsl  );
            return Mono.zip(aIns, bIns);
        };
        return this.transact( ins );
    }

    @Override
    @Transactional
    public Mono<Void> deletePair(UUID a, UUID b) {
        Function<DSLContext, Mono<Void>> del = trxDsl -> {
            var aDel = aTable.deleteById( a, trxDsl )
                    .doOnNext( this::throwIfZero );
            var bDel = bTable.deleteById( b, trxDsl )
                    .doOnNext( this::throwIfZero );
            return Mono.zip(aDel, bDel).then();
        };
        return Mono.from( this.transact( del ) );
    }

    @Override
    public Mono<ATableEntity> fetch(ATableEntity tableA) {
        return aTable.fetchById( tableA.getId(), dsl );
    }

    @Override
    public Mono<BTableEntity> fetch(BTableEntity tableB) {
        return bTable.fetchById( tableB.getId(), dsl );
    }

    <U> Mono<U> transact(@NonNull Function<DSLContext, Mono<U>> function) {
        Mono<DSLContext> connection = ConnectionFactoryUtils.getConnection( cfi ).map(DSL::using);

        return Mono.from( connection.flatMap(function::apply) );
    }

    <U> Mono<U> transact2(@NonNull Function<DSLContext, Mono<U>> function) {
        TransactionalOperator transOperator = TransactionalOperator.create(transactionManager);
        return transOperator.transactional( function.apply(dsl) );
    }

    private void throwIfZero(Long num) throws IllegalArgumentException {
        if (num.equals(0L) ) {
            throw new IllegalStateException("Failure to delete");
        }
    }
}
