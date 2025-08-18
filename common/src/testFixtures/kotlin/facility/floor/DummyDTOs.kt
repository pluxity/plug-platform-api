package facility.floor

import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.facility.floor.dto.FloorResponse

fun dummyCreateFloorRequest(): FloorRequest = FloorRequest("name", "floorId")

fun dummyFloorResponse(
    name: String = "Floor name",
    floorId: String = "floorId",
): FloorResponse = FloorResponse(name, floorId)
