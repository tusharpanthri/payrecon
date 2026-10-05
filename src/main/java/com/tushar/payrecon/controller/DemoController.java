package com.tushar.payrecon.controller;

import com.tushar.payrecon.dto.SeedResponse;
import com.tushar.payrecon.service.DemoDataService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoDataService demoDataService;

    public DemoController(DemoDataService demoDataService) {
        this.demoDataService = demoDataService;
    }

    /** Deletes everything and generates fresh demo data. POST /api/demo/seed?count=40 */
    @PostMapping("/seed")
    public SeedResponse seed(@RequestParam(defaultValue = "40") @Min(1) @Max(1000) int count) {
        return demoDataService.seed(count);
    }
}
