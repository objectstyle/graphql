package org.objectstyle.graphql.example;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLTemplate;
import org.objectstyle.graphql.example.cayenne.E1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import io.bootique.Bootique;

/**
 * GraphQL server runner with example schema.
 */
public class Main implements Module {

	private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		Bootique.app(args).autoLoadModules().module(Main.class).run();
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(TestDataLoader.class).asEagerSingleton();
	}

	static class TestDataLoader {

		@Inject
		public TestDataLoader(ServerRuntime runtime) {
            createDummyData(runtime.newContext());
		}


		private void createDummyData(ObjectContext context) {
            if (ObjectSelect.query(E1.class).selectFirst(context) == null) {
                LOGGER.info("Creating a new dummy data");

                for (int i = 0; i < 26; i++) {
                    String name = String.valueOf((char) (i + 65));
                    insert(context, "e1", "id, name", String.format("%d, '%s'", i, name));
                    insert(context, "e2", "id, name, e1_id",
                            String.format("%d, '%s', %d", i + 26, String.format("%s%s", name, name), i % 5));
                    insert(context, "e3", "id, name, e2_id",
                            String.format("%d, '%s', %d", i + 52, String.format("%s%s%s", name, name, name), 26 + i % 5));
                }
            }
		}

		private void insert(ObjectContext context, String table, String columns, String values) {
			String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
			context.performGenericQuery(new SQLTemplate(table, insertSql));
		}
	}
}
