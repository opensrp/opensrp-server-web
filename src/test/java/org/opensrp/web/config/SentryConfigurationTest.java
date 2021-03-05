package org.opensrp.web.config;

import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.internal.WhiteboxImpl;

public class SentryConfigurationTest {
	
	private SentryConfiguration sentryConfiguration;
	
	@Before
	public void setUp() {
		sentryConfiguration = spy(new SentryConfiguration());
	}
	
	@Test
	public void testInitializeShouldNotInitializeSentryIfDsnIsEmpty() {
		WhiteboxImpl.setInternalState(sentryConfiguration, "dsn", "");
		sentryConfiguration.initialize();
		verify(sentryConfiguration, never()).initializeSentry();
	}
	
	@Test
	public void testInitializeShouldInitializeSentryIfDsnIsNotEmpty() {
		WhiteboxImpl.setInternalState(sentryConfiguration, "dsn", "http://23232323.sentry.io/343");
		doNothing().when(sentryConfiguration).initializeSentry();
		sentryConfiguration.initialize();
		verify(sentryConfiguration, atMostOnce()).initializeSentry();
	}
}
