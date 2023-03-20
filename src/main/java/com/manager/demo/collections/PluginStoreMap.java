package com.manager.demo.collections;
import com.plugin.api.IPlugin;

import java.util.concurrent.ConcurrentHashMap;

public class PluginStoreMap {
   // public static final ConcurrentHashMap<String, PluginLoader> loaderMap=new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<ClassLoader, IPlugin> pluginMap=new ConcurrentHashMap<>();
}
