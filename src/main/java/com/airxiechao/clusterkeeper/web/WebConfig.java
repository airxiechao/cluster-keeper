package com.airxiechao.clusterkeeper.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置http接口拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private NodeSecurityInterceptor nodeSecurityInterceptor;

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){

        // 节点安全
        registry.addInterceptor(nodeSecurityInterceptor).addPathPatterns(
                "/node/**",
                "/master/**",
                "/cluster/**");
    }
}
