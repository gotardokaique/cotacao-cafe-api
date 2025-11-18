package com.api.cotacao.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    // ANSI colors
    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    private final boolean developerMode;

    public RequestLoggingInterceptor(@Value("${app.developer:false}") boolean developerMode) {
        this.developerMode = developerMode;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        if (!developerMode) {
            return true;
        }

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        if (handler instanceof HandlerMethod handlerMethod) {
            String controller = handlerMethod.getBeanType().getSimpleName();
            String method = handlerMethod.getMethod().getName();

            String methodColored    = GREEN  + httpMethod + RESET;
            String endpointColored  = YELLOW + "\"" + uri + "\"" + RESET;
            String handlerColored   = GREEN  + controller + "." + method + RESET;

            log.info("[DEV][HTTP] {} - {} - {}",
                    methodColored,
                    endpointColored,
                    handlerColored
            );
        } else {
            String methodColored   = GREEN  + httpMethod + RESET;
            String endpointColored = YELLOW + "\"" + uri + "\"" + RESET;

            log.info("[DEV][HTTP] {} - {}",
                    methodColored,
                    endpointColored
            );
        }

        return true;
    }
}
