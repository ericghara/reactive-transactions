package org.ericgha.dbtransactions.repository;

import lombok.NonNull;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ATableRepository extends ReactiveCrudRepository<ATableEntity, UUID> {

    @Modifying
    @Query( "DELETE from a_table where id = $1" )
    Mono<Long> deleteByIdReturning(@NonNull UUID uuid);

}
