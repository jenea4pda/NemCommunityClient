package org.nem.deploy;

import org.nem.core.deploy.*;
import org.nem.ncc.deploy.appconfig.NccAppConfig;
import org.nem.ncc.web.servlet.*;

import org.apache.commons.cli.Option;
import org.hamcrest.core.*;
import org.junit.*;

import java.util.ArrayList;

public class NccConfigurationPolicyTest {
	//region get class

	@Test
	public void canGetNisAppConfigClass() {
		// Arrange:
		final NccConfigurationPolicy policy = createPolicy();

		// Assert:
		Assert.assertThat(policy.getAppConfigClass(), IsEqual.equalTo(NccAppConfig.class));
	}

	@Test
	public void canGetNisWebAppInitializerClass() {
		// Arrange:
		final NccConfigurationPolicy policy = createPolicy();

		// Assert:
		Assert.assertThat(policy.getWebAppInitializerClass(), IsEqual.equalTo(NccWebAppInitializer.class));
	}

	@Test
	public void canGetJarFileServletClass() {
		// Arrange:
		final NccConfigurationPolicy policy = createPolicy();

		// Assert:
		Assert.assertThat(policy.getJarFileServletClass(), IsEqual.equalTo(JarFileServlet.class));
	}

	@Test
	public void canGetDefaultServletClass() {
		// Arrange:
		final NccConfigurationPolicy policy = createPolicy();

		// Assert:
		Assert.assertThat(policy.getDefaultServletClass(), IsEqual.equalTo(NccDefaultServlet.class));
	}

	//endregion

	@Test
	public void loadConfigReturnsValidConfiguration() {
		// Arrange:
		final NccConfigurationPolicy policy = createPolicy();

		// Act:
		final CommonConfiguration configuration = policy.loadConfig(new NemCommandLine(new ArrayList<>()));

		// Assert:
		Assert.assertThat(configuration, IsNull.notNullValue());
	}

	private static NccConfigurationPolicy createPolicy() {
		return new NccConfigurationPolicy();
	}
}
