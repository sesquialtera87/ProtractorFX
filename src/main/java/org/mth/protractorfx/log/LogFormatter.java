package org.mth.protractorfx.log;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class LogFormatter extends Formatter {

    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss a";
    static DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public String format(LogRecord record) {
        return MessageFormat.format("[{0}] [{1}] -> {2}\n",
                record.getLevel().getName(),
                record.getSourceClassName(),
                record.getMessage());
    }
}
