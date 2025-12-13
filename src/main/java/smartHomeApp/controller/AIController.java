package smartHomeApp.controller;

import org.springframework.web.bind.annotation.*;
import smartHomeApp.model.Device;
import smartHomeApp.model.DeviceType;
import smartHomeApp.model.User;
import smartHomeApp.service.ChatbotService;
import smartHomeApp.service.DeviceService;
import smartHomeApp.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final ChatbotService chatbotService;
    private final UserService userService;
    private final DeviceService deviceService;

    public AIController(ChatbotService chatbotService, UserService userService, DeviceService deviceService) {
        this.chatbotService = chatbotService;
        this.userService = userService;
        this.deviceService = deviceService;
    }


    @GetMapping("/suggestions/{userId}")
    public Map<String, Object> getAutomationSuggestions(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {

            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Double preferredTemp = user.getPreferredTemperature();


            List<Device> allDevices = deviceService.findAll();


            List<Device> thermostats = allDevices.stream()
                    .filter(d -> d.getType() == DeviceType.THERMOSTAT)
                    .toList();

            StringBuilder suggestions = new StringBuilder();


            if (preferredTemp != null && !thermostats.isEmpty()) {
                for (Device thermostat : thermostats) {
                    Double currentTemp = thermostat.getValue();
                    if (currentTemp == null) continue;

                    double difference = Math.abs(preferredTemp - currentTemp);

                    if (difference > 2.0) {
                        String roomName = thermostat.getRoom() != null ?
                                thermostat.getRoom().getName() : "Unknown Room";

                        suggestions.append(String.format(
                                "Suggestion: Adjust '%s' in %s from %.1f°C to %.1f°C to match your comfort profile. " +
                                "Temperature difference: %.1f°C. ",
                                thermostat.getName(),
                                roomName,
                                currentTemp,
                                preferredTemp,
                                difference
                        ));
                    }
                }
            }


            List<Device> lightsOn = allDevices.stream()
                    .filter(d -> d.getType() == DeviceType.LIGHT && Boolean.TRUE.equals(d.getStatus()))
                    .toList();

            if (lightsOn.size() > 3) {
                suggestions.append(String.format(
                        "Energy Tip: You have %d lights currently on. Consider turning off unused lights to save energy. ",
                        lightsOn.size()
                ));
            }


            if (suggestions.isEmpty()) {
                suggestions.append(" All devices are operating within optimal parameters based on your preferences.");
            }

            response.put("success", true);
            response.put("userId", userId);
            response.put("preferredTemperature", preferredTemp);
            response.put("totalDevices", allDevices.size());
            response.put("thermostats", thermostats.size());
            response.put("suggestion", suggestions.toString().trim());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("suggestion", "Unable to generate suggestions at this time.");
        }

        return response;
    }


    @PostMapping("/generate")
    public Map<String, String> generateResponse(@RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("prompt", "");

        if (prompt.isBlank()) {
            return Map.of(
                    "error", "Prompt is required",
                    "response", ""
            );
        }

        try {

            Long userId = 1L;
            if (request.containsKey("userId")) {
                try {
                    userId = Long.valueOf(request.get("userId"));
                } catch (NumberFormatException e) {

                }
            }

            String aiResponse = chatbotService.getSuggestionForUser(prompt, userId);
            return Map.of(
                    "response", aiResponse,
                    "model", "intelligent-ai",
                    "status", "success"
            );
        } catch (Exception e) {
            return Map.of(
                    "error", "AI service unavailable: " + e.getMessage(),
                    "response", "Unable to generate recommendations at this time.",
                    "status", "error"
            );
        }
    }
}

