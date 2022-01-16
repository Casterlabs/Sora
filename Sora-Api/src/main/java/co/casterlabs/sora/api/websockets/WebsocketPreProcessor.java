package co.casterlabs.sora.api.websockets;

import lombok.NonNull;

public interface WebsocketPreProcessor<T> {

    /**
     * @return true if the connection should drop.
     */
    public boolean preprocessWebsocketSession(@NonNull T data, @NonNull SoraWebsocketSession session);

}
