package com.pluxity.cctv.entity

fun dummyCctv(
    id: String = "cctvId",
    name: String = "cctvName",
    url: String = "url",
): Cctv = Cctv(id, name, url)
