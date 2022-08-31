package org.ericgha.dbtransactions.service;

import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JooqServiceTest {
    
    @Autowired
    private JooqService jooqService;
    
    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void before() {
        var delLink = Mono.from( dsl.query("DELETE FROM link;" ) );
        var delA = Mono.from( dsl.query("DELETE FROM a_table;" ) );
        var delB = Mono.from( dsl.query("DELETE FROM b_table;" ) );
        Flux.concat(delLink, delA, delB).blockLast();
    }

    @Test
    void createPairCommits() {
        var tableAEntity = new ATableEntity( UUID.randomUUID() );
        var tableBEntity = new BTableEntity( UUID.randomUUID() );
        var insert = jooqService.createPair( tableAEntity, tableBEntity );
        StepVerifier.create(insert).expectNextCount( 1 )
                .verifyComplete();
        StepVerifier.create(jooqService.fetch( tableAEntity ) ).expectNext( tableAEntity ).verifyComplete();
        StepVerifier.create(jooqService.fetch(tableBEntity)).expectNext( tableBEntity ).verifyComplete();
    }

    @Test
    void createPairRollsBack() {
        var tableAEntity0 = new ATableEntity( UUID.randomUUID() );
        var tableBEntity = new BTableEntity( UUID.randomUUID() );
        var insert = jooqService.createPair( tableAEntity0, tableBEntity ).block();
        var tableAEntity1 = new ATableEntity( UUID.randomUUID() );
        // tableBEntity is duplicate, so should throw
        var insert2 = jooqService.createPair( tableAEntity1, tableBEntity );
        StepVerifier.create( insert2 ).verifyError( DataAccessException.class );
        // transaction should be rolled back
        StepVerifier.create( jooqService.fetch(tableAEntity1) ).expectNextCount( 0 ).verifyComplete();
    }

    @Test
    void deletePairCommits() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        var insert = jooqService.createPair( tableAEntity, tableBEntity ).doOnNext(tup2 -> {
            // this is a hack...
            tableAEntity.setId(tup2.getT1().getId() );
            tableBEntity.setId( tup2.getT2().getId() );
        } ).block();
        var del = jooqService.deletePair( tableAEntity.getId(), tableBEntity.getId() );
        StepVerifier.create( del ).verifyComplete();
        // both deleted
        StepVerifier.create(jooqService.fetch( tableAEntity ) ).expectNextCount( 0 ).verifyComplete();
        StepVerifier.create(jooqService.fetch(tableBEntity)).expectNextCount(0).verifyComplete();
    }

    @Test
    void deletePairRollsBack() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        var insert = jooqService.createPair( tableAEntity, tableBEntity ).doOnNext(tup2 -> {
            // this is a hack...
            tableAEntity.setId(tup2.getT1().getId() );
            tableBEntity.setId( tup2.getT2().getId() );
        } ).block();
        var del = jooqService.deletePair( tableAEntity.getId(), UUID.randomUUID() );
        StepVerifier.create( del ).verifyError(IllegalStateException.class);
        // rolled back A
        StepVerifier.create(jooqService.fetch( tableAEntity ) ).expectNext( tableAEntity ).verifyComplete();
    }

    @Test
    void fetch() {
    }

    @Test
    void testFetch() {
    }
}