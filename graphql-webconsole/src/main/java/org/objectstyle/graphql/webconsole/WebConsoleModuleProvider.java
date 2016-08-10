package org.objectstyle.graphql.webconsole;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class WebConsoleModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new WebConsoleModule();
	}
}
