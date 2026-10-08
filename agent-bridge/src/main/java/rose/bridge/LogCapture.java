package rose.bridge;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;

/** Copies the game's log lines (INFO and above) into the {@link EventLog} as {@code log} events. */
final class LogCapture extends AbstractAppender {
    private LogCapture() {
        super("RoseAgentBridge", null, null, true, Property.EMPTY_ARRAY);
    }

    static void install() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LogCapture appender = new LogCapture();
        appender.start();
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, Level.INFO, null);
        context.updateLoggers();
    }

    @Override
    public void append(LogEvent event) {
        JsonObject data = new JsonObject();
        data.addProperty("level", event.getLevel().name());
        data.addProperty("logger", event.getLoggerName());
        data.addProperty("thread", event.getThreadName());
        data.addProperty("message", event.getMessage().getFormattedMessage());
        if (event.getThrown() != null) data.addProperty("error", event.getThrown().toString());
        EventLog.add("log", data);
    }
}
