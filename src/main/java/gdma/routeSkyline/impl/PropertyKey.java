package gdma.routeSkyline.impl;

public class PropertyKey implements Comparable<PropertyKey> {
    public final String NAME;

    public PropertyKey(String NAME) {
        this.NAME = NAME;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof PropertyKey) {
            return this.NAME.equals(((PropertyKey) anObject).NAME);
        }
        return false;
    }

    @Override
    public int compareTo(PropertyKey o) {
        return this.NAME.compareTo(o.NAME);
    }

    @Override
    public int hashCode() {
        return this.NAME.hashCode();
    }
}