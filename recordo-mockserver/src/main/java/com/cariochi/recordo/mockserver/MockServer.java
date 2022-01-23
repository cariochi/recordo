package com.cariochi.recordo.mockserver;

import java.lang.annotation.*;

/**
 * Records and replays REST requests.
 *
 * <ul>
 * <li>If the file is absent, real requests and responses will be recorded to the file.</li>
 * <li>If the file is present, requests and responses will be replayed.</li>
 * </ul>
 *
 * <pre class="code"><code class="java">
 *
 *  &#064;Test
 *  &#064;MockServer("/mocks/get_gists.json")
 *  void should_retrieve_gists() {
 *      ...
 *      List&lt;GistResponse&gt; gists = gitHubClient.getGists();
 *      ...
 *  }
 *
 * </code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MockServers.class)
@Inherited
public @interface MockServer {

    String value();

    /**
     * The mapping matches URLs using the following rules:
     * <ul>
     * <li>? matches one character</li>
     * <li>* matches zero or more characters</li>
     * <li>** matches zero or more directories in a path</li>
     * </ul>
     */
    String urlPattern() default "**";

    JsonCompareMode jsonCompareMode() default @JsonCompareMode;

    @Retention(RetentionPolicy.RUNTIME)
    @interface JsonCompareMode {

        boolean extensible() default false;

        boolean strictOrder() default true;

    }

}
