package com.example.codedockbackend.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter implements Filter {
    public static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            // you can also propagate incoming header X-Trace-Id if present:
            if (request instanceof HttpServletRequest) {
                String incoming = ((HttpServletRequest) request).getHeader("X-Trace-Id");
                if (incoming != null && !incoming.isBlank()) {
                    MDC.put(TRACE_ID, incoming);
                }
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
