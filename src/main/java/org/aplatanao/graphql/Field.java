package org.aplatanao.graphql;

import java.util.ArrayList;
import java.util.List;

public class Field {

    private String name;

    private String description;

    private FieldType type;

    private List<Argument> args = new ArrayList<>();

    public String getName() {
        return name;
    }

    public Field setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Field setDescription(String description) {
        this.description = description;
        return this;
    }

    public FieldType getType() {
        return type;
    }

    public Field setType(FieldType type) {
        this.type = type;
        return this;
    }

    public List<Argument> getArgs() {
        return args;
    }

    public Field setArgs(List<Argument> args) {
        this.args = args;
        return this;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", type='" + type.getName() + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
