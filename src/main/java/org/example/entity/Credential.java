package org.example.entity;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;

@Data
@Getter
@Entity
@Table(name = "credentials")
public class Credential {
    @Id
    @Column(name = "id", columnDefinition = "VARBINARY(128) NOT NULL")
    private byte[] id;

    @ManyToOne
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    @Column(nullable = false)
    private Long count;

    @Column(name = "public_key_cose", nullable = false)
    private byte[] publicKeyCose;

    @Column
    private String transports;
}
