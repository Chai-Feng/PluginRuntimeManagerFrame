package com.manager.demo.collections;

import com.manager.demo.classloder.PluginLoader2;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Author: Alex
 * @Email: chai5885@gmail.com
 * @Description:
 * @Date: 2023/4/6 23:59
 */
@Data
@AllArgsConstructor
public class PluginStore {

    private GenericApplicationContext ctx;

    private PluginLoader2 loader;
}
