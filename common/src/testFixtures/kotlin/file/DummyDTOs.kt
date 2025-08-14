package file

import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse

fun dummyFileResponse(
    id: Long = 1L,
    url: String = "url",
    originFileName: String = "originFileName",
    contentType: String = "image",
    fileStatus: String = "COMPLETE",
) = FileResponse(
    id,
    url,
    originFileName,
    contentType,
    fileStatus,
    dummyBaseResponse(),
)

fun dummyBaseResponse() = BaseResponse("", "", "", "")
