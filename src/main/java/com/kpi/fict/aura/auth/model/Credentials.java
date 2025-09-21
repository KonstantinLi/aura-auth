package com.kpi.fict.aura.auth.model;

import com.kpi.fict.aura.auth.converter.StringEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "credentials")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = { "username" }, callSuper = false)
public class Credentials extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Convert(converter = StringEncryptionConverter.class)
    private String username;

    private String password;

    public Credentials(String username) {
        this.username = username;
    }

}
