package org.ericgha.dbtransactions.service;

import lombok.AllArgsConstructor;
import org.ericgha.dbtransactions.entity.ATableEntity;
import org.ericgha.dbtransactions.entity.BTableEntity;
import org.ericgha.dbtransactions.repository.ATableRepository;
import org.ericgha.dbtransactions.repository.BTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SpringDataService implements DbService {

    private final ATableRepository aTableRepository;
    private final BTableRepository bTableRepository;

    @Transactional
    public Mono<Tuple2<ATableEntity, BTableEntity>> insertPair(ATableEntity tableA, BTableEntity tableB) {
        var aIns = aTableRepository.save(tableA);
        var bIns = bTableRepository.save( tableB );
        return Mono.zip(aIns, bIns);
    }

    @Transactional
    public Mono<Void> deletePair(UUID a, UUID b) {
        var aDel = aTableRepository.deleteByIdReturning( a ).doOnSuccess(this::throwIfZero);
        var bDel = bTableRepository.deleteByIdReturning( b ).doOnSuccess( this::throwIfZero );
        return Mono.zip(aDel, bDel).then();
    }

    @Transactional(readOnly = true)
    public Mono<ATableEntity> fetch(ATableEntity tableA) {
        return aTableRepository.findById( tableA.getId() );
    }

    @Transactional(readOnly = true)
    public Mono<BTableEntity> fetch(BTableEntity tableB) {
        return bTableRepository.findById( tableB.getId() );
    }

    private void throwIfZero(Long num) throws IllegalArgumentException {
        if (num.equals(0L) ) {
            throw new IllegalStateException("Failure to delete");
        }
    }
}
