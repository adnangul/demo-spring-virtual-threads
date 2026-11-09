package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

/**
 * LoadTest2 is an enhanced version of LoadTest that includes performance metrics tracking.
 * It fires a specified number of concurrent requests to the /io-bound-task endpoint and
 * tracks the number of successful and failed requests, along with total execution time.
 */
public class LoadTest2 {
    public static void main(String[] args) {
        int concurrentRequests = 1000;
        String url = "http://localhost:8080/io-bound-task";

        // High-concurrency performance counters
        LongAdder successfulRequests = new LongAdder();
        LongAdder failedRequests = new LongAdder();

        System.out.println("Firing " + concurrentRequests + " requests simultaneously from Instrumented Client...");
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
                            if (response.statusCode() == 200) {
                                successfulRequests.increment(); // Thread-safe tracking
                            } else {
                                failedRequests.increment();
                                System.err.println("HTTP Error Code: " + response.statusCode());
                            }
                        } catch (Exception e) {
                            failedRequests.increment();
                            System.err.println("Network Error: " + e.getMessage());
                        }
                    }, taskExecutor))
                    .toArray(CompletableFuture[]::new);

            // 3. Wait cleanly for the entire array of async futures to complete
            CompletableFuture.allOf(futures).join();
        } // The taskExecutor safely spins down here AFTER all responses are back

        long endTime = System.currentTimeMillis();
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        
        System.out.println("\n----------------- TEST METRICS -----------------");
        System.out.println("Total Requests Executed : " + concurrentRequests);
        System.out.println("Successful Requests (200): " + successfulRequests.sum());
        System.out.println("Failed/Error Requests   : " + failedRequests.sum());
        System.out.println("All requests finished! Total Time Elapsed      : " + totalTimeSeconds + " seconds");
        System.out.println("------------------------------------------------");
    }
}
