package org.aplatanao.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class Client {

    private ObjectMapper mapper = new ObjectMapper();

    private CloseableHttpClient client = HttpClients.createDefault();

    private URI endpoint;

    public API api;

    private Type queryType;

    private Map<String, Type> types = new HashMap<>();

    private boolean initialized = false;

    private String status;

    public Client(API api) throws URISyntaxException {
        this.api = api;
        this.endpoint = new URI(api.uri);
    }

    public void init() {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(this.getClass()
                .getResourceAsStream("/schema.graphql")))) {

            String schemaQuery = buffer.lines().collect(Collectors.joining("\n"));
            ObjectNode node = execute(schemaQuery);

            JsonNode schema = node.path("data").path("__schema");
            for (JsonNode t : schema.path("types")) {
                Type type = mapper.treeToValue(t, Type.class);
                types.put(type.getName(), type);
            }

            TypeRef queryType = mapper.treeToValue(schema.path("queryType"), TypeRef.class);
            this.queryType = types.get(queryType.getName());
            initialized = true;
        } catch (Exception e) {
            initialized = false;
            status = e.getClass().getSimpleName() + ": " + e.getMessage();
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpResponse execute(HttpRequestBase request) throws IOException {
        return client.execute(request);
    }

    public ObjectNode execute(String query) throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("query", query);

        HttpPost post = new HttpPost(endpoint);
        post.setEntity(new StringEntity(mapper.writeValueAsString(node), Consts.UTF_8));
        post.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        CloseableHttpResponse response = execute(post);
        status = response.getStatusLine().toString();
        return mapper.readValue(response.getEntity().getContent(), ObjectNode.class);
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public Client setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public API getApi() {
        return api;
    }

    public Client setApi(API api) {
        this.api = api;
        return this;
    }

    public Field getQuery(String name) {
        return queryType.getFields().stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }

    public List<Field> getQueries() {
        return queryType.getFields().stream()
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toList());
    }

    public Set<String> getTypes() {
        return types.keySet();
    }

    public Type getType(String name) {
        return types.get(name);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Client{" +
                "api=" + api +
                ", initialized=" + initialized +
                ", status='" + status + '\'' +
                '}';
    }
}