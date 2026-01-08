package facility

import base.entity.withId
import com.pluxity.facility.Facility
import com.pluxity.facility.category.FacilityCategory
import com.pluxity.facility.floor.Floor
import io.mockk.mockk

fun dummyFacility(id: Long = 1L) = mockk<Facility>().withId(id)

fun dummyFloor(
    id: Long = 1L,
    facility: Facility? = dummyFacility(),
    floorId: String = "1F",
    name: String = "1층",
) = Floor(facility = facility, floorId = floorId, name = name).withId(id)

fun dummyFacilityCategory(
    id: Long = 1L,
    name: String,
) = FacilityCategory(categoryName = name).withId(id)
