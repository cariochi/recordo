package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.config.ObjectMapperConfig;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.UserClient.TestBodyDto;
import com.cariochi.recordo.mockmvc.dto.ErrorDto;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import com.cariochi.recordo.read.Read;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

@Slf4j
@WebMvcTest({UserController.class, ObjectMapperConfig.class})
@ExtendWith(RecordoExtension.class)
class UserControllerClientTest {

    @Autowired
    private UserClient userClient;

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public RequestInterceptor localeInterceptor() {
            return new LocaleInterceptor("UA");
        }

    }

    @Test
    void should_get_user_by_id_with_mock_mvc() {

        assertAsJson(userClient.getById(1, "Test User"))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(userClient.getById_withParams(1))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(userClient.getById_withHeader(1, "Test User", "UA"))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(userClient.getById_asRequest(1, "Test User").perform().getBody())
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(userClient.getById_asResponse(1, "Test User").getBody())
                .isEqualTo("/mockmvc/user.json");

        assertThat(userClient.getById_asString(1, "Test User"))
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}");

        assertThat(userClient.getById_asBytes(1, "Test User"))
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}".getBytes());
    }

    @Test
    void should_get_unauthorized() {
        final ErrorDto error = userClient.getById_withErrors(1, "Test User", "Bearer Fake TOKEN");
        assertThat(error.getMessage())
                .isEqualTo("401 UNAUTHORIZED");
    }

    @Test
    void should_get_all_users() {

        assertAsJson(userClient.findAll())
                .isEqualTo("/mockmvc/users_page.json");

        assertAsJson(userClient.findAll(2))
                .isEqualTo("/mockmvc/users_page.json");

        final Page<UserDto> page = userClient.findAll(2, PageRequest.of(5, 50, Sort.by(asc("name"), desc("age"))));
        assertThat(page.getNumber()).isEqualTo(5);
        assertThat(page.getSize()).isEqualTo(50);

    }

    @Test
    void should_get_user_slice() {

        assertAsJson(userClient.getSlice(2))
                .isEqualTo("/mockmvc/users_slice.json");

        final Slice<UserDto> slice = userClient.getSlice(2, PageRequest.of(5, 50, Sort.by(asc("name"), desc("age"))));
        assertThat(slice.getNumber()).isEqualTo(5);
        assertThat(slice.getSize()).isEqualTo(50);
    }

    @Test
    void should_get_empty_page() {
        final Page<UserDto> users = userClient.findAll(0);
        assertThat(users).isEmpty();
    }

    @Test
    void should_create_user(
            @Read("/mockmvc/new_user.json") UserDto user
    ) {
        final UserDto userDto = userClient.create(user);
        assertAsJson(userDto).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_upload_files_with_param() {
        final MultipartFile file1 = new MockMultipartFile("file1", "Upload File 1".getBytes());
        final File file2 = File.builder().name("file2").content("Upload File 2".getBytes()).build();
        final String content = userClient.upload(1, file1, file2, "File content");
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_upload_with_body_dto() {
        final TestBodyDto bodyDto = TestBodyDto.builder()
                .file1(new MockMultipartFile("file1", "Upload File 1".getBytes()))
                .file2(File.builder().name("file2").content("Upload File 2".getBytes()).build())
                .prefix("File content")
                .build();
        final String content = userClient.upload_withObjectDto(1, bodyDto);
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_upload_files_with_param_and_put_method() {
        final MultipartFile file1 = new MockMultipartFile("file1", "Upload File 1".getBytes());
        final File file2 = File.builder().name("file2").content("Upload File 2".getBytes()).build();
        final String content = userClient.uploadPut(1, file1, file2, "File content");
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_delete_user_by_id() {
        userClient.delete(1);
    }

    @Test
    void should_update_user(
            @Read("/mockmvc/user.json") UserDto user
    ) {
        final UserDto updated = userClient.update(user);
        assertAsJson(updated).isEqualTo("/mockmvc/updated_user.json");
    }

    @Test
    void should_patch_user(
            @Read("/mockmvc/user.json") UserDto user
    ) {
        final UserDto patched = userClient.patch(1, user);
        assertAsJson(patched).isEqualTo("/mockmvc/updated_user.json");
    }

}
