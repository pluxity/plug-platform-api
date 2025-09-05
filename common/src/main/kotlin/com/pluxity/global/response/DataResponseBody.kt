package com.pluxity.global.response

import com.pluxity.global.constant.SuccessCode

class DataResponseBody<T>(
    val data: T?,
) : ResponseBody(SuccessCode.SUCCESS.getHttpStatus().value(), SuccessCode.SUCCESS.getMessage())
