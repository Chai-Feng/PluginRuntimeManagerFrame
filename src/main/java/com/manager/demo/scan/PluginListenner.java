package com.manager.demo.scan;
import com.manager.demo.ioc.PluginManger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import java.io.File;

@Log4j2
public class PluginListenner extends FileAlterationListenerAdaptor {

    //根容器
    AnnotationConfigApplicationContext rootContext;

    public PluginListenner(AnnotationConfigApplicationContext context) {
        this.rootContext = context;
    }


    @Override
    public void onDirectoryCreate(File directory) {
    }

    @Override
    public void onDirectoryChange(File directory) {
    }

    @Override
    public void onDirectoryDelete(File directory) {
    }

    @Override
    public void onFileCreate(File file) {
        String name = file.getName();
        String path = file.getPath();
        /**
         * 拉起插件容器
         * 扫描到yaml 获取具体要扫描的包名
         */

        if(name.endsWith(".jar")||name.endsWith(".yaml")) {
            log.info("发现新插件 {} 正在初始化", name);
            PluginManger pluginManger = new PluginManger(rootContext);
            GenericApplicationContext pluginContext = pluginManger.doStart(path);

            //根据调用计划 消息 命令 决定是否执行
            pluginManger.doExectue(pluginContext);


            //测试删除
               pluginManger.destroy(pluginContext);
        }
    }


    @Override
    public void onFileChange(File file) {
        String name = file.getName();
        String path = file.getPath();
        if (name.endsWith(".jar")||name.endsWith(".yaml")) {
            log.info("插件已被更新！！！");


            //先卸载旧版，再加载新版 destroy --> doStart
        }
    }

    @Override
    public void onFileDelete(File file) {
        String name = file.getName();
        String path = file.getPath();
        if (name.endsWith(".jar")||name.endsWith(".yaml")) {
            log.info("插件被删除");
        }
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }

    private String getFileMeta(File file) {
        String name = file.getName();
        String path = file.getPath();
        return name;
    }

}
