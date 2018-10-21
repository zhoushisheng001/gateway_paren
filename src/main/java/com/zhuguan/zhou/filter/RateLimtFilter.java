package com.zhuguan.zhou.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 限流接口的实战防止服务被宕机
 */
@Component
public class RateLimtFilter extends ZuulFilter {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建1000个令牌意思是最多同时只能有1000个用户访问这是网关限流
     * nginx也是能做限流的
     */
    private static final RateLimiter rete = RateLimiter.create(1000);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
    /**
     * 在最前的地方进行限流越小越先执行
     * @return
     */
    @Override
    public int filterOrder() {
        return -5;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String token = request.getHeader("token");
        if (!StringUtils.isBlank(token)) return true;//表示需要拦截
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        logger.info("需要拦截的接口。。。");
        RequestContext context = RequestContext.getCurrentContext();
        boolean tryAcquire = rete.tryAcquire();//获得令牌如果1000都被拿走就会返回fasle
        if (!tryAcquire) {//如果不能获得令牌的话就直接返回这样能保护服务
            HttpServletRequest request = context.getRequest();
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}