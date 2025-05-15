import com.berkepite.RateDistributionEngine.TCPSubscriber.TCPConfig;
import com.berkepite.RateDistributionEngine.TCPSubscriber.TCPSubscriber;
import com.berkepite.RateDistributionEngine.common.ICoordinator;
import com.berkepite.RateDistributionEngine.common.exception.subscriber.SubscriberBadCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TCPSubscriberTest {

    // A test subclass that allows us to inject our mocks.
    @Test
    void init_shouldEstablishConnection() throws Exception {
        // Arrange
        TCPConfig mockConfig = mock(TCPConfig.class);
        when(mockConfig.getUrl()).thenReturn("localhost");
        when(mockConfig.getPort()).thenReturn(8080);
        when(mockConfig.getName()).thenReturn("TestSub");

        ICoordinator mockCoordinator = mock(ICoordinator.class);

        TCPSubscriber subscriber = new TCPSubscriber(mockCoordinator, mockConfig);

        Socket mockSocket = mock(Socket.class);

        // Replace the socket after creation (setSocket)
        subscriber.setSocket(mockSocket);

        // Act
        subscriber.init();

        // Assert
        assertNotNull(subscriber.getWriter());
        assertNotNull(subscriber.getReader());
    }

    @Test
    void connect_shouldAuthenticateSuccessfully() throws Exception {
        // Arrange
        TCPConfig mockConfig = mock(TCPConfig.class);
        when(mockConfig.getUsername()).thenReturn("client");
        when(mockConfig.getPassword()).thenReturn("2345");

        ICoordinator mockCoordinator = mock(ICoordinator.class);
        TCPSubscriber subscriber = new TCPSubscriber(mockCoordinator, mockConfig);

        BufferedReader reader = new BufferedReader(new StringReader("AUTH SUCCESS\n"));
        PrintWriter writer = new PrintWriter(new StringWriter());

        subscriber.setReader(reader);
        subscriber.setWriter(writer);

        // Act
        subscriber.connect();

        // Assert
        verify(mockCoordinator).onConnect(subscriber);
    }

    @Test
    void connect_shouldThrowExceptionWhenAuthFails() throws Exception {
        // Arrange
        TCPConfig mockConfig = mock(TCPConfig.class);
        when(mockConfig.getUsername()).thenReturn("bad");
        when(mockConfig.getPassword()).thenReturn("credentials");

        ICoordinator mockCoordinator = mock(ICoordinator.class);
        TCPSubscriber subscriber = new TCPSubscriber(mockCoordinator, mockConfig);

        BufferedReader reader = new BufferedReader(new StringReader("AUTH FAILED\n"));
        PrintWriter writer = new PrintWriter(new StringWriter());

        subscriber.setReader(reader);
        subscriber.setWriter(writer);

        // Assert
        assertThrowsExactly(SubscriberBadCredentialsException.class, subscriber::connect);
    }

    @Test
    void subscribe_shouldSendSubCommands() {
        // Arrange
        TCPConfig mockConfig = mock(TCPConfig.class);
        when(mockConfig.getIncludeRates()).thenReturn(List.of());
        when(mockConfig.getExcludeRates()).thenReturn(List.of());

        ICoordinator mockCoordinator = mock(ICoordinator.class);
        TCPSubscriber subscriber = new TCPSubscriber(mockCoordinator, mockConfig);

        PrintWriter writer = spy(new PrintWriter(new StringWriter(), true));
        subscriber.setWriter(writer);

        subscriber.setReader(new BufferedReader(new StringReader(""))); // dummy
        subscriber.setSocket(mock(Socket.class)); // dummy

        // Act
        subscriber.subscribe(List.of("EUR_USD", "USD_TRY"));

        // Assert
        verify(writer).println("sub|EURUSD");
        verify(writer).println("sub|USDTRY");
    }
}
