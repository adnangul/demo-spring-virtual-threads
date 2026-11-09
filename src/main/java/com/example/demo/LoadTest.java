package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class LoadTest {
    public static void main(String[] args) {
        int concurrentRequests = 400;
        String url = "http://localhost:8080/io-bound-task";

        System.out.println("Firing " + concurrentRequests + " requests simultaneously from Stable Java Client...");
        long startTime = System.currentTimeMillis();

        // 1. Create a single, shared HttpClient utilizing a Virtual Thread engine for worker tasks
        try (var taskExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            HttpClient client = HttpClient.newBuilder()
                    .executor(taskExecutor)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // 2. Submit all network requests in parallel without prematurely closing the client selector
            CompletableFuture<?>[] futures = IntStream.range(0, concurrentRequests)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response.statusCode() != 200) {
                                System.err.println("Failed with code: " + response.statusCode());
                            }
                        } catch (Exception e) {
                            System.err.println("Request error: " + e.getMessage());
                        }
                    }, taskExecutor))
                    .toArray(CompletableFuture[]::new);

            // 3. Wait cleanly for the entire array of async futures to complete
            CompletableFuture.allOf(futures).join();
        } // The taskExecutor safely spins down here AFTER all responses are back

        long endTime = System.currentTimeMillis();
        System.out.println("----------------------------------------");
        System.out.println("All requests finished! Total time: " + ((endTime - startTime) / 1000.0) + " seconds.");
    }
}
