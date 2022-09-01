package org.ericgha.dbtransactions.service;

import io.r2dbc.spi.ConnectionFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.entity.LinkEntity;
import org.ericgha.dbtransactions.repository.ATableJooq;
import org.ericgha.dbtransactions.repository.BTableJooq;
import org.ericgha.dbtransactions.repository.LinkTableJooq;
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
    private final DSLContext dsl;
    private final ConnectionFactory cfi;
    private final TransactionalOperator transactionalOperator;

    @Override
    @Transactional
    public Mono<Tuple2<ATableEntity, BTableEntity>> insertPair(ATableEntity tableA, BTableEntity tableB) {
        Function<DSLContext, Mono<Tuple2<ATableEntity, BTableEntity>>> ins = trxDsl -> {
            var aIns = aTable.insert( tableA, trxDsl );
            var bIns = bTable.insert( tableB, trxDsl );
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
    @Transactional(readOnly = true)
    public Mono<ATableEntity> fetch(ATableEntity tableA) {
        return aTable.fetchById( tableA.getId(), dsl );
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BTableEntity> fetch(BTableEntity tableB) {
        return bTable.fetchById( tableB.getId(), dsl );
    }

    @Transactional(readOnly = true)
    public Mono<LinkEntity> fetchLink(UUID idA, UUID idB) {
        Function<DSLContext, Mono<LinkEntity>> fetch = trxDsl ->
                linkTable.fetchBy( idA, idB, trxDsl );
        return Mono.from(this.transact( fetch ) );
    }

    @Transactional
    public Mono<Long> deleteLink(UUID idA, UUID idB) throws IllegalStateException {
        Function<DSLContext, Mono<Long>> fetch = trxDsl ->
            linkTable.delete( idA, idB, trxDsl ).doOnNext( this::throwIfZero );
        return Mono.from(this.transact( fetch ) );
    }
    @Transactional
    public Mono<LinkEntity> insertLink(UUID idA, UUID idB) throws IllegalStateException {
        Function<DSLContext, Mono<LinkEntity>> ins = trxDsl ->
            linkTable.insert( idA, idB, trxDsl ).doOnNext( this::throwIfNull );
        return Mono.from(this.transact( ins ) );
    }

    <U> Mono<U> transact(@NonNull Function<DSLContext, Mono<U>> function) {
        Mono<DSLContext> connection = ConnectionFactoryUtils.getConnection( cfi ).map(DSL::using);

        return Mono.from( connection.flatMap(function::apply) );
    }

    <U> Mono<U> transact2(@NonNull Function<DSLContext, Mono<U>> function) {
        // This actually doesn't really work, it just allows multiple @Transacitonal annotated 
        // methods to be wrapped in a larger transaction without adding @Transacitonal to the method
        // so this is only really useful for testing where void method signature precludes method
        // as being recognised as a reactive transaction and instead becomes managed by PlatformTransactionManager
        // which doesn't exist in a reactive context
        var txDslPub = ConnectionFactoryUtils.getConnection( cfi ).map( DSL::using );
        return txDslPub.flatMap( txDsl-> transactionalOperator.transactional( function.apply(txDsl) ) );
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
