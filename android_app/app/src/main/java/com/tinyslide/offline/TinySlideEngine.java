package com.tinyslide.offline;

import android.content.Context;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TinySlideEngine implements AutoCloseable {
    private final OrtEnvironment environment;
    private final OrtSession session;
    private final String inputName;

    public TinySlideEngine(Context context) throws IOException, OrtException {
        environment = OrtEnvironment.getEnvironment();
        byte[] modelBytes = readAsset(context, "slide_classifier.onnx");
        session = environment.createSession(modelBytes, new OrtSession.SessionOptions());
        inputName = session.getInputNames().iterator().next();
    }

    public JSONObject makeSlides(String text) throws OrtException, JSONException {
        List<String> sentences = cleanSentences(text);
        Map<String, List<String>> groupedContent = new LinkedHashMap<>();

        for (String sentence : sentences) {
            String contentType = predictContentType(sentence);
            List<String> bullets = groupedContent.get(contentType);

            if (bullets == null) {
                bullets = new ArrayList<>();
                groupedContent.put(contentType, bullets);
            }

            bullets.add(cleanBullet(sentence));
        }

        JSONArray slides = new JSONArray();
        for (Map.Entry<String, List<String>> entry : groupedContent.entrySet()) {
            JSONArray bullets = new JSONArray();
            for (String bullet : entry.getValue()) {
                bullets.put(bullet);
            }

            JSONObject slide = new JSONObject();
            slide.put("heading", generateSlideHeading(entry.getKey()));
            slide.put("content_type", entry.getKey());
            slide.put("bullets", bullets);
            slide.put("layout", suggestLayout(entry.getValue().size()));
            slides.put(slide);
        }

        JSONObject result = new JSONObject();
        result.put("title", generateTitle(text));
        result.put("slides", slides);
        return result;
    }

    private String predictContentType(String sentence) throws OrtException {
        String rulePrediction = ruleBasedContentType(sentence);
        if (rulePrediction != null) {
            return rulePrediction;
        }

        String[][] input = new String[][]{{sentence}};
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, input);
             OrtSession.Result result = session.run(Collections.singletonMap(inputName, tensor))) {
            Object value = result.get(0).getValue();
            String prediction = parsePrediction(value);

            if ("definition".equals(prediction)) {
                return "overview";
            }

            return prediction;
        }
    }

    private String parsePrediction(Object value) {
        if (value instanceof String[]) {
            String[] labels = (String[]) value;
            return labels.length > 0 ? labels[0] : "overview";
        }

        if (value instanceof Object[]) {
            Object[] labels = (Object[]) value;
            if (labels.length > 0 && labels[0] != null) {
                return labels[0].toString();
            }
        }

        return value == null ? "overview" : value.toString();
    }

    private String ruleBasedContentType(String sentence) {
        String text = sentence.toLowerCase(Locale.US);

        if (containsAny(text, "step-by-step", "implementing", "implementation", "requires",
                "first", "second", "third", "method", "deploy")) {
            return "implementation";
        }

        if (containsAny(text, "ultimately", "benefit", "benefits", "impact", "improves",
                "reduces", "saves", "self-optimizing", "optimizing")) {
            return "benefit";
        }

        if (containsAny(text, "strategy", "long-term plan", "plan", "phased rollout",
                "rollout", "pilot zone", "scale", "phase")) {
            return "strategy";
        }

        if (containsAny(text, "approach to building", "system relies", "integrating",
                "iot sensors", "sensors", "predictive", "machine learning",
                "architecture", "components")) {
            return "architecture";
        }

        if (containsAny(text, "key features", "features", "include", "includes",
                "adaptive", "dashboard", "alerts")) {
            return "features";
        }

        if (containsAny(text, "solution", "to solve", "solves", "transitioning",
                "automates", "proposed")) {
            return "solution";
        }

        if (containsAny(text, "compared", "unlike", "whereas", "while", "than")) {
            return "comparison";
        }

        if (containsAny(text, "for example", "for instance", "use case")) {
            return "example";
        }

        if (containsAny(text, "percent", "%", "accuracy", "latency", "metric", "data")) {
            return "statistic";
        }

        if (containsAny(text, "problem", "challenge", "issue", "gridlock", "bottleneck",
                "congestion", "chaotic")) {
            return "problem";
        }

        if (containsAny(text, " is a ", " refers to ", " means ", " defined as ")) {
            return "definition";
        }

        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private List<String> cleanSentences(String text) {
        String[] rawSentences = text.trim().split("(?<=[.!?])\\s+");
        List<String> sentences = new ArrayList<>();

        for (String rawSentence : rawSentences) {
            String sentence = rawSentence.trim();
            if (sentence.length() > 10) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }

    private String generateTitle(String text) {
        String[] words = text.trim().split("\\s+");
        StringBuilder title = new StringBuilder();

        for (int i = 0; i < Math.min(words.length, 6); i++) {
            if (i > 0) {
                title.append(" ");
            }
            title.append(toTitleCase(words[i]));
        }

        return title.length() == 0 ? "Untitled Presentation" : title.toString();
    }

    private String generateSlideHeading(String contentType) {
        switch (contentType) {
            case "definition":
                return "Definition";
            case "problem":
                return "Problem Statement";
            case "solution":
                return "Proposed Solution";
            case "architecture":
                return "System Architecture";
            case "features":
                return "Key Features";
            case "implementation":
                return "Implementation Steps";
            case "strategy":
                return "Rollout Strategy";
            case "benefit":
                return "Key Benefits";
            case "process":
                return "Process Overview";
            case "comparison":
                return "Comparison";
            case "example":
                return "Example";
            case "statistic":
                return "Key Statistics";
            case "overview":
                return "Topic Overview";
            default:
                return "Slide Overview";
        }
    }

    private String cleanBullet(String sentence) {
        String bullet = sentence.trim()
                .replace("basically", "")
                .replace("actually", "")
                .replace("very", "")
                .replace("really", "")
                .trim();

        if (bullet.length() > 120) {
            bullet = bullet.substring(0, 117) + "...";
        }

        if (bullet.isEmpty()) {
            return bullet;
        }

        return bullet.substring(0, 1).toUpperCase(Locale.US) + bullet.substring(1);
    }

    private String suggestLayout(int bulletCount) {
        if (bulletCount <= 2) {
            return "title_and_content";
        }

        if (bulletCount <= 4) {
            return "two_column";
        }

        return "comparison";
    }

    private String toTitleCase(String word) {
        if (word.isEmpty()) {
            return word;
        }

        return word.substring(0, 1).toUpperCase(Locale.US)
                + word.substring(1).toLowerCase(Locale.US);
    }

    private byte[] readAsset(Context context, String assetName) throws IOException {
        try (InputStream inputStream = context.getAssets().open(assetName);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            return outputStream.toByteArray();
        }
    }

    @Override
    public void close() throws OrtException {
        session.close();
    }
}
