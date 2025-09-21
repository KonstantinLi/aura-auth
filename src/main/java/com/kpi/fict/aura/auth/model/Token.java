package com.kpi.fict.aura.auth.model;

import com.kpi.fict.aura.auth.converter.StringEncryptionConverter;
import com.kpi.fict.aura.auth.dto.TokenScope;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "token")
public class Token extends BaseEntity {

    @Convert(converter = StringEncryptionConverter.class)
    private String token;

    @Convert(converter = StringEncryptionConverter.class)
    private String refreshToken;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TokenScope scope;

}