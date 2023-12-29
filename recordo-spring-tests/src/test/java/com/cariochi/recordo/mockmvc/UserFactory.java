package com.cariochi.recordo.mockmvc;

import com.cariochi.objecto.Modifier;
import com.cariochi.recordo.mockmvc.dto.UserDto;
import com.cariochi.recordo.read.Read;
import com.cariochi.recordo.read.RecordoObjectFactory;
import java.util.List;

@RecordoObjectFactory
public interface UserFactory {

    @Read("/mockmvc/new_user.json")
    UserDto user();

    @Read("/mockmvc/users.json")
    List<UserDto> users();

    @Modifier("id")
    UserFactory id(Integer id);

}
