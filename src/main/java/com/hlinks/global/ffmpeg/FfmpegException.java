package com.hlinks.global.ffmpeg;

import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.BaseResponseCode;

public class FfmpegException extends BaseException {

    public FfmpegException(BaseResponseCode responseCode) {
        super(responseCode);
    }

    public FfmpegException(BaseResponseCode responseCode, Throwable cause) {
        super(responseCode);
        initCause(cause);
    }
}
