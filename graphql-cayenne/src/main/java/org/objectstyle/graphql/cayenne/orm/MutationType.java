package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.ObjectContext;

public class MutationType extends BaseType {

    public static MutationType.Builder builder(ObjectContext objectContext) {
        return new MutationType.Builder(objectContext);
    }

    public static class Builder extends BaseType.Builder {
        private MutationType mutationType;

        private Builder(ObjectContext objectContext) {
            super(objectContext);
            this.mutationType = new MutationType();
        }

        public MutationType build() {
            mutationType.setEntityBuilder(super.build().getEntityBuilder());
            return mutationType;
        }
    }
}
