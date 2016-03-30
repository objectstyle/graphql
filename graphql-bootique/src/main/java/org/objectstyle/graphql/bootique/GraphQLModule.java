package org.objectstyle.graphql.bootique;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.glassfish.jersey.server.ResourceConfig;
import org.objectstyle.graphql.rest.GraphQLFeature;
import org.objectstyle.graphql.rest.json.JacksonReaderWriter;
import org.objectstyle.graphql.rest.json.JsonReader;
import org.objectstyle.graphql.rest.json.JsonWriter;
import org.objectstyle.graphql.cayenne.orm.DefaultSchemaTranslator;
import org.objectstyle.graphql.cayenne.orm.SchemaTranslator;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.jersey.JerseyBinder;
import com.nhl.bootique.jersey.JerseyServlet;
import com.nhl.bootique.jersey.JerseyServletFactory;
import com.nhl.bootique.jetty.MappedServlet;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class GraphQLModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {

		binder.bind(JacksonReaderWriter.class).in(Singleton.class);
		binder.bind(JsonReader.class).to(JacksonReaderWriter.class);
		binder.bind(JsonWriter.class).to(JacksonReaderWriter.class);

		JerseyBinder.contributeTo(binder).features(GraphQLFeature.class);
	}

	@Provides
	@Singleton
	GraphQL createGraphQL(ServerRuntime cayenneRuntime, SchemaTranslator translator) {
		GraphQLSchema schema = translator.toGraphQL(cayenneRuntime.getChannel().getEntityResolver());
		return new GraphQL(schema);
	}

	/**
	 * Overrides JerseyModule JerseyServlet definition with the one mapped to
	 * "/graphql" path.
	 * 
	 * @param configFactory
	 *            Bootique configuration factory.
	 * @param config
	 *            Jersey config object.
	 * @return Jersey servlet as {@link MappedServlet}.
	 */
	@JerseyServlet
	@Provides
	@Singleton
	MappedServlet createJerseyServlet(ConfigurationFactory configFactory, ResourceConfig config) {
		return configFactory.config(JerseyServletFactory.class, configPrefix).initServletPathIfNotSet("/graphql/*")
				.createJerseyServlet(config);
	}

	@Provides
	@Singleton
	SchemaTranslator createSchemaTranslator(ServerRuntime cayenneRuntime) {
		return new DefaultSchemaTranslator(cayenneRuntime.newContext());
	}

}
