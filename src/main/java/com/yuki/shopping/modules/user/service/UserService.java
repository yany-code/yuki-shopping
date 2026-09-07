package com.yuki.shopping.modules.user.service;

import com.yuki.shopping.modules.user.domain.AddressDTO;
import com.yuki.shopping.modules.user.domain.AddressVO;
import com.yuki.shopping.modules.user.domain.UserProfileDTO;
import com.yuki.shopping.modules.user.domain.UserVO;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {

    /**
     * 查询信息
     * @return
     */
    UserVO me();

    /**
     * 更新信息
     * @param request
     * @return
     */
    UserVO updateProfile(@Valid UserProfileDTO request);

    /**
     * 查询地址
     * @return
     */
    List<AddressVO> listAddresses();

    /**
     * 增加地址
     * @param request
     * @return
     */
    AddressVO addAddress(@Valid AddressDTO request);

    /**
     * 更新地址
     * @param id
     * @param request
     * @return
     */
    AddressVO updateAddress(Long id, @Valid AddressDTO request);

    /**
     * 删除地址
     * @param id
     */
    void deleteAddress(Long id);

    /**
     * 设置默认地址
     * @param id
     */
    void setDefaultAddress(Long id);

}
