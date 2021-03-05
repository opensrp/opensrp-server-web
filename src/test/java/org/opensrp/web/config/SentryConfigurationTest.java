package org.opensrp.web.config;

import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.internal.WhiteboxImpl;

import io.sentry.Sentry;

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
		WhiteboxImpl.setInternalState(sentryConfiguration, "dsn", "https://examplePublicKey@o0.ingest.sentry.io/0");
		sentryConfiguration.initialize();
		verify(sentryConfiguration, atMostOnce()).initializeSentry();
		Sentry.close();
	}
}
