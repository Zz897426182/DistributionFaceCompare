package com.hzgc.compare.rpc.server.annotation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 注解扫描器，用来扫描RpcService注解
 */
public class RpcServiceScanner {
    private Logger logger = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 使用此方法默认扫描com.hzgc包路径下以及子包的注解
     *
     * @return 接口类名称以及对应实现类的封装，此处使用了FastClass，比原生java反射效率高
     */
    public Map<String, FastClass> scanner() {
        return scanner(Lists.newArrayList("com.hzgc"));
    }

    /**
     * 使用此方法可扫描自定义包路径下以及子包的注解
     *
     * @param packageList 需要扫描的包路径集合
     * @return 接口类名称以及对应实现类的封装，此处使用了FastClass，比原生java反射效率高
     */
    public Map<String, FastClass> scanner(List<String> packageList) {
        Map<String, FastClass> classList = Maps.newHashMap();
        try {
            ClassPath classPath = ClassPath.from(RpcServiceScanner.class.getClassLoader());
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses()) {
                if (isContains(classInfo.getPackageName(), packageList)) {
                    Class<?> aClass = classInfo.load();
                    if (aClass.isAnnotationPresent(RpcService.class)) {
                        classList.put(aClass.getAnnotation(RpcService.class).value().getName(), FastClass.create(aClass));
                        logger.info("Annotation RpcService is found, class name is:{}", aClass.getName());
                    }
                }
            }
            return classList;
        } catch (IOException e) {
            logger.info(e.getLocalizedMessage());
            return Maps.newHashMap();
        }
    }

    private boolean isContains(String packageName, List<String> filterList) {
        for (String filter : filterList) {
            if (packageName.contains(filter)) {
                return true;
            }
        }
        return false;
    }
}
