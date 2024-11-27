package com.erebelo.springneptunedemo.config;

import com.erebelo.springneptunedemo.filter.UserRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UserRequestFilter> userRequestFilterRegistrationBean() {
        FilterRegistrationBean<UserRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new UserRequestFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);

        return registrationBean;
    }
}
