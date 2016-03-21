package org.objectstyle.cayenne.graphql.it.fixture;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.QueryChain;
import org.apache.cayenne.query.RefreshQuery;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.objectstyle.cayenne.graphql.GraphQLModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.jersey.JerseyModule;

/**
 * A main superclass of LinkRest unit tests that require full stack Jersey
 * container.
 */
public abstract class GraphQLJerseyTestOnDerby extends JerseyTest {

	protected static ServerRuntime runtime;
	protected static DerbyManager derbyAssembly;

	@BeforeClass
	public static void setUpClass() throws IOException, SQLException {
		GraphQLJerseyTestOnDerby.derbyAssembly = new DerbyManager("target/derby");
		GraphQLJerseyTestOnDerby.runtime = ServerRuntimeBuilder.builder().addConfig("cayenne-tests.xml")
				.url("jdbc:derby:target/derby;create=true").jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver")
				.addModule(cayenneExtensions()).build();
	}

	private static org.apache.cayenne.di.Module cayenneExtensions() {
		return (b) -> {
			b.bind(SchemaUpdateStrategy.class).to(CreateIfNoSchemaStrategy.class);
		};
	}

	@AfterClass
	public static void tearDownClass() throws IOException, SQLException {
		runtime.shutdown();
		runtime = null;

		derbyAssembly.shutdown();
		derbyAssembly = null;
	}

	protected Injector injector;
	protected ObjectContext context;

	public GraphQLJerseyTestOnDerby() throws TestContainerException {
		super(new InMemoryTestContainerFactory());
	}

	@Override
	protected ResourceConfig configure() {
		this.injector = Guice.createInjector(createTestModules());
		return injector.getInstance(ResourceConfig.class);
	}

	protected Iterable<Module> createTestModules() {
		Module m = (b) -> {
			b.bind(ConfigurationFactory.class).toInstance(mock(ConfigurationFactory.class));
			b.bind(ServerRuntime.class).toInstance(runtime);
		};

		Module combined = Modules.override(new JerseyModule()).with(new GraphQLModule());
		return Arrays.asList(m, combined);
	}

	@Before
	public void before() {
		this.context = runtime.newContext();

		// this is to prevent shared caches from returning bogus data between
		// test runs
		context.performQuery(new RefreshQuery());

		QueryChain chain = new QueryChain();

		// ordering is important to avoid FK constraint failures on delete
		chain.addQuery(new EJBQLQuery("delete from E3"));
		chain.addQuery(new EJBQLQuery("delete from E2"));
		chain.addQuery(new EJBQLQuery("delete from E1"));

		context.performGenericQuery(chain);
	}

	protected int intForQuery(String querySql) {
		return SQLSelect.scalarQuery(Integer.class, querySql).selectOne(context).intValue();
	}

	protected void insert(String table, String columns, String values) {
		String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
		context.performGenericQuery(new SQLTemplate(table, insertSql));
	}

	protected Response postGraphQL(String graphQL) {
		String json = "{\"query\":\"" + graphQL + "\"}";
		System.out.println(json);
		Entity<String> e = Entity.entity(json, MediaType.APPLICATION_JSON);
		return target("/").request().post(e);
	}
	
	protected String singleLineJson(Response response) {
		String json = response.readEntity(String.class);
		
		if(json == null) {
			return null;
		}
		return json;
	}
}
