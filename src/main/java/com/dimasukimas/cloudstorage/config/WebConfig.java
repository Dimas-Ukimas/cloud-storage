package com.dimasukimas.cloudstorage.config;

import com.dimasukimas.cloudstorage.interceptor.LoggingContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoggingContextInterceptor loggingContextInterceptor;

    public WebConfig(LoggingContextInterceptor loggingContextInterceptor) {
        this.loggingContextInterceptor = loggingContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingContextInterceptor);
    }
}
