/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.pspwsclient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.Socket;
import lombok.SneakyThrows;
import lombok.val;
import org.java_websocket.handshake.HandshakeImpl1Server;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;

class PharmaServiceProviderWSClientTest {

  private final String testUri = "abcTest";
  private final String apoId = "apoId";

  private final String tsstMessage =
      "{\n"
          + "  \"deliveryOption\" : null,\n"
          + "  \"clientId\" : \"apoAmFlughafen\",\n"
          + "  \"transactionId\" : \"transactionTestID\",\n"
          + "  \"blob\" : \"TUlBR0N5cUdTSWIzRFFFSkVBRVhvSUF3Z0FJQkFER0NBZ013Z2dIL0FnRUFNSUdtTUlHYU1Rc3dDUVlEVlFRR0V3SkVSVEVmTUIwR0ExVUVDZ3dXWjJWdFlYUnBheUJIYldKSUlFNVBWQzFXUVV4SlJERklNRVlHQTFVRUN3dy9TVzV6ZEdsMGRYUnBiMjRnWkdWeklFZGxjM1Z1WkdobGFYUnpkMlZ6Wlc1ekxVTkJJR1JsY2lCVVpXeGxiV0YwYVd0cGJtWnlZWE4wY25WcmRIVnlNU0F3SGdZRFZRUUREQmRIUlUwdVUwMURRaTFEUVRJMElGUkZVMVF0VDA1TVdRSUhBWExld1VKWHhqQk5CZ2txaGtpRzl3MEJBUWN3UUtBUE1BMEdDV0NHU0FGbEF3UUNBUVVBb1J3d0dnWUpLb1pJaHZjTkFRRUlNQTBHQ1dDR1NBRmxBd1FDQVFVQW9nOHdEUVlKS29aSWh2Y05BUUVKQkFBRWdnRUFUQ0NxbEJvcmVtM3FHNXc4UWJwNXJKdEkyTm03anVQL1dEdUpLbXRLQzZ4OXErRjRvTis1Sk9kcXNZWWdBbGZwbitFL1lCcmlybHVXalpUNGZraFJRODF4TDZJa3Q5aG4rRmVldUxoYjhuL1dzenl4SDZCMG1ydFJvYUVDcVlDUjJkeFY4VnFkSUNJcS9Md2dOWFR5TXRNaXZIQUxST2gvWnVCUndxM0dVWlh5empBd0VOOVkxdm12QVl1RmNaT2xDYm8xSkxIZUgyRm5aNk02aEJyWVVpQjZJM21JWmVyWXA2TndicXBwS3ExZzZCUzczWmJMbG5lVnowZURSNHh0RFVoZlUya3I2OFFQNWJ1UkhvcWc2WklTZG5HWWRqV2VlK1g5RGE0QWc1cVppQlZ5M0syYWM0QkNYL0RKMGU3dnhlSU13VFZEaGVhOS8wcVVLUjRCT2pDQUJna3Foa2lHOXcwQkJ3RXdIZ1lKWUlaSUFXVURCQUV1TUJFRURIa0ZNK20xR3VreFdLV3JGUUlCRUlDQ0Ficm5KNmlvRFNGczV3clp0YnNsOWNSK3ZQYmhOc3drOHBlWDg4YWloRGVjZlovRGFhSnFoaVljYURac1JyV2p3bi91a3NjQW5nWklqTzQxVnVKREpFZUdzdXpGd1FoZUM3MHVONkxFR1E4SXdKeXJwT21Vam0wTGlWcjNVSlNWenh2TVZHMDJyL25DWmp0TmZGaUVkdlB4ZVFCVHJSTkp5MzJjL0VHaFNqdmRQTE93WW5kMmtxMFBLWHlISkdoektEYVFuZnRXZXlZakh2aCtqaUo4bWJueU9IaVkvRjZqMjdXU0tJZ0pmbWw3NE5uUWQ3V3FHbmgrckNSN2I2VlR0QmNMWjF0MjF6dTk0RjNaUnRwNkZxL2pidGpLeDM3dnlmSlBmU0tDTEZUczN2RHN1UmZBSUgrc1phY0ZlZHpEVlFDbU9GOWpDN0dCd3F5QitiQW55eGZKYlM3MitPWlBxeGFWM1l2dHA5UmpOWUJ4VDBFRVZFSzkvUHJGcm1CNVdpekdnM3NnaDRqWk9BVEw4Ym9FakpndmFwWWxWOWdkOGw0NE5SN2o3OUl6Z2tYNXg3K3ZTdFpFTExCNDhuUVkrY1JlZ1Q0YjhsL2ZscllJRXp2bDdnWU5xcUYycmxXb3RzM3djQzJvd0NsdFErNyt3T2UwbWhpclduakZkc3ZpMW5PdzJBVkZxWlJtQlpHdzV4cWMyYmhtTjVmUVlWNWpvK1dYS0t4VStPdFlYL09POGdXQjVJQ3hEeXNUb2k0Kzc3N0hLYnBCRWNmbnBIdmtBQUFFRU5PTktVV1dJZnBIMTlzUnlyeS8vQUVBQUFBQUFBQT0=\",\n"
          + "  \"note\" : \"arrived @ null with two Ids\"\n"
          + "}";

  @Test
  void shouldAcceptUriWithoutTailingSlashTest() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId, "asd");
    assertEquals(testUri + "/" + apoId, client.getURI().getPath());
  }

  @Test
  void shouldAcceptUriWithTailingSlashTest() {
    val testUriWithTailingSlash = testUri + "/";
    val client = new PharmaServiceProviderWSClient(testUriWithTailingSlash, apoId);
    assertEquals(testUriWithTailingSlash + apoId, client.getURI().getPath());
  }

  @Test
  void shouldNotThrowOnOpenWithEmptyContent() {
    ServerHandshake serverHandshake = new HandshakeImpl1Server();
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    assertDoesNotThrow(() -> client.onOpen(serverHandshake));
  }

  @Test
  void shouldNotThrowOnOpenWithContent() {
    ServerHandshake serverHandshake = mock(ServerHandshake.class);
    when(serverHandshake.getContent()).thenReturn("Teststring".getBytes());
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    assertDoesNotThrow(() -> client.onOpen(serverHandshake));
  }

  @SneakyThrows
  @Test
  void shouldDetectConnectionMessage() {
    val mockClient = mock(PharmaServiceProviderWSClient.class);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    doCallRealMethod().when(mockClient).onMessage(anyString());
    doCallRealMethod().when(mockClient).isConnected();
    mockClient.onMessage("connected to WebSocketServerImpl");
    assertTrue(mockClient.isConnected());
  }

  @SneakyThrows
  @Test
  void shouldThrowInvalidMessage() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    val mockClient = spy(client);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    doCallRealMethod().when(mockClient).onMessage(anyString());
    doCallRealMethod().when(mockClient).hasMessage();
    when(mockClient.getQueueLength()).thenCallRealMethod();
    assertDoesNotThrow(() -> mockClient.onMessage("{abc"));
    assertEquals(0, mockClient.getQueueLength());
    assertFalse(mockClient.hasMessage());
  }

  @SneakyThrows
  @Test
  void shouldHaveOneNewMessage() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    val mockClient = spy(client);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    doCallRealMethod().when(mockClient).onMessage(anyString());
    doCallRealMethod().when(mockClient).hasMessage();
    when(mockClient.getQueueLength()).thenCallRealMethod();
    assertDoesNotThrow(() -> mockClient.onMessage(tsstMessage));
    assertEquals(1, mockClient.getQueueLength());
    assertTrue(mockClient.hasMessage());
  }

  @SneakyThrows
  @Test
  void shouldClearMessageQueue() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    val mockClient = spy(client);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    doCallRealMethod().when(mockClient).onMessage(anyString());
    doCallRealMethod().when(mockClient).hasMessage();
    doCallRealMethod().when(mockClient).clearQueue();
    when(mockClient.getQueueLength()).thenCallRealMethod();
    mockClient.onMessage(tsstMessage);
    mockClient.onMessage(tsstMessage);
    assertEquals(2, mockClient.getQueueLength());
    mockClient.clearQueue();
    assertFalse(mockClient.hasMessage());
  }

  @SneakyThrows
  @Test
  void shouldConsumeOldest() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    val mockClient = spy(client);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    doCallRealMethod().when(mockClient).onMessage(anyString());
    when(mockClient.getQueueLength()).thenCallRealMethod();
    mockClient.onMessage(tsstMessage);
    mockClient.onMessage(tsstMessage);
    assertEquals(2, mockClient.getQueueLength());
    mockClient.consumeOldest();
    assertEquals(1, mockClient.getQueueLength());
    mockClient.consumeOldest(100);
    assertEquals(0, mockClient.getQueueLength());
  }

  @SneakyThrows
  @Test
  void consumeOldestShouldReturnOptional() {
    val client = new PharmaServiceProviderWSClient(testUri, apoId);
    val mockClient = spy(client);
    val socketMock = mock(Socket.class);
    when(socketMock.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    when(mockClient.getSocket()).thenReturn(socketMock);
    when(mockClient.getQueueLength()).thenCallRealMethod();
    assertTrue(mockClient.consumeOldest().isEmpty());
  }
}
