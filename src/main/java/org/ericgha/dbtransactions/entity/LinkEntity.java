package org.ericgha.dbtransactions.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(name="link")
public class LinkEntity {

    // Didn't investigate but this breaks everything

//    @Id
    private UUID id_a;
//    @Id
    private UUID id_b;

}
