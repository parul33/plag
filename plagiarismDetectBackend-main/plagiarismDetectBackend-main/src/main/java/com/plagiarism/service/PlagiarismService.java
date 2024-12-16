package com.plagiarism.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.plagiarism.util.SemanticHashing;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.plagiarism.util.AdaptiveJaccardSimilarity;
import com.plagiarism.util.ContentDensityChecker;
import com.plagiarism.util.SlidingWindowSimilarity;
import com.plagiarism.util.SemanticHashing;

@Service
public class PlagiarismService {
    private static final List<String> SEARCH_URLS = Arrays.asList(
            "https://www.google.com/search?q="
    );

    public Map<String, Object> detectPlagiarism(String inputText) {
        Map<String, Object> results = new HashMap<>();

        // Check content density first
        if (ContentDensityChecker.hasLowDensity(inputText)) {
            results.put("error", "Input has low content density");
            return results;
        }

        List<Double> plagiarismScores = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(SEARCH_URLS.size());

        try {
            for (String searchUrl : SEARCH_URLS) {
                String webContent = fetchWebContent(searchUrl + inputText.replace(" ", "+"));
                System.out.println("=== Similarity for one fetched content ===");

                double adaptiveJaccardSimilarity = AdaptiveJaccardSimilarity.calculate(inputText, webContent) ;
                double slidingWindowSimilarity = SlidingWindowSimilarity.calculate(inputText, webContent) ;
                double semanticSimilarity = SemanticHashing.calculate(inputText, webContent) ;

                System.out.printf("Adaptive Jaccard Similarity: %.2f%%%n", adaptiveJaccardSimilarity * 100);
                System.out.printf("Sliding Window Similarity: %.2f%%%n", slidingWindowSimilarity * 100);
                System.out.printf("Semantic hash Similarity: %.2f%%%n", semanticSimilarity * 100);

                double averageSimilarity = (adaptiveJaccardSimilarity + slidingWindowSimilarity + semanticSimilarity) / 3.0;
                plagiarismScores.add(averageSimilarity);
            }
        } catch (Exception e) {
            results.put("error", e.getMessage());
            return results;
        } finally {
            executor.shutdown();
        }

        // Calculate overall plagiarism percentage as the average of all scores
        double overallPlagiarismPercentage = plagiarismScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        results.put("plagiarismPercentage", overallPlagiarismPercentage * 100); // Convert to percentage
        return results;
    }

    private String fetchWebContent(String url) throws IOException {
        return Jsoup.connect(url).get().body().text();
    }
}

