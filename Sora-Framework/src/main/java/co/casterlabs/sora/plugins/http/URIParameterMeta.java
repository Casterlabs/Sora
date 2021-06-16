package co.casterlabs.sora.plugins.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;

public class URIParameterMeta {
    private static final String PATH_PART_REGEX = "[^/]*";

    private List<Integer> parameterPositions = new ArrayList<>();
    private String[] uriParts;

    private @Getter String uriRegex;

    public URIParameterMeta(@NonNull String uri) {
        this.uriParts = uri.split("/");

        String[] partsCopy = this.uriParts.clone();

        for (int i = 0; i < partsCopy.length; i++) {
            String part = partsCopy[i];

            // URI parameters start with ':'
            if (part.startsWith(":")) {
                this.uriParts[i] = part.substring(1); // Drop the leading ':'

                this.parameterPositions.add(i);
                partsCopy[i] = PATH_PART_REGEX; // Add a regex that'll catch.
            }
        }

        this.uriRegex = String.join("/", partsCopy);
    }

    public Map<String, String> decode(@NonNull String uri) {
        if (this.parameterPositions.size() == 0) {
            return Collections.emptyMap();
        } else {
            Map<String, String> parameters = new HashMap<>();

            String[] split = uri.split("/");

            for (Integer pos : this.parameterPositions) {
                String name = this.uriParts[pos];
                String value = split[pos];

                parameters.put(name, value);
            }

            return parameters;
        }
    }

}
