package com.example.entity;

import com.example.utils.ByteArrayAttributeConverter;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AttestedCredentialData;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "credentials")
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "count", nullable = false)
    private Long count;

    @Lob
    @Convert(converter = ByteArrayAttributeConverter.class)
    @Column(name = "credentialId", nullable = false, columnDefinition = "VARBINARY(128)")
    private ByteArray credentialId;

    @Lob
    @Convert(converter = ByteArrayAttributeConverter.class)
    @Column(name = "public_key_cose", nullable = false, columnDefinition = "VARBINARY(500)")
    private ByteArray publicKeyCose;

    @Column(name = "transports")
    private String transports;
}
