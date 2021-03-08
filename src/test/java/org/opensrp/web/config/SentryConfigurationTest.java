package org.opensrp.web.config;

import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

import io.sentry.Sentry;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Sentry.class)
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
		PowerMockito.mockStatic(Sentry.class);
		WhiteboxImpl.setInternalState(sentryConfiguration, "dsn", "https://examplePublicKey.sdsd.w/0");
		sentryConfiguration.initialize();
		verify(sentryConfiguration, atMostOnce()).initializeSentry();
	}
}
