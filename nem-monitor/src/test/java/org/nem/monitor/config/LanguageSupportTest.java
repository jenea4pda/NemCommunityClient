package org.nem.monitor.config;

import org.nem.core.i18n.LanguageSupport;

import org.junit.Test;

public class LanguageSupportTest {

	@Test
	public void languageBundleIsInitialized() {
		// Assert (would throw if languageBundle is not initialized):
		LanguageSupport.message("tooltip.info");
	}
}
