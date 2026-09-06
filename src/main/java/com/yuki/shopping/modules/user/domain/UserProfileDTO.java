package com.yuki.shopping.modules.user.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDTO {

    @Size(max = 50)
    private String nickname;

    @Size(max = 20)
    private String phone;

    @Email
    private String email;

    private String avatar;
}
