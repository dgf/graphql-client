package org.aplatanao.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ClientImpl implements Client {

    private ObjectMapper mapper = new ObjectMapper();

    private CloseableHttpClient client;

    private URI endpoint;

    private API api;

    private Type queryType;

    private Map<String, Type> types = new HashMap<>();

    private boolean initialized = false;

    private String status;

    ClientImpl(API api) {
        this.api = api;

        try {
            this.endpoint = new URI(api.getUri());
        } catch (URISyntaxException e) {
            throw new ClientException("invalid endpoint", e);
        }

        try {
            String[] protocols = {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
            SSLContext context = SSLContexts.custom().loadTrustMaterial((c, a) -> true).build();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(context, protocols, null, NoopHostnameVerifier.INSTANCE);
            this.client = HttpClients.custom().setSSLSocketFactory(factory).build();
        } catch (Exception e) {
            throw new ClientException("HTTP client setup failed", e);
        }
    }

    @Override
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
            throw new ClientException("init failed", e);
        }
    }

    private CloseableHttpResponse execute(HttpRequestBase request) throws IOException {
        return client.execute(request);
    }

    @Override
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

    @Override
    public API getApi() {
        return api;
    }

    @Override
    public void setApi(API api) {
        this.api = api;
    }

    @Override
    public Field getQuery(String name) {
        return queryType.getFields().stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<Field> getQueries() {
        return queryType.getFields().stream()
                .sorted(Comparator.comparing(Field::getName))
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getTypes() {
        return types.keySet();
    }

    @Override
    public Type getType(String name) {
        return types.get(name);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
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
