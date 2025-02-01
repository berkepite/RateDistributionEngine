package com.berkepite.MainApplication32Bit.coordinator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.Assertions;

@SpringBootTest
@ActiveProfiles("test")
public class CoordinatorTest {

    @Autowired
    private Coordinator coordinator;

    @Test
    public void testCoordinatorConstructor_OK() {
        Assertions.assertNotNull(coordinator);
    }
}
