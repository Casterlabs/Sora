package co.casterlabs.sora.networking.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraUtil;
import co.casterlabs.sora.api.DropConnectionException;
import co.casterlabs.sora.api.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpResponse.TransferEncoding;
import co.casterlabs.sora.api.http.HttpSession;
import co.casterlabs.sora.api.http.HttpStatus;
import co.casterlabs.sora.api.websockets.Websocket;
import co.casterlabs.sora.api.websockets.WebsocketListener;
import co.casterlabs.sora.api.websockets.WebsocketSession;
import co.casterlabs.sora.networking.Server;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class UndertowServer implements Server, HttpHandler, WebSocketConnectionCallback {
    private Undertow undertow;

    static {
        System.setProperty("org.jboss.logging.provider", "slf4j"); // This mutes it.
    }

    public UndertowServer(String address, int port) {
        //@formatter:off
        this.undertow = Undertow.builder()
                .addHttpListener(port, address)
                .setHandler(Handlers.websocket(this, this))
                .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
                .build();
        //@formatter:on
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            HttpSession session = new UndertowHttpSessionWrapper(exchange);
            HttpResponse response = SoraFramework.getInstance().getSora().serveHttp(session);

            if (response == null) {
                exchange.setStatusCode(HttpStatus.NOT_IMPLEMENTED.getStatusCode());
                exchange.setReasonPhrase(HttpStatus.NOT_IMPLEMENTED.getDescription());
            } else if (response.getStatus() == HttpStatus.NO_RESPONSE) {
                exchange.getConnection().close();
            } else {
                exchange.setStatusCode(response.getStatus().getStatusCode());
                exchange.setReasonPhrase(response.getStatus().getDescription());

                for (Map.Entry<String, String> entry : response.getAllHeaders().entrySet()) {
                    exchange.getResponseHeaders().add(HttpString.tryFromString(entry.getKey()), entry.getValue());
                }

                if (response.getMode() == TransferEncoding.FIXED_LENGTH) {
                    exchange.setResponseContentLength(response.getLength());
                }

                exchange.dispatch(() -> {
                    try {
                        exchange.startBlocking();

                        SoraUtil.writeInputStreamToOutputStream(response.getResponseStream(), exchange.getOutputStream());

                        response.getResponseStream().close();
                        exchange.getResponseSender().close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (DropConnectionException e) {
            exchange.getConnection().close();
        } catch (Exception e) {
            exchange.getConnection().close();
            e.printStackTrace();
        }
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        try {
            WebsocketSession session = new UndertowWebsocketSessionWrapper(exchange, channel);
            WebsocketListener listener = SoraFramework.getInstance().getSora().serveWebsocket(session);

            if (listener != null) {
                Websocket websocket = new UndertowWebsocketChannelWrapper(channel, session);

                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                    @Override
                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                        listener.onText(websocket, message.getData());
                    }

                    @SuppressWarnings("deprecation")
                    @Override
                    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
                        for (ByteBuffer buffer : message.getData().getResource()) {
                            listener.onBinary(websocket, buffer.array());
                        }
                    }

                    @Override
                    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                        listener.onClose(websocket);
                        try {
                            webSocketChannel.sendClose();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onError(WebSocketChannel channel, Throwable ignored) {}

                });

                channel.resumeReceives();
            } else {
                try {
                    channel.sendClose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (DropConnectionException e) {
            exchange.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws IOException {
        this.undertow.start();
    }

    @Override
    public void stop() throws IOException {
        this.undertow.stop();
    }

}
