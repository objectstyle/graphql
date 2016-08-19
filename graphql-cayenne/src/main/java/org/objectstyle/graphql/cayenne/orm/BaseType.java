package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectContext;

public class BaseType {
    private EntityBuilder entityBuilder;

    BaseType() {

    }

    public EntityBuilder getEntityBuilder() {
        return entityBuilder;
    }

    public void setEntityBuilder(EntityBuilder entityBuilder) {
        this.entityBuilder = entityBuilder;
    }

    public static class Builder {
        private BaseType baseType;
        private EntityBuilder.Builder entityBuilder;

        public Builder(ObjectContext objectContext) {
            this.baseType = new BaseType();
            this.entityBuilder = EntityBuilder.builder(objectContext);
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

        public BaseType build() {
            this.baseType.entityBuilder = entityBuilder.build();
            return this.baseType;
        }
    }
}

