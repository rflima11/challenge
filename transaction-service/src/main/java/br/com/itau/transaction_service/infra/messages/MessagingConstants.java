package br.com.itau.transaction_service.infra.messages;

public final class MessagingConstants {

    private MessagingConstants() {}

    public static final String HEADER_EVENT_TYPE = "eventType";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    public static final String MDC_KEY_CORRELATION_ID = "traceId";
}
