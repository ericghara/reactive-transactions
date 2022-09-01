package org.ericgha.dbtransactions.repository.jooq;

import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.manager.JooqTransaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static com.ericgha.docuCloud.jooq.tables.ATable.A_TABLE;
import static org.jooq.impl.DSL.asterisk;

@Repository
@RequiredArgsConstructor
@Transactional
public class ATableJooq {

    private final JooqTransaction jooqTransaction;

    public Mono<Long> deleteById(UUID uuid) {
        return jooqTransaction.transact( dsl -> dsl.delete( A_TABLE ).where( A_TABLE.ID.eq( uuid ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<ATableEntity> insert(ATableEntity entity) {
        return jooqTransaction.transact( dsl -> dsl.insertInto( A_TABLE ).set( A_TABLE.ID, Objects.requireNonNullElse( entity.getId(), UUID.randomUUID() ) ).returning( asterisk() ) )
                .map( record -> new ATableEntity( record.getId() ) );
    }

    public Mono<ATableEntity> fetchById(UUID id) {
        return jooqTransaction.transact( dsl -> dsl.selectFrom( A_TABLE ).where( A_TABLE.ID.eq( id ) ) )
                .map( record -> new ATableEntity( record.getId() ) );
    }
}
