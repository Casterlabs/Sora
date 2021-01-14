package co.casterlabs.sora.networking.nano;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.api.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpResponse.TransferEncoding;
import co.casterlabs.sora.api.http.HttpStatus;
import co.casterlabs.sora.api.websockets.WebsocketListener;
import co.casterlabs.sora.networking.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoWSD;

public class NanoServer extends NanoWSD implements Server {

    static {
        try {
            Field field = NanoHTTPD.class.getDeclaredField("LOG");

            field.setAccessible(true);

            Logger log = (Logger) field.get(null);

            log.setLevel(Level.OFF); // Hush
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public NanoServer(String address, int port) {
        super(address, port);
    }

    @Override
    public void start() throws IOException {
        this.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    // Serves http sessions or calls super to serve websockets
    @Override
    public Response serve(IHTTPSession nanoSession) {
        try {
            if (this.isWebsocketRequested(nanoSession)) {
                return super.serve(nanoSession);
            } else {
                HttpResponse response = SoraFramework.getInstance().getSora().serveHttp(new NanoHttpSessionWrapper(nanoSession));

                if ((response != null) && (response.getStatus() != HttpStatus.NO_RESPONSE)) {
                    IStatus status = convertStatus(response.getStatus());
                    String mime = response.getAllHeaders().getOrDefault("content-type", "text/plaintext");

                    response.removeHeader("content-type");

                    Response nanoResponse;

                    if (response.getMode() == TransferEncoding.CHUNKED) {
                        nanoResponse = NanoHTTPD.newChunkedResponse(status, mime, response.getResponseStream());
                    } else {
                        nanoResponse = NanoHTTPD.newFixedLengthResponse(status, mime, response.getResponseStream(), response.getLength());
                    }

                    for (Map.Entry<String, String> header : response.getAllHeaders().entrySet()) {
                        nanoResponse.addHeader(header.getKey(), header.getValue());
                    }

                    return nanoResponse;
                }
            }
        } catch (NullPointerException ignored) {}

        // NanoHTTPD.class:194, this wild card allows us to drop
        // the connection by throwing an unchecked exception.
        /*
            } catch (Exception e) {
        */
        throw new RuntimeException("Drop connection.");
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession nanoSession) {
        WebsocketListener listener = SoraFramework.getInstance().getSora().serveWebsocket(new NanoWebsocketSessionWrapper(nanoSession));

        if (listener != null) {
            return new NanoWebsocketWrapper(nanoSession, listener);
        } else {
            return null;
        }
    }

    private static IStatus convertStatus(HttpStatus status) {
        return new IStatus() {
            @Override
            public String getDescription() {
                return status.getStatusCode() + " " + status.getDescription(); // What the hell Nano
            }

            @Override
            public int getRequestStatus() {
                return status.getStatusCode();
            }
        };
    }

}
