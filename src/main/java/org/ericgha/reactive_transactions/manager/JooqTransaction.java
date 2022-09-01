package org.ericgha.reactive_transactions.manager;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@AllArgsConstructor
public class JooqTransaction {

    private final ConnectionFactory cfi;

    public Mono<DSLContext> get() {
        // publish a transaction aware connection
        // and wrap it in a DSLContext
        return ConnectionFactoryUtils.getConnection( cfi )
                .map( DSL::using );
    }

    public <T> Mono<T> transact(@NonNull Function<DSLContext, Publisher<T>> monoFunction) {
        return this.get().flatMap( trxDsl -> Mono.from(monoFunction.apply(trxDsl) ) );
    }

    public <T> Flux<T> transactMany(@NonNull Function<DSLContext, Publisher<T>> fluxFunction) {
        return this.get().flatMapMany( trxDsl -> Flux.from(fluxFunction.apply(trxDsl) ) );
    }

}
