package com.eaglebank.model;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id; // usr-...

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String addressJson;

    @Column
    private String phoneNumber;

    @Column(nullable = false)
    private OffsetDateTime createdTimestamp;

    @Column(nullable = false)
    private OffsetDateTime updatedTimestamp;
}
