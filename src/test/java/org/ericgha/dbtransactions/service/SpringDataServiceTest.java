package org.ericgha.dbtransactions.service;

import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpringDataServiceTest {

    @Autowired
    SpringDataService domainService;

    @Autowired
    DatabaseClient databaseClient;

    @BeforeEach
    void before() {
        var delLink = databaseClient.inConnection( conn -> Mono.from( conn.createStatement("DELETE FROM link" ).execute() ) );
        var delA = databaseClient.inConnection( conn -> Mono.from( conn.createStatement("DELETE FROM a_table" ).execute() ) );
        var delb = databaseClient.inConnection( conn -> Mono.from( conn.createStatement("DELETE FROM b_table" ).execute() ) );
        Flux.concat(delLink, delA, delb).blockLast();
    }

    @Test
    void createPair() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        var insert = domainService.createPair( tableAEntity, tableBEntity );
        StepVerifier.create(insert).assertNext( tup2 -> {
            assertEquals(tableAEntity, tup2.getT1(), "A" );
            assertEquals( tableBEntity, tup2.getT2(), "B" );
        } ).verifyComplete();
        StepVerifier.create(domainService.fetch( tableAEntity ) ).expectNext( tableAEntity ).verifyComplete();
        StepVerifier.create(domainService.fetch(tableBEntity)).expectNext( tableBEntity ).verifyComplete();
    }

    @Test
    void deletePairCommits() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        domainService.createPair( tableAEntity, tableBEntity ).block();
        var del = domainService.deletePair( tableAEntity.getId(), tableBEntity.getId() );
        StepVerifier.create(del).verifyComplete();
    }

    @Test
    void deletePairRollsBack() {
        var tableAEntity = new ATableEntity( );
        var tableBEntity = new BTableEntity( );
        domainService.createPair( tableAEntity, tableBEntity ).block();
        var del = domainService.deletePair( tableAEntity.getId(), UUID.randomUUID() );
        StepVerifier.create(del).verifyError(IllegalStateException.class);
        // assert tableA got rolled back
        StepVerifier.create( domainService.fetch( tableAEntity ) ).expectNextCount( 1 ).verifyComplete();
    }

    @Test
    void fetch() {
    }

    @Test
    void testFetch() {
    }
}