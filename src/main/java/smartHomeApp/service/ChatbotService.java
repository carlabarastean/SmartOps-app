package smartHomeApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String modelName;
    private final IntelligentAIService intelligentAIService;

    public ChatbotService(RestTemplate restTemplate,
                          @Value("${ollama.api.url}") String apiUrl,
                          @Value("${ollama.model}") String modelName,
                          IntelligentAIService intelligentAIService) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.modelName = modelName;
        this.intelligentAIService = intelligentAIService;
    }

    public String getSuggestion(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", modelName,
                    "prompt", enhancePrompt(prompt),
                    "stream", false
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            if (response != null && response.get("response") != null) {
                return response.get("response").toString();
            }
        } catch (Exception e) {
            System.out.println("Ollama not available, using intelligent fallback: " + e.getMessage());
        }

        return intelligentAIService.generateSmartRecommendation(prompt, 1L);
    }

    public String getSuggestionForUser(String prompt, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", modelName,
                    "prompt", enhancePromptWithContext(prompt, userId),
                    "stream", false
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            if (response != null && response.get("response") != null) {
                return response.get("response").toString();
            }
        } catch (Exception e) {
            System.out.println("Ollama not available for user " + userId + ", using intelligent fallback: " + e.getMessage());
        }

        return intelligentAIService.generateSmartRecommendation(prompt, userId);
    }

    private String enhancePrompt(String prompt) {
        return "You are a smart home AI assistant. Analyze this request and provide helpful, specific recommendations for home automation: " + prompt;
    }

    private String enhancePromptWithContext(String prompt, Long userId) {
        return "You are a smart home AI assistant for user ID " + userId + ". Provide personalized recommendations based on their smart home setup: " + prompt;
    }
}
