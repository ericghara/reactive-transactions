package org.ericgha.dbtransactions.repository;

import com.ericgha.docuCloud.jooq.Routines;
import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static com.ericgha.docuCloud.jooq.tables.BTable.B_TABLE;
import static org.jooq.impl.DSL.asterisk;

@Repository
@RequiredArgsConstructor
public class BTableJooq {

    public Mono<Long> deleteById(UUID uuid, DSLContext dsl) {
        return Mono.from( dsl.delete( B_TABLE ).where( B_TABLE.ID.eq( uuid ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<BTableEntity> insert(BTableEntity entity, DSLContext dsl) {
        return Mono.from( dsl.insertInto( B_TABLE ).set( B_TABLE.ID, Objects.requireNonNullElse(entity.getId(), UUID.randomUUID() ) )
                        .returning( asterisk() ) )
                .map( record -> new BTableEntity( record.getId() ) );
    }
    
    public Mono<BTableEntity> fetchById(UUID id, DSLContext dsl) {
        return Mono.from( dsl.selectFrom(B_TABLE).where(B_TABLE.ID.eq(id) ) )
                .map( record -> new BTableEntity( record.getId() ) );
    }
}
