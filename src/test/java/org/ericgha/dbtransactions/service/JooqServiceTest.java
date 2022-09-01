package org.ericgha.dbtransactions.service;

import io.r2dbc.spi.ConnectionFactory;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.entity.LinkEntity;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JooqServiceTest {
    
    @Autowired
    private JooqService jooqService;
    
    @Autowired
    private DSLContext dsl;

    @Value("classpath:/schema.sql")
    private Resource resource;

    @BeforeEach
    void before() throws IOException, RuntimeException {
        assertTrue(this.resource.exists(), "Schema not found" );
        String sql = Files.readString( this.resource.getFile().toPath() );
        Mono.from(dsl.query( sql ) ).block( Duration.ofMillis( 500 ) );
    }

    @Test
    void createPairCommits() {
        var tableAEntity = new ATableEntity( UUID.randomUUID() );
        var tableBEntity = new BTableEntity( UUID.randomUUID() );
        var insert = jooqService.insertPair( tableAEntity, tableBEntity );
        StepVerifier.create(insert).expectNextCount( 1 )
                .verifyComplete();
        StepVerifier.create(jooqService.fetch( tableAEntity ) ).expectNext( tableAEntity ).verifyComplete();
        StepVerifier.create(jooqService.fetch(tableBEntity)).expectNext( tableBEntity ).verifyComplete();
    }

    @Test
    void createPairRollsBack() {
        var tableAEntity0 = new ATableEntity( UUID.randomUUID() );
        var tableBEntity = new BTableEntity( UUID.randomUUID() );
        var insert = jooqService.insertPair( tableAEntity0, tableBEntity ).block();
        var tableAEntity1 = new ATableEntity( UUID.randomUUID() );
        // tableBEntity is duplicate, so should throw
        var insert2 = jooqService.insertPair( tableAEntity1, tableBEntity );
        StepVerifier.create( insert2 ).verifyError( DataAccessException.class );
        // transaction should be rolled back
        StepVerifier.create( jooqService.fetch(tableAEntity1) ).expectNextCount( 0 ).verifyComplete();
    }

    @Test
    void deletePairCommits() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        var insert = jooqService.insertPair( tableAEntity, tableBEntity ).doOnNext( tup2 -> {
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
        var insert = jooqService.insertPair( tableAEntity, tableBEntity ).doOnNext( tup2 -> {
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
    void insertLinkCommits() {
        var aEntity = new ATableEntity(UUID.randomUUID() );
        var bEntity = new BTableEntity(UUID.randomUUID() );
        var insert = jooqService.insertPair(aEntity, bEntity)
                .then(jooqService.insertLink( aEntity.getId(), bEntity.getId() ) );
        var expected = new LinkEntity(aEntity.getId(), bEntity.getId() );
        StepVerifier.create( insert ).expectNext( expected ).verifyComplete();
        StepVerifier.create( jooqService.fetch( aEntity ) ).expectNextCount( 1 ).verifyComplete();
        StepVerifier.create( jooqService.fetch( bEntity ) ).expectNextCount( 1 ).verifyComplete();
    }

    @Test
    // can't annotate as @Transacitonal, so using operator
    void insertLinkRollsBackPriorInserts(@Autowired TransactionalOperator rxtx) {
        var aEntity = new ATableEntity(UUID.randomUUID() );
        var bEntity = new BTableEntity(UUID.randomUUID() );
        var insert = jooqService.insertPair(aEntity, bEntity)
                // will fail
                .flatMap(x -> jooqService.insertLink( aEntity.getId(), UUID.randomUUID() ) )
                .as(rxtx::transactional);
        insert.as(StepVerifier::create).verifyError(DataAccessException.class);
        jooqService.fetch( aEntity ).as(StepVerifier::create).expectNextCount( 0 ).verifyComplete();
        jooqService.fetch( bEntity ).as(StepVerifier::create).expectNextCount( 0 ).verifyComplete();
    }
}