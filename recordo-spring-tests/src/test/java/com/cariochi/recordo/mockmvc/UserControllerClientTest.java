package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.config.ObjectMapperConfig;
import com.cariochi.recordo.core.Recordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.UserApiClient.TestBodyDto;
import com.cariochi.recordo.mockmvc.dto.ErrorDto;
import com.cariochi.recordo.mockmvc.dto.UserDto;
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
    private UserApiClient apiClient;
    private final UserApiClient apiClient2 = Recordo.create(UserApiClient.class);

    @Autowired
    private UserFactory userFactory;
    private final UserFactory userFactory2 = Recordo.create(UserFactory.class);

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public RequestInterceptor localeInterceptor() {
            return new LocaleInterceptor("UA");
        }

    }

    @Test
    void should_create_two_api_clients() {
        assertThat(apiClient).isNotNull();
        assertThat(apiClient2).isNotNull();

        assertThat(apiClient.findAll()).isEqualTo(apiClient2.findAll());
    }

    @Test
    void should_create_two_user_factories() {
        assertThat(userFactory).isNotNull();
        assertThat(userFactory2).isNotNull();

        assertThat(userFactory.users()).isEqualTo(userFactory2.users());
    }

    @Test
    void test_object_methods() {
        assertThat(apiClient.hashCode()).isNotZero();
        assertThat(apiClient.toString()).startsWith(UserApiClient.class.getName());
        assertThat(apiClient.equals(apiClient)).isTrue();
    }

    @Test
    void should_get_user_by_id_with_mock_mvc() {

        assertAsJson(apiClient.getById(1, "Test User"))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(apiClient.getById_withParams(1))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(apiClient.getById_withHeader(1, "Test User", "UA"))
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(apiClient.getById_asRequest(1, "Test User").perform().getBody())
                .isEqualTo("/mockmvc/user.json");

        assertAsJson(apiClient.getById_asResponse(1, "Test User").getBody())
                .isEqualTo("/mockmvc/user.json");

        assertThat(apiClient.getById_asString(1, "Test User"))
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}");

        assertThat(apiClient.getById_asBytes(1, "Test User"))
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}".getBytes());
    }

    @Test
    void should_load_users() {
        assertThat(userFactory.users()).hasSize(2);
    }

    @Test
    void should_get_unauthorized() {
        final ErrorDto error = apiClient.getById_withErrors(1, "Test User", "Bearer Fake TOKEN");
        assertThat(error.getMessage())
                .isEqualTo("401 UNAUTHORIZED");
    }

    @Test
    void should_get_all_users() {

        assertAsJson(apiClient.findAll())
                .isEqualTo("/mockmvc/users_page.json");

        assertAsJson(apiClient.findAll(2))
                .isEqualTo("/mockmvc/users_page.json");

        final Page<UserDto> page = apiClient.findAll(2, PageRequest.of(5, 50, Sort.by(asc("name"), desc("age"))));
        assertThat(page.getNumber()).isEqualTo(5);
        assertThat(page.getSize()).isEqualTo(50);

    }

    @Test
    void should_get_user_slice() {

        assertAsJson(apiClient.getSlice(2))
                .isEqualTo("/mockmvc/users_slice.json");

        final Slice<UserDto> slice = apiClient.getSlice(2, PageRequest.of(5, 50, Sort.by(asc("name"), desc("age"))));
        assertThat(slice.getNumber()).isEqualTo(5);
        assertThat(slice.getSize()).isEqualTo(50);
    }

    @Test
    void should_get_empty_page() {
        final Page<UserDto> users = apiClient.findAll(0);
        assertThat(users).isEmpty();
    }

    @Test
    void should_create_user() {
        final UserDto user = userFactory.user();
        final UserDto userDto = apiClient.create(user);
        assertAsJson(userDto).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_upload_files_with_param() {
        final MultipartFile file1 = new MockMultipartFile("file1", "Upload File 1".getBytes());
        final File file2 = File.builder().name("file2").content("Upload File 2".getBytes()).build();
        final String content = apiClient.upload(1, file1, file2, "File content");
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_upload_with_body_dto() {
        final TestBodyDto bodyDto = TestBodyDto.builder()
                .file1(new MockMultipartFile("file1", "Upload File 1".getBytes()))
                .file2(File.builder().name("file2").content("Upload File 2".getBytes()).build())
                .prefix("File content")
                .build();
        final String content = apiClient.upload_withObjectDto(1, bodyDto);
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_upload_files_with_param_and_put_method() {
        final MultipartFile file1 = new MockMultipartFile("file1", "Upload File 1".getBytes());
        final File file2 = File.builder().name("file2").content("Upload File 2".getBytes()).build();
        final String content = apiClient.uploadPut(1, file1, file2, "File content");
        assertThat(content).isEqualTo("File content: Upload File 1");
    }

    @Test
    void should_delete_user_by_id() {
        apiClient.delete(1);
    }

    @Test
    void should_update_user() {
        final UserDto user = userFactory.withId(1).user();
        final UserDto updated = apiClient.update(user);
        assertAsJson(updated).isEqualTo("/mockmvc/updated_user.json");
    }

    @Test
    void should_patch_user() {
        final UserDto user = userFactory.withId(1).user();
        final UserDto patched = apiClient.patch(1, user);
        assertAsJson(patched).isEqualTo("/mockmvc/updated_user.json");
    }

}
