package com.manager.demo;

import PluginEnv.PluginConfig;
import com.manager.demo.scan.PluginListenner;
import com.manager.demo.scan.PluginMonitor;
import com.plugin.api.INotify;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


import java.io.InputStream;


@Log4j2
public class PluginMarTemplate {

    public static final PluginConfig pluginConfig;

    static PluginMonitor pluginMonitor;

    static AnnotationConfigApplicationContext rootContext;


    static {
        Constructor constructor = new Constructor(PluginConfig.class);
        Yaml yaml = new Yaml(constructor);
        InputStream resourceAsStream = PluginMarTemplate.class.getClassLoader().getResourceAsStream("config.yaml");
        pluginConfig = yaml.load(resourceAsStream);
        System.out.println("---------------资源加载中------------");
        System.out.println(pluginConfig.pluginPath);
        System.out.println(pluginConfig.logPath);
        System.out.println("---------------测试开始--------------");
    }

    public static void main(String[] args) {


        init(null);
    }


    public static void init(INotify iNotify) {
        //创建根容器
        rootContext = new AnnotationConfigApplicationContext();

        rootContext.scan("com.manager.demo");

        rootContext.removeBeanDefinition("sentinel");
        //拉起容器
        rootContext.refresh();

        for (String bN : rootContext.getBeanDefinitionNames()) {
            log.info("rootContext -- {}", bN);
        }

        PluginListenner pluginListenner = new PluginListenner(rootContext);
        pluginMonitor = new PluginMonitor(500);
        pluginMonitor.doScan(pluginConfig.pluginPath, pluginListenner);

        try {
            pluginMonitor.start();
        } catch (Exception e) {
            throw new RuntimeException("插件扫描器：开启失败");
        }
        if (iNotify != null) {
            //回调
            iNotify.updatePlugin();
        }

    }

    public void detroy() {
        try {
            pluginMonitor.stop();
        } catch (Exception e) {
            throw new RuntimeException("插件扫描器：关闭失败");
        }
    }
}
