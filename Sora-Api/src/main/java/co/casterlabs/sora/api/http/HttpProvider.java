package co.casterlabs.sora.api.http;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;

/**
 * For tagging classes with http listener methods. Example:
 * 
 * <pre>
 * &#64;HttpEndpoint(uri = "/hello")
 * public HttpResponse onHelloRequest(HttpSession session) {
 *     // Do what you want, keeping in mind that returning null
 *     // or setting the status to NO_RESPONSE will cause the
 *     // connection will be dropped without a response.
 *     return HttpResponse.newFixedLengthResponse(HttpStatus.OK, "Hello world!");
 * }
 * </pre>
 */
public interface HttpProvider {

    default HttpResponse onNoProvider(HttpSession session) {
        return null;
    }

}
