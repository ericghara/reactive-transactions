package org.ericgha.dbtransactions.repository;

import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static com.ericgha.docuCloud.jooq.tables.ATable.A_TABLE;
import static org.jooq.impl.DSL.asterisk;

@Repository
@RequiredArgsConstructor
public class ATableJooq {

    public Mono<Long> deleteById(UUID uuid, DSLContext dsl) {
        return Mono.from( dsl.delete( A_TABLE ).where( A_TABLE.ID.eq( uuid ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<ATableEntity> insert(ATableEntity entity, DSLContext dsl) {
        return Mono.from( dsl.insertInto( A_TABLE ).set( A_TABLE.ID, Objects.requireNonNullElse(entity.getId(), UUID.randomUUID() ) ).returning( asterisk() ) )
                .map( record -> new ATableEntity( record.getId() ) );
    }

    public Mono<ATableEntity> fetchById(UUID id, DSLContext dsl) {
        return Mono.from( dsl.selectFrom(A_TABLE).where(A_TABLE.ID.eq(id) ) )
                .map( record -> new ATableEntity( record.getId() ) );
    }
}
