package com.mestro.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mestro.common.dto.ApiResponse;
import com.mestro.common.dto.InventoryResponse;
import com.mestro.common.dto.ProductResponse;
import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.exception.BusinessException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductWebClient {

    @Value("${product-service.url}")
    private String serviceUrl;

    @Value("${product-service.timeout-ms:5000}")
    private long timeoutMs;

    private Vertx vertx;
    private WebClient webClient;

    private final ObjectMapper objectMapper;

    public ProductWebClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        String url = serviceUrl.trim();
        boolean ssl = url.startsWith("https");
        String withoutScheme = url.replaceFirst("https?://", "");
        String[] hostPort = withoutScheme.split(":");
        String host = hostPort[0];
        int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1].split("/")[0]) : (ssl ? 443 : 80);

        vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions()
                .setDefaultHost(host)
                .setDefaultPort(port)
                .setSsl(ssl)
                .setConnectTimeout((int) timeoutMs)
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setKeepAlive(true)
                .setMaxPoolSize(20);

        webClient = WebClient.create(vertx, options);
        log.info("ProductWebClient initialised — target: {}:{} ssl={}", host, port, ssl);
    }

    @PreDestroy
    public void destroy() {
        if (webClient != null) webClient.close();
        if (vertx != null) vertx.close();
        log.info("ProductWebClient shut down");
    }

    // -------------------------------------------------------------------------
    // Product endpoints
    // -------------------------------------------------------------------------

    /** GET /api/v1/products/{id} */
    public Future<ApiResponse<ProductResponse>> getProductById(Long id) {
        return get("/api/v1/products/" + id, new TypeReference<>() {});
    }

    /** GET /api/v1/products/sku/{sku} */
    public Future<ApiResponse<ProductResponse>> getProductBySku(String sku) {
        return get("/api/v1/products/sku/" + sku, new TypeReference<>() {});
    }

    // -------------------------------------------------------------------------
    // Inventory – read endpoints
    // -------------------------------------------------------------------------

    /** GET /api/v1/inventories/product/{productId} */
    public Future<ApiResponse<List<InventoryResponse>>> getInventoriesByProduct(Long productId) {
        return get("/api/v1/inventories/product/" + productId, new TypeReference<>() {});
    }

    /** GET /api/v1/inventories/product/{productId}/total */
    public Future<ApiResponse<Integer>> getTotalAvailableQuantity(Long productId) {
        return get("/api/v1/inventories/product/" + productId + "/total", new TypeReference<>() {});
    }

    /** GET /api/v1/inventories/product/{productId}/warehouse/{warehouseId} */
    public Future<ApiResponse<InventoryResponse>> getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        return get("/api/v1/inventories/product/" + productId + "/warehouse/" + warehouseId, new TypeReference<>() {});
    }

    // -------------------------------------------------------------------------
    // Inventory – reserve endpoints
    // -------------------------------------------------------------------------

    /** PUT /api/v1/inventories/product/{productId}/reserve?quantity={quantity} */
    public Future<ApiResponse<InventoryResponse>> reserveByProductId(Long productId, Integer quantity) {
        return put("/api/v1/inventories/product/" + productId + "/reserve", quantity, new TypeReference<>() {});
    }

    /** PUT /api/v1/inventories/product/{productId}/warehouse/{warehouseId}/reserve?quantity={quantity} */
    public Future<ApiResponse<InventoryResponse>> reserveByProductAndWarehouse(
            Long productId, Long warehouseId, Integer quantity) {
        return put(
                "/api/v1/inventories/product/" + productId + "/warehouse/" + warehouseId + "/reserve",
                quantity,
                new TypeReference<>() {});
    }

    // -------------------------------------------------------------------------
    // Inventory – release endpoints
    // -------------------------------------------------------------------------

    /** PUT /api/v1/inventories/product/{productId}/release?quantity={quantity} */
    public Future<ApiResponse<InventoryResponse>> releaseByProductId(Long productId, Integer quantity) {
        return put("/api/v1/inventories/product/" + productId + "/release", quantity, new TypeReference<>() {});
    }

    /** PUT /api/v1/inventories/product/{productId}/warehouse/{warehouseId}/release?quantity={quantity} */
    public Future<ApiResponse<InventoryResponse>> releaseByProductAndWarehouse(
            Long productId, Long warehouseId, Integer quantity) {
        return put(
                "/api/v1/inventories/product/" + productId + "/warehouse/" + warehouseId + "/release",
                quantity,
                new TypeReference<>() {});
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Non-blocking GET — returns a Vert.x Future deserialised into the requested type. */
    private <T> Future<T> get(String uri, TypeReference<T> typeRef) {
        log.debug("GET {}", uri);
        Promise<T> promise = Promise.promise();

        webClient.get(uri).putHeader("Accept", "application/json").send(ar -> {
            if (ar.succeeded()) {
                HttpResponse<Buffer> resp = ar.result();
                if (isSuccess(resp.statusCode())) {
                    deserialize(resp.bodyAsString(), typeRef, promise);
                } else {
                    promise.fail(new BusinessException(
                            CommonErrorCode.INTERNAL_SERVER_ERROR,
                            "Product service returned HTTP " + resp.statusCode() + " for GET " + uri));
                }
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    /**
     * Non-blocking PUT — quantity sent as a query param, matching Feign's @RequestParam.
     * Returns a Vert.x Future deserialised into the requested type.
     */
    private <T> Future<T> put(String uri, Integer quantity, TypeReference<T> typeRef) {
        log.debug("PUT {}?quantity={}", uri, quantity);
        Promise<T> promise = Promise.promise();

        webClient
                .put(uri)
                .putHeader("Accept", "application/json")
                .addQueryParam("quantity", String.valueOf(quantity))
                .sendBuffer(Buffer.buffer(), ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<Buffer> resp = ar.result();
                        if (isSuccess(resp.statusCode())) {
                            deserialize(resp.bodyAsString(), typeRef, promise);
                        } else {
                            promise.fail(new BusinessException(
                                    CommonErrorCode.INTERNAL_SERVER_ERROR,
                                    "Product service returned HTTP " + resp.statusCode() + " for PUT " + uri));
                        }
                    } else {
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    /** Deserialises JSON into the requested type, completing or failing the promise. */
    private <T> void deserialize(String json, TypeReference<T> typeRef, Promise<T> promise) {
        try {
            promise.complete(objectMapper.readValue(json, typeRef));
        } catch (Exception e) {
            log.error("Failed to deserialise response: {}", json, e);
            promise.fail(new BusinessException(
                    CommonErrorCode.INTERNAL_SERVER_ERROR, "Failed to parse response from product service"));
        }
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
