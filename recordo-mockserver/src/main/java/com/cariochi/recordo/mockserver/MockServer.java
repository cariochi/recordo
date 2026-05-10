package com.cariochi.recordo.mockserver;

import java.lang.annotation.*;

/**
 * Enables HTTP recording and replay for one test method.
 * <p>
 * If the configured recording file or folder is missing, Recordo lets real HTTP calls execute and stores the
 * captured requests and responses. If the recording already exists, matching requests are replayed from the
 * recorded interactions.
 *
 * <p><b>Usage example:</b></p>
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
     * Path to a JSON file or folder with recorded requests and responses, relative to the configured Recordo
     * resource root.
     */
    String value();

    /**
     * Name of the HTTP client Spring bean or {@code @RecordoBean} field to use.
     * <p>
     * Leave empty when there is only one supported client, or one supported client marked as {@code @Primary}.
     */
    String client() default "";

    /**
     * Name of an {@code ObjectMapper} or {@code JsonMapper} Spring bean or {@code @RecordoBean} field.
     */
    String objectMapper() default "";

    /**
     * URL pattern used for routing requests to this recording. Supports the following wildcards:
     * <ul>
     * <li>? matches one character</li>
     * <li>* matches zero or more characters</li>
     * <li>** matches zero or more directories in a path</li>
     * </ul>
     */
    String urlPattern() default "**";

    /**
     * Whether request body comparison allows extra JSON fields in actual requests.
     */
    boolean allowExtraFields() default false;

    /**
     * Whether request body comparison requires strict array order.
     */
    boolean strictOrder() default true;

}
