package com.example.eventmanagement.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Value("${ratelimit.capacity}")
    private int capacity;

    @Value("${ratelimit.duration-minutes}")
    private int durationMinutes;

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter(this.capacity, this.durationMinutes));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
