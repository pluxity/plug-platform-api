package file

import base.dummyBaseResponse
import com.pluxity.file.dto.FileResponse

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
