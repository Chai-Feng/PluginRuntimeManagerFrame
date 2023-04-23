package com.manager.demo.ioc;

import com.manager.demo.PluginMarTemplate;
import com.manager.demo.access.PluginMan;
import com.manager.demo.classloder.PluginLoader;
import com.manager.demo.classloder.PluginLoader2;
import com.manager.demo.collections.PluginStore;
import com.manager.demo.collections.PluginStoreMap;
import com.plugin.api.IDriverPlugin;
import com.plugin.api.IPlugin;
import com.plugin.exception.PluginException;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

@Log4j2
public class PluginManger {

    GenericApplicationContext rootContext;
    PluginMan pluginMan;

    List<URL>  urlList;

    public PluginManger(AnnotationConfigApplicationContext rootContext) {
        this.rootContext = rootContext;
    }

    public PluginManger(GenericApplicationContext rootContext,List<URL> list) {
        this.rootContext = rootContext;
        this.urlList=list;
    }

    /**
     * 初始化容器，拉起插件
     * @param path
     * @return
     */
    public GenericApplicationContext doStart(String basePackage,String pluginName,String...path){
        Assert.notNull(rootContext, "this rootContext must not null!!!");

        //1、创建插件容器
        GenericApplicationContext pluginContext = new GenericApplicationContext(rootContext);
        //GenericApplicationContext pluginContext = new GenericApplicationContext();

        //2、设置beanFactory
        DefaultListableBeanFactory beanFactory = pluginContext.getDefaultListableBeanFactory();
        //bean 类加载器 （要保存）

        System.out.println("beanFactory.getBeanClassLoader() = "+beanFactory.getBeanClassLoader());
      //  PluginLoader2 pluginLoader = new PluginLoader2(beanFactory.getBeanClassLoader(), path);
        PluginLoader2 pluginLoader = new PluginLoader2(beanFactory.getBeanClassLoader(), urlList.toArray(new URL[]{}));


        beanFactory.setBeanClassLoader(pluginLoader);

        //3、资源解析器（一定要把类加载器指定）
        pluginContext.setResourceLoader(new PathMatchingResourcePatternResolver(pluginLoader));
        //4、绑定register 扫描Bean后注册
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(pluginContext);

        //5、注册哨兵
       // pluginContext.registerBean(PluginMan.class,1111, PluginMarTemplate.pluginConfig.logPath,pluginName);
        BeanDefinitionBuilder bdBUilder = BeanDefinitionBuilder.genericBeanDefinition(PluginMan.class);
        bdBUilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        bdBUilder.addConstructorArgValue(PluginMarTemplate.pluginConfig.logPath);
        bdBUilder.addConstructorArgValue(pluginName);
        beanFactory.registerBeanDefinition("sentinel",bdBUilder.getBeanDefinition());

      //  beanFactory.registerSingleton("sentinel",new PluginMan(PluginMarTemplate.pluginConfig.logPath,pluginName));

        //这个包应当写在插件对应的yaml里
        int candidates = scanner.scan(basePackage);

        log.info("插件内候选组件 有{}个", candidates);
        log.info("当前beandefinition  有{}个", pluginContext.getBeanDefinitionCount());

        for (String b:beanFactory.getBeanDefinitionNames()){
            System.out.println("pCTX ---before Refresh ---"+b);
        }

        //6、刷新容器
        pluginContext.refresh();

        System.out.println("容器已经拉起---");
        //入口服务
        log.info("当前beandefinition  有{}个", pluginContext.getBeanDefinitionCount());
        for (String b:pluginContext.getBeanDefinitionNames()){
            System.out.println("pCTX ---after Refresh ---"+b);
        }

         pluginMan =  pluginContext.getBean(PluginMan.class);
        //启动插件执行日志
        pluginMan.logHepler();
        Logger dmsDriver  = pluginMan.getLogger(pluginName);

        //dmsDriver.setAdditive(true);
        dmsDriver.log(Level.INFO,"插件初始化执行");



        //7、保存
        PluginStore instance = new PluginStore(pluginContext, pluginLoader);
        PluginStoreMap.pluginMap.put(pluginName,instance);

        return pluginContext;
    }


    /**
     * 调用插件  根据插件的yaml 来确定服务类型
     * @param pluginContext
     */
    public void doExectue(GenericApplicationContext pluginContext,String pN){

        IPlugin dmsPlugin = (IPlugin) pluginContext.getBean(IPlugin.class);

        Logger dmsDriver = pluginMan.getLogger(pN);
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
    public void destroy(GenericApplicationContext pluginContext,String pluginName){
        Assert.notNull(pluginContext, "this pluginContext must not null!!!");
        ClassLoader pluginLoader=null;
        Logger dmsDriver = pluginMan.getLogger(pluginName);
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
            if(PluginStoreMap.pluginMap.containsKey(pluginName)){

                if(pluginLoader instanceof URLClassLoader){
                    try {
                        ((URLClassLoader) pluginLoader).close();
                        dmsDriver.log(Level.INFO,"map清除前 {}",PluginStoreMap.pluginMap);
                        PluginStoreMap.pluginMap.remove(pluginName);
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
