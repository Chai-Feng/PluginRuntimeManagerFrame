package com.manager.demo.ioc;

import com.manager.demo.PluginMarTemplate;
import com.manager.demo.access.PluginMan;
import com.manager.demo.classloder.PluginLoader;
import com.manager.demo.collections.PluginStoreMap;
import com.plugin.api.IDriverPlugin;
import com.plugin.api.IPlugin;
import com.plugin.exception.PluginException;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URLClassLoader;

@Log4j2
public class PluginManger {

    AnnotationConfigApplicationContext rootContext;
    PluginMan pluginMan;

    public PluginManger(AnnotationConfigApplicationContext rootContext) {
        this.rootContext = rootContext;
    }

    /**
     * 初始化容器，拉起插件
     * @param path
     * @return
     */
    public GenericApplicationContext doStart(String...path){
        Assert.notNull(rootContext, "this rootContext must not null!!!");

        //1、创建插件容器
        GenericApplicationContext pluginContext = new GenericApplicationContext(rootContext);

        //2、设置beanFactory
        DefaultListableBeanFactory beanFactory = pluginContext.getDefaultListableBeanFactory();
        //bean 类加载器 （要保存）
        PluginLoader pluginLoader = new PluginLoader(beanFactory.getBeanClassLoader(), path);

        beanFactory.setBeanClassLoader(pluginLoader);

        //3、资源解析器（一定要把类加载器指定）
        pluginContext.setResourceLoader(new PathMatchingResourcePatternResolver(pluginLoader));
        //4、绑定register 扫描Bean后注册
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(pluginContext);

        //5、注册哨兵
        pluginContext.registerBean(PluginMan.class,1111, PluginMarTemplate.pluginConfig.logPath,"dmsDriver");

        //这个包应当写在插件对应的yaml里
        int candidates = scanner.scan("com.plugin.instance","org.plugin.instance");

        log.info("插件内候选组件 有{}个", candidates);


        //6、刷新容器
        pluginContext.refresh();

        //入口服务
        IPlugin plugin = (IPlugin) pluginContext.getBean("dmsPlugin");
         pluginMan =  pluginContext.getBean(PluginMan.class);
        //启动插件执行日志
        pluginMan.logHepler();
        LoggerContext ctx = pluginMan.getCtx();
        Logger dmsDriver = ctx.getLogger("dmsDriver_logger");

        dmsDriver.setAdditive(true);
        dmsDriver.log(Level.INFO,"插件初始化执行");

        for(String bN:pluginContext.getBeanDefinitionNames()){
            dmsDriver.log(Level.INFO,"插件内组件 -- {}",bN);
        }



        //7、保存
        PluginStoreMap.pluginMap.put(pluginLoader,plugin);

        return pluginContext;
    }


    /**
     * 调用插件  根据插件的yaml 来确定服务类型
     * @param pluginContext
     */
    public void doExectue(GenericApplicationContext pluginContext){

        IPlugin dmsPlugin = (IPlugin) pluginContext.getBean("dmsPlugin");
        LoggerContext ctx = pluginMan.getCtx();
        Logger dmsDriver = ctx.getLogger("dmsDriver_logger");
        try{
        if (dmsPlugin != null) {
            if (dmsPlugin instanceof IDriverPlugin) {
                IDriverPlugin driverPlugin = (IDriverPlugin) dmsPlugin;
                driverPlugin.start();
                driverPlugin.execute("1002", "hahaha", 1L, 2L);
                //日志

                dmsDriver.log(Level.INFO, "插件服务正在执行");
            } else if (false) {
                //saas 调用

            }
        }

        }catch (Exception e){
            dmsDriver.log(Level.INFO, "插件服务正在执行失败");
        }finally {
            dmsDriver.log(Level.INFO, "插件服务执行结束");
        }

    }

    /**
     * 注销插件
     * @param pluginContext
     */
    public void destroy(GenericApplicationContext pluginContext){
        Assert.notNull(pluginContext, "this pluginContext must not null!!!");
        ClassLoader pluginLoader=null;
        LoggerContext ctx = pluginMan.getCtx();
        Logger dmsDriver = ctx.getLogger("dmsDriver_logger");
        try{
            dmsDriver.log(Level.INFO,"插件准备注销");

            log.info("插件正在被注销");
            //还要关闭类加载器
            if(pluginContext.isRunning()){
                dmsDriver.log(Level.INFO,"插件容器正在被关闭");

                pluginContext.stop();
                pluginContext.close();
                 pluginLoader = pluginContext.getBeanFactory().getBeanClassLoader();

                dmsDriver.log(Level.INFO,"获取当前容器的bean 加载器 {}",pluginLoader);

            }
        }finally {
            if(PluginStoreMap.pluginMap.containsKey(pluginLoader)){

                if(pluginLoader instanceof URLClassLoader){
                    try {
                        ((URLClassLoader) pluginLoader).close();
                        dmsDriver.log(Level.INFO,"map清除前 {}",PluginStoreMap.pluginMap);
                        PluginStoreMap.pluginMap.remove(pluginLoader);
                        dmsDriver.log(Level.INFO,"map清除后 {}",PluginStoreMap.pluginMap);

                        // pluginLoader=null;
                    } catch (IOException e) {
                        dmsDriver.log(Level.INFO,"插件加载器关闭失败");
                    }
                }
            }

            /**
             * Close this application context, destroying all beans in its bean factory.
             * Delegates to doClose() for the actual closing procedure. Also removes a JVM shutdown hook,
             * if registered, as it's not needed anymore.
             */
            System.gc();

        }

    }
}
