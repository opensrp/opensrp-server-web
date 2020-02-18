package org.opensrp.web.security;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;

import org.opensrp.domain.custom.Role;
import org.opensrp.domain.custom.User;
import org.opensrp.web.custom.service.CustomUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.convert.Converter;

@Component
public class DrishtiAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory.getLogger(DrishtiAuthenticationProvider.class.toString());

	public static final String INVALID_CREDENTIALS = "The username or password you entered is incorrect. Please enter the correct credentials.";

	public static final String USER_NOT_FOUND = "The username or password you entered is incorrect. Please enter the correct credentials.";

	public static final String USER_NOT_ACTIVATED = "The user has been registered but not activated. Please contact your local administrator.";

	public static final String INTERNAL_ERROR = "Failed to authenticate user due to internal server error. Please try again later.";

	protected static final String AUTH_HASH_KEY = "_auth";

	private static final String GET_ALL_EVENTS_ROlE = "OpenSRP: Get All Events";

	//private OpenmrsUserService openmrsUserService;
	
	//@Autowired
	private CustomUserService customUserService;

	/*@Resource(name = "redisTemplate")
	private HashOperations<String, String, Authentication> hashOps;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;*/

	@Value("#{opensrp['opensrp.authencation.cache.ttl']}")
	private int cacheTTL;

	@Autowired
	public DrishtiAuthenticationProvider(CustomUserService customUserService) {
		//this.openmrsUserService = openmrsUserService;
		//this.passwordEncoder = passwordEncoder;
		this.customUserService = customUserService;		
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userAddress = ((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress();
		String key = userAddress + authentication.getName();
		/*if (hashOps.hasKey(key, AUTH_HASH_KEY)) {
			Authentication auth = hashOps.get(key, AUTH_HASH_KEY);
			// if credentials is same as cached returned cached else eject cached
			// authentication
			if (auth.getCredentials().equals(authentication.getCredentials()))
				return auth;
			else
				hashOps.delete(key, AUTH_HASH_KEY);

		}*/
		User user = getDrishtiUser(authentication, authentication.getName());
		// get user after authentication
		if (user == null) {
			throw new BadCredentialsException(USER_NOT_FOUND);
		}

		/*if (user.getVoided() != null && user.getVoided()) {
			throw new BadCredentialsException(USER_NOT_ACTIVATED);
		}*/

		Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getName(),
				authentication.getCredentials(), getRolesAsAuthorities(user));
		/*hashOps.put(key, AUTH_HASH_KEY, auth);
		redisTemplate.expire(key, cacheTTL, TimeUnit.SECONDS);*/
		
		return auth;

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
				&& authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	protected List<SimpleGrantedAuthority> getRolesAsAuthorities(User user) {
		List<String> roles = new ArrayList<String>();
				
		for(Role role : user.getRoles()){
			roles.add(role.getName());
		}		
		
		return Lambda.convert(roles, new Converter<String, SimpleGrantedAuthority>() {

			@Override
			public SimpleGrantedAuthority convert(String role) {
				if (GET_ALL_EVENTS_ROlE.equals(role))
					return new SimpleGrantedAuthority("ROLE_ALL_EVENTS");
				return new SimpleGrantedAuthority("ROLE_OPENMRS");
			}
		});
	}

	public User getDrishtiUser(Authentication authentication, String username) {
		User user = null;
		if (authentication instanceof OAuth2Authentication) {
			if (!((OAuth2Authentication) authentication).getUserAuthentication().isAuthenticated()) {
				throw new BadCredentialsException(INVALID_CREDENTIALS);
			}
		} else {
			user = (User) checkIsAuthenticated(authentication.getName(), authentication.getCredentials().toString());
		}

		try {
			/*boolean response = openmrsUserService.deleteSession(authentication.getName(),
					authentication.getCredentials().toString());*/

			//user = openmrsUserService.getUser(username);
			//user = new User("12345");
			if (user == null) {
				logger.error(format("{0}. Exception: {1}", INTERNAL_ERROR, "Unable to clear session"));
			}
		} catch (Exception e) {
			logger.error(format("{0}. Exception: {1}", INTERNAL_ERROR, e));
			e.printStackTrace();
			//throw new BadCredentialsException(INTERNAL_ERROR);
		}
		/*Set<Role> r = new HashSet<>();
		r.add(new Role("ROLE_OPENMRS"));		
		user.setRoles(r);*/
		return user;
	}

	private User checkIsAuthenticated(String username, String password) {
		User result = null;
		
		try {
			result = customUserService.authenticate(username, password);
			//Boolean isAuthenticated = openmrsUserService.authenticate(username, password);
			if (result == null) {
				throw new BadCredentialsException(INTERNAL_ERROR);
			} 
		} catch (Exception e) {
			logger.error(format("{0}. Exception: {1}", INTERNAL_ERROR, e));
			e.printStackTrace();
			throw new BadCredentialsException(INTERNAL_ERROR);
		}
		return result;
	}
}
