import com.berkepite.RateDistributionEngine.RestSubscriber.RateMapper;
import com.berkepite.RateDistributionEngine.common.exception.RateMapperException;
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
        String json = """
                {
                    "name": "GBPUSD",
                    "bid": 1.2529170540185999,
                    "ask": 1.3120093231713592,
                    "timestamp": "2025-05-20T19:16:37.117447348Z"
                }
                """;

        // Act
        RawRate rate = rateMapper.createRawRate(json);

        // Assert
        assertEquals("GBP_USD", rate.getType());
        assertEquals(1.2529170540185999, rate.getBid());
        assertEquals(1.3120093231713592, rate.getAsk());
        assertEquals(Instant.parse("2025-05-20T19:16:37Z"), rate.getTimestamp());
        assertEquals("REST_PROVIDER", rate.getProvider());
    }

    @Test
    void createRawRate_shouldThrowExceptionOnInvalidData() {
        // Arrange: Hatalı timestamp
        String json = """
                {
                    "name": "GBPUSD",
                    "bid": 1.2529,
                    "ask": 1.3120,
                    "timestamp": "not-a-timestamp"
                }
                """;

        // Act & Assert
        RateMapperException exception = assertThrows(RateMapperException.class, () -> {
            rateMapper.createRawRate(json);
        });

        assertTrue(exception.getMessage().contains("Could not parse Raw Rate data"));
    }

    @Test
    void mapRateEnumToEndpoint_shouldRemoveUnderscore() {
        // Act
        String result = rateMapper.mapRateEnumToEndpoint("GBP_USD");

        // Assert
        assertEquals("GBPUSD", result);
    }

    @Test
    void mapRateEnumToEndpoints_shouldReturnMappedList() {
        // Arrange
        List<String> rates = List.of("GBP_USD", "EUR_USD");

        // Act
        List<String> endpoints = rateMapper.mapRateEnumToEndpoints(rates);

        // Assert
        assertEquals(List.of("GBPUSD", "EURUSD"), endpoints);
    }

    @Test
    void mapEndpointToRateEnum_shouldInsertUnderscore() throws Exception {
        // Act
        String result = rateMapper.mapEndpointToRateEnum("GBPUSD");

        // Assert
        assertEquals("GBP_USD", result);
    }
}
