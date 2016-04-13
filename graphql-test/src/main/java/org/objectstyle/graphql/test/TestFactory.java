package org.objectstyle.graphql.test;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.dbsync.CreateIfNoSchemaStrategy;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.query.SQLTemplate;

import org.apache.commons.io.FileUtils;

public class TestFactory {

    private ServerRuntime serverRuntime;
    private ObjectContext objectContext;

    public TestFactory(){
        createServerRuntime();
    }

    private void createServerRuntime() {
        removeDerbyDirectory();

        serverRuntime = ServerRuntimeBuilder.builder().addConfig("cayenne-tests.xml")
                .url("jdbc:derby:target/derby;create=true").jdbcDriver("org.apache.derby.jdbc.EmbeddedDriver")
                .addModule(cayenneExtensions()).build();

        objectContext = serverRuntime.newContext();

        insertTestData();
    }

    public void stopServerRuntime() {
        serverRuntime.shutdown();
        serverRuntime = null;

        stopDerby();
    }

    private static org.apache.cayenne.di.Module cayenneExtensions() {
        return (b) -> b.bind(SchemaUpdateStrategy.class).to(CreateIfNoSchemaStrategy.class);
    }

    private void insertTestData() {
        insert("e1", "id, name", "1, 'a'");
        insert("e1", "id, name", "2, 'b'");

        insert("e2", "id, name, e1_id", "3, 'c', 1");
        insert("e2", "id, name, e1_id", "4, 'd', 2");
        insert("e2", "id, name, e1_id", "5, 'e', 2");

        insert("e3", "id, name, e2_id", "6, 'f', 4");
        insert("e3", "id, name, e2_id", "7, 'g', 4");
        insert("e3", "id, name, e2_id", "8, 'h', 5");
    }

    private void insert(String table, String columns, String values) {
        String insertSql = "INSERT INTO utest." + table + " (" + columns + ") VALUES (" + values + ")";
        objectContext.performGenericQuery(new SQLTemplate(table, insertSql));
    }

    private void removeDerbyDirectory() {

        File derbyDir = new File("target/derby");
        if (derbyDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(derbyDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void stopDerby() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ignored) {
        }
    }

    public ObjectContext getObjectContext() {
        return objectContext;
    }

    public ServerRuntime getServerRuntime() {
        return serverRuntime;
    }
}
