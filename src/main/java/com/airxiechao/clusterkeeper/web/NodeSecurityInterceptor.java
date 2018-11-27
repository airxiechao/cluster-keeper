package com.airxiechao.clusterkeeper.web;

import com.airxiechao.clusterkeeper.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 节点安全验证拦截器
 */
@Component
public class NodeSecurityInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(NodeSecurityInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String token = request.getHeader(SecurityUtil.NODE_TOKEN_HEADER);
        if(null != token && SecurityUtil.validateNodeToken(token)){
            return true;
        }

        logger.info("节点token验证无效，请求：{}，来自：{}", request.getRequestURI(), request.getRemoteAddr());
        return false;
    }
}
