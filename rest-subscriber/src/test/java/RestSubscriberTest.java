import com.berkepite.RateDistributionEngine.RestSubscriber.RestConfig;
import com.berkepite.RateDistributionEngine.RestSubscriber.RestSubscriber;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriber;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestSubscriberTest {

    @Mock
    private ICoordinator coordinator;

    @Mock
    private RestConfig config;

    @Mock
    private HttpResponse<String> httpResponse;

    private RestSubscriber subscriber;

    @BeforeEach
    void setUp() {
        when(config.getUrl()).thenReturn("http://localhost:1000");
        when(config.getUsername()).thenReturn("client");
        when(config.getPassword()).thenReturn("1234");
        when(config.getName()).thenReturn("TestSubscriber");
        when(config.getRequestInterval()).thenReturn(2000);

        subscriber = new RestSubscriber(coordinator, config);
    }

    @Test
    void testConnect_Success() {
        when(config.getHealthRequestRetryLimit()).thenReturn(5);
        subscriber.connect();

        // Verify the subscriber notifies the coordinator on successful connection
        verify(coordinator, times(1)).onConnect(subscriber);
    }

    @Test
    void testSubscribe_Success() {
        when(config.getRequestRetryLimit()).thenReturn(5);
        List<String> rates = List.of("USD_TRY", "EUR_USD", "GBP_USD");

        subscriber.subscribe(rates);

        verify(coordinator, timeout(5000).times(3)).onSubscribe(subscriber);
    }

    @Test
    void testSubscribe_TriggersRateUpdate() throws JsonProcessingException {
        when(config.getRequestRetryLimit()).thenReturn(5);
        List<String> rates = List.of("USD_TRY", "EUR_USD", "GBP_USD");

        subscriber.subscribe(rates);

        ArgumentCaptor<ISubscriber> subscriberCaptor = ArgumentCaptor.forClass(ISubscriber.class);
        ArgumentCaptor<RawRate> rateCaptor = ArgumentCaptor.forClass(RawRate.class);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)  // Maximum wait time
                .untilAsserted(() -> {
                    verify(coordinator, atLeast(3)).onRateUpdate(subscriberCaptor.capture(), rateCaptor.capture());
                });

        Assertions.assertNotNull(rateCaptor.getValue());
    }
}
