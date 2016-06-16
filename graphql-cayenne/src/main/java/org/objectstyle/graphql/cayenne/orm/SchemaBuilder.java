package org.objectstyle.graphql.cayenne.orm;

import graphql.Scalars;
import graphql.schema.*;

import org.apache.cayenne.CayenneDataObject;
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

    private Map<String, Class<? extends DataFetcher>> dataFetchers = new HashMap<>();

    private EntityBuilder entityBuilder;

    private Map<String, Select<?>> queries = new HashMap<>();

    private SchemaBuilder() {
    }

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

        Set entityTypes = entityTypes();
        GraphQLObjectType queryType = queryType(entityTypes);

        GraphQLObjectType mutationType = mutationType(entityTypes);

        graphQLSchema = GraphQLSchema.newSchema().query(queryType).mutation(mutationType).build(entityTypes);

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

        // naive... root type should be a user-visible builder
        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            et.getFieldDefinitions().forEach(fd -> {
                if (fd.getType() instanceof GraphQLScalarType) {
                    argList.add(GraphQLArgument
                            .newArgument()
                            .name(fd.getName())
                            .type((GraphQLInputType) fd.getType())
                            .build());
                }
            });

            argList.addAll(createDefaultFilters());

            // ... create search by field operations for all entities
            GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
                    .name(et.getName())
                    .type(new GraphQLList(et))
                    .argument(argList)
                    .dataFetcher(getDataFetcher(et.getName()))
                    .build();

            typeBuilder.field(f);
        });

        queries.forEach((k, v) -> {
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

        entityTypes.forEach(et -> {
            List<GraphQLArgument> argList = new ArrayList<>();

            et.getFieldDefinitions().forEach(fd -> {
                if (fd.getType() instanceof GraphQLScalarType) {
                    argList.add(GraphQLArgument
                            .newArgument()
                            .name(fd.getName())
                            .type((GraphQLInputType) fd.getType())
                            .build());
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

    private Set<GraphQLObjectType> entityTypes() {

        Set<GraphQLObjectType> types = new HashSet<>();

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
                    e.getAttributes().forEach(tea -> argList.add(GraphQLArgument
                            .newArgument()
                            .name(tea.getName())
                            .type(mapType(tea.getJavaClass()))
                            .build()));

                    argList.addAll(createDefaultFilters());
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
        private EntityBuilder.Builder entityBuilder;

        private Builder(ObjectContext objectContext) {
            this.schemaBuilder = new SchemaBuilder();
            this.schemaBuilder.objectContext = objectContext;

            this.entityBuilder = EntityBuilder.builder(objectContext);
        }

        private ObjEntity getObjEntityByClass(Class<? extends CayenneDataObject> entity) {
            return this.schemaBuilder.objectContext.getEntityResolver().getObjEntity(((Class) entity).getSimpleName());
        }

        public Builder dataFetcher(String entity, Class<? extends DataFetcher> dataFetcher) {
            schemaBuilder.dataFetchers.put(entity, dataFetcher);
            return this;
        }

        public Builder dataFetcher(Class<? extends CayenneDataObject> entity, Class<? extends DataFetcher> dataFetcher) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                return dataFetcher(oe.getName(), dataFetcher);
            }
            return this;
        }

        public Builder query(String propertyName, Select<?> query) {
            schemaBuilder.queries.put(propertyName, query);
            return this;
        }

        public Builder includeEntities(String... entities) {
            this.entityBuilder.includeEntities(entities);
            return this;
        }

        @SafeVarargs
        public final Builder includeEntities(Class<? extends CayenneDataObject>... entities) {
            this.entityBuilder.includeEntities(entities);
            return this;
        }

        public Builder excludeEntities(String... entities) {
            this.entityBuilder.excludeEntities(entities);
            return this;
        }

        @SafeVarargs
        public final Builder excludeEntities(Class<? extends CayenneDataObject>... entities) {
            this.entityBuilder.excludeEntities(entities);
            return this;
        }

        public Builder includeEntityProperty(Class<? extends CayenneDataObject> entity, String... properties) {
            this.entityBuilder.includeEntityProperty(entity, properties);
            return this;
        }

        public Builder includeEntityProperty(String entity, String... properties) {
            this.entityBuilder.includeEntityProperty(entity, properties);
            return this;
        }

        public Builder excludeEntityProperty(Class<? extends CayenneDataObject> entity, String... properties) {
            this.entityBuilder.excludeEntityProperty(entity, properties);
            return this;
        }

        public Builder excludeEntityProperty(String entity, String... properties) {
            this.entityBuilder.excludeEntityProperty(entity, properties);
            return this;
        }

        public GraphQLSchema build() {
            this.schemaBuilder.entityBuilder = this.entityBuilder.build();
            return schemaBuilder.initialize().getGraphQLSchema();
        }
    }
}
