/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.pharmacyserviceprovider.intergationtests;

import de.gematik.test.erezept.pspwsclient.PharmaServiceProviderWSClient;
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PSPIntegrationTest {

    static String SERVER_AUTH;
    static final int WAIT_MILLIS_BEFORE_ASSERT = 300;
    static final int WAIT_MILLIS_BEFORE_SEND = 100;
    static final String apoId = "apoAmFlughafen";
    static PharmaServiceProviderWSClient clientEndPoint;
    static byte[] body;

    static String URL_WSS_GRIZZLY;
    static String SERVER_URL;

    static void fetchTargets() {
        IntegrationSetupTest.fetchTargets();
        SERVER_AUTH = IntegrationSetupTest.serverAuth;
        URL_WSS_GRIZZLY = IntegrationSetupTest.wssUrl;
        SERVER_URL = IntegrationSetupTest.serverUrl;
    }

    @SneakyThrows
    @BeforeAll
    static void setup() {
        fetchTargets();
        // generate connection
        if (URL_WSS_GRIZZLY.toLowerCase().startsWith("wss")) {
            clientEndPoint = new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, apoId, SERVER_AUTH);
        } else {
            clientEndPoint = new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, apoId);
        }
        clientEndPoint.connectBlocking(1, TimeUnit.SECONDS);
        byte[] array = new byte[350 * 30];
        new Random().nextBytes(array);
        body = array;
    }

    @SneakyThrows
    @AfterAll
    static void closeConnection() {
            clientEndPoint.clearQueueOnServer();
            clientEndPoint.clearQueue();
            clientEndPoint.close();
    }

    static void sendUnirestPostWithTwoIdsAndBody(String url) {
        Unirest.post(url)
                .routeParam(Map.of("ti_id", apoId, "transactionID", "transactionTestID"))
                .header("X-Authorization", SERVER_AUTH)
                .header("Content-Type", "application/pkcs7-mime")
                .body(body)
                .asString();
    }

    static void sendUnirestPostToMockEndpiont(String mockApo) {
        Unirest.post(format(SERVER_URL + "/pspmock/%s/{ti_id}?req={transactionID}", mockApo))
                .routeParam(Map.of("ti_id", apoId, "transactionID", "transactionTestID"))
                .header("X-Authorization", SERVER_AUTH)
                .header("Content-Type", "application/pkcs7-mime")
                .body(body)
                .asString();
    }

    @SneakyThrows
    @BeforeEach
    void clearMessage() {
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
        clientEndPoint.clearQueue();
    }

    @SneakyThrows
    @Test
    void expectCorrectDeliveryOption() {
        clientEndPoint.clearQueue();
        sendUnirestPostToMockEndpiont("shipment");
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        assertEquals(
                DeliveryOption.SHIPMENT,
                clientEndPoint.consumeOldest(1000).orElseThrow().getDeliveryOption());
    }

    @SneakyThrows
    @Test
    void expectNoMessage() {
        clientEndPoint.clearQueue();
        sendUnirestPostToMockEndpiont("liefern");
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        assertFalse(clientEndPoint.hasMessage());
    }

    @SneakyThrows
    @Test
    void testPickUpWsConnectionShouldAnsTwoIds() {
        clientEndPoint.clearQueue();
        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/pick_up/{ti_id}?req={transactionID}");
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);

        assertTrue(clientEndPoint.hasMessage());
        val optionalMessage = clientEndPoint.consumeOldest(1000);
        Assertions.assertEquals(
                DeliveryOption.ON_PREMISE, optionalMessage.orElseThrow().getDeliveryOption());
        assertTrue(optionalMessage.isPresent());
        val message = optionalMessage.orElseThrow();
        assertEquals(body.length, message.getBlob().length);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
            "'/pspmock/versand/{ti_id}?req={transactionID}', 'arrived @ SHIPMENT' ",
            "'/local_delivery/{ti_id}?req={transactionID}', 'arrived @ DELIVERY' ",
            "'/delivery_only/{ti_id}?req={transactionID}', 'arrived @ SHIPMENT' ",
    })
    void shouldAnswer(String send, String expect) {
        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + send);
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        Assertions.assertEquals(
                expect,
                clientEndPoint.consumeOldest(1000).orElseThrow(() -> new AssertionError()).getNote());
    }

    // QueueTests
    @SneakyThrows
    @Test
    void testQueueShouldBeSizeOfTree() {
        clientEndPoint.clearQueue();
        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}");

        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}");

        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}");

        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        assertEquals(3, clientEndPoint.getQueueLength());
    }

    @SneakyThrows
    @Test
    void clearQueueShouldWork() {
        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}");
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        assertTrue(clientEndPoint.hasMessage());
        clientEndPoint.clearQueue();
        assertEquals(0, clientEndPoint.getQueueLength());
    }

    @SneakyThrows
    @Test
    void consumeOldestWorksCorrect() {
        for (int i = 0; i < 3; i++) {
            sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}");
            Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
        }
        Unirest.post(SERVER_URL + "/pspmock/shipment/{ti_id}?req={transactionID}")
                .routeParam(Map.of("ti_id", apoId, "transactionID", "44"))
                .header("X-Authorization", SERVER_AUTH)
                .header("Content-Type", "application/pkcs7-mime")
                .body(body)
                .asString();
        Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
                .routeParam(Map.of("ti_id", apoId, "transactionID", "44"))
                .header("X-Authorization", SERVER_AUTH)
                .header("Content-Type", "application/pkcs7-mime")
                .body(body)
                .asString();

        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        try {
            val recipeData = clientEndPoint.consumeOldest(1000);
            assertEquals("44", recipeData.orElseThrow(() -> new AssertionError()).getTransactionId());
        } catch (IndexOutOfBoundsException e) {
            throw new AssertionError(e);
        }
    }


    @SneakyThrows
    @Test
    void hasMassage() {
        clientEndPoint.clearQueue();
        sendUnirestPostWithTwoIdsAndBody(SERVER_URL + "/pspmock/shipment/{ti_id}?req={transactionID}");
        Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
        assertTrue(clientEndPoint.hasMessage());
    }


}
