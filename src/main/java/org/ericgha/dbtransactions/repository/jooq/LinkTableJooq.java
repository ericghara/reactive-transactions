package org.ericgha.dbtransactions.repository.jooq;

import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.LinkEntity;
import org.ericgha.dbtransactions.manager.JooqTransaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.ericgha.docuCloud.jooq.Tables.LINK;
import static org.jooq.impl.DSL.asterisk;

@Repository
@Transactional
@RequiredArgsConstructor
public class LinkTableJooq {

    private final JooqTransaction jooqTransaction;

    public Mono<Long> delete(UUID idA, UUID idB) {
        return jooqTransaction.transact( dsl -> dsl.deleteFrom( LINK ).where( LINK.ID_A.eq( idA ).and( LINK.ID_B.eq( idB ) ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<LinkEntity> insert(UUID idA, UUID idB) {
        return jooqTransaction.transact( dsl -> dsl.insertInto( LINK ).set( LINK.ID_A, idA ).set( LINK.ID_B, idB ).returning( asterisk() ) )
                .map( linkRecord -> new LinkEntity( linkRecord.getIdA(), linkRecord.getIdB() ) );
    }

    public Mono<LinkEntity> fetchBy(UUID idA, UUID idB) {
        return jooqTransaction.transact( dsl -> dsl.selectFrom( LINK ).where( LINK.ID_A.eq( idA ).and( LINK.ID_B.eq( idB ) ) ) )
                .map( linkRecord -> new LinkEntity( linkRecord.getIdA(), linkRecord.getIdB() ) );
    }
}
