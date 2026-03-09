package com.plantidentifier.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Фильтр корреляции запросов.
 *
 * Каждый запрос получает уникальный X-Request-ID.
 * Этот ID проставляется во все логи через MDC.
 *
 * MDC (Mapped Diagnostic Context) — это ThreadLocal Map
 * в которую мы кладём данные и они автоматически
 * добавляются во все лог сообщения этого потока.
 *
 * @Order(1) — этот фильтр выполняется ПЕРВЫМ
 * чтобы requestId был доступен во всех последующих фильтрах
 */
@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    // Ключ в MDC — совпадает с %X{requestId} в application.properties
    private static final String MDC_REQUEST_ID    = "requestId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);

        // Если клиент не передал ID — генерируем сами
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        try {
            // Кладём в MDC — теперь во ВСЕХ логах этого запроса
            // будет: [550e8400-e29b-41d4-a716-446655440000]
            MDC.put(MDC_REQUEST_ID, requestId);

            // Возвращаем ID клиенту в ответе —
            // клиент может использовать для отладки
            response.setHeader(REQUEST_ID_HEADER, requestId);

            // Передаём дальше по цепочке фильтров
            filterChain.doFilter(request, response);

        } finally {
            // ВАЖНО: очищаем MDC после запроса!
            // Потоки в thread pool переиспользуются —
            // без очистки requestId "протечёт" в следующий запрос
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}