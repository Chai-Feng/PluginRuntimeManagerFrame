package com.manager.demo.classloder;


import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

//自定义jar内的加载器
public class PluginLoader extends URLClassLoader {
   // public static URL[] urls;
    public PluginLoader(ClassLoader parent,String...paths) {
        super(addURL(paths),
                findParentClassLoader(parent));
    }

    public static URL[] addURL(String...paths) {
        List<URL> list = new ArrayList<>();


        try {
            for(String path:paths){
                File file = new File(path);
                URL url = file.toURI().toURL();
                list.add(url);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("创建加载器: 插件路径错误");
        }
        return list.toArray(new URL[]{});
    }


    private static ClassLoader findParentClassLoader(ClassLoader parent) {
        if (parent == null) {
            parent = PluginLoader.class.getClassLoader();
        }
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        return parent;
    }

}
