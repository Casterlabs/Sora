package co.casterlabs.sora.api.http;

import co.casterlabs.rakurai.io.http.server.HttpResponse;

/**
 * For tagging classes with http listener methods. Example:
 * 
 * <pre>
 * &#64;HttpEndpoint(uri = "/hello")
 * public HttpResponse onHelloRequest(SoraHttpSession session) {
 *     // Do what you want, keeping in mind that returning null
 *     // or setting the status to NO_RESPONSE will cause the
 *     // connection will be dropped without a response.
 *     return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, "Hello world!");
 * }
 * </pre>
 */
public interface HttpProvider {

    default HttpResponse onNoProvider(SoraHttpSession session) {
        return null;
    }

}
