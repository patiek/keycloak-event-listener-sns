package fr.redfroggy.keycloak;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ExtendWith({MockitoExtension.class})
class SnsEventPublisherTest {

    @Mock
    private SnsClient snsClientMock;

    @Mock
    private SnsEvent snsEventMock;

    @Mock
    private SnsAdminEvent snsAdminEventMock;

    @Mock
    private ObjectMapper mapperMock;

    @Mock
    private SnsEventListenerConfiguration snsEventListenerConfigurationMock;

    @InjectMocks
    private SnsEventPublisher snsEventPublisher;

    @Test
    void shouldSendEventWhenEventTopicArnExists() throws JsonProcessingException {
        when(snsEventListenerConfigurationMock.getEventTopicArn()).thenReturn("eventTopicArn");
        when(mapperMock.writeValueAsString(snsEventMock)).thenReturn("eventJSONString");
        snsEventPublisher.sendEvent(snsEventMock);
        PublishRequest publishRequest = PublishRequest.builder()
                .message("eventJSONString")
                .topicArn("eventTopicArn")
                .build();
        verify(snsClientMock).publish(publishRequest);
    }

    @Test
    void shouldNotSendEventWhenEventTopicArnDoenstExistsAndGetAWarning() {
        snsEventPublisher.sendEvent(snsEventMock);
        verify(snsClientMock, never()).publish((PublishRequest) any());
    }

    @Test
    void shouldSendAdminEventWhenEventTopicArnExists() throws JsonProcessingException {
        when(snsEventListenerConfigurationMock.getAdminEventTopicArn()).thenReturn("adminEventTopicArn");
        when(mapperMock.writeValueAsString(snsAdminEventMock)).thenReturn("adminEventJSONString");
        snsEventPublisher.sendAdminEvent(snsAdminEventMock);
        PublishRequest publishRequest = PublishRequest.builder()
                .message("adminEventJSONString")
                .topicArn("adminEventTopicArn")
                .build();
        verify(snsClientMock).publish(publishRequest);
    }

    @Test
    void shouldNotSendAdminEventWhenEventTopicArnDoenstExistsAndGetAWarning() {
        snsEventPublisher.sendAdminEvent(snsAdminEventMock);
        verify(snsClientMock, never()).publish((PublishRequest) any());
    }

    @Test
    void shouldNotSendEventAndLogAnErrorWhenAnyValueIsWrite() throws JsonProcessingException {
        when(snsEventListenerConfigurationMock.getEventTopicArn()).thenReturn("eventTopicArn");
        when(mapperMock.writeValueAsString(snsEventMock)).thenThrow(JsonProcessingException.class);
        snsEventPublisher.sendEvent(snsEventMock);
        verify(snsClientMock, never()).publish((PublishRequest) any());
    }

    @Test
    void shouldNotPropagateRuntimeExceptionIfPublishFailed() {
        when(snsEventListenerConfigurationMock.getEventTopicArn()).thenReturn("eventTopicArn");
        when(snsClientMock.publish((PublishRequest) any())).thenThrow(new RuntimeException("test"));
        snsEventPublisher.sendEvent(snsEventMock);
    }
}
