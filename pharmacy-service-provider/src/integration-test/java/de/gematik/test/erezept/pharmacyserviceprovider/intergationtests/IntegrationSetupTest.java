/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.pharmacyserviceprovider.intergationtests;

public class IntegrationSetupTest {


    static final String TARGET_URL_KEY = "psp.target.url";
    static final String TARGET_DEFAULT_URL = "http://localhost:9095";
    static final String TARGET_AUTH_KEY = "psp.target.auth";

    static String serverUrl;
    static String wssUrl;
    static String serverAuth;

    static void fetchTargets() {
        serverUrl = System.getProperty(TARGET_URL_KEY, TARGET_DEFAULT_URL);
        wssUrl = serverUrl.replace("http", "ws");
        serverAuth = System.getProperty(TARGET_AUTH_KEY, "000");
    }
}
