import com.berkepite.RateDistributionEngine.TCPSubscriber.RateMapper;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberRateException;
import com.berkepite.RateDistributionEngine.common.rates.RawRate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateMapperTest {

    private final RateMapper rateMapper = new RateMapper();

    @Test
    void createRawRate_shouldParseValidDataCorrectly() throws Exception {
        // Arrange
        String data = "name=USDTRY|bid=32.20|ask=32.25|timestamp=2024-05-13T11:00:00Z";

        // Act
        RawRate rate = rateMapper.createRawRate(data);

        // Assert
        assertEquals("USD_TRY", rate.getType());
        assertEquals(32.20, rate.getBid());
        assertEquals(32.25, rate.getAsk());
        assertEquals(Instant.parse("2024-05-13T11:00:00Z"), rate.getTimestamp());
        assertEquals("TCP_PROVIDER", rate.getProvider());
    }

    @Test
    void createRawRate_shouldThrowExceptionOnInvalidData() {
        // Arrange: timestamp değeri eksik
        String invalidData = "name=USDTRY|bid=32.20|ask=32.25|timestamp=INVALID";

        // Act & Assert
        SubscriberRateException exception = assertThrows(SubscriberRateException.class, () -> {
            rateMapper.createRawRate(invalidData);
        });

        assertTrue(exception.getMessage().contains("Could not parse Raw Rate data"));
    }

    @Test
    void mapRateEnumToEndpoint_shouldRemoveUnderscore() {
        // Act
        String result = rateMapper.mapRateEnumToEndpoint("EUR_USD");

        // Assert
        assertEquals("EURUSD", result);
    }

    @Test
    void mapRateEnumToEndpoints_shouldReturnMappedList() {
        // Arrange
        List<String> rates = List.of("USD_TRY", "EUR_USD");

        // Act
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        // Assert
        assertEquals(List.of("USDTRY", "EURUSD"), endpoints);
    }

    @Test
    void mapEndpointToRateEnum_shouldInsertUnderscore() throws Exception {
        // Act
        String result = rateMapper.mapEndpointToRateEnum("USDTRY");

        // Assert
        assertEquals("USD_TRY", result);
    }
}
