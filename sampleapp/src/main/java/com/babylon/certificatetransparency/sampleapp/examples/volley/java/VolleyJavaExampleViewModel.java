/*
 * Copyright 2018 Babylon Healthcare Services Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babylon.certificatetransparency.sampleapp.examples.volley.java;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.babylon.certificatetransparency.sampleapp.examples.BaseExampleViewModel;
import org.certificatetransparency.ctlog.HostnameVerifierBuilder;
import org.certificatetransparency.ctlog.Logger;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class VolleyJavaExampleViewModel extends BaseExampleViewModel {

    private final Context applicationContext;

    public VolleyJavaExampleViewModel(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @NotNull
    @Override
    public String getSampleCodeTemplate() {
        return "volley-java.txt";
    }

    private void enableCertificateTransparencyChecks(
            HttpURLConnection connection,
            Set<String> hosts,
            boolean isFailOnError,
            Logger defaultLogger
    ) {
        if (connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

            // Create a hostname verifier wrapping the original
            HostnameVerifierBuilder builder = new HostnameVerifierBuilder(httpsConnection.getHostnameVerifier())
                    .setFailOnError(isFailOnError)
                    .setLogger(defaultLogger);

            for (String host : hosts) {
                builder.addHost(host);
            }

            httpsConnection.setHostnameVerifier(builder.build());
        }
    }

    // A normal client would create this ahead of time and share it between network requests
    // We create it dynamically as we allow the user to set the hosts for certificate transparency
    private RequestQueue createRequestQueue(Set<String> hosts, boolean isFailOnError, Logger defaultLogger) {
        return Volley.newRequestQueue(applicationContext,
                new HurlStack() {
                    @Override
                    protected HttpURLConnection createConnection(URL url) throws IOException {
                        HttpURLConnection connection = super.createConnection(url);

                        enableCertificateTransparencyChecks(connection, hosts, isFailOnError, defaultLogger);

                        return connection;
                    }
                }
        );
    }

    @Override
    public void openConnection(@NotNull String connectionHost, @NotNull Set<String> hosts, boolean isFailOnError, @NotNull Logger defaultLogger) {
        RequestQueue queue = createRequestQueue(hosts, isFailOnError, defaultLogger);

        // Failure. Send message to the UI as logger won't catch generic network exceptions
        Request<String> request = new StringRequest(Request.Method.GET, "https://" + connectionHost,
                response -> {
                    // Success. Reason will have been sent to the logger
                },
                this::sendException);

        // Explicitly disable cache so we always call the interceptor and thus see the certificate transparency results
        request.setShouldCache(false);

        // Add the request to the RequestQueue.
        queue.add(request);

    }
}