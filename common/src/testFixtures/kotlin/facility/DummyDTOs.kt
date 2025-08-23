package facility

import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityUpdateRequest

fun dummyCreateFacilityRequest(): FacilityCreateRequest =
    FacilityCreateRequest(
        "시설명",
        "code",
        "시설 설명",
        null,
        null,
        null,
        null,
        null,
    )

fun dummyUpdateFacilityRequest(): FacilityUpdateRequest =
    FacilityUpdateRequest(
        "수정시설명",
        "code",
        "시설 설명",
        null,
        null,
        null,
        null,
    )
