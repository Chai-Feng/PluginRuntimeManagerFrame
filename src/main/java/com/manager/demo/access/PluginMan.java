package com.manager.demo.access;
import lombok.Data;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;


/**
 * 插件(容器)日志哨兵
 */

@Data
@Component("pluginMan")
public class PluginMan {

    private int id;
    private String logPath;

    private  LoggerContext ctx;

    public PluginMan(int id, String logPath, String pluginName) {
        this.id = id;
        this.logPath = logPath;
        this.pluginName = pluginName;
    }

    private String pluginName;

    private LoggerContext loggerContext;


    public PluginMan() {
    }

    public void logHepler() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = ctx.getConfiguration();

        PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
        layoutBuilder.withPattern("%d{yyyy-MM-dd hh:mm:ss.SSS} %-5level %class{36} %L %M - %msg%xEx%n");
        layoutBuilder.withCharset(Charset.forName("UTF-8"));
        layoutBuilder.withConfiguration(configuration);
        Layout layout = layoutBuilder.build();

        TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build();
        ThresholdFilter thresholdFilter = ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY);
        RollingRandomAccessFileAppender randomAccessFileAppender = RollingRandomAccessFileAppender.newBuilder()
                .setName(pluginName)
                .withFileName(logPath+"/"+pluginName+"/"+pluginName+".log")
                .withFilePattern(logPath+"/%d{yyyy-MM-dd}/abc_test.log")
                .withImmediateFlush(true)
                .withPolicy(timeBasedTriggeringPolicy)
                .setFilter(thresholdFilter)
                .setLayout(layout)
                .build();
        randomAccessFileAppender.start();
        configuration.addAppender(randomAccessFileAppender);

        AppenderRef ref = AppenderRef.createAppenderRef(pluginName, null, null);

        AppenderRef[] refs = new AppenderRef[]{ref};
        //能看见控制台打印  additivtiy =false
       // LoggerConfig loggerConfig = AsyncLoggerConfig.createLogger(false, Level.INFO, pluginName+"Logger", "false", refs, null, configuration, null);

        LoggerConfig loggerConfig = AsyncLoggerConfig.createLogger(true, Level.INFO, pluginName+"Logger", "false", refs, null, configuration, null);

        loggerConfig.addAppender(randomAccessFileAppender, Level.TRACE, null);

        configuration.addLogger(pluginName+"_logger", loggerConfig);
        configuration.start();

        ctx.updateLoggers();
        Logger logger = ctx.getLogger(pluginName + "_logger");
        logger.log(Level.INFO,"插件"+pluginName+" \t日志开启");

        this.ctx=ctx;


    }
}
