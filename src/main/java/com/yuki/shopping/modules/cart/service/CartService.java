package com.yuki.shopping.modules.cart.service;

import com.yuki.shopping.modules.cart.domain.CartCheckAllDTO;
import com.yuki.shopping.modules.cart.domain.CartItemAddDTO;
import com.yuki.shopping.modules.cart.domain.CartItemUpdateDTO;
import com.yuki.shopping.modules.cart.domain.CartItemVO;

import java.util.List;

public interface CartService {

    List<CartItemVO> list();

    CartItemVO add(CartItemAddDTO request);

    CartItemVO update(Long id, CartItemUpdateDTO request);

    void delete(Long id);

    void checkAll(CartCheckAllDTO request);
}
