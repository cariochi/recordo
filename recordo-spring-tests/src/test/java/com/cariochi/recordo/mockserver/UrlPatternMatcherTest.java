package com.cariochi.recordo.mockserver;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static com.cariochi.recordo.mockserver.UrlPatternMatcherTest.MatcherAssert.assertMatcher;
import static org.assertj.core.api.Assertions.assertThat;

class UrlPatternMatcherTest {

    @Test
    void run() {
        assertMatcher("https://s?rver.com")
                .match("https://server.com")
                .match("HTTPS://SERVER.COM")
                .match("https://surver.com")
                .notMatch("https://server.com/");

        assertMatcher("https://server.com/*")
                .notMatch("https://server.com")
                .match("https://server.com/")
                .match("https://server.com/first")
                .match("HTTPS://SERVER.COM/FIRST")
                .notMatch("https://server.com/first/second");

        assertMatcher("https://server.com/**")
                .notMatch("https://server.com")
                .match("https://server.com/")
                .match("https://server.com/first")
                .match("https://server.com/first/")
                .match("https://server.com/first/second")
                .match("HTTPS://SERVER.COM/FIRST/SECOND");

        assertMatcher("https://server.com/*/an?/**")
                .match("https://server.com/first/and/")
                .match("https://server.com/first/ant/")
                .match("https://server.com/first/and/second")
                .match("https://server.com/first/and/second/third")
                .match("HTTPS://SERVER.COM/FIRST/AND/");

    }

    @RequiredArgsConstructor
    public static class MatcherAssert {

        private final UrlPatternMatcher matcher;

        public static MatcherAssert assertMatcher(String pattern) {
            return new MatcherAssert(new UrlPatternMatcher(pattern));
        }

        public MatcherAssert match(String url) {
            assertThat(matcher.match(url)).isTrue();
            return this;
        }

        public MatcherAssert notMatch(String url) {
            assertThat(matcher.match(url)).isFalse();
            return this;
        }

    }

}
