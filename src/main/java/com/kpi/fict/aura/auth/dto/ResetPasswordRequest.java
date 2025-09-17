package com.kpi.fict.aura.auth.dto;

import com.kpi.fict.aura.auth.controller.validation.NotEmail;
import com.kpi.fict.aura.auth.controller.validation.ValidPassword;

public record ResetPasswordRequest(@ValidPassword @NotEmail String newPassword) {}