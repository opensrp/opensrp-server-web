package org.opensrp.web.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.sentry.SentryOptions;
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

	@Test(expected = IllegalAccessError.class)
	public void testPopulateTagsShouldThrowExceptionIfTagsInvalid() {
		WhiteboxImpl.setInternalState(sentryConfiguration, "tags", "{sample");
		SentryOptions sentryOptions = mock(SentryOptions.class);
		sentryConfiguration.populateTags(sentryOptions);
		verify(sentryOptions, never()).setTag(anyString(), anyString());
	}

	@Test
	public void testPopulateTagsShouldNotAddTagsIfNotPresent() {
		WhiteboxImpl.setInternalState(sentryConfiguration, "tags", "{}");
		SentryOptions sentryOptions = mock(SentryOptions.class);
		sentryConfiguration.populateTags(sentryOptions);
		verify(sentryOptions, never()).setTag(anyString(), anyString());
	}

	@Test
	public void testPopulateTagsShouldAddTagsToSentryOptions() {
		String releaseName = "release-name";
		String release = "release-a";
		WhiteboxImpl.setInternalState(sentryConfiguration, "tags", String.format("{%s:'%s'}", releaseName, release));
		SentryOptions sentryOptions = mock(SentryOptions.class);
		sentryConfiguration.populateTags(sentryOptions);
		verify(sentryOptions, only()).setTag(eq(releaseName), eq(release));
	}

}
