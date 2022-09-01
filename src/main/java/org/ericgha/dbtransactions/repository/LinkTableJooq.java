package org.ericgha.dbtransactions.repository;

import lombok.RequiredArgsConstructor;
import org.ericgha.dbtransactions.entity.LinkEntity;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.ericgha.docuCloud.jooq.Tables.LINK;
import static org.jooq.impl.DSL.asterisk;

@Repository
@RequiredArgsConstructor
public class LinkTableJooq {

    public Mono<Long> delete(UUID idA, UUID idB, DSLContext dsl) {
        return Mono.from( dsl.deleteFrom( LINK ).where( LINK.ID_A.eq( idA ).and( LINK.ID_B.eq( idB ) ) ) )
                .map( (Number n) -> n.longValue() );
    }

    public Mono<LinkEntity> insert(UUID idA, UUID idB, DSLContext dsl) {
        return Mono.from( dsl.insertInto( LINK ).set( LINK.ID_A, idA ).set( LINK.ID_B, idB ).returning( asterisk() ) )
                .map( linkRecord -> new LinkEntity( linkRecord.getIdA(), linkRecord.getIdB() ) );
    }

    public Mono<LinkEntity> fetchBy(UUID idA, UUID idB, DSLContext dsl) {
        return Mono.from( dsl.selectFrom(LINK).where( LINK.ID_A.eq( idA ).and( LINK.ID_B.eq( idB ) ) ) )
                .map( linkRecord -> new LinkEntity( linkRecord.getIdA(), linkRecord.getIdB() ) );
    }
}
