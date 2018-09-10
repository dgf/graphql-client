package org.aplatanao.graphql;

public class Argument {

    private String name;

    private String description;

    private Object defaultValue;

    private TypeRef type;

    public String getName() {
        return name;
    }

    public Argument setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Argument setDescription(String description) {
        this.description = description;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Argument setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public TypeRef getType() {
        return type;
    }

    public Argument setType(TypeRef type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", defaultValue=" + defaultValue +
                ", description='" + description + '\'' +
                '}';
    }
}
