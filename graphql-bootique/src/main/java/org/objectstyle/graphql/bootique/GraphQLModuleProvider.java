package org.objectstyle.graphql.bootique;

import java.util.Collection;
import java.util.Collections;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;
import com.nhl.bootique.jersey.JerseyModule;

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
