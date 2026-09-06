package com.yuki.shopping.modules.auth.service;

import com.yuki.shopping.modules.auth.domain.LoginDTO;
import com.yuki.shopping.modules.auth.domain.RefreshDTO;
import com.yuki.shopping.modules.auth.domain.RegisterDTO;
import com.yuki.shopping.modules.auth.domain.TokenVO;

public interface AuthService {

    TokenVO login(LoginDTO request);

    TokenVO register(RegisterDTO request);

    TokenVO refresh(RefreshDTO request);
}
