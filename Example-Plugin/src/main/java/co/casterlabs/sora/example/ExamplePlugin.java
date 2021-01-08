package co.casterlabs.sora.example;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpSession;
import co.casterlabs.sora.api.http.HttpStatus;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.api.websockets.Websocket;
import co.casterlabs.sora.api.websockets.WebsocketListener;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.WebsocketSession;
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
    public HttpResponse onExample(HttpSession session) {
        return HttpResponse.newFixedLengthResponse(HttpStatus.OK, "Hello " + session.getRemoteIpAddress() + "!");
    }

    @WebsocketEndpoint(uri = "/echo")
    public WebsocketListener onEcho(WebsocketSession session) {
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

    @Override
    public HttpResponse onNoProvider(HttpSession session) {
        return HttpResponse.newFixedLengthResponse(HttpStatus.NOT_FOUND, "Navigate to /example or open a Websocket on /echo");
    }

}
