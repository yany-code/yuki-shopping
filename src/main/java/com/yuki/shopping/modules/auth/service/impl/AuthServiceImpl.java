package com.yuki.shopping.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.JwtProperties;
import com.yuki.shopping.common.security.JwtTokenProvider;
import com.yuki.shopping.common.security.LoginUser;
import com.yuki.shopping.common.security.UserType;
import com.yuki.shopping.modules.auth.domain.LoginDTO;
import com.yuki.shopping.modules.auth.domain.RefreshDTO;
import com.yuki.shopping.modules.auth.domain.RegisterDTO;
import com.yuki.shopping.modules.auth.service.AuthService;
import com.yuki.shopping.modules.auth.domain.TokenVO;
import com.yuki.shopping.modules.user.domain.User;
import com.yuki.shopping.modules.user.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_KEY_PREFIX = "auth:refresh:";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public TokenVO login(LoginDTO request) {
        //1.查询用户是否存在
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        //2.不存在则报错
        if(user == null || user.getStatus()!=1
            ||!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new BusinessException(40900,"用户名或密码错误");
        }
        //3.存在返回
        return issueTokens(user);
    }

    @Override
    public TokenVO register(RegisterDTO request) {
        //1.拿取用户提交的用户名mp查询数据库中是否已存在此用户名
       Long usernameCount = userMapper.selectCount(
               new LambdaQueryWrapper<User>().eq(User::getUsername,request.getUsername())
       );

       //2.如果用户名重复，报错
        if(usernameCount>0){
            throw new BusinessException(40900,"用户名已被占用");
        }
        //3.查询手机号是否被占用
        if(request.getPhone() != null && !request.getPhone().isBlank()){
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone,request.getPhone())
            );
            if(phoneCount>0){
                throw new BusinessException(40900,"手机号已被占用");
            }
        }

        //4. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);

        try {
            userMapper.insert(user);
        } catch (Exception e) {
            //查重与插入之间的并发窗口由唯一索引兜底
            throw new BusinessException(40900,"用户名或手机号已被占用");
        }

        //5.返回数据
        return issueTokens(user);

    }

    @Override
    public TokenVO refresh(RefreshDTO request) {

        LoginUser loginUser;
        try {
            loginUser = jwtTokenProvider.parse(request.getRefreshToken(), JwtTokenProvider.TokenType.REFRESH);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(40100,"刷新令牌无效或已过期");
        }
        if(loginUser.userType() != UserType.USER){
            throw new BusinessException(40100,"刷新令牌无效");
        }

        //白名单: Redis 里存的必须是“当前这枚”refresh token，旧token / 已登出 token 一律拒绝
        String key = REFRESH_KEY_PREFIX + loginUser.userId();
        String stored = stringRedisTemplate.opsForValue().get(key);
        if(stored == null || !stored.equals(request.getRefreshToken())){
            throw new BusinessException(40100,"刷新令牌已失效");
        }
        //回查账号最新状态：改名，禁用，注销要在续命时立即生效
        User user = userMapper.selectById(loginUser.userId());
        if(user == null || user.getStatus() != 1){
            throw new BusinessException(40100,"账号不可用");
        }
        return issueTokens(user);
    }

    /**
     * 签发access + refresh
     * 把refresh写入Redis白名单(TTL 与 refresh 有效期一致)
     * @param user
     * @return
     */
    private TokenVO issueTokens(User user){
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), UserType.USER, null);
        String accessToken = jwtTokenProvider.createAccessToken(loginUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(loginUser);
        stringRedisTemplate.opsForValue().set(REFRESH_KEY_PREFIX + user.getId(),refreshToken,
                Duration.ofDays(jwtProperties.getRefreshTokenTtlDays()));
        TokenVO vo = new TokenVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtProperties.getAccessTokenTtlMinutes()*60);
        return vo;
    }
}
