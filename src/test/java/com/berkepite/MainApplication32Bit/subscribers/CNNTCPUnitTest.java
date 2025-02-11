package com.berkepite.MainApplication32Bit.subscribers;

import com.berkepite.MainApplication32Bit.coordinator.Coordinator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CNNTCPUnitTest {

    @Mock
    private Coordinator mockCoordinator;

    @InjectMocks
    private CNNTCPSubscriber underTest;

    @Test
    void shouldThrowExceptionWhenIncorrectDataFormat() {
        assertThatThrownBy(() ->
                underTest.createRate("wrong format")
        );
    }

    @Test
    void shouldNotThrowExceptionWhenCorrectDataFormat() {
        assertThatCode(() -> underTest.createRate("name=USDGBP|bid=0.794749723591663|ask=0.8345469814393971|timestamp=2025-02-11T21:17:35.845273291Z"))
                .doesNotThrowAnyException();
    }
}
