package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VirtualThreadDemoController {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadDemoController.class);

    @GetMapping("/io-bound-task")
    public String executeTask() throws InterruptedException {
        // Capture the name of the current thread handling this request
        String initialThreadName = Thread.currentThread().toString();
        
        log.info("Starting a slow I/O bound task on: {}", initialThreadName);

        // Simulate a heavy 2-second blocking network/database call
        Thread.sleep(2000);

        log.info("Completed task on: {}", Thread.currentThread().toString());
        
        return "Task processed successfully! Started on: " + initialThreadName 
                + " | Ended on: " + Thread.currentThread().toString();
    }
}
