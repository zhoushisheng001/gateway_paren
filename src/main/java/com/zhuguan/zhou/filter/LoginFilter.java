package com.zhuguan.zhou.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoginFilter extends ZuulFilter {

    /**
     * 过滤去的类型有前置 后置，错误
     * @return
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 执行的顺序值越小越先执行
     * @return
     */
    @Override
    public int filterOrder() {
        return 4;
    }

    /**
     * 是否进行拦截 返回false就是不需要拦截
     * true 就是拦截进入下面的run方法里面
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //RequestContext是网关里面的对象注意引入包的位置
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String URL = request.getRequestURL().toString();
        String uri = request.getRequestURI();
        System.out.println("======url：" + URL);
        System.out.println("======uri：" + uri);
        if ("/api/product/product/getProductDtoByName".equals(uri)) return true;
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("==========开始拦截了============");
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            context.setSendZuulResponse(false);//设置不能继续往后面执行
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
