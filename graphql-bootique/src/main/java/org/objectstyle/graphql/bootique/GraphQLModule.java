package org.objectstyle.graphql.bootique;

import com.google.inject.*;
import io.bootique.jersey.JerseyModule;
import io.bootique.ConfigModule;

import org.apache.cayenne.configuration.server.ServerRuntime;

import org.objectstyle.graphql.rest.json.JacksonReaderWriter;
import org.objectstyle.graphql.rest.json.JsonReader;
import org.objectstyle.graphql.rest.json.JsonWriter;
import org.objectstyle.graphql.cayenne.orm.DefaultSchemaTranslator;
import org.objectstyle.graphql.cayenne.orm.SchemaTranslator;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class GraphQLModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {

		binder.bind(JacksonReaderWriter.class).in(Singleton.class);
		binder.bind(JsonReader.class).to(JacksonReaderWriter.class);
		binder.bind(JsonWriter.class).to(JacksonReaderWriter.class);

        JerseyModule.contributeFeatures(binder).addBinding().toProvider(GraphQLFeatureProvider.class);
	}

	@Provides
	@Singleton
	GraphQL createGraphQL( SchemaTranslator translator) {
		GraphQLSchema schema = translator.toGraphQL();
        return new GraphQL(schema);
	}

	@Provides
	@Singleton
	SchemaTranslator createSchemaTranslator(ServerRuntime cayenneRuntime) {
		return new DefaultSchemaTranslator(cayenneRuntime.newContext());
	}
}
