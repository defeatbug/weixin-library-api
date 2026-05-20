package com.weixinlibrary.dto;

import com.weixinlibrary.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPayload {
    private User user;
    private String authToken;
}
