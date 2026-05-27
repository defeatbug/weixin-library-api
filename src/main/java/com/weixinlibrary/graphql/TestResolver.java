package com.weixinlibrary.graphql;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TestResolver {

    @QueryMapping
    public String hello() {
        return "Hello GraphQL!";
    }
}
