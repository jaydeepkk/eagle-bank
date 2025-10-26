package com.eaglebank.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id; // tan-...

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_number")
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "GBP";

    @Column(nullable = false)
    private String type;

    private String reference;

    @Column(nullable = false)
    private String userId; // usr-...

    @Column(nullable = false)
    private OffsetDateTime createdTimestamp;
}
