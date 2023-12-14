package de.gematik.test.erezept.primsys;

import lombok.val;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class GrizzlyServerShutdownHookTest {

    @Test
     void shouldThrowInterruptedException() throws ExecutionException, InterruptedException {
        val server = mock(HttpServer.class);
        val shutdownHook = new GrizzlyServerShutdownHook(server);
        GrizzlyFuture<HttpServer> future = mock(GrizzlyFuture.class);

        when(server.shutdown(anyLong(), any(TimeUnit.class))).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("Interrupted!"));
        shutdownHook.start();
        assertThrows(InterruptedException.class, future::get);


    }

    @Test
     void testRun() throws ExecutionException, InterruptedException {
        val server = mock(HttpServer.class);
        val shutdownHook = new GrizzlyServerShutdownHook(server);
        GrizzlyFuture<HttpServer> future = mock(GrizzlyFuture.class);

        when(future.get()).thenReturn(server);
        when(server.shutdown(anyLong(), any(TimeUnit.class))).thenReturn(future);
        shutdownHook.start();
        assertFalse(shutdownHook.isInterrupted());


    }


}
