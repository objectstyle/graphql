package org.objectstyle.graphql.example.cayenne.auto;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;
import org.objectstyle.graphql.example.cayenne.E2;

/**
 * Class _E3 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E3 extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final Property<Integer> E2_ID = new Property<Integer>("e2_id");
    public static final Property<Integer> ID = new Property<Integer>("id");
    public static final Property<String> NAME = new Property<String>("name");
    public static final Property<org.objectstyle.graphql.example.cayenne.E2> E2 = new Property<E2>("e2");

    public void setE2_id(Integer e2_id) {
        writeProperty("e2_id", e2_id);
    }
    public Integer getE2_id() {
        return (Integer)readProperty("e2_id");
    }

    public void setId(Integer id) {
        writeProperty("id", id);
    }
    public Integer getId() {
        return (Integer)readProperty("id");
    }

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setE2(E2 e2) {
        setToOneTarget("e2", e2, true);
    }

    public E2 getE2() {
        return (E2)readProperty("e2");
    }


}
