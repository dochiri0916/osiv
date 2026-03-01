package com.dochiri.osiv.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6SpySqlFormat implements MessageFormattingStrategy {

    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url
    ) {
        if (sql == null || sql.isBlank()) {
            return "";
        }

        if ("statement".equals(category) || "batch".equals(category)) {
            P6SpyQueryCounter.increment();
        }

        String normalizedSql = sql.replaceAll("\\s+", " ").trim();
        return "[P6SPY] " + elapsed + "ms | " + category + " | " + normalizedSql;
    }
}
