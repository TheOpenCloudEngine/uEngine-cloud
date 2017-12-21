package org.uengine.cloud;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.uengine.iam.util.ServiceException;
import org.uengine.iam.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpCookie;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by uengine on 2016. 6. 15..
 */
public class ProxyAction {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(ProxyAction.class);

    protected boolean doLog = false;
    protected boolean doForwardIP = true;
    protected boolean doSendUrlFragment = true;


    private HttpServletRequest request;
    private HttpServletResponse response;
    private String host;
    private String path;
    private HttpClient proxyClient;
    private HttpClient proxyHttpsClient;
    private Map<String, String> overridHeaders;
    private Map<String, String> overridResponseHeaders;

    protected URI targetUriObj;
    protected HttpHost targetHost;

    public ProxyAction(ProxyRequest proxyRequest) {
        this.request = proxyRequest.getRequest();
        this.response = proxyRequest.getResponse();
        this.host = proxyRequest.getHost();
        this.path = proxyRequest.getPath();
        this.overridHeaders = proxyRequest.getHeaders();
        this.overridResponseHeaders = proxyRequest.getResponseHeaders();

        try {
            targetUriObj = new URI(host);
        } catch (Exception e) {
            throw new ServiceException("Trying to process targetUri init parameter: " + e, e);
        }
        targetHost = URIUtils.extractHost(targetUriObj);

        HttpParams hcParams = new BasicHttpParams();
        hcParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        hcParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false); // See #70

        proxyClient = createHttpClient(hcParams);
        proxyHttpsClient = createHttpsClient(hcParams);
    }

    protected HttpClient createHttpClient(HttpParams hcParams) {
        try {
            //as of HttpComponents v4.2, this class is better since it uses System
            // Properties:
            Class clientClazz = Class.forName("org.apache.http.impl.client.SystemDefaultHttpClient");
            Constructor constructor = clientClazz.getConstructor(HttpParams.class);
            return (HttpClient) constructor.newInstance(hcParams);
        } catch (ClassNotFoundException e) {
            //no problem; use v4.1 below
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Fallback on using older client:
        return new DefaultHttpClient(new ThreadSafeClientConnManager(), hcParams);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    protected HttpClient createHttpsClient(HttpParams hcParams) {
        try {
            TrustManager easyTrustManager = new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    // no-op
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            };
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{easyTrustManager}, null);

            SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext,
                    SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

            Scheme sch = new Scheme("https", 443, socketFactory);

            try {
                //as of HttpComponents v4.2, this class is better since it uses System
                // Properties:
                Class clientClazz = Class.forName("org.apache.http.impl.client.SystemDefaultHttpClient");
                Constructor constructor = clientClazz.getConstructor(HttpParams.class);
                HttpClient httpClient = (HttpClient) constructor.newInstance(hcParams);
                httpClient.getConnectionManager().getSchemeRegistry().register(sch);
                return httpClient;
            } catch (ClassNotFoundException e) {
                //no problem; use v4.1 below
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //Fallback on using older client:
            DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(), hcParams);
            httpClient.getConnectionManager().getSchemeRegistry().register(sch);
            return httpClient;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void service() throws ServletException, IOException {

        String method = request.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(request);
        HttpRequest proxyRequest;

        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null ||
                request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            proxyRequest = newProxyRequestWithEntity(method, proxyRequestUri, request);
        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }

        copyRequestHeaders(request, proxyRequest);

        copyOverrideHeaders(overridHeaders, proxyRequest);

        setXForwardedForHeader(request, proxyRequest);

        HttpResponse proxyResponse = null;
        try {
            // Execute the request
            if (doLog) {
                logger.info("proxy " + method + " uri: " + request.getRequestURI() + " -- " + proxyRequest.getRequestLine().getUri());
            }

            if (proxyRequestUri.startsWith("https")) {
                proxyResponse = proxyHttpsClient.execute(targetHost, proxyRequest);
            } else {
                proxyResponse = proxyClient.execute(targetHost, proxyRequest);
            }

            int statusCode = proxyResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

            copyResponseHeaders(proxyResponse, request, response);


            if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {

                response.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            } else {

                copyResponseEntity(proxyResponse, response, proxyRequest, request);
            }

        } catch (Exception e) {
            //abort request, according to best practice with HttpClient
            if (proxyRequest instanceof AbortableHttpRequest) {
                AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
                abortableHttpRequest.abort();
            }
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            if (e instanceof ServletException)
                throw (ServletException) e;
            //noinspection ConstantConditions
            if (e instanceof IOException)
                throw (IOException) e;
            throw new RuntimeException(e);

        } finally {
            // entity 를 컨섬 처리를 해야 커넥션이 이어짐
            if (proxyResponse != null) {
                consumeQuietly(proxyResponse.getEntity());
            }
            //return proxyResponse;
            //Note: 여기서 아웃풋 스트림을 종료할 필요는 없음
            // http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
        }
    }

    private HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri,
                                                  HttpServletRequest servletRequest)
            throws IOException {
        HttpEntityEnclosingRequest eProxyRequest =
                new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        // 스트림 처리된 input entity 를 생성
        //  servletInputStream 종료 처리할 필요 없음.
        eProxyRequest.setEntity(
                new InputStreamEntity(servletRequest.getInputStream(), servletRequest.getContentLength()));
        return eProxyRequest;
    }


    protected void consumeQuietly(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {//ignore
            logger.error(e.getMessage(), e);
        }
    }

    protected static final HeaderGroup hopByHopHeaders;

    static {
        hopByHopHeaders = new HeaderGroup();
        String[] headers = new String[]{
                "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
                "TE", "Trailers", "Transfer-Encoding", "Upgrade"};
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }


    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        // Get an Enumeration of all of the header names sent by the client
        Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();
            copyRequestHeader(servletRequest, proxyRequest, headerName);
        }
    }

    protected void copyOverrideHeaders(Map<String, String> overridHeaders, HttpRequest proxyRequest) {
        for (Map.Entry<String, String> entry : overridHeaders.entrySet()) {
            proxyRequest.setHeader(entry.getKey(), entry.getValue());
        }
    }


    protected void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest,
                                     String headerName) {
        //InputStreamEntity 에 의해 content-length 배정
        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH))
            return;
        if (hopByHopHeaders.containsHeader(headerName))
            return;

        Enumeration headers = servletRequest.getHeaders(headerName);
        while (headers.hasMoreElements()) {//sometimes more than one value
            String headerValue = (String) headers.nextElement();

            if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                HttpHost host = targetHost;
                headerValue = host.getHostName();
                if (host.getPort() != -1)
                    headerValue += ":" + host.getPort();
            } else if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
                headerValue = getRealCookie(headerValue);
            }
            proxyRequest.addHeader(headerName, headerValue);
        }
    }

    private void setXForwardedForHeader(HttpServletRequest servletRequest,
                                        HttpRequest proxyRequest) {
        if (doForwardIP) {
            String headerName = "X-Forwarded-For";
            String newHeader = servletRequest.getRemoteAddr();
            String existingHeader = servletRequest.getHeader(headerName);
            if (existingHeader != null) {
                newHeader = existingHeader + ", " + newHeader;
            }
            proxyRequest.setHeader(headerName, newHeader);
        }
    }

    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (this.overridResponseHeaders != null) {
                if (!this.overridResponseHeaders.containsKey(header.getName())) {
                    copyResponseHeader(servletRequest, servletResponse, header);
                }
            }
        }
        if (this.overridResponseHeaders != null) {
            for (Map.Entry<String, String> entry : this.overridResponseHeaders.entrySet()) {
                servletResponse.addHeader(entry.getKey(), entry.getValue());
            }
        }
        servletResponse.setHeader("Access-Control-Expose-Headers", "x-uengine-pagination-totalnbrecords, x-uengine-pagination-maxnbrecords");
    }

    protected void copyResponseHeader(HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse, Header header) {
        String headerName = header.getName();
        if (hopByHopHeaders.containsHeader(headerName))
            return;
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE) ||
                headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // LOCATION Header 는 다시 쓰여져야함.
            servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    protected void copyProxyCookie(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse, String headerValue) {
        List<HttpCookie> cookies = HttpCookie.parse(headerValue);
        String path = servletRequest.getContextPath();
        path += servletRequest.getServletPath();

        for (HttpCookie cookie : cookies) {

            String proxyCookieName = getCookieNamePrefix() + cookie.getName();
            Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
            servletCookie.setComment(cookie.getComment());
            servletCookie.setMaxAge((int) cookie.getMaxAge());
            servletCookie.setPath(path); //set to the path of the proxy servlet

            servletCookie.setSecure(cookie.getSecure());
            servletCookie.setVersion(cookie.getVersion());
            servletResponse.addCookie(servletCookie);
        }
    }

    protected String getRealCookie(String cookieValue) {
        StringBuilder escapedCookie = new StringBuilder();
        String cookies[] = cookieValue.split("; ");
        for (String cookie : cookies) {
            String cookieSplit[] = cookie.split("=");
            if (cookieSplit.length == 2) {
                String cookieName = cookieSplit[0];
                if (cookieName.startsWith(getCookieNamePrefix())) {
                    cookieName = cookieName.substring(getCookieNamePrefix().length());
                    if (escapedCookie.length() > 0) {
                        escapedCookie.append("; ");
                    }
                    escapedCookie.append(cookieName).append("=").append(cookieSplit[1]);
                }
            }

            cookieValue = escapedCookie.toString();
        }
        return cookieValue;
    }


    protected String getCookieNamePrefix() {
        return "!Proxy!" + "uengine-billing";
    }


    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
            throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            try {
                entity.writeTo(servletOutputStream);
            }
            //broken pipe 에러는 프록시측의 소켓이 먼저 종료되었을경우로 주로 404 에러상황에서 자주 발생한다.
            //특별한 실패 처리는 하지 않도록 한다.
            catch (IOException ex) {
                logger.warn("Pipe closed while copyResponseEntity", ex);
            }
        }
    }


    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(host);

        if (!StringUtils.isEmpty(path)) {
            uri.append(encodeUriQuery(path));
        }

        //  query string & fragment 처리
        String queryString = servletRequest.getQueryString();
        String fragment = null;

        if (queryString != null) {
            int fragIdx = queryString.indexOf('#');
            if (fragIdx >= 0) {
                fragment = queryString.substring(fragIdx + 1);
                queryString = queryString.substring(0, fragIdx);
            }
        }

        queryString = rewriteQueryStringFromRequest(servletRequest, queryString);
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            uri.append(encodeUriQuery(queryString));
        }

        if (doSendUrlFragment && fragment != null) {
            uri.append('#');
            uri.append(encodeUriQuery(fragment));
        }
        return uri.toString();
    }

    protected String rewriteQueryStringFromRequest(HttpServletRequest servletRequest, String queryString) {
        return queryString;
    }

    protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {

        final String targetUri = host;
        if (theUrl.startsWith(targetUri)) {

            StringBuffer curUrl = servletRequest.getRequestURL();//no query
            int pos;

            if ((pos = curUrl.indexOf("://")) >= 0) {
                if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
                    curUrl.setLength(pos);
                }
            }

            curUrl.append(servletRequest.getContextPath());
            curUrl.append(servletRequest.getServletPath());
            curUrl.append(theUrl, targetUri.length(), theUrl.length());
            theUrl = curUrl.toString();
        }
        return theUrl;
    }

    protected static CharSequence encodeUriQuery(CharSequence in) {
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get((int) c)) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null)
                    outBuf.append(c);
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5 * 3);
                    outBuf.append(in, 0, i);
                    formatter = new Formatter(outBuf);
                }
                formatter.format("%%%02X", (int) c);
            }
        }
        return outBuf != null ? outBuf : in;
    }

    protected static final BitSet asciiQueryChars;

    static {
        char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
        char[] c_punct = ",;:$&+=".toCharArray();
        char[] c_reserved = "?/[]@".toCharArray();//plus punct

        asciiQueryChars = new BitSet(128);
        for (char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int) c);
        for (char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int) c);
        for (char c = '0'; c <= '9'; c++) asciiQueryChars.set((int) c);
        for (char c : c_unreserved) asciiQueryChars.set((int) c);
        for (char c : c_punct) asciiQueryChars.set((int) c);
        for (char c : c_reserved) asciiQueryChars.set((int) c);

        asciiQueryChars.set((int) '%');//leave existing percent escapes in place
    }
}

