package co.casterlabs.sora.networking.nano;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import co.casterlabs.sora.api.websockets.WebsocketCloseCode;
import co.casterlabs.sora.api.websockets.WebsocketFrameType;
import co.casterlabs.sora.api.websockets.WebsocketListener;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import fi.iki.elonen.NanoWSD.WebSocketFrame.OpCode;
import lombok.NonNull;

public class NanoWebsocketWrapper extends WebSocket {
    private WebsocketListener listener;

    private WebSocket instance = this;
    private SoraWebsocket soraWebsocket = new SoraWebsocket();

    public NanoWebsocketWrapper(IHTTPSession nanoSession, WebsocketListener listener) {
        super(nanoSession);

        this.listener = listener;
    }

    // Nano WebSocket Impl
    @Override
    protected void onOpen() {
        this.listener.onOpen(this.soraWebsocket);
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        this.listener.onClose(this.soraWebsocket);
    }

    @Override
    protected void onMessage(WebSocketFrame frame) {
        if (frame.getOpCode() == OpCode.Binary) {
            this.listener.onFrame(this.soraWebsocket, new co.casterlabs.sora.api.websockets.WebsocketFrame() {
                @Override
                public WebsocketFrameType getFrameType() {
                    return WebsocketFrameType.BINARY;
                }

                @Override
                public String getAsText() {
                    return new String(this.getBytes(), StandardCharsets.UTF_8);
                }

                @Override
                public byte[] getBytes() {
                    return frame.getBinaryPayload();
                }

                @Override
                public int getSize() {
                    return this.getBytes().length;
                }
            });
        } else if (frame.getOpCode() == OpCode.Text) {
            this.listener.onFrame(this.soraWebsocket, new co.casterlabs.sora.api.websockets.WebsocketFrame() {

                @Override
                public WebsocketFrameType getFrameType() {
                    return WebsocketFrameType.TEXT;
                }

                @Override
                public String getAsText() {
                    return frame.getTextPayload();
                }

                @Override
                public byte[] getBytes() {
                    return this.getAsText().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public int getSize() {
                    return this.getBytes().length;
                }

            });
        }
    }

    @Override
    protected void onPong(WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException ignored) {}

    public class SoraWebsocket extends co.casterlabs.sora.api.websockets.Websocket {

        @Override
        public void send(@NonNull String message) throws IOException {
            instance.send(message);
        }

        @Override
        public void send(@NonNull byte[] bytes) throws IOException {
            instance.send(bytes);
        }

        @Override
        public void close(@NonNull WebsocketCloseCode code) throws IOException {
            instance.close(CloseCode.find(code.getCode()), "", false);
        }

        // Request headers
        @Override
        public @NonNull Map<String, String> getHeaders() {
            return instance.getHandshakeRequest().getHeaders();
        }

        // URI
        @Override
        public String getUri() {
            return instance.getHandshakeRequest().getUri();
        }

        @Override
        public @NonNull Map<String, List<String>> getAllQueryParameters() {
            return instance.getHandshakeRequest().getParameters();
        }

        @SuppressWarnings("deprecation")
        @Override
        public @NonNull Map<String, String> getQueryParameters() {
            return instance.getHandshakeRequest().getParms();
        }

        @Override
        public @NonNull String getQueryString() {
            if (instance.getHandshakeRequest().getQueryParameterString() == null) {
                return "";
            } else {
                return "?" + instance.getHandshakeRequest().getQueryParameterString();
            }
        }

        // Misc
        @Override
        public @NonNull String getRemoteIpAddress() {
            return instance.getHandshakeRequest().getRemoteIpAddress();
        }

    }

}
