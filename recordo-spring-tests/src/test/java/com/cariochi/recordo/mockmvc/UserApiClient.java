package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.dto.ErrorDto;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RecordoApiClient(interceptors = {LocaleInterceptor.class, AuthInterceptor.class})
@RequestMapping("/users")
public interface UserApiClient {

    @GetMapping("/{id}")
    UserDto getById(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    UserDto getById_withHeader(@PathVariable int id, @RequestParam("name") String name, @RequestHeader("Locale") String locale);

    @GetMapping("/{id}")
    Request<UserDto> getById_asRequest(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    Response<UserDto> getById_asResponse(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    String getById_asString(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    byte[] getById_asBytes(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping(value = "/{id}", params = "name=Test User", produces = APPLICATION_JSON_VALUE)
    UserDto getById_withParams(@PathVariable int id);

    @GetMapping("/{id}")
    @ResponseStatus(UNAUTHORIZED)
    ErrorDto getById_withErrors(@PathVariable int id, @RequestParam("name") String name, @RequestHeader("Authorization") String auth);

    @GetMapping
    Page<UserDto> findAll();

    @GetMapping
    Page<UserDto> findAll(@RequestParam("count") int count);

    @GetMapping
    Page<UserDto> findAll(@RequestParam("count") int count, Pageable pageable);

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    UserDto create(@RequestBody UserDto userDto);

    @PostMapping("/{id}/upload")
    String upload(@PathVariable int id, @RequestParam MultipartFile file1, @RequestParam File file2, @RequestParam("prefix") String prefix);

    @PostMapping("/{id}/upload")
    String upload_withObjectDto(@PathVariable int id, TestBodyDto bodyDto);

    @PutMapping("/{id}/upload")
    String uploadPut(@PathVariable int id, @RequestParam MultipartFile file1, @RequestParam File file2, @RequestParam("prefix") String prefix);

    @PutMapping
    UserDto update(@RequestBody UserDto userDto);

    @PatchMapping("/{id}")
    UserDto patch(@PathVariable int id, @RequestBody UserDto userDto);


    @GetMapping("/slice")
    Slice<UserDto> getSlice(@RequestParam("count") int count);

    @GetMapping("/slice")
    Slice<UserDto> getSlice(@RequestParam("count") int count, Pageable pageable);

    @DeleteMapping("/{id}")
    void delete(@PathVariable int id);


    @Data
    @SuperBuilder
    class TestBodyParentDto {

        private MultipartFile file1;
        private String prefix;
        private String nullable;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    class TestBodyDto extends TestBodyParentDto {

        private File file2;

    }

}
