package com.pluxity.cctv

import com.pluxity.cctv.dto.MediaMtxErrorResponse
import com.pluxity.cctv.dto.MediaMtxPathListResponse
import com.pluxity.cctv.dto.MediaMtxPathResponse
import com.pluxity.cctv.dto.MediaMtxRequest
import com.pluxity.collect.config.WebClientFactory
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.properties.MediaMtxProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class MediaMtxService(
    webClientFactory: WebClientFactory,
    mediaMtxProperties: MediaMtxProperties,
) {
    private val client: WebClient =
        webClientFactory
            .createClient(mediaMtxProperties.apiUrl)
            .mutate()
            .build()

    fun getAllPath(): List<MediaMtxPathResponse> {
        val response =
            client
                .get()
                .uri("/v3/config/paths/list")
                .retrieve()
                .bodyToMono<MediaMtxPathListResponse>()
                .block()
        return response?.let { response.items } ?: emptyList()
    }

    fun addPath(
        path: String,
        source: String,
    ) {
        val request = MediaMtxRequest(source)
        client
            .post()
            .uri("/v3/config/paths/add/$path")
            .bodyValue(request)
            .retrieve()
            .onStatus({ !it.is2xxSuccessful }) { resp ->
                resp
                    .bodyToMono<MediaMtxErrorResponse>()
                    .defaultIfEmpty(MediaMtxErrorResponse("empty body"))
                    .flatMap { body ->
                        Mono.error(CustomException(errorCode = ErrorCode.MEDIAMTX_ADD_ERROR, body.error))
                    }
            }.toBodilessEntity()
            .block()
    }

    fun deletePath(path: String) {
        client
            .delete()
            .uri("/v3/config/paths/delete/$path")
            .retrieve()
            .onStatus({ !it.is2xxSuccessful }) { resp ->
                if (resp.statusCode().value() == 404) {
                    Mono.empty()
                } else {
                    resp
                        .bodyToMono<MediaMtxErrorResponse>()
                        .defaultIfEmpty(MediaMtxErrorResponse("empty body"))
                        .flatMap { body ->
                            Mono.error(CustomException(errorCode = ErrorCode.MEDIAMTX_DELETE_ERROR, body.error))
                        }
                }
            }.toBodilessEntity()
            .block()
    }
}
