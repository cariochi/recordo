# Record and Playback REST Requests

Record and replay HTTP network interaction for a test.

### Initialization

{% tabs %}
{% tab title="OkHttp" %}
```java
@EnableRecordo
private OkHttpClient client;
```
{% endtab %}

{% tab title="Apache HttpClient" %}
```java
@EnableRecordo
private HttpClient httpClient;
```
{% endtab %}
{% endtabs %}

### Examples

```java
@Test
@MockHttpServer("/mockhttp/should_retrieve_gists.rest.json")
void should_retrieve_gists() {
    ...
    final List<GistResponse> gists = gitHubClient.getGists();
    ...
}
```

## 

