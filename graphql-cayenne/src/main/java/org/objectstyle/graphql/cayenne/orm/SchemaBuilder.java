package org.objectstyle.graphql.cayenne.orm;

import graphql.Scalars;
import graphql.schema.*;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SchemaBuilder {
    private ConcurrentMap<Class<?>, GraphQLScalarType> typeCache;
    private GraphQLSchema graphQLSchema;
    private ObjectContext objectContext;

    private final List<String> includeEntities = new ArrayList<>();
    private final List<String> excludeEntities = new ArrayList<>();

    private SchemaBuilder initialize() {
        this.typeCache = new ConcurrentHashMap<>();

        typeCache.put(Boolean.class, Scalars.GraphQLBoolean);

        typeCache.put(String.class, Scalars.GraphQLString);

        typeCache.put(Integer.class, Scalars.GraphQLInt);
        typeCache.put(Integer.TYPE, Scalars.GraphQLInt);
        typeCache.put(Short.class, Scalars.GraphQLInt);
        typeCache.put(Short.TYPE, Scalars.GraphQLInt);
        typeCache.put(Byte.class, Scalars.GraphQLInt);
        typeCache.put(Byte.TYPE, Scalars.GraphQLInt);

        typeCache.put(Long.class, Scalars.GraphQLLong);
        typeCache.put(Long.TYPE, Scalars.GraphQLLong);
        typeCache.put(BigInteger.class, Scalars.GraphQLLong);

        typeCache.put(Float.class, Scalars.GraphQLFloat);
        typeCache.put(Float.TYPE, Scalars.GraphQLFloat);
        typeCache.put(Double.class, Scalars.GraphQLFloat);
        typeCache.put(Double.TYPE, Scalars.GraphQLFloat);
        typeCache.put(BigDecimal.class, Scalars.GraphQLFloat);

        Set entityTypes = entityTypes(objectContext.getEntityResolver());
        GraphQLObjectType rootQueryType = queryType(entityTypes);

        graphQLSchema = GraphQLSchema.newSchema().query(rootQueryType).build(entityTypes);

        return this;
    }

    private GraphQLObjectType queryType(Set<GraphQLObjectType> entityTypes) {
        GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name("root");

        // naive... root type should be a user-visible builder

        // ... create select operations for all entities
        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            et.getFieldDefinitions().forEach(fd -> argList.addAll(fd.getArguments()));

            argList.addAll(createDefaultFilters());

            GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                    .name("all" + et.getName() + "s")
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(new DefaultDataFetcher(et.getName(), objectContext))
                    .build();

            typeBuilder.field(f);
        });

        // ... create search by field operations for all entities
        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            et.getFieldDefinitions().forEach(fd -> argList.addAll(fd.getArguments()));

            argList.addAll(createDefaultFilters());

            GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                    .name(et.getName())
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(new DefaultDataFetcher(et.getName(), objectContext))
                    .build();

            typeBuilder.field(f);
        });

        return typeBuilder.build();
    }

    private Set<GraphQLObjectType> entityTypes(EntityResolver cayenneSchema) {

        Set<GraphQLObjectType> types = new HashSet<>();

        for (ObjEntity oe : cayenneSchema.getObjEntities()) {
            if (!isValidField(oe.getName())) {
                continue;
            }

            GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name(oe.getName());

            // add attributes
            oe.getAttributes().forEach(oa -> {
                GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                        .name(oa.getName())
                        .type(mapType(oa.getJavaClass()))
                        .build();

                typeBuilder.field(f);
            });

            // add relationships
            for (ObjRelationship or : oe.getRelationships()) {
                if (!isValidField(or.getTargetEntityName())) {
                    continue;
                }

                List<GraphQLArgument> argList = new ArrayList<>();

                or.getTargetEntity().getAttributes().forEach(tea -> argList.add(GraphQLArgument
                        .newArgument()
                        .name(tea.getName())
                        .type(mapType(tea.getJavaClass()))
                        .build()));

                argList.addAll(createDefaultFilters());

                GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                        .name(or.getName())
                        .argument(argList)
                        .type(or.isToMany() ? new GraphQLList(new GraphQLTypeReference(or.getTargetEntityName())) : new GraphQLTypeReference(or.getTargetEntityName()))
                        .dataFetcher(new DefaultDataFetcher(or.getTargetEntityName(), objectContext, or.getName()))
                        .build();
                typeBuilder.field(f);
            }

            types.add(typeBuilder.build());
        }

        return types;
    }

    private List<GraphQLArgument> createDefaultFilters() {
        List<GraphQLArgument> argList = new ArrayList<>();

        new DefaultFilters().getFilters().forEach((k, v) -> {
            if (k != FilterType.UNDEFINED) {
                argList.add(GraphQLArgument
                        .newArgument()
                        .name(v)
                        .type(new GraphQLList(Scalars.GraphQLString))
                        .build());
            }
        });

        return argList;
    }

    private GraphQLScalarType mapType(Class<?> javaType) {
        return typeCache.computeIfAbsent(javaType, jt -> Scalars.GraphQLString);
    }

    private boolean isValidField(String name) {
        if (!includeEntities.isEmpty() && !includeEntities.contains(name)) {
            return false;
        }

        if (!excludeEntities.isEmpty() && excludeEntities.contains(name)) {
            return false;
        }

        return true;
    }

    public static Builder newSchemaBuilder() {
        return new Builder();
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public static class Builder {
        private SchemaBuilder schemaBuilder;


        private Builder() {
            this.schemaBuilder = new SchemaBuilder();
        }

        public Builder objectContext(ObjectContext objectContext) {
            schemaBuilder.objectContext = objectContext;
            return this;
        }

        public Builder includeEntities(Object... entities) {
            fillList(schemaBuilder.includeEntities, entities);
            return this;
        }

        public Builder excludeEntities(Object... entities) {
            fillList(schemaBuilder.excludeEntities, entities);
            return this;
        }

        private void fillList(List<String> list, Object... entities) {
            Arrays.asList(entities).forEach(e -> {
                if (e instanceof String) {
                    list.add(e.toString());
                }

                if (e instanceof Class) {
                    ObjEntity oe = schemaBuilder.objectContext.getEntityResolver().getObjEntity(((Class) e).getSimpleName());
                    if (oe != null) {
                        list.add(oe.getName());
                    }
                }
            });
        }

        public GraphQLSchema build() {
            return schemaBuilder.initialize().getGraphQLSchema();
        }
    }
}
