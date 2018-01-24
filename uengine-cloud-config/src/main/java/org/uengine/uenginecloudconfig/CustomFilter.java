package org.uengine.uenginecloudconfig;

import org.springframework.http.HttpMethod;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by uengine on 2018. 1. 22..
 */
public class CustomFilter extends GenericFilterBean {

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String authorization = request.getHeader("Authorization");

        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            chain.doFilter(req, res);
        } else {
            chain.doFilter(req, res);
//            response.setStatus(401);
//            response.setHeader("Cache-Control","no-store");
//            response.setDateHeader("Expires",0);
//            response.setHeader("WWW-authenticate","Basic Realm=\"Cloud Config Server\"");
        }
    }
}

