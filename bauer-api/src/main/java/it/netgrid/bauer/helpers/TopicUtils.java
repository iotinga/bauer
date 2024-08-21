package it.netgrid.bauer.helpers;

public class TopicUtils {

    private static final String TOPIC_GLUE = "/";
    private static final String TOPIC_WILD_STEP = "+";
    private static final String TOPIC_WILD_TAIL = "#";

    public static boolean match(String mqttLikePattern, String topic) {
        if(topic == null || mqttLikePattern == null) return false;
        
        String[] patternLevels = mqttLikePattern.split(TOPIC_GLUE);
        String[] topicLevels = topic.split(TOPIC_GLUE);

        int index = 0;

        while (index < patternLevels.length) {
            String patternPart = patternLevels[index];

            if (patternPart.equals(TOPIC_WILD_TAIL)) {
                return index == patternLevels.length - 1;
            }

            if (patternPart.equals(TOPIC_WILD_STEP)) {
                index++;
                if (index >= topicLevels.length && index < patternLevels.length) {
                    return false;
                }
                continue;
            }

            if (index >= topicLevels.length || !patternPart.equals(topicLevels[index])) {
                return false;
            }

            index++;
        }

        return index == topicLevels.length && index == patternLevels.length;
    }
}
