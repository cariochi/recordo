package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import com.cariochi.recordo.mockmvc.utils.Types;
import com.cariochi.recordo.read.Read;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@WebMvcTest(UserController.class)
@ExtendWith(RecordoExtension.class)
@RequiredArgsConstructor
class UserControllerTest {

    @Test
    void should_get_user_by_id_with_mock_mvc(RecordoMockMvc mockMvc) {
        final Response<UserDto> response = mockMvc.get("/users/{id}", UserDto.class)
                .uriVars(1)
                .param("name", "Test User")
                .header("locale", "UA")
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
    void should_get_all_users_with_mock_mvc(RecordoMockMvc mockMvc) {
        final Request<Page<UserDto>> request = mockMvc.get("/users", Types.pageOf(UserDto.class));
        final Response<Page<UserDto>> response = request.perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/users_page.json");
    }

    @Test
    void should_get_all_users(
            @Get("/users") Request<Page<UserDto>> request
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
    void should_get_empty_page(
            @Get("/users?count=0") Page<UserDto> users
    ) {
        assertThat(users).isEmpty();
    }

    @Test
    void should_create_user(
            RecordoMockMvc mockMvc,
            @Read("/mockmvc/new_user.json") UserDto user
    ) {
        final Response<UserDto> response = mockMvc.post("/users", UserDto.class).body(user).perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user(
            @Post(value = "/users", body = "/mockmvc/new_user.json") Request<UserDto> request
    ) {
        final Response<UserDto> response = request.perform();
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user(
            @Post(value = "/users", body = "/mockmvc/new_user.json") Response<UserDto> response
    ) {
        assertAsJson(response.getBody()).isEqualTo("/mockmvc/created_user.json");
    }

    @Test
    void should_create_user(
            @Post(value = "/users", body = "/mockmvc/new_user.json") UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_delete_user_by_id(RecordoMockMvc mockMvc) {
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
            @Put(value = "/users", body = "/mockmvc/user.json") UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/updated_user.json");
    }

    @Test
    void should_patch_user(
            @Patch(value = "/users/1", body = "/mockmvc/user.json") UserDto user
    ) {
        assertAsJson(user).isEqualTo("/mockmvc/updated_user.json");
    }

    public static class LocaleInterceptor implements RequestInterceptor {

        @Override
        public <T> Request<T> intercept(Request<T> request, RecordoMockMvc http) {
            final Request<UserDto> user = http.get("/users/1", UserDto.class);
            assertThat(user).isNotNull();
            return request.header("locale", "UA");
        }

    }

}
