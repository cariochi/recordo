package com.cariochi.recordo.mockserver;

import java.lang.annotation.*;

/**
 * Annotation to record and replay REST requests.
 * <p>
 * This annotation allows you to capture REST interactions during a test and either replay them or record new interactions
 * depending on whether a corresponding file exists. If the specified file is absent, real requests and responses will be
 * recorded. If the file is present, the recorded interactions will be replayed, making your tests faster and more predictable.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre class="code"><code class="java">
 *
 *  &#064;Test
 *  &#064;MockServer("/mocks/get_gists.json")
 *  void should_retrieve_gists() {
 *      ...
 *      List&lt;GistResponse&gt; gists = gitHubClient.getGists();
 *      ...
 *  }
 * </code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MockServers.class)
@Inherited
public @interface MockServer {

    /**
     * Path to JSON file or folder with recorded requests and responses.
     */
    String value();

    /**
     * Name of the RestTemplate, OkHttp, or Apache HTTP Client bean or test class field to use.
     */
    String httpClient() default "";

    /**
     * Name of the ObjectMapper bean or test class field to use for serialization/deserialization.
     */
    String objectMapper() default "";

    /**
     * URL pattern used for matching requests. Supports the following wildcards:
     * <ul>
     * <li>? matches one character</li>
     * <li>* matches zero or more characters</li>
     * <li>** matches zero or more directories in a path</li>
     * </ul>
     */
    String urlPattern() default "**";

    /**
     * JSON comparison mode used to compare JSON responses.
     */
    JsonCompareMode jsonCompareMode() default @JsonCompareMode;

    /**
     * Defines the mode for comparing JSON responses, including extensibility and strict order.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @interface JsonCompareMode {

        /**
         * Whether the comparison is extensible, allowing extra fields in the response.
         */
        boolean extensible() default false;

        /**
         * Whether the comparison requires strict order of JSON elements.
         */
        boolean strictOrder() default true;

    }

}
