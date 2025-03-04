package com.berkepite.MainApplication32Bit.rates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RawRateMappersUnitTest {

    BloombergRateMapper bloombergRateMapper;
    CNNRateMapper cnnRateMapper;

    RawRateMappersUnitTest() {
        bloombergRateMapper = new BloombergRateMapper();
        cnnRateMapper = new CNNRateMapper();
    }

    @Test
    public void shouldNotThrowWhenCorrectData() {
        Assertions.assertThatCode(() -> bloombergRateMapper.mapRate("{\"name\":\"USDTRY\",\"bid\":35.58865287000356,\"ask\":36.593605245433096,\"timestamp\":\"2025-02-12T17:56:03.341276687Z\"}", new RawRate()))
                .doesNotThrowAnyException();
        Assertions.assertThatCode(() -> cnnRateMapper.mapRate("name=GBPUSD|bid=0.794749723591663|ask=0.8345469814393971|timestamp=2025-02-11T21:17:35.845273291Z", new RawRate()))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowWhenIncorrectData() {
        Assertions.assertThatThrownBy(() -> bloombergRateMapper.mapRate("wrong format", new RawRate()));
        Assertions.assertThatThrownBy(() -> cnnRateMapper.mapRate("wrong format", new RawRate()));
    }

}
