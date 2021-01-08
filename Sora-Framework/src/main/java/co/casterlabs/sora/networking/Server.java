package co.casterlabs.sora.networking;

import java.io.IOException;

public interface Server {

    public void start() throws IOException;

    public void stop() throws IOException;

}
