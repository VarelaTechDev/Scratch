package com.example.entities;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Set;

@Data
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private byte[] recoveryToken;

    @Column
    private Timestamp registrationStart;

    @Column
    private Timestamp registrationAddStart;

    @Column
    private byte[] registrationAddToken;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private Set<Credentials> credentials;  // Adding a set of Credentials
}
