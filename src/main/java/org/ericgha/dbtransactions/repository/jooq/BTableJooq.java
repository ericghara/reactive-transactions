package org.ericgha.dbtransactions.repository.jooq;

import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.manager.JooqTransaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static com.ericgha.docuCloud.jooq.tables.BTable.B_TABLE;
import static org.jooq.impl.DSL.asterisk;

@Repository
@RequiredArgsConstructor
@Transactional
public class BTableJooq {

    private final JooqTransaction jooqTransaction;

    public Mono<Long> deleteById(UUID uuid) {
        return jooqTransaction.transact( dsl -> dsl.delete( B_TABLE ).where( B_TABLE.ID.eq( uuid ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<BTableEntity> insert(BTableEntity entity) {
        return jooqTransaction.transact( dsl -> dsl.insertInto( B_TABLE ).set( B_TABLE.ID, Objects.requireNonNullElse( entity.getId(), UUID.randomUUID() ) )
                        .returning( asterisk() ) )
                .map( record -> new BTableEntity( record.getId() ) );
    }

    public Mono<BTableEntity> fetchById(UUID id) {
        return jooqTransaction.transact( dsl -> dsl.selectFrom( B_TABLE ).where( B_TABLE.ID.eq( id ) ) )
                .map( record -> new BTableEntity( record.getId() ) );
    }
}
