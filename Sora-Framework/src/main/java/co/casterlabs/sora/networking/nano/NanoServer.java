package co.casterlabs.sora.networking.nano;

import java.io.IOException;
import java.io.InputStream;

import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.api.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpResponse.TransferEncoding;
import co.casterlabs.sora.api.http.HttpStatus;
import co.casterlabs.sora.api.websockets.WebsocketListener;
import co.casterlabs.sora.networking.Server;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD;

public class NanoServer extends NanoWSD implements Server {
    private static final InputStream NULL_STREAM = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };

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
        if (this.isWebsocketRequested(nanoSession)) {
            try {
                return super.serve(nanoSession);
            } catch (NullPointerException ignored) {}
        } else {
            HttpResponse response = SoraFramework.getInstance().getSora().serveHttp(new NanoHttpSessionWrapper(nanoSession));

            if ((response != null) && (response.getStatus() != HttpStatus.NO_RESPONSE)) {
                IStatus status = convertStatus(response.getStatus());
                String mime = response.getAllHeaders().get("content-type");

                if (response.getMode() == TransferEncoding.CHUNKED) {
                    return NanoHTTPD.newChunkedResponse(status, mime, response.getResponseStream());
                } else {
                    return NanoHTTPD.newFixedLengthResponse(status, mime, response.getResponseStream(), response.getLength());
                }
            }
        }

        // Unfortunately we can't drop connections with NanoHTTPD as-is.
        return NanoHTTPD.newFixedLengthResponse(Status.NOT_IMPLEMENTED, "text/plain", NULL_STREAM, 0);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession nanoSession) {
        WebsocketListener listener = SoraFramework.getInstance().getSora().serveWebsocket(new NanoHttpSessionWrapper(nanoSession));

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
