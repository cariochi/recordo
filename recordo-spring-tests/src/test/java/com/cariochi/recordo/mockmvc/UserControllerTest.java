package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.config.ObjectMapperConfig;
import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockmvc.Post.File;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import com.cariochi.recordo.read.Read;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static com.cariochi.recordo.mockmvc.utils.TypeReferences.pageOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@WebMvcTest({UserController.class, ObjectMapperConfig.class})
@ExtendWith(RecordoExtension.class)
class UserControllerTest {

    @EnableRecordo
    private RequestInterceptor<?> defaultInterceptor = request -> request.header("Authorization", "Bearer token");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecordoMockMvc mockMvc;

    @Test
    void should_get_user_by_id_with_mock_mvc() {
        final Response<UserDto> response = mockMvc.get("/users/{id}", UserDto.class)
                .uriVars(1)
                .param("name", "Test User")
                .header("locale", "UA")
                .header("Authorization", "Bearer token")
                .expectedStatus(OK)
                .perform();

        assertAsJson(response.getBody()).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_get_user_by_id_with_perform(
            @Perform(method = GET, path = "/users/1?name=Test User", interceptors = LocaleInterceptor.class) Response<UserDto> response
    ) {
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_get_user_by_id_with_get(
            @Get(value = "/users/1?name=Test User", headers = "locale=UA") Response<UserDto> response
    ) {
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_get_user_by_id_with_get(
            @Get(value = "/users/1?name=Test User", headers = "locale=UA") UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_get_as_string(
            @Get(value = "/users/1?name=Test User", headers = "locale=UA") String userString
    ) {
        assertThat(userString)
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}");
    }

    @Test
    void should_get_as_bytes(
            @Get(value = "/users/1?name=Test User", headers = "locale=UA") byte[] userBytes
    ) {
        assertThat(userBytes)
                .isEqualTo("{\"id\":1,\"name\":\"Test User UA\"}".getBytes(UTF_8));
    }

    @Test
    void should_get_all_users_with_mock_mvc() {
        final Request<Page<UserDto>> request = mockMvc.get("/users", pageOf(UserDto.class));
        final Response<Page<UserDto>> response = request.perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/users_page.json");
    }

    @Test
    void should_get_all_users(
            @Get(value = "/users", objectMapper = "objectMapper") Request<Page<UserDto>> request
    ) {
        final Response<Page<UserDto>> response = request.perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/users_page.json");
    }

    @Test
    void should_get_all_users(
            @Get("/users") Response<Page<UserDto>> response
    ) {
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/users_page.json");
    }

    @Test
    void should_get_all_users(
            @Get("/users") Page<UserDto> users
    ) {
        assertAsJson(users).isEqualTo("/mockmvc/users_page.json");
    }

    @Test
    void should_get_all_users(
            @Get("/users/slice") Slice<UserDto> users
    ) {
        assertAsJson(users).isEqualTo("/mockmvc/users_slice.json");
    }

    @Test
    void should_get_empty_page(
            @Get("/users?count=0") Page<UserDto> users
    ) {
        assertThat(users).isEmpty();
    }

    @Test
    void should_create_user_1(
            @Read("/mockmvc/new_user.json") UserDto user
    ) {
        final Response<UserDto> response = mockMvc.post("/users", UserDto.class).body(user).perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user_2(
            @Post(value = "/users", body = @Content(file = "/mockmvc/new_user.json")) Request<UserDto> request
    ) {
        final Response<UserDto> response = request.perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user_3(
            @Post(value = "/users", body = @Content(file = "/mockmvc/new_user.json"), objectMapper = "objectMapper") Response<UserDto> response
    ) {
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user_4(
            @Post(value = "/users", body = @Content(file = "/mockmvc/new_user.json")) UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_upload_files(
            @Post(
                    value = "/users/1/upload",
                    files = {
                            @File(name = "file1", content = @Content(file = "/mockmvc/upload_file_1.txt")),
                            @File(name = "file2", content = @Content(file = "/mockmvc/upload_file_2.txt"))
                    }
            ) String content
    ) {
        assertThat(content).isEqualTo("Upload File 1\n");
    }

    @Test
    void should_upload_files_with_param(
            @Post("/users/1/upload") Request<String> request
    ) {
        final String content = request
                .file(Request.File.builder().name("file1").content("Upload File 1".getBytes(UTF_8)).build())
                .file(Request.File.builder().name("file2").content("Upload File 2".getBytes(UTF_8)).build())
                .param("prefix", "File content")
                .perform()
                .getBody();
        assertThat(content).isEqualTo("File content: Upload File 1");
    }


    @Test
    void should_delete_user_by_id() {
        mockMvc.delete("/users/{id}").uriVars(1).perform();
    }

    @Test
    void should_delete_user_by_id(
            @Delete("/users/{id}") Request<Void> request
    ) {
        request.uriVars(1).perform();
    }

    @Test
    void should_delete_user_by_id(
            @Delete("/users/1") Response<Void> response
    ) {
    }

    @Test
    void should_update_user(
            @Put(value = "/users", body = @Content(file = "/mockmvc/user.json")) UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/updated_user.json");
    }

    @Test
    void should_patch_user(
            @Patch(value = "/users/1", body = @Content(file = "/mockmvc/user.json")) UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/updated_user.json");
    }

    public static class LocaleInterceptor implements RequestInterceptor<UserDto> {

        @Override
        public Request<UserDto> apply(Request<UserDto> request) {
            final Request<UserDto> user = request.client().get("/users/1", UserDto.class);
            assertThat(user).isNotNull();
            return request.header("locale", "UA");
        }

    }

}
