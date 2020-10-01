# Quick start

{% hint style="info" %}
This documentation is still under development, so you can find some missing sections.
{% endhint %}

### Maven dependency

```markup
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.1.8</version>
    <scope>test</scope>
</dependency>
```

### Initialization

```java
@ExtendWith(RecordoExtension.class)
class BookServiceTest {
    ...
}
```

### Enable ObjectMapper to be used by Recordo \(Optional\)

```java
@EnableRecordo
private ObjectMapper objectMapper;
```

