import com.berkepite.RateDistributionEngine.RestSubscriber.RestConfig;
import com.berkepite.RateDistributionEngine.RestSubscriber.RestSubscriber;
import com.berkepite.RateDistributionEngine.common.coordinator.ICoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestSubscriberTest {

    private RestSubscriber subscriber;
    private RestConfig config;
    private ICoordinator coordinator;
    private HttpClient mockHttpClient;
    private HttpResponse<String> mockResponse;

    @BeforeEach
    void setup() throws Exception {
        config = mock(RestConfig.class);
        coordinator = mock(ICoordinator.class);
        mockHttpClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);

        when(config.getUsername()).thenReturn("client");
        when(config.getPassword()).thenReturn("1234");
        when(config.getName()).thenReturn("REST-TEST");
        when(config.getUrl()).thenReturn("http://localhost:1000");
        when(config.getRequestInterval()).thenReturn(100);
        when(config.getHealthRequestRetryLimit()).thenReturn(1);

        // Anonymous subclass to inject mock HttpClient
        subscriber = new RestSubscriber(coordinator, config);
        subscriber.setHttpClient(mockHttpClient);

        subscriber.init();
    }

    @Test
    void connect_shouldCallOnConnect_whenHealthCheckReturns200() throws Exception {
        // Arrange
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // Act
        subscriber.connect();

        // Assert
        verify(coordinator).onConnect(subscriber);
    }

    @Test
    void connect_shouldNotifyCoordinatorOnFailure() throws Exception {
        // Arrange
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(500);

        // Act
        subscriber.connect();

        // Assert
        verify(coordinator).onSubscriberError(eq(subscriber), any());
    }

    @Test
    void subscribe_shouldTrySubscribingToEndpoints() throws Exception {
        when(config.getIncludeRates()).thenReturn(List.of());
        when(config.getExcludeRates()).thenReturn(List.of());

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        // Act
        subscriber.subscribe(List.of("USD_TRY"));

        // Assert: should at least try to map and send request
        verify(coordinator, atLeastOnce()).onSubscribe(subscriber);
    }

    @Test
    void subscribe_shouldCallOnConnectionError_whenFails() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Simulated failure"));

        // Act
        subscriber.subscribe(List.of("USD_TRY"));

        // Assert
        verify(coordinator).onSubscriberError(eq(subscriber), any());
    }
}
