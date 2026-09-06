package com.yuki.shopping.modules.auth.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshDTO {

    @NotBlank
    private String refreshToken;
}
