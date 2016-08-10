package org.objectstyle.graphql.bootique;

import java.util.Collection;
import java.util.Collections;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jersey.JerseyModule;

public class GraphQLModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new GraphQLModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(JerseyModule.class);
    }
}
