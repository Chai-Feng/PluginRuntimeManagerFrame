package com.manager.demo.scan;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;


@Log4j2
public class PluginMonitor {


    FileAlterationMonitor monitor =null;


    public PluginMonitor(long interval) {
        this.monitor = new FileAlterationMonitor(interval);
    }

    //装配monitor，准备扫描工作
    public void doScan(String path, FileAlterationListener listener){
        FileAlterationObserver observer = new FileAlterationObserver(path);
        observer.addListener(listener);
        monitor.addObserver(observer);
        //添加工作线程
    }

    public void start() throws Exception {
        log.info("插件监视器开启");
        monitor.start();
    }

    public void stop() throws Exception {
        log.info("插件监视器关闭");
        monitor.stop();
    }

}
