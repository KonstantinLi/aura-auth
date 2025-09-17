package com.kpi.fict.aura.auth.dto;

import com.kpi.fict.aura.auth.model.DeviceStatus;

public record DeviceResponse(
        Long id,
        String name,
        Long ownerId,
        DeviceStatus status) {}