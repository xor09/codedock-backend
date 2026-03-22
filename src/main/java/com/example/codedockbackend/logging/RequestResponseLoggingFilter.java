package com.example.codedockbackend.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Component
public class RequestResponseLoggingFilter implements Filter{
        private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
        private static final int CACHE_LIMIT = 51200; //50 kb

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {

            if (!(request instanceof HttpServletRequest servletRequest) || !(response instanceof HttpServletResponse servletResponse)) {
                chain.doFilter(request, response);
                return;
            }

            var req = new ContentCachingRequestWrapper(servletRequest, CACHE_LIMIT); //50 kb cache limit
            var res = new ContentCachingResponseWrapper(servletResponse);

            long start = System.currentTimeMillis();

            chain.doFilter(req, res);

            long duration = System.currentTimeMillis() - start;
            String requestBody = new String(req.getContentAsByteArray());
            String responseBody = new String(res.getContentAsByteArray());

            log.info("HTTP {} {} | status={} | duration={}ms | req={} | res={}",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    duration,
                    requestBody,
                    responseBody
            );

            res.copyBodyToResponse(); // important!
        }
}
