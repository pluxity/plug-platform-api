package cctv

import com.pluxity.cctv.entity.Cctv

fun dummyCctv(
    id: String = "cctvId",
    name: String = "cctvName",
    url: String = "url",
): Cctv = Cctv(id, name, url)
