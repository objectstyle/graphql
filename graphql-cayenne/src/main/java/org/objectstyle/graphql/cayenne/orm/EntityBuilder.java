package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.util.*;

public class EntityBuilder {
    private ObjectContext objectContext;

    private final List<String> includeEntities = new ArrayList<>();
    private final List<String> excludeEntities = new ArrayList<>();

    private Map<String, List<String>> includeProperties = new HashMap<>();
    private Map<String, List<String>> excludeProperties = new HashMap<>();

    private Collection<Entity> entities = new ArrayList<>();

    private EntityBuilder(ObjectContext objectContext) {
        this.objectContext = objectContext;
    }

    ObjectContext getObjectContext() {
        return this.objectContext;
    }

    Collection<Entity> getEntities() {
        return this.entities;
    }

    Entity getEntityByName(String entity) {
        for (Entity e : entities) {
            if (e.getObjEntity().getName().equals(entity))
                return e;
        }

        return null;
    }

    private EntityBuilder initialize() {

        for (ObjEntity oe : objectContext.getEntityResolver().getObjEntities()) {
            if (isValidEntity(oe.getName())) {
                Entity entity = new Entity(oe);

                for (ObjAttribute oa : oe.getAttributes()) {
                    if (isValidProperty(oe.getName(), oa.getName()))
                        entity.addAttributes(Collections.singletonList(oa));
                }

                for (ObjRelationship or : oe.getRelationships()) {
                    if (isValidProperty(oe.getName(), or.getName()) && isValidEntity(or.getTargetEntityName())) {
                        entity.addRelationships(Collections.singletonList(or));
                    }
                }

                entities.add(entity);
            }
        }

        return this;
    }

    private boolean isValidEntity(String entity) {
        if (!includeEntities.isEmpty() && !includeEntities.contains(entity)) {
            return false;
        }

        if (!excludeEntities.isEmpty())
            if (excludeEntities.contains(entity)) {
                return false;
            }

        return true;
    }

    private boolean isValidProperty(String entity, String property) {
        if (!includeProperties.isEmpty() && includeProperties.containsKey(entity) && !includeProperties.get(entity).contains(property)) {
            return false;
        }

        if (!excludeProperties.isEmpty() && excludeProperties.containsKey(entity) && excludeProperties.get(entity).contains(property)) {
            return false;
        }

        return true;
    }

    public static Builder builder(ObjectContext objectContext) {
        return new Builder(objectContext);
    }

    public static class Builder {
        private EntityBuilder entityBuilder;

        private Builder(ObjectContext objectContext) {
            this.entityBuilder = new EntityBuilder(objectContext);
        }

        public Builder includeEntities(String... entities) {
            entityBuilder.includeEntities.addAll(Arrays.asList(entities));
            return this;
        }

        @SafeVarargs
        public final Builder includeEntities(Class<? extends CayenneDataObject>... entities) {
            fillEntitiesList(entityBuilder.includeEntities, entities);
            return this;
        }

        public Builder excludeEntities(String... entities) {
            entityBuilder.excludeEntities.addAll(Arrays.asList(entities));
            return this;
        }

        @SafeVarargs
        public final Builder excludeEntities(Class<? extends CayenneDataObject>... entities) {
            fillEntitiesList(entityBuilder.excludeEntities, entities);
            return this;
        }

        private ObjEntity getObjEntityByClass(Class<? extends CayenneDataObject> entity) {
            return entityBuilder.objectContext.getEntityResolver().getObjEntity(((Class) entity).getSimpleName());
        }

        @SafeVarargs
        private final void fillEntitiesList(List<String> list, Class<? extends CayenneDataObject>... entities) {
            Arrays.asList(entities).forEach(e -> {
                ObjEntity oe = getObjEntityByClass(e);
                if (oe != null) {
                    list.add(oe.getName());
                }
            });
        }

        public Builder includeEntityProperty(Class<? extends CayenneDataObject> entity, String... properties) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                return includeEntityProperty(oe.getName(), properties);
            }
            return this;
        }

        public Builder includeEntityProperty(String entity, String... properties) {
            entityBuilder.includeProperties.computeIfAbsent(entity, s -> new ArrayList<>());

            entityBuilder.includeProperties.get(entity).addAll(Arrays.asList(properties));
            return this;
        }

        public Builder excludeEntityProperty(Class<? extends CayenneDataObject> entity, String... properties) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                return excludeEntityProperty(oe.getName(), properties);
            }
            return this;
        }

        public Builder excludeEntityProperty(String entity, String... properties) {
            entityBuilder.excludeProperties.computeIfAbsent(entity, s -> new ArrayList<>());

            entityBuilder.excludeProperties.get(entity).addAll(Arrays.asList(properties));

            return this;
        }

        public EntityBuilder build() {
            return entityBuilder.initialize();
        }
    }
}
