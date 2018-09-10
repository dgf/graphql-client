package org.aplatanao.graphql;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface Client {

    static Client create(API api) {
        return new ClientImpl(api);
    }

    ObjectNode execute(String query) throws IOException;

    API getApi();

    void setApi(API api);

    void init();

    boolean isInitialized();

    Field getQuery(String query);

    List<Field> getQueries();

    String getStatus();

    Type getType(String type);

    Set<String> getTypes();
}
