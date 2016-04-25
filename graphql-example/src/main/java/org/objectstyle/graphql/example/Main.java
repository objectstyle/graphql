package org.objectstyle.graphql.example;

import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * GraphQL server runner with example schema.
 */
public class Main {
    private static ObjectContext objectContext;

    public static void main(String[] args) {

        removeDerby();

		BQRuntime runtime = Bootique.app(args).autoLoadModules().createRuntime();

        ServerRuntime serverRuntime = runtime.getInstance(ServerRuntime.class);

        createDymmyData(serverRuntime);

        runtime.getRunner().run();
	}

    private static void createDymmyData(ServerRuntime serverRuntime) {
        objectContext = serverRuntime.newContext();

        for(int i = 0 ; i < 26; i++) {
            String name = String.valueOf((char)(i + 65));

            insert("e1", "id, name",
                    String.format("%d, '%s'", i, name));

            insert("e2", "id, name, e1_id",
                    String.format("%d, '%s', %d", i + 26, String.format("%s%s", name, name), i % 5));

            insert("e3", "id, name, e2_id",
                    String.format("%d, '%s', %d", i + 52, String.format("%s%s%s", name, name, name), 26 + i % 5));
        }
    }

    private static void removeDerby(){
		File derbyDir = new File("target/derby");
		if (derbyDir.isDirectory()) {
			try {
				FileUtils.deleteDirectory(derbyDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

    private static void insert(String table, String columns, String values) {
        String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
        objectContext.performGenericQuery(new SQLTemplate(table, insertSql));
    }
}
