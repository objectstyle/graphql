package org.objectstyle.cayenne.graphql.orm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.EntityResolver;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchema.Builder;
import graphql.schema.GraphQLTypeReference;

public class DefaultSchemaTranslator implements SchemaTranslator {

	private ConcurrentMap<Class<?>, GraphQLOutputType> typeCache;
	private ObjectContext selectContext;

	public DefaultSchemaTranslator(ObjectContext selectContext) {
		this.selectContext = selectContext;

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
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public GraphQLSchema toGraphQL(EntityResolver cayenneSchema) {
		Builder builder = GraphQLSchema.newSchema();

		// had to strip generics, as graphql-java is not using collection
		// bounds...
		Set entityTypes = entityTypes(cayenneSchema);
		GraphQLObjectType rootQueryType = queryType(entityTypes);

		return builder.query(rootQueryType).build(entityTypes);
	}
	
	protected GraphQLObjectType queryType(Set<GraphQLObjectType> entityTypes) {
		GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name("root");

		// naive... root type should be a user-visible builder

		// ... create select operations for all entities
		entityTypes.forEach(et -> {
			List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();
			
			et.getFieldDefinitions().forEach(fd ->{
				argList.addAll(fd.getArguments());
			});
			
			argList.addAll(createDefaultFilters());
			
			GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
					.name("all" + et.getName() + "s")
					.type(new GraphQLList(et))
					.argument(argList)
					.dataFetcher(new DefaultDataFetcher(et.getName(), selectContext))
					.build();

			typeBuilder.field(f);
		});
		
		// ... create search by field operations for all entities
		entityTypes.forEach(et -> {
			List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();
			
			et.getFieldDefinitions().forEach(fd ->{
				argList.addAll(fd.getArguments());
			});
			
			argList.addAll(createDefaultFilters());
						
			GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
							.name(et.getName())
							.type(new GraphQLList(et))
							.argument(argList)
							.dataFetcher(new DefaultDataFetcher(et.getName(), selectContext))
							.build();
			
			typeBuilder.field(f);
		});

		return typeBuilder.build();
	}
	
	protected Set<GraphQLObjectType> entityTypes(EntityResolver cayenneSchema) {

		Set<GraphQLObjectType> types = new HashSet<>();
		cayenneSchema.getObjEntities().forEach(oe -> {
			GraphQLObjectType.Builder typeBuilder = GraphQLObjectType.newObject().name(oe.getName());
		
			// add attributes
			oe.getAttributes().forEach(oa -> {
				GraphQLOutputType type = mapAttributeType(oa.getJavaClass());
				GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
						.name(oa.getName())
						.type(type)
						.build();
				
				typeBuilder.field(f);
			});

			// add relationships
			oe.getRelationships().forEach(or -> {
				List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();
				
				or.getTargetEntity().getAttributes().forEach(tea -> {
					argList.add(GraphQLArgument
								.newArgument()
								.name(tea.getName())
								.type(Scalars.GraphQLString)
								.build());
				});
				
				argList.addAll(createDefaultFilters());
				
				GraphQLFieldDefinition f = GraphQLFieldDefinition.newFieldDefinition()
						.name(or.getName())
						.argument(argList)
						.type(new GraphQLList(new GraphQLTypeReference(or.getTargetEntityName())))
						.dataFetcher(new DefaultDataFetcher(or.getTargetEntityName(), selectContext, or))
						.build();
				typeBuilder.field(f);
			});
			
			types.add(typeBuilder.build());
		});

		return types;
	}
	
	protected List<GraphQLArgument> createDefaultFilters(){
		List<GraphQLArgument> argList = new ArrayList<GraphQLArgument>();
		
		new DefaultFilters().getFilters().forEach((k, v) -> {
			if(k != FilterType.UNDEFINED) {
				argList.add(GraphQLArgument
							.newArgument()
							.name(v)
							.type(new GraphQLList(Scalars.GraphQLString))
							.build());
			}
		});

		return argList;
	}
					
	protected GraphQLOutputType mapAttributeType(Class<?> javaType) {

		return typeCache.computeIfAbsent(javaType, jt -> {
			// TODO: weak
			return Scalars.GraphQLString;
		});
	}
}
