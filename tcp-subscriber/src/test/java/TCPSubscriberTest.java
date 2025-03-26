import com.berkepite.RateDistributionEngine.TCPSubscriber.RateMapper;
import com.berkepite.RateDistributionEngine.TCPSubscriber.TCPConfig;
import com.berkepite.RateDistributionEngine.TCPSubscriber.TCPSubscriber;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.ISubscriberConfig;
import com.berkepite.RateDistributionEngine.common.status.ConnectionStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TCPSubscriberTest {

    // A test subclass that allows us to inject our mocks.
    public static class TestTCPSubscriber extends TCPSubscriber {
        public TestTCPSubscriber(ICoordinator coordinator, ISubscriberConfig config) throws IOException {
            super(coordinator, config);
        }

        // Setter to inject mocks for socket, writer, and reader
        public void setTestResources(Socket socket, PrintWriter writer, BufferedReader reader) {
            // Directly set the protected fields (assume package-private or use reflection if needed)
            this.setSocket(socket);
            this.setWriter(writer);
            this.setReader(reader);
        }
    }

    @Mock
    private ICoordinator coordinator;

    @Mock
    private TCPConfig config;

    @Mock
    private RateMapper rateMapper;

    @Mock
    private Socket socket;

    @Mock
    private PrintWriter writer;

    @Mock
    private BufferedReader reader;

    private TestTCPSubscriber subscriber;

    @BeforeEach
    void setUp() throws Exception {
        // Set up configuration mocks.
        when(config.getUrl()).thenReturn("localhost");
        when(config.getPort()).thenReturn(2000);
        when(config.getName()).thenReturn("TestSubscriber");

        subscriber = new TestTCPSubscriber(coordinator, config);
        // Inject our mocked socket, writer, and reader
        subscriber.setTestResources(socket, writer, reader);
    }

    @Test
    void testConnect_SuccessfulAuthentication() throws IOException {
        // Simulate successful authentication:
        // When reader.readLine() is called, first return "AUTH SUCCESS", then null (to end loop).
        when(config.getUsername()).thenReturn("client");
        when(config.getPassword()).thenReturn("2345");

        when(reader.readLine()).thenReturn("AUTH SUCCESS", (String) null);

        // Call connect() (which runs in the current thread and reads from our mocked reader)
        subscriber.connect();

        // Verify that coordinator.onConnect was called exactly once.
        verify(coordinator, timeout(5000).times(1)).onConnect(subscriber);
    }

    @Test
    void testConnect_AuthenticationFailure() throws IOException {
        // Simulate authentication failure:
        when(reader.readLine()).thenReturn("AUTH FAILED", (String) null);

        subscriber.connect();

        // Capture the ConnectionStatus passed to onConnectionError
        ArgumentCaptor<ConnectionStatus> captor = ArgumentCaptor.forClass(ConnectionStatus.class);
        verify(coordinator, timeout(5000).times(1)).onConnectionError(eq(subscriber), captor.capture());

        ConnectionStatus status = captor.getValue();
        assertNotNull(status);
        // Assuming that in handleInitialResponses "AUTH FAILED" produces a ConnectionStatus with notes "Auth failure"
        assertEquals("Auth failure", status.getNotes());
    }

    @Test
    void testSubscribe_SendsSubscriptionRequests() throws IOException {
        // Prepare a dummy list of rates.
        List<String> rates = List.of("EURUSD", "USDGBP");
        // We also need to simulate the mapping of rate enums to endpoints.
        // For simplicity, assume the CNNRateMapper returns the same strings.
        // (You could also spy on rateMapper if needed.)

        // Simulate subscribe: we don't need the reader for this test.
        subscriber.subscribe(rates);

        // Verify that for each endpoint the writer's println method is called with "sub|<endpoint>".
        // For example, if endpoints are exactly the same as input rates:
        for (String rate : rates) {
            verify(writer, timeout(1000)).println("sub|" + rate);
        }

        // Also verify that a listener is started.
        // Since subscribe() calls executorService.execute(this::listen), we can verify that onRateUpdate
        // is eventually called if we simulate an incoming rate response.
    }

    @Test
    void testUnSubscribe_SendsUnsubscriptionRequests() {
        List<String> rates = List.of("USDEUR", "GBPUSD");

        subscriber.unSubscribe(rates);

        for (String rate : rates) {
            verify(writer, times(1)).println("unsub|" + rate);
        }
    }

    @Test
    void testHandleResponses_ProcessesRateUpdate() throws Exception {
        String rawRateData = "name=GBPUSD|bid=0.794749723591663|ask=0.8345469814393971|timestamp=2025-02-11T21:17:35.845273291Z";

        when(reader.readLine()).thenReturn(rawRateData, (String) null);

        // Start subscribe() which will call listen() in a separate thread.
        subscriber.subscribe(List.of("GBPUSD"));

        // Await until coordinator.onRateUpdate is called.
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(coordinator, atLeastOnce()).onRateUpdate(eq(subscriber), any()));
    }
}
