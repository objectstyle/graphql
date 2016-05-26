package org.objectstyle.graphql.cayenne.orm;

import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.util.ArrayList;
import java.util.Collection;

class Entity {
    private ObjEntity objEntity;
    private Collection<ObjAttribute> attributes = new ArrayList<>();
    private Collection<ObjRelationship> relationships = new ArrayList<>();

    Entity(ObjEntity objEntity) {
        this.objEntity = objEntity;
    }

    ObjEntity getObjEntity() {
        return this.objEntity;
    }

    Collection<ObjAttribute> getAttributes() {
        return this.attributes;
    }

    void addAttributes(Collection<ObjAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    Collection<ObjRelationship> getRelationships() {
        return this.relationships;
    }

    void addRelationships(Collection<ObjRelationship> relationships) {
        this.relationships.addAll(relationships);
    }
}
