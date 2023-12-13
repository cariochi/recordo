package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.mockmvc.dto.UserDto;
import com.cariochi.recordo.read.Param;
import com.cariochi.recordo.read.Read;
import com.cariochi.recordo.read.RecordoObjectFactory;
import java.util.List;

@RecordoObjectFactory
public interface UserObjectFactory {

    @Read("/mockmvc/new_user.json")
    UserDto user();

    @Read("/mockmvc/users.json")
    List<UserDto> users();

    UserObjectFactory id(@Param("id") Integer id);
}
