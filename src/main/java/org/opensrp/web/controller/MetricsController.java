package org.opensrp.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private PrometheusMeterRegistry registry;

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf(TextFormat.CONTENT_TYPE_004))
                .body(registry.scrape());
    }
}
