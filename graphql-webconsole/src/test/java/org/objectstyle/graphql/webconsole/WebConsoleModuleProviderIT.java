package org.objectstyle.graphql.webconsole;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.assertEquals;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.junit.Test;

import io.bootique.BQModuleProvider;

public class WebConsoleModuleProviderIT {

    @Test
    public void testPresentInJar() {
        long c = StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
                .filter(p -> p instanceof WebConsoleModuleProvider).collect(counting());
        assertEquals("No provider found", 1, c);
    }
}
