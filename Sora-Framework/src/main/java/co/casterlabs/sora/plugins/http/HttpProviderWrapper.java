package co.casterlabs.sora.plugins.http;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpSession;
import lombok.NonNull;

public class HttpProviderWrapper {
    private List<HttpEndpointWrapper> endpoints;
    private HttpProvider provider;

    public HttpProviderWrapper(HttpProvider provider) {
        this.provider = provider;

        this.endpoints = HttpEndpointWrapper.wrap(this.provider);
    }

    public @Nullable HttpResponse serve(@NonNull HttpSession session) {
        for (HttpEndpointWrapper endpoint : this.endpoints) {
            HttpResponse response = endpoint.serve(session);

            if (response != null) {
                return response;
            }
        }

        return null;
    }

    public HttpResponse onNoProvider(@NonNull HttpSession session) {
        return this.provider.onNoProvider(session);
    }

}
