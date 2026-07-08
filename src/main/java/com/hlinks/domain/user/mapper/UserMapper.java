package com.hlinks.domain.user.mapper;

import com.hlinks.domain.user.dto.LoginUserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
// Java 코드에서 MyBatis SQL을 호출하는 입구
public interface UserMapper {
    Optional<LoginUserDto> findByLoginId(@Param("loginId") String loginId);

    Optional<LoginUserDto> findByUserId(@Param("userId") Long userId);

    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    int updateMyProfile(
            @Param("userId") Long userId,
            @Param("phone") String phone,
            @Param("encodedPassword") String encodedPassword
    );
}
