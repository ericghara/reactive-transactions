package org.ericgha.dbtransactions.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Didn't investigate how to get composite keys working with
// data r2dbc.  Only used by jOOQ example
public class LinkEntity {

    private UUID id_a;

    private UUID id_b;

}
