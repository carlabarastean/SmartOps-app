package smartHomeApp.service;

import org.springframework.stereotype.Service;
import smartHomeApp.model.Device;
import smartHomeApp.model.DeviceType;
import smartHomeApp.model.TemperatureReading;
import smartHomeApp.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntelligentAIService {

    private final DeviceService deviceService;
    private final TemperatureService temperatureService;
    private final UserService userService;

    public IntelligentAIService(DeviceService deviceService,
                               TemperatureService temperatureService,
                               UserService userService) {
        this.deviceService = deviceService;
        this.temperatureService = temperatureService;
        this.userService = userService;
    }

    public String generateSmartRecommendation(String prompt, Long userId) {

        List<Device> devices = deviceService.findAll();
        List<TemperatureReading> temperatureHistory = temperatureService.getLast100Readings();
        User user = userService.getUserById(userId).orElse(null);


        String lowercasePrompt = prompt.toLowerCase();

        if (lowercasePrompt.contains("energy") && lowercasePrompt.contains("saving")) {
            return generateEnergySavingRecommendation(devices);
        }

        if (lowercasePrompt.contains("security")) {
            return generateSecurityRecommendation(devices);
        }

        if (lowercasePrompt.contains("heating") || lowercasePrompt.contains("temperature") || lowercasePrompt.contains("cooling")) {
            return generateHeatingOptimizationRecommendation(devices, temperatureHistory, user);
        }

        if (lowercasePrompt.contains("lighting") || lowercasePrompt.contains("light")) {
            return generateLightingRecommendation(devices);
        }

        if (lowercasePrompt.contains("morning") && lowercasePrompt.contains("routine")) {
            return generateMorningRoutineRecommendation(devices);
        }

        if (lowercasePrompt.contains("automation")) {
            return generateGeneralAutomationRecommendation(devices, temperatureHistory);
        }


        return generateComprehensiveRecommendation(devices, temperatureHistory, user);
    }

    private String generateEnergySavingRecommendation(List<Device> devices) {
        List<Device> activeLights = devices.stream()
                .filter(d -> d.getType() == DeviceType.LIGHT && d.getStatus())
                .collect(Collectors.toList());

        List<Device> thermostats = devices.stream()
                .filter(d -> d.getType() == DeviceType.THERMOSTAT)
                .collect(Collectors.toList());

        StringBuilder recommendation = new StringBuilder();
        recommendation.append("🔋 **Smart Energy Analysis**\\n\\n");

        if (activeLights.size() > 2) {
            recommendation.append(" **Lighting Optimization:** You have ")
                    .append(activeLights.size())
                    .append(" lights currently active. Consider:\\n")
                    .append("   • Use motion sensors for automatic off switching\\n")
                    .append("   • Reduce brightness by 20% - most people won't notice\\n")
                    .append("   • Potential savings: ~15-25% on lighting costs\\n\\n");
        }

        for (Device thermostat : thermostats) {
            if (thermostat.getValue() != null) {
                double temp = thermostat.getValue();
                if (temp > 23) {
                    recommendation.append(" **Heating Efficiency:** ")
                            .append(thermostat.getRoom().getName())
                            .append(" thermostat at ")
                            .append(temp)
                            .append("°C\\n")
                            .append("   • Lower by 1°C = ~7% energy savings\\n")
                            .append("   • Recommended: 21-22°C for optimal comfort/efficiency\\n\\n");
                }
            }
        }

        recommendation.append(" **Smart Schedule:** Create automated routines:\\n")
                .append("   • Lower temperature 2°C during sleep (22:00-06:00)\\n")
                .append("   • Auto-dim lights after 21:00\\n")
                .append("   • Weekend vs weekday temperature profiles\\n");

        return recommendation.toString();
    }

    private String generateSecurityRecommendation(List<Device> devices) {
        List<Device> locks = devices.stream()
                .filter(d -> d.getType() == DeviceType.LOCK)
                .collect(Collectors.toList());

        StringBuilder recommendation = new StringBuilder();
        recommendation.append(" **Smart Security Analysis**\\n\\n");

        long unlockedCount = locks.stream()
                .filter(d -> !d.getStatus())
                .count();

        if (unlockedCount > 0) {
            recommendation.append(" **Security Alert:** ")
                    .append(unlockedCount)
                    .append(" lock(s) currently unlocked\\n\\n");
        }

        recommendation.append(" **Recommended Security Enhancements:**\\n")
                .append("   • **Auto-lock Schedule:** Automatically lock all doors at 22:00\\n")
                .append("   • **Away Mode:** Auto-lock when no motion detected for 30 minutes\\n")
                .append("   • **Smart Lighting Security:** Random light patterns when away\\n")
                .append("   • **Temperature Monitoring:** Unusual temp changes = potential intrusion\\n\\n")
                .append("📱 **Integration Ideas:**\\n")
                .append("   • Lock status + light dimming = 'bedtime mode'\\n")
                .append("   • Unlock + temperature adjustment = 'welcome home'\\n");

        return recommendation.toString();
    }

    private String generateHeatingOptimizationRecommendation(List<Device> devices,
                                                           List<TemperatureReading> history,
                                                           User user) {
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("🌡️ **Smart Heating & Cooling Optimization**\\n\\n");

        if (!history.isEmpty()) {
            double currentTemp = history.get(0).getTemperature();
            double avgTemp = history.stream()
                    .mapToDouble(TemperatureReading::getTemperature)
                    .average()
                    .orElse(22.0);

            double minTemp = history.stream()
                    .mapToDouble(TemperatureReading::getTemperature)
                    .min()
                    .orElse(20.0);

            double maxTemp = history.stream()
                    .mapToDouble(TemperatureReading::getTemperature)
                    .max()
                    .orElse(25.0);

            recommendation.append(" **Current Analysis:**\\n")
                    .append("   • Current: ").append(String.format("%.1f°C", currentTemp)).append("\\n")
                    .append("   • 48h Average: ").append(String.format("%.1f°C", avgTemp)).append("\\n")
                    .append("   • Range: ").append(String.format("%.1f°C - %.1f°C", minTemp, maxTemp)).append("\\n\\n");

            double variation = maxTemp - minTemp;
            if (variation > 6) {
                recommendation.append("⚡ **High Temperature Variation Detected (")
                        .append(String.format("%.1f°C", variation))
                        .append(" range)**\\n")
                        .append("   • Consider: Better insulation or HVAC tuning\\n")
                        .append("   • Smart scheduling can reduce this by 40%\\n\\n");
            }
        }

        recommendation.append(" **Personalized Recommendations:**\\n");

        if (user != null && user.getPreferredTemperature() != null) {
            recommendation.append("   • Your preferred: ").append(user.getPreferredTemperature()).append("°C\\n");
        }

        recommendation.append("   • **Morning (06:00-09:00):** Gradual warm-up to 21°C\\n")
                .append("   • **Day (09:00-18:00):** Maintain 21-22°C for productivity\\n")
                .append("   • **Evening (18:00-22:00):** Comfortable 22°C\\n")
                .append("   • **Night (22:00-06:00):** Energy-saving 19-20°C\\n\\n")
                .append(" **Savings Potential:** Proper scheduling = 15-30% heating cost reduction");

        return recommendation.toString();
    }

    private String generateLightingRecommendation(List<Device> devices) {
        List<Device> lights = devices.stream()
                .filter(d -> d.getType() == DeviceType.LIGHT)
                .collect(Collectors.toList());

        StringBuilder recommendation = new StringBuilder();
        recommendation.append("💡 **Smart Lighting Optimization**\\n\\n");

        long activeLights = lights.stream().filter(Device::getStatus).count();

        recommendation.append(" **Current Status:** ")
                .append(activeLights).append("/").append(lights.size())
                .append(" lights active\\n\\n");

        recommendation.append(" **Efficiency Recommendations:**\\n")
                .append("   • **Circadian Lighting:** Warm colors (2700K) evening, cool (5000K) morning\\n")
                .append("   • **Adaptive Brightness:** 80% day, 40% evening, 10% night\\n")
                .append("   • **Room-Based Control:** Different profiles per room type\\n\\n")
                .append("⚡ **Energy Optimization:**\\n")
                .append("   • Motion sensors: 30-50% energy savings\\n")
                .append("   • Daylight sensors: Additional 20-30% savings\\n")
                .append("   • Smart dimming: 15% reduction barely noticeable\\n\\n")
                .append(" **Room-Specific Tips:**\\n")
                .append("   • Living Room: Dimmable for ambiance\\n")
                .append("   • Kitchen: Bright task lighting\\n")
                .append("   • Bedroom: Warm, low-intensity evening mode\\n");

        return recommendation.toString();
    }

    private String generateMorningRoutineRecommendation(List<Device> devices) {
        return " **Smart Morning Routine**\\n\\n" +
                "☀ **6:00 AM - Gentle Wake-Up:**\\n" +
                "   • Gradual light increase (0→50% over 15 minutes)\\n" +
                "   • Temperature rise to 21°C\\n\\n" +
                " **6:30 AM - Home Activation:**\\n" +
                "   • Kitchen lights to 80% (breakfast prep)\\n" +
                "   • Bathroom lights to full brightness\\n" +
                "   • Living room lights soft (30%)\\n\\n" +
                "☕ **7:00 AM - Peak Activity:**\\n" +
                "   • All main areas fully lit\\n" +
                "   • Temperature at optimal 22°C\\n" +
                "   • Security: All doors status check\\n\\n" +
                " **8:00 AM - Departure Mode:**\\n" +
                "   • Auto-dim to 20% (security lighting)\\n" +
                "   • Lower temperature to 20°C (energy saving)\\n" +
                "   • Auto-lock all doors\\n\\n" +
                " **Automation Benefits:** 35% energy savings + perfect comfort timing";
    }

    private String generateGeneralAutomationRecommendation(List<Device> devices, List<TemperatureReading> history) {
        StringBuilder recommendation = new StringBuilder();
        recommendation.append(" **Smart Home Automation Recommendations**\\n\\n");

        recommendation.append(" **Priority Automations:**\\n")
                .append("   1. **Temperature Scheduling:** Based on your 48h data pattern\\n")
                .append("   2. **Security Integration:** Lights + locks coordination\\n")
                .append("   3. **Energy Optimization:** Automatic efficiency modes\\n\\n");

        if (!history.isEmpty()) {

            recommendation.append(" **Pattern Analysis:** Your heating system is most active between certain hours\\n")
                    .append("   • **Recommendation:** Pre-heat 30 minutes before peak usage\\n")
                    .append("   • **Smart Tip:** Learn from usage patterns for predictive heating\\n\\n");
        }

        recommendation.append("🔧 **Suggested Automation Rules:**\\n")
                .append("   • **IF** no motion for 30 min **THEN** dim lights 50%\\n")
                .append("   • **IF** temperature < 20°C **AND** time > 18:00 **THEN** increase heating\\n")
                .append("   • **IF** all doors locked **THEN** activate night mode (dim lights, lower temp)\\n")
                .append("   • **IF** first motion detected **THEN** morning routine activation\\n\\n")
                .append("📱 **Smart Integration:** Connect with weather data for predictive adjustments");

        return recommendation.toString();
    }

    private String generateComprehensiveRecommendation(List<Device> devices,
                                                     List<TemperatureReading> history,
                                                     User user) {
        StringBuilder recommendation = new StringBuilder();

        int totalDevices = devices.size();
        long activeDevices = devices.stream().filter(Device::getStatus).count();
        long lights = devices.stream().filter(d -> d.getType() == DeviceType.LIGHT).count();
        long thermostats = devices.stream().filter(d -> d.getType() == DeviceType.THERMOSTAT).count();
        long locks = devices.stream().filter(d -> d.getType() == DeviceType.LOCK).count();

        recommendation.append(" **Comprehensive Smart Home Analysis**\\n\\n");

        recommendation.append(" **System Overview:**\\n")
                .append("   • Total Devices: ").append(totalDevices).append("\\n")
                .append("   • Active: ").append(activeDevices).append("/").append(totalDevices).append("\\n")
                .append("   • Lights: ").append(lights).append(" | Thermostats: ").append(thermostats)
                .append(" | Locks: ").append(locks).append("\\n\\n");

        if (!history.isEmpty()) {
            double currentTemp = history.get(0).getTemperature();
            recommendation.append(" **Climate Status:** Current temperature ")
                    .append(String.format("%.1f°C", currentTemp))
                    .append(" in ").append(history.get(0).getRoomName()).append("\\n\\n");
        }


        double efficiencyScore = calculateEfficiencyScore(devices, history);
        recommendation.append(" **Smart Home Efficiency Score: ")
                .append(String.format("%.0f", efficiencyScore)).append("/100**\\n\\n");

        if (efficiencyScore < 70) {
            recommendation.append("🔧 **Priority Improvements:**\\n")
                    .append("   • Implement automated scheduling\\n")
                    .append("   • Add motion-based light control\\n")
                    .append("   • Optimize temperature patterns\\n\\n");
        } else {
            recommendation.append(" **Excellent Optimization!** Your system is running efficiently.\\n\\n");
        }

        recommendation.append(" **Next Level Upgrades:**\\n")
                .append("   • **Predictive AI:** Learn from patterns for proactive adjustments\\n")
                .append("   • **Weather Integration:** Adjust heating based on forecasts\\n")
                .append("   • **Presence Detection:** Room-by-room occupancy optimization\\n")
                .append("   • **Energy Monitoring:** Real-time consumption analytics\\n\\n")
                .append(" **Potential Savings:** Up to 40% energy reduction with full automation");

        return recommendation.toString();
    }

    private double calculateEfficiencyScore(List<Device> devices, List<TemperatureReading> history) {
        double score = 50.0;


        long deviceTypes = devices.stream()
                .map(Device::getType)
                .distinct()
                .count();
        score += deviceTypes * 10;


        if (!history.isEmpty()) {
            double tempVariation = history.stream()
                    .mapToDouble(TemperatureReading::getTemperature)
                    .max().orElse(25) -
                    history.stream()
                    .mapToDouble(TemperatureReading::getTemperature)
                    .min().orElse(20);

            if (tempVariation < 5) score += 20;
            else if (tempVariation < 8) score += 10;
        }


        long activeLights = devices.stream()
                .filter(d -> d.getType() == DeviceType.LIGHT && d.getStatus())
                .count();
        long totalLights = devices.stream()
                .filter(d -> d.getType() == DeviceType.LIGHT)
                .count();

        if (totalLights > 0) {
            double lightRatio = (double) activeLights / totalLights;
            if (lightRatio < 0.7) score += 10;
        }

        return Math.min(100, score);
    }
}
