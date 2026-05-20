package com.weixinlibrary.graphql;

import com.weixinlibrary.dto.AuthPayload;
import com.weixinlibrary.dto.LoginInput;
import com.weixinlibrary.dto.RegisterInput;
import com.weixinlibrary.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

    @MutationMapping
    public AuthPayload register(@Argument String email,
                                @Argument String password,
                                @Argument String displayName) {
        RegisterInput input = new RegisterInput(email, password, displayName);
        return authService.register(input);
    }

    @MutationMapping
    public AuthPayload login(@Argument String email,
                             @Argument String password) {
        LoginInput input = new LoginInput(email, password);
        return authService.login(input);
    }
}
