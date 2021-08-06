package co.casterlabs.sora.example;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;
import lombok.SneakyThrows;

@PluginImplementation
public class ExamplePlugin extends SoraPlugin implements HttpProvider, WebsocketProvider {

    @Override
    public void onInit(Sora sora) {
        sora.addHttpProvider(this, this);
        sora.addWebsocketProvider(this, this);
    }

    @Override
    public void onClose() {} // Listeners are automatically unregistered for you.

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getAuthor() {
        return "Casterlabs";
    }

    @Override
    public String getName() {
        return "Example Plugin";
    }

    @Override
    public String getId() {
        // Doesn't have to match your package, it's just nice to be unique.
        return "co.casterlabs.sora.example.ExamplePlugin";
    }

    @HttpEndpoint(uri = "/example")
    public HttpResponse onExample(SoraHttpSession session) {
        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, "Hello " + session.getRemoteIpAddress() + "!");
    }

    @HttpEndpoint(uri = "/echo/:echo")
    public HttpResponse onEchoParam(SoraHttpSession session) {
        // You can have uri parameters.
        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, "Your parameter: " + session.getUriParameters().get("echo"));
    }

    @WebsocketEndpoint(uri = "/echo")
    public WebsocketListener onEcho(SoraWebsocketSession session) {
        return new WebsocketListener() {

            // SneakyThrows is a helpful annotation from lombok.
            @SneakyThrows
            @Override
            public void onOpen(Websocket websocket) {
                websocket.send("Hello " + session.getRemoteIpAddress());
            }

            @SneakyThrows
            @Override
            public void onText(Websocket websocket, String message) {
                websocket.send(message);
            }

            @SneakyThrows
            @Override
            public void onBinary(Websocket websocket, byte[] bytes) {
                websocket.send(bytes);
            }

        };
    }

}
