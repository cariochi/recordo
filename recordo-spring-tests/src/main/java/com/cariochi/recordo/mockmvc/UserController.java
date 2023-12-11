package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.mockmvc.dto.ErrorDto;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        userDto.setId(1);
        return userDto;
    }

    @SneakyThrows
    @PostMapping("/{id}/upload")
    public String upload(@PathVariable int id,
                         @RequestParam("file1") MultipartFile file1,
                         @RequestParam("file2") MultipartFile file2,
                         @RequestParam(value = "prefix", required = false) String prefix) {
        try (final InputStream inputStream = (id == 1 ? file1 : file2).getInputStream()) {
            final String fileContent = IOUtils.toString(inputStream);
            return Stream.of(prefix, fileContent)
                    .filter(Objects::nonNull)
                    .collect(joining(": "));
        }
    }

    @SneakyThrows
    @PutMapping("/{id}/upload")
    public String uploadPut(@PathVariable int id,
                            @RequestParam("file1") MultipartFile file1,
                            @RequestParam("file2") MultipartFile file2,
                            @RequestParam(value = "prefix", required = false) String prefix) {
        try (final InputStream inputStream = (id == 1 ? file1 : file2).getInputStream()) {
            final String fileContent = IOUtils.toString(inputStream);
            return Stream.of(prefix, fileContent)
                    .filter(Objects::nonNull)
                    .collect(joining(": "));
        }
    }

    @PutMapping
    public UserDto update(@RequestBody UserDto userDto) {
        userDto.setName("Updated");
        return userDto;
    }

    @PatchMapping("/{id}")
    public UserDto patch(@PathVariable int id, @RequestBody UserDto userDto) {
        userDto.setId(id);
        userDto.setName("Updated");
        return userDto;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable int id,
                           @RequestParam(value = "name", required = false) String name,
                           @RequestHeader(value = "Locale", required = false) String locale,
                           @RequestHeader(value = "Authorization", required = false) String token
    ) {
        if (!"Bearer TOKEN".equals(token)) {
            throw new HttpServerErrorException(UNAUTHORIZED);
        }
        return UserDto.builder()
                .id(id)
                .name(name + " " + locale)
                .build();
    }

    @GetMapping
    public Page<UserDto> findAll(@RequestParam(required = false, defaultValue = "2") int count, Pageable pageable) {
        final List<UserDto> users = IntStream.range(1, count + 1)
                .mapToObj(i -> UserDto.builder().id(i).name("user_" + i).build())
                .collect(toList());
        return count == 0 ? Page.empty() : new PageImpl<>(users, pageable, users.size());
    }

    @GetMapping("/slice")
    public Slice<UserDto> getSlice(@RequestParam(required = false, defaultValue = "2") int count, Pageable pageable) {
        final List<UserDto> users = IntStream.range(1, count + 1)
                .mapToObj(i -> UserDto.builder().id(i).name("user_" + i).build())
                .collect(toList());
        return count == 0 ? Page.empty() : new SliceImpl<>(users, pageable, true);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("User {} deleted", id);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ErrorDto handleUnauthorized(HttpServerErrorException e) {
        return new ErrorDto(e.getMessage());
    }

}
