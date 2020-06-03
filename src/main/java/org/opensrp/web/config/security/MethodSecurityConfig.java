/**
 * 
 */
package org.opensrp.web.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * @author Samuel Githengi created on 06/03/20
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
	
	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		PermissionEvaluator permissionEvaluator = new PermissionEvaluator();
		expressionHandler.setPermissionEvaluator(permissionEvaluator);
		return expressionHandler;
		
	}
	
}
