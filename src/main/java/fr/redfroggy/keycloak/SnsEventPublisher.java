package fr.redfroggy.keycloak;

import org.jboss.logging.Logger;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class SnsEventPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper mapper;
    private static final Logger log = Logger.getLogger(SnsEventPublisher.class);
    private final SnsEventListenerConfiguration snsEventListenerConfiguration;

    public SnsEventPublisher(SnsClient snsClient, SnsEventListenerConfiguration snsEventListenerConfiguration,
            ObjectMapper mapper) {
        this.snsClient = snsClient;
        this.snsEventListenerConfiguration = snsEventListenerConfiguration;
        this.mapper = mapper;
    }

    public void sendEvent(SnsEvent snsEvent) {
        if (snsEventListenerConfiguration.getEventTopicArn() == null) {
            log.warn(
                    "No topicArn specified. Can not send event to AWS SNS! Set environment variable KC_SNS_EVENT_TOPIC_ARN");
            return;
        }
        publishEvent(snsEvent.getEventId(), snsEvent, snsEventListenerConfiguration.getEventTopicArn());
    }

    public void sendAdminEvent(SnsAdminEvent snsAdminEvent) {
        if (snsEventListenerConfiguration.getAdminEventTopicArn() == null) {
            log.warn(
                    "No topicArn specified. Can not send event to AWS SNS! Set environment variable KC_SNS_ADMIN_EVENT_TOPIC_ARN");
            return;
        }
        publishEvent(snsAdminEvent.getEventId(), snsAdminEvent, snsEventListenerConfiguration.getAdminEventTopicArn());
    }

    private void publishEvent(String id, Object event, String topicArn) {

        try {
            String messageGroupID = null;
            String messageDeduplicationID = null;

            // if endpoint is FIFO, set group and deduplication IDs
            if (topicArn.endsWith(".fifo")) {
                messageGroupID = id;
                messageDeduplicationID = id;
            }

            PublishRequest request = PublishRequest.builder()
                    .message(mapper.writeValueAsString(event))
                    .messageGroupId(messageGroupID)
                    .messageDeduplicationId(messageDeduplicationID)
                    .topicArn(topicArn)
                    .build();


            PublishResponse result = snsClient.publish(request);

        } catch (JsonProcessingException e) {
            log.error("The payload wasn't created.", e);
        } catch (Exception e) {
            log.error("Exception occurred during the event publication", e);
        }
    }
}
