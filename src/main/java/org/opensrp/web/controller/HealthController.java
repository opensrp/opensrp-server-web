package org.opensrp.web.controller;

import org.opensrp.web.Constants;
import org.opensrp.web.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    @Qualifier("HealthServiceImpl")
    private HealthService healthService;

    @GetMapping
    public ResponseEntity<ModelMap> index() {
        ModelMap modelMap = healthService.aggregateHealthCheck();
        ModelMap problemsModelMap = (ModelMap) modelMap.get(Constants.HealthIndicator.PROBLEMS);
        return ResponseEntity.status(problemsModelMap.isEmpty() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(modelMap);
    }
}
