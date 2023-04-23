package com.manager.demo.classloder;


import com.manager.demo.util.TestFather;


import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

//自定义jar内的加载器
public class PluginLoader2 extends URLClassLoader {


    public PluginLoader2(ClassLoader parent, String...paths) {
        super(addURL(paths), findParentClassLoader(parent) );
    }
    public PluginLoader2(ClassLoader parent, URL[] urls) {
        super(urls, findParentClassLoader(parent) );
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


    @Override
    public URL getResource(String name) {
        URL url;
        url =  this.findResource(name);
        if(url!=null){
            return url;
        }else{
            url=super.getResource(name);
            return url;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //先尝试自己加载，加载不成功找父类
       // return this.loadClass(name,false);
     return this.loadClassPro(name,false);

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
       return super.findClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (getParent()!=null){
            try { //classUtil 会修改 查找的名称，导致找不到spring注解类
                System.out.println("Parent().loadClass 当前正在加载的类名 --- "+name);
              return   getParent().loadClass(name);
            }catch (ClassNotFoundException ex){
                System.out.println("自定义加载器 当前正在加载的类名 --- "+name);
                return findClass(name);
            }
        }else
            System.out.println("无法解析的类 默认双亲委派加载 类名 --- "+name);
            return super.loadClass(name,false);
    }




    protected Class<?> loadClassPro(String name, boolean resolve) throws ClassNotFoundException {
        try {
            try{
                Class<?> cc = Class.forName(name, false, ClassLoader.getSystemClassLoader());
                return cc;
            }catch (ClassNotFoundException e){
            }
            System.out.println("完全尝试自己加载 --- "+name);
            return findClass(name);
        }catch(ClassNotFoundException ex){
            System.out.println("自己加载 找不到 -- "+name);
            try {
                return   getParent().loadClass(name);
            }catch (ClassNotFoundException ex2){
                System.out.println(name+ "-- 最终加载失败");
                ex2.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private static ClassLoader findParentClassLoader(ClassLoader parent) {
        if (parent == null) {
          parent = PluginLoader2.class.getClassLoader();
        }
        System.out.println("parent  父加载器为 "+parent);
        return parent;
    }


    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> aClass = Class.forName("com.manager.demo.util.TestFather", false, ClassLoader.getSystemClassLoader());
        System.out.println("---------------------");
        TestFather o = (TestFather) aClass.newInstance();
        System.out.println("---------------------");
        o.sayHello();
    }

}
