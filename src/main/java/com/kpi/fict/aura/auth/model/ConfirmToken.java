package com.kpi.fict.aura.auth.model;

import com.kpi.fict.aura.auth.converter.StringEncryptionConverter;
import com.kpi.fict.aura.auth.dto.ConfirmTokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirm_token")
@Getter
@Setter
public class ConfirmToken extends BaseEntity {

    @Convert(converter = StringEncryptionConverter.class)
    private String code;

    @Convert(converter = StringEncryptionConverter.class)
    private String token;

    private Long credentialsId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ConfirmTokenType tokenType;

    private LocalDateTime sentAt;

    private LocalDateTime expiredAt;

}
