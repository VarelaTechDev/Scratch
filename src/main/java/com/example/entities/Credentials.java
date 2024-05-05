package com.example.entities;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "credentials")
public class Credentials {
    @Id
    @Column(length = 128)
    private byte[] id;

    @ManyToOne
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    @Column(nullable = false)
    private long count;

    @Column(nullable = false, length = 500)
    private byte[] publicKeyCose;

    @Column
    private String transports;

    public long getAppUserId() {
        return appUser.getId();  // Ensure there is a getId method in AppUser
    }

    public void setAppUserId(long id) {
        appUser.setId(id);  // Ensure there is a setId method in AppUser
    }
}
