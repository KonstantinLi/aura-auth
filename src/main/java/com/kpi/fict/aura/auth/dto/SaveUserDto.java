package com.kpi.fict.aura.auth.dto;

import lombok.Data;

@Data
public class SaveUserDto {

    private String firstName;

    private String lastName;

    private String email;

    private String username;

    private String password;

    private boolean enabled;

}