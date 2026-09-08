package com.yuki.shopping.modules.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuki.shopping.common.exception.BusinessException;
import com.yuki.shopping.common.security.SecurityUtils;
import com.yuki.shopping.modules.user.domain.*;
import com.yuki.shopping.modules.user.mapper.UserAddressMapper;
import com.yuki.shopping.modules.user.mapper.UserMapper;
import com.yuki.shopping.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;

    /**
     * 查询信息
     * @return
     */
    @Override
    public UserVO me() {
        //拿取当前线程用户信息
        return toUserVO(requireCurrentUser());
    }

    /**
     * 更新信息
     * @param request
     * @return
     */
    @Override
    public UserVO updateProfile(UserProfileDTO request) {
        User user = requireCurrentUser();

        //仅在传入了手机号并且不为空,新手机号与旧手机号不一致时检验
        if(request.getPhone() != null && !request.getPhone().isBlank()
                && !request.getPhone().equals(user.getPhone())){
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone,request.getPhone())
                    .ne(User::getId,user.getId()));
            if(count > 0){
                throw new BusinessException(40900,"手机号已被占用");
            }
        }

        //updateById默认跳过null字段，天然实现局部更新
        User patch = new User();
        patch.setId(user.getId());
        patch.setNickname(request.getNickname());
        patch.setPhone(request.getPhone());
        patch.setEmail(request.getEmail());
        patch.setAvatar(request.getAvatar());

        userMapper.updateById(patch);

        return toUserVO(requireCurrentUser());
    }

    /**
     * 批量查询地址信息
     * @return
     */
    @Override
    public List<AddressVO> listAddresses() {
        return addressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId,SecurityUtils.currentUserId())
                .orderByDesc(UserAddress::getIsDefault)
                .orderByAsc(UserAddress::getId))
                .stream().map(this::toAddressVO).toList();
    }

    /**
     * 新增地址
     * @param request
     * @return
     */
    @Override
    @Transactional
    public AddressVO addAddress(AddressDTO request) {
//        UserAddress address = new UserAddress();
//        address.setUserId(SecurityUtils.currentUserId());

        Long userId = SecurityUtils.currentUserId();

        //锁住当前用户行，同一用户的地址默认状态变更为串行执行，防止并行导致的用户多默认地址的情况发生
        requireCurrentUserForUpdate();

        UserAddress address = new UserAddress();
        address.setUserId(userId);

        //复制地址信息
        copyAddress(request,address);

        if(Integer.valueOf(1).equals(address.getIsDefault())){
            //取消其余默认地址
            clearDefault(address.getUserId());
        }

        addressMapper.insert(address);
        return toAddressVO(address);
    }

    /**
     * 更新地址
     * @param id
     * @param request
     * @return
     */
    @Override
    @Transactional
    public AddressVO updateAddress(Long id, AddressDTO request) {
        Long userId = SecurityUtils.currentUserId();

        //必须先锁用户行，再操作默认地址
        requireCurrentUserForUpdate();

        //拿取当前地址
        UserAddress address = requireOwnedAddress(id);

        //复制新地址内容
        copyAddress(request,address);

        if(Integer.valueOf(1).equals(address.getIsDefault())){
            //清楚原先默认地址
            clearDefault(address.getUserId());
        }

        //更新地址
        addressMapper.updateById(address);
        return toAddressVO(address);
    }

    /**
     * 根据id删除指定地址
     * @param id
     */
    @Override
    public void deleteAddress(Long id) {
        requireOwnedAddress(id);
        addressMapper.deleteById(id);
    }

    /**
     * 设置默认地址
     * @param id
     */
    //TODO 可优化内容：设置默认地址可以和更新地址信息用一个方法（待定）
    @Override
    @Transactional
    public void setDefaultAddress(Long id) {
        Long userId = SecurityUtils.currentUserId();

        //保证统一用户的默认地址更改串行化
        requireCurrentUserForUpdate();

        UserAddress address = requireOwnedAddress(id);

        clearDefault(address.getUserId());

        UserAddress update = new UserAddress();
        update.setId(id);
        update.setIsDefault(1);

        addressMapper.updateById(update);
    }

    /**
     * 归属校验统一入口：不存在40400，不属于当前用户40300
     * @param id
     * @return
     */
    private UserAddress requireOwnedAddress(Long id){
        UserAddress address = addressMapper.selectById(id);

        if(address == null){
            throw new BusinessException(40400,"地址不存在");
        }

        if(!address.getUserId().equals(SecurityUtils.currentUserId())){
            throw new BusinessException(40300,"无权限操作该地址");
        }

        return address;
    }

    /**
     * 拿取当前用户对象
     * @return
     */
    private User requireCurrentUser(){
        User user = userMapper.selectById(SecurityUtils.currentUserId());

        if(user == null){
            throw new BusinessException(40100,"登录状态已失效");
        }

        return user;
    }

    /**
     * 悲观锁拿取用户对象，防止对线程并行对默认地址进行修改，导致一个用户拥有多个默认地址
     * @return
     */
    private User requireCurrentUserForUpdate(){
        Long userId = SecurityUtils.currentUserId();

        User user = userMapper.selectByIdForUpdate(userId);

        if(user == null){
            throw new BusinessException(40100,"登录状态已失效");
        }

        return user;
    }

    /**
     * 清楚该用户当前的默认地址
     * 需要在事务内部和新默认的写入一起执行
     * @param userId
     */
    private void clearDefault(Long userId){
        UserAddress clear = new UserAddress();

        clear.setIsDefault(0);

        addressMapper.update(clear,new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId,userId)
                .eq(UserAddress::getIsDefault,1));
    }

    /**
     * 复制地址信息
     * @param request
     * @param address
     */
    private void copyAddress(AddressDTO request,UserAddress address){
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setIsDefault(normalizeIsDefault(request.getIsDefault()));
    }

    /**
     * 判断地址状态
     * @param isDefault
     * @return
     */
    private int normalizeIsDefault(Integer isDefault){
        return isDefault == null ? 0 : isDefault;
    }

    /**
     * User转UserVO
     * @param user
     * @return
     */
    private UserVO toUserVO(User user){
        UserVO vo = new UserVO();

        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());

        return vo;
    }

    /**
     * Address转AddressVO
     * @param address
     * @return
     */
    private AddressVO toAddressVO(UserAddress address){
        AddressVO vo = new AddressVO();

        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetail(address.getDetail());
        vo.setIsDefault(address.getIsDefault());

        return vo;
    }
}
