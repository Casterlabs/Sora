package co.casterlabs.sora;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.sora.SoraBasicRequestPreProcessor.Config;
import co.casterlabs.sora.SoraBasicRequestPreProcessor.Config.ValidationErrorType;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.SoraHttpSession;
import lombok.NonNull;

public class SoraBasicRequestPreProcessor implements HttpPreProcessor<Config> {
    public static final String ID = "sora.http.basic";
    public static final SoraBasicRequestPreProcessor INSTANCE = new SoraBasicRequestPreProcessor();

    private SoraBasicRequestPreProcessor() {}

    @Override
    public @Nullable HttpResponse preprocessHttpSession(@NonNull Config data, @NonNull SoraHttpSession session) {

        // Check required query parameters
        for (String requiredQueryParameter : data.getRequiredQueryParameters()) {
            if (!session.getAllQueryParameters().containsKey(requiredQueryParameter)) {
                return data.respond(ValidationErrorType.MISSING_QUERY_PARAMETER, requiredQueryParameter, null, null);
            }
        }

        // Check required headers
        for (String requiredHeader : data.getRequiredHeaders()) {
            if (!session.getHeaders().containsKey(requiredHeader)) {
                return data.respond(ValidationErrorType.MISSING_HEADER, requiredHeader, null, null);
            }
        }

        // Check the query parameter regex
        for (Map.Entry<String, String> entry : data.getQueryParameterRegex().entrySet()) {
            String queryParameter = entry.getKey();
            String regex = entry.getValue();

            List<String> values = session.getAllQueryParameters().get(queryParameter);

            if (values != null) {
                for (String value : values) {
                    if (!value.matches(regex)) {
                        return data.respond(ValidationErrorType.INVALID_QUERY_VALUE, queryParameter, value, regex);
                    }
                }
            }
        }

        // Check the header regex
        for (Map.Entry<String, String> entry : data.getHeaderRegex().entrySet()) {
            String header = entry.getKey();
            String regex = entry.getValue();

            List<String> values = session.getHeaders().get(header);

            if (values != null) {
                for (String value : values) {
                    if (!value.matches(regex)) {
                        return data.respond(ValidationErrorType.INVALID_HEADER_VALUE, header, value, regex);
                    }
                }
            }
        }

        return null;
    }

    public static interface Config {

        /**
         * Gets a list of required query parameters (their presence is required).
         *
         * @return the required query parameters, or {@link Collections.emptyList()} if
         *         none.
         */
        public @NonNull List<String> getRequiredQueryParameters();

        /**
         * Gets a list of required headers (their presence is required).
         *
         * @return the required headers, or {@link Collections.emptyList()} if none.
         */
        public @NonNull List<String> getRequiredHeaders();

        /**
         * Gets the query parameter regex (key = header, value = regex).
         *
         * @return the query parameter regex, or {@link Collections.emptyMap()} if none.
         */
        public @NonNull Map<String, String> getQueryParameterRegex();

        /**
         * Gets the header regex (key = header, value = regex).
         *
         * @return the header regex, or {@link Collections.emptyMap()} if none.
         */
        public @NonNull Map<String, String> getHeaderRegex();

        /**
         * @param  error   the error
         * @param  reason  the reason (e.g the missing query parameter)
         * @param  culprit the culprit (e.g the invalid query parameter value)
         * @param  rule    the rule (e.g the regex)
         * 
         * @return         the response, or null.
         */
        default @Nullable HttpResponse respond(@NonNull ValidationErrorType error, @Nullable String reason, @Nullable String culprit, @Nullable String rule) {
            String response = null;

            switch (error) {
                case MISSING_QUERY_PARAMETER:
                    response = String.format("Missing required query parameter: %s", reason);
                    break;

                case MISSING_HEADER:
                    response = String.format("Missing required header: %s", reason);
                    break;

                case INVALID_QUERY_VALUE:
                    response = String.format("Invalid query value %s=%s, (must match /%s/)", reason, culprit, rule);
                    break;

                case INVALID_HEADER_VALUE:
                    response = String.format("Invalid header value [%s: %s], (must match /%s/)", reason, culprit, rule);
                    break;
            }

            if (response == null) {
                return null;
            } else {
                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST, response);
            }
        }

        public static enum ValidationErrorType {
            MISSING_QUERY_PARAMETER,
            MISSING_HEADER,
            INVALID_QUERY_VALUE,
            INVALID_HEADER_VALUE;
        }

    }

}
