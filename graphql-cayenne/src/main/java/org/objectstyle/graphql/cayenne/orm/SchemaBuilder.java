package org.objectstyle.graphql.cayenne.orm;

import graphql.Scalars;
import graphql.schema.*;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLSelect;
import org.apache.cayenne.query.Select;
import org.apache.cayenne.query.SelectQuery;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SchemaBuilder {
    private ConcurrentMap<Class<?>, GraphQLScalarType> typeCache;
    private GraphQLSchema graphQLSchema;
    private ObjectContext objectContext;
    private QueryType queryType = null;
    private MutationType mutationType = null;

    private Map<String, Class<? extends DataFetcher>> dataFetchers = null;

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

        if(queryType == null) {
            queryType = QueryType.builder(objectContext).build();
        }

        dataFetchers = queryType.getDataFetchers();

        Set queryTypes = entityTypes(queryType);

        GraphQLSchema.Builder graphQLSchemaBuilder = GraphQLSchema.newSchema().query(queryType(queryTypes));

        if(mutationType != null) {
            Set mutationTypes = entityTypes(mutationType);

            graphQLSchemaBuilder.mutation(mutationType(mutationTypes));
        }

        graphQLSchema = graphQLSchemaBuilder.build();

        return this;
    }

    private DataFetcher getDataFetcher(String entity) {
        DataFetcher df;

        try {
            if (dataFetchers.containsKey(entity)) {
                df = dataFetchers.get(entity).getConstructor(ObjectContext.class).newInstance(objectContext);
            } else {
                df = DefaultDataFetcher.class.getConstructor(ObjectContext.class).newInstance(objectContext);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return df;
    }

    private GraphQLObjectType queryType(Set<GraphQLObjectType> entityTypes) {
        GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name("queryType");

        EntityBuilder entityBuilder =  queryType.getEntityBuilder();

        // naive... root type should be a user-visible builder
        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            Entity e = entityBuilder.getEntityByName(et.getName());

            et.getFieldDefinitions().forEach(fd -> {
                if (fd.getType() instanceof GraphQLScalarType) {
                    if(e.getArguments().contains(fd.getName())) {
                        argList.add(GraphQLArgument
                                .newArgument()
                                .name(fd.getName())
                                .type((GraphQLInputType) fd.getType())
                                .build());
                    }
                }
            });

            argList.addAll(addAdditionalArguments());

            // ... create search by field operations for all entities
            GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                    .name(et.getName())
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(getDataFetcher(et.getName()))
                    .build();

            typeBuilder.field(f);
        });

        queryType.getQueries().forEach((k, v) -> {
            GraphQLObjectType ot = null;

            ObjEntity oe = v.getMetaData(objectContext.getEntityResolver()).getObjEntity();

            for (GraphQLObjectType o : entityTypes) {
                if (o.getName().equals(oe.getName())) {
                    ot = o;
                    break;
                }
            }

            if (ot != null) {
                List<GraphQLArgument> argList = new ArrayList<>();

                Expression expr = null;

                if (v instanceof SelectQuery) {
                    expr = ((SelectQuery) v).getQualifier();
                }

                if (v instanceof ObjectSelect) {
                    expr = ((ObjectSelect) v).getWhere();
                }

                if(expr != null) {
                    for (int i = 0; i < expr.getOperandCount(); i++) {
                        Object operand = expr.getOperand(i);

                        if (operand instanceof ASTObjPath) {
                            String path = ((ASTObjPath) operand).getPath();

                            ot.getFieldDefinitions().forEach(fd -> {
                                if (fd.getName().equals(path) && fd.getType() instanceof GraphQLScalarType) {
                                    argList.add(GraphQLArgument
                                            .newArgument()
                                            .name(fd.getName())
                                            .type((GraphQLInputType) fd.getType())
                                            .build());
                                }
                            });
                        }
                    }
                }

                if(v instanceof SQLSelect) {
                    GraphQLObjectType finalOt = ot;
                    ((SQLSelect) v).getParams().forEach((pk, pv) -> {
                        finalOt.getFieldDefinitions().forEach(fd -> {
                            if (fd.getName().equals(pk) && fd.getType() instanceof GraphQLScalarType) {
                                argList.add(GraphQLArgument
                                        .newArgument()
                                        .name(fd.getName())
                                        .type((GraphQLInputType) fd.getType())
                                        .build());
                            }
                        });
                    });
                }

                GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                        .name(k)
                        .type(new GraphQLList(ot))
                        .argument(argList)
                        .dataFetcher(new CustomQueryDataFetcher(objectContext, v))
                        .build();

                typeBuilder.field(f);
            }
        });

        return typeBuilder.build();
    }

    private GraphQLObjectType mutationType(Set<GraphQLObjectType> entityTypes) {
        GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name("mutationType");

        EntityBuilder entityBuilder =  mutationType.getEntityBuilder();

        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            Entity e = entityBuilder.getEntityByName(et.getName());

            et.getFieldDefinitions().forEach(fd -> {
                if (fd.getType() instanceof GraphQLScalarType) {
                    if(e.getArguments().contains(fd.getName())) {
                        argList.add(GraphQLArgument
                                .newArgument()
                                .name(fd.getName())
                                .type((GraphQLInputType) fd.getType())
                                .build());
                    }
                }
            });

            GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                    .name("update" + et.getName())
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(new UpdateDataFetcher(objectContext))
                    .build();

            typeBuilder.field(f);

            f = GraphQLFieldDefinition.newFieldDefinition()
                    .name("delete" + et.getName())
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(new DeleteDataFetcher(objectContext))
                    .build();

            typeBuilder.field(f);
        });

        return typeBuilder.build();
    }

    private Set<GraphQLObjectType> entityTypes(BaseType baseType) {
        Set<GraphQLObjectType> types = new HashSet<>();

        EntityBuilder entityBuilder =  baseType.getEntityBuilder();

        for (Entity oe : entityBuilder.getEntities()) {
            GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name(oe.getObjEntity().getName());

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
                List<GraphQLArgument> argList = new ArrayList<>();

                Entity e = entityBuilder.getEntityByName(or.getTargetEntityName());

                if (e != null) {
                    e.getAttributes().forEach(tea -> {
                        if(e.getArguments().contains(tea.getName())) {
                            argList.add(GraphQLArgument
                                    .newArgument()
                                    .name(tea.getName())
                                    .type(mapType(tea.getJavaClass()))
                                    .build());
                        }
                    });

                    argList.addAll(addAdditionalArguments());
                }

                GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                        .name(or.getName())
                        .argument(or.isToMany() ? argList : new ArrayList<>())
                        .type(or.isToMany() ? new GraphQLList(new GraphQLTypeReference(or.getTargetEntityName())) : new GraphQLTypeReference(or.getTargetEntityName()))
                        .dataFetcher(getDataFetcher(or.getName()))
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

    private List<GraphQLArgument> createDirectivesArguments() {
        List<GraphQLArgument> argList = new ArrayList<>();

        argList.add(GraphQLArgument
                .newArgument()
                .name("if")
                .type(Scalars.GraphQLBoolean)
                .build());

        return argList;
    }

    private List<GraphQLArgument> addAdditionalArguments() {
        List<GraphQLArgument> argList = new ArrayList<>();

        argList.addAll(createDefaultFilters());
        argList.addAll(createDirectivesArguments());

        return argList;
    }

    private GraphQLScalarType mapType(Class<?> javaType) {
        return typeCache.computeIfAbsent(javaType, jt -> Scalars.GraphQLString);
    }

    public static Builder builder(ObjectContext objectContext) {
        return new Builder(objectContext);
    }

    private GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public static class Builder {
        private SchemaBuilder schemaBuilder;

        private Builder(ObjectContext objectContext) {
            this.schemaBuilder = new SchemaBuilder();
            this.schemaBuilder.objectContext = objectContext;
        }

        public Builder queryType(QueryType queryType) {
            this.schemaBuilder.queryType = queryType;
            return this;
        }

        public Builder mutationType(MutationType mutationType) {
            this.schemaBuilder.mutationType = mutationType;
            return this;
        }

        public GraphQLSchema build() {
            return schemaBuilder.initialize().getGraphQLSchema();
        }
    }
}
