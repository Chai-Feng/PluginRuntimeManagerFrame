package com.manager.demo.scan;

import PluginEnv.PluginInfo;
import com.manager.demo.PluginMarTemplate;
import com.manager.demo.collections.PluginStoreMap;
import com.manager.demo.ioc.PluginManger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class PluginListenner extends FileAlterationListenerAdaptor {

    //根容器
    GenericApplicationContext rootContext;
    //保存插件名


    private static PluginInfo pluginInfo;

    private static String pluginPath;

    private static List<URL> urls = new ArrayList<>();


    public PluginListenner(GenericApplicationContext context) {
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

        String[] pathNames = path.split("\\" + file.separator);

        //补充解压、从yaml中获取配置
        if (name.endsWith("yaml")) {
            log.info("找到了yaml {}", name);
            pluginInfo = getPluginInfo(path);
        } else if (name.endsWith("jar")) {
            try {
                URL url = new File(path).toURI().toURL();
                urls.add(url);
                System.out.println("记录扫描到的url " + url);
                System.out.println("LIst<url> size = " + urls.size());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            if (pathNames[pathNames.length - 2].equalsIgnoreCase(name.split("\\.jar")[0])) {
                //记录路径

                log.info("找到了插件 {}", path);
                //校验1： 通过路径最后一个文件夹名称与jar包对比，不存在yaml和jar先后顺序问题
                pluginPath = path;
            }
        }
        if (pluginInfo != null && pluginPath != null && urls.size() == 9) {
            log.info("插件名 {}, 路径{}", pluginInfo.pluginName, pluginPath);
            PluginManger pluginManger = new PluginManger(rootContext, urls);
            GenericApplicationContext ctx = pluginManger.doStart(pluginInfo.basePackage, pluginInfo.pluginName, pluginPath);
            pluginManger.doExectue(ctx, pluginInfo.pluginName);


            pluginInfo = null;
            pluginPath = null;
            urls.stream().forEach(System.out::println);
            urls.clear();
        }
        log.info("插件保存Map {}", PluginStoreMap.pluginMap);


    }

    //加载插件配置信息
    private PluginInfo getPluginInfo(String fileName) {
        FileInputStream fileInputStream = null;
        try {
            Constructor constructor = new Constructor(PluginInfo.class);
            Yaml yaml = new Yaml(constructor);
            fileInputStream = new FileInputStream(fileName);

            PluginInfo info = yaml.load(fileInputStream);
            return info;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void onFileChange(File file) {
 /*       String name = file.getName();
        String path = file.getPath();
        if (name.endsWith(".jar")||name.endsWith(".yaml")) {
            log.info("插件已被更新！！！");
            //先卸载旧版，再加载新版 destroy --> doStart
        }*/
    }

    @Override
    public void onFileDelete(File file) {
        String name = file.getName();
        String path = file.getPath();
        if (name.endsWith(".jar") || name.endsWith(".yaml")) {
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
