package com.kpi.fict.aura.auth.model;

import com.kpi.fict.aura.auth.converter.StringEncryptionConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Convert(converter = StringEncryptionConverter.class)
    private String firstName;

    @Convert(converter = StringEncryptionConverter.class)
    private String lastName;

    @Convert(converter = StringEncryptionConverter.class)
    private String email;

    private LocalDateTime registeredAt;

    private boolean registered;

    private boolean enabled;

    private boolean twoFactorEnabled;

    @Convert(converter = StringEncryptionConverter.class)
    private String totpSecret;

    @Convert(converter = StringEncryptionConverter.class)
    private String recoveryCodes;

    @Override
    public String toString() {
        String firstName = this.firstName != null ? this.firstName : "";
        String lastName = this.lastName != null ? this.lastName : "";
        String name = firstName.concat(" ").concat(lastName).trim();
        return !name.isBlank() ? name : email;
    }

}