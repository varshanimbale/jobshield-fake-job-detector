package com.varsha.jobdetector;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin
public class JobController {

    @Autowired
    private ScamJobRepository repo;

    @PostMapping("/check")
    public String checkJob(@RequestBody String url) {

        url = url.toLowerCase();
        int score = 0;

        // 1. Local DB check
        List<ScamJob> list = repo.findAll();
        for(ScamJob s : list) {
            if(url.contains(s.getKeyword())) {
                score += 20;
            }
        }

        // 2. Domain check
        if(url.contains("clinchbridge")) {
            score += 50;
        }

        // 3. VirusTotal API check
        try {
            String apiKey = "0fbbccfacaad2c8385d4abda89c9685b6014e55e77e571f40cd95269cf3c4d38\r\n"
            		+ "";

            String apiUrl = "https://www.virustotal.com/api/v3/urls";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-apikey", apiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>("url=" + url, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            String result = response.getBody();

            if(result != null && result.contains("malicious")) {
                score += 50;
            }

        } catch(Exception e) {
            System.out.println("API error: " + e.getMessage());
        }

        // Final result
        if(score >= 60) {
            return "❌ Fake Job (" + score + "% risk)";
        } else if(score >= 30) {
            return "⚠️ Suspicious Job (" + score + "% risk)";
        } else {
            return "✅ Safe Job (" + score + "% risk)";
        }
    }
}