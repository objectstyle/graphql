package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.util.*;

public class EntityBuilder {

    public enum ConfigureType {
        INCLUDE_OBJECT, EXCLUDE_OBJECT
    }

    private ObjectContext objectContext;

    private final List<String> includeEntities = new ArrayList<>();
    private final List<String> excludeEntities = new ArrayList<>();

    private Map<String, List<String>> includeProperties = new HashMap<>();
    private Map<String, List<String>> excludeProperties = new HashMap<>();

    private Map<String, List<String>> includeArguments = new HashMap<>();
    private Map<String, List<String>> excludeArguments = new HashMap<>();

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

    Map<String, List<String>> getArguments(ConfigureType type) {
        return type == ConfigureType.INCLUDE_OBJECT ? includeArguments : excludeArguments;
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
                    if (isValidProperty(oe.getName(), oa.getName())) {
                        entity.addAttributes(Collections.singletonList(oa));
                        if(isValidArgument(oe.getName(), oa.getName())) {
                            entity.addArgument(oa.getName());
                        }
                    }
                }

                for (ObjRelationship or : oe.getRelationships()) {
                    if (isValidProperty(oe.getName(), or.getName()) && isValidEntity(or.getTargetEntityName())) {
                        entity.addRelationships(Collections.singletonList(or));

                        if(isValidArgument(oe.getName(), or.getName())) {
                            entity.addArgument(or.getName());
                        }
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

        if (!excludeEntities.isEmpty()) {
            if (excludeEntities.contains(entity)) {
                return false;
            }
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

    private boolean isValidArgument(String entity, String arg) {
        if (!includeArguments.isEmpty() && includeArguments.containsKey(entity) && !includeArguments.get(entity).contains(arg)) {
            return false;
        }

        if (!excludeArguments.isEmpty() && excludeArguments.containsKey(entity) && excludeArguments.get(entity).contains(arg)) {
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

        public Builder configureEntities(ConfigureType type, String... entities){
            (type == ConfigureType.INCLUDE_OBJECT ? entityBuilder.includeEntities : entityBuilder.excludeEntities).addAll(Arrays.asList(entities));

            return this;
        }

        @SafeVarargs
        public final Builder configureEntities(ConfigureType type, Class<? extends CayenneDataObject>... entities) {
            Arrays.asList(entities).forEach(e -> {
                ObjEntity oe = getObjEntityByClass(e);
                if (oe != null) {
                    (type == ConfigureType.INCLUDE_OBJECT ? entityBuilder.includeEntities : entityBuilder.excludeEntities).add(oe.getName());
                }
            });
            return this;
        }

        private ObjEntity getObjEntityByClass(Class<? extends CayenneDataObject> entity) {
            return entityBuilder.objectContext.getEntityResolver().getObjEntity(((Class) entity).getSimpleName());
        }

        private void addValueToMap(Map<String, List<String>> map, String entity, String... properties){
            map.computeIfAbsent(entity, s -> new ArrayList<>());

            map.get(entity).addAll(Arrays.asList(properties));
        }

        public Builder configureProperties(ConfigureType type, String entity, String... properties) {
            addValueToMap(type == ConfigureType.INCLUDE_OBJECT ? entityBuilder.includeProperties : entityBuilder.excludeProperties, entity, properties);
            return this;
        }

        public Builder configureProperties(ConfigureType type, Class<? extends CayenneDataObject> entity, String... properties) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                configureProperties(type, oe.getName(), properties);
            }

            return this;
        }

        public Builder configureArguments(ConfigureType type, String entity, String... arguments) {
            addValueToMap(type == ConfigureType.INCLUDE_OBJECT ? entityBuilder.includeArguments : entityBuilder.excludeArguments, entity, arguments);
            return this;
        }

        public Builder configureArguments(ConfigureType type, Class<? extends CayenneDataObject> entity, String... arguments) {
            ObjEntity oe = getObjEntityByClass(entity);
            if (oe != null) {
                configureArguments(type, oe.getName(), arguments);
            }

            return this;
        }

        public EntityBuilder build() {
            return entityBuilder.initialize();
        }
    }
}
