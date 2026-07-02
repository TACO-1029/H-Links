package com.hlinks.global.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorResponseCode implements BaseResponseCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_400_1", "잘못된 요청입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "GLOBAL_400_2", "HTTP 요청 바디의 형식이 잘못되었습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_3", "HTTP 요청 파라미터의 형식이 잘못되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL_403", "해당 요청에 접근 권한이 없습니다."),
    NOT_FOUND_ENDPOINT(HttpStatus.NOT_FOUND, "GLOBAL_404", "존재하지 않는 엔드포인트입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_405", "지원하지 않는 HTTP 메서드입니다."),
    FFMPEG_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_1", "ffmpeg를 사용할 수 없습니다."),
    FFMPEG_INVALID_INPUT(HttpStatus.BAD_REQUEST, "FFMPEG_400_1", "변환할 영상 파일이 올바르지 않습니다."),
    FFMPEG_TEMP_DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_2", "ffmpeg 임시 디렉터리 생성에 실패했습니다."),
    FFMPEG_CONVERT_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_3", "ffmpeg 변환 시간이 초과되었습니다."),
    FFMPEG_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_4", "ffmpeg 변환에 실패했습니다."),
    FFMPEG_OUTPUT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_5", "ffmpeg 변환 결과 파일이 생성되지 않았습니다."),
    FFMPEG_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_6", "ffmpeg 실행 중 IO 오류가 발생했습니다."),
    FFMPEG_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_7", "ffmpeg 실행이 중단되었습니다."),
    FFMPEG_TEMP_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_500_8", "ffmpeg 임시 파일 삭제에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500", "서버 내부 오류가 발생했습니다."),

    // Course Domain
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL_404_1", "존재하지 않는 강의입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
