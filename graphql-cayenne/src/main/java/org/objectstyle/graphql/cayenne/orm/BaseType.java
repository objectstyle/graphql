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

        public Builder configureEntities(EntityBuilder.ConfigureType type, String... entities) {
            this.entityBuilder.configureEntities(type, entities);
            return this;
        }

        @SafeVarargs
        public final Builder configureEntities(EntityBuilder.ConfigureType type, Class<? extends CayenneDataObject>... entities) {
            this.entityBuilder.configureEntities(type, entities);
            return this;
        }

        public Builder configureProperties(EntityBuilder.ConfigureType type, Class<? extends CayenneDataObject> entity, String... properties) {
            this.entityBuilder.configureProperties(type, entity, properties);
            return this;
        }

        public Builder configureProperties(EntityBuilder.ConfigureType type, String entity, String... properties) {
            this.entityBuilder.configureProperties(type, entity, properties);
            return this;
        }

        public Builder configureArguments(EntityBuilder.ConfigureType type, Class<? extends CayenneDataObject> entity, String... arguments) {
            this.entityBuilder.configureArguments(type, entity, arguments);
            return this;
        }

        public Builder configureArguments(EntityBuilder.ConfigureType type, String entity, String... arguments) {
            this.entityBuilder.configureArguments(type, entity, arguments);
            return this;
        }

        public BaseType build() {
            this.baseType.entityBuilder = entityBuilder.build();
            return this.baseType;
        }
    }
}

