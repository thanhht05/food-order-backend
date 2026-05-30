package com.thanh.foodOrder.util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.thanh.foodOrder.domain.respone.RestResponse;
import com.thanh.foodOrder.util.anotation.ApiMessage;

@RestControllerAdvice
public class FormatResResponse implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
            Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        int statusCode = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        RestResponse<Object> formatResResponse = new RestResponse<>();
        formatResResponse.setStatusCode(statusCode);
        String path = request.getURI().getPath();

        if (path.contains("/v3/api-docs") || path.contains("/swagger")) {
            return body;
        }
        if (statusCode >= 400) {
            return body;
        } else {
            ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);

            String message = apiMessage != null ? apiMessage.value() : "Call api success";
            formatResResponse.setMessage(message);
            formatResResponse.setData(body);
        }
        return formatResResponse;
    }

}
