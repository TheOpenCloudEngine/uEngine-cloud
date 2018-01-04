package org.uengine.cloud;

import org.opencloudengine.garuda.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2016. 4. 22..
 */
@WebFilter
public class RestFilter implements Filter {

    @Autowired
    Environment environment;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/gitlab")) {
            try {
                doGitlabProxy(request, response);
                return;
            } catch (Exception ex) {
                response.setStatus(400);
                this.addCors(response);
                ExceptionUtils.httpExceptionResponse(ex, response);
                return;
            }
        } else if (requestURI.startsWith("/dcos")) {
            if(request.getMethod().equals("OPTIONS")){
                response.setStatus(200);
                this.addCors(response);
                return;
            }
            try {
                doDcosProxy(request, response);
                return;
            } catch (Exception ex) {
                response.setStatus(400);
                this.addCors(response);
                ExceptionUtils.httpExceptionResponse(ex, response);
                return;
            }
        } else {
            this.addCors(response);
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {

    }

    private void doDcosProxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map requiredHeaders = new HashMap();
        requiredHeaders.put("Authorization", "token=" + environment.getProperty("dcos.token"));

        ProxyRequest proxyRequest = new ProxyRequest();
        proxyRequest.setRequest(request);
        proxyRequest.setResponse(response);

        proxyRequest.setHost(environment.getProperty("dcos.host"));
        proxyRequest.setPath(request.getRequestURI().replaceFirst("/dcos", ""));
        proxyRequest.setHeaders(requiredHeaders);
        proxyRequest.setResponseHeaders(this.addReponseHeaders());

        new ProxyService().doProxy(proxyRequest);
    }

    private void doGitlabProxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map requiredHeaders = new HashMap();
        requiredHeaders.put("PRIVATE-TOKEN", environment.getProperty("gitlab.token"));

        ProxyRequest proxyRequest = new ProxyRequest();
        proxyRequest.setRequest(request);
        proxyRequest.setResponse(response);

        proxyRequest.setHost(environment.getProperty("gitlab.host"));
        proxyRequest.setPath(request.getRequestURI().replaceFirst("/gitlab", ""));
        proxyRequest.setHeaders(requiredHeaders);
        proxyRequest.setResponseHeaders(this.addReponseHeaders());

        new ProxyService().doProxy(proxyRequest);
    }

    private Map<String, String> addReponseHeaders() {
        Map<String, String> map = new HashMap();

        map.put("Access-Control-Allow-Origin", "*");
        map.put("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
        map.put("Access-Control-Max-Age", "3600");
        map.put("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept, " +
                "management-key, management-secret, client-key, client-secret, authorization, Location, access_token, " +
                "PRIVATE-TOKEN");
        return map;
    }

    private void addCors(HttpServletResponse response) {
        Map<String, String> map = this.addReponseHeaders();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
    }
}
