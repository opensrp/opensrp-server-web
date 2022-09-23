package org.opensrp.web.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TokenEndpointRetryInterceptor {
    private static final int MAX_RETRIES = 4;

    @Around("execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.*(..))")
    public Object execute (ProceedingJoinPoint aJoinPoint) throws Throwable {
        Logger logger = LogManager.getLogger(TokenEndpointRetryInterceptor.class.toString());
        int tts = 1000;
        for (int i=0; i<MAX_RETRIES; i++) {
            try {
                logger.info("got intercepted in by TokenEndpointEntrypointInterceptor");
                return aJoinPoint.proceed();
            } catch (DuplicateKeyException e) {
                Thread.sleep(tts);
                tts=tts*2;
            }
        }
        throw new IllegalStateException("Could not execute: " + aJoinPoint.getSignature().getName());
    }

}
