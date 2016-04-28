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

public class SchemaBuilder {
    private ConcurrentMap<Class<?>, GraphQLScalarType> typeCache;
    private GraphQLSchema graphQLSchema;
    private ObjectContext objectContext;

    private List<String> includeList;
    private List<String> excludeList;

    SchemaBuilder(ObjectContext objectContext, EntityResolver entityResolver, List<String> includeList, List<String> excludeList) {
        this.objectContext = objectContext;
        this.includeList = includeList;
        this.excludeList = excludeList;

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

        Set entityTypes = entityTypes(entityResolver);
        GraphQLObjectType rootQueryType = queryType(entityTypes);

        graphQLSchema = GraphQLSchema.newSchema().query(rootQueryType).build(entityTypes);
    }

    protected GraphQLObjectType queryType(Set<GraphQLObjectType> entityTypes) {
        GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name("root");

        // naive... root type should be a user-visible builder

        // ... create select operations for all entities
        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();

            et.getFieldDefinitions().forEach(fd -> {
                argList.addAll(fd.getArguments());
            });

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
            List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();

            et.getFieldDefinitions().forEach(fd -> {
                argList.addAll(fd.getArguments());
            });

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

    protected Set<GraphQLObjectType> entityTypes(EntityResolver cayenneSchema) {

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

                List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();

                or.getTargetEntity().getAttributes().forEach(tea -> {
                    argList.add(GraphQLArgument
                            .newArgument()
                            .name(tea.getName())
                            .type(mapType(tea.getJavaClass()))
                            .build());
                });

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

    protected List<GraphQLArgument> createDefaultFilters() {
        List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();

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

    protected GraphQLScalarType mapType(Class<?> javaType) {
        return typeCache.computeIfAbsent(javaType, jt -> {
            return Scalars.GraphQLString;
        });
    }

    protected boolean isValidField(String name) {
        if (!includeList.isEmpty() && !includeList.contains(name)) {
            return false;
        }

        if (!excludeList.isEmpty() && excludeList.contains(name)) {
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
        private ObjectContext objectContext;
        private EntityResolver entityResolver;
        private final List<String> includeList = new ArrayList<>();
        private final List<String> excludeList = new ArrayList<>();

        public Builder objectContext(ObjectContext objectContext) {
            this.objectContext = objectContext;
            return this;
        }

        public Builder entityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
            return this;
        }

        public Builder includeList(List<String> includeList) {
            this.includeList.addAll(includeList);
            return this;
        }

        public Builder excludeList(List<String> excludeList) {
            this.excludeList.addAll(excludeList);

            return this;
        }

        public SchemaBuilder build() {
            return new SchemaBuilder(objectContext, entityResolver, includeList, excludeList);
        }
    }
}
