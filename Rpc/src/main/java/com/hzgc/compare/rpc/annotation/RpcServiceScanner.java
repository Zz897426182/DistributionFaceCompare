package com.hzgc.compare.rpc.annotation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RpcServiceScanner {
    private Logger logger = LoggerFactory.getLogger(RpcServiceScanner.class);

    public Map<String, FastClass> scanner() {

        return scanner(Lists.newArrayList("com.hzgc"));
    }

    public Map<String, FastClass> scanner(List<String> packageList) {
        Map<String, FastClass> classList = Maps.newHashMap();
        try {
            ClassPath classPath = ClassPath.from(RpcServiceScanner.class.getClassLoader());
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses()) {
                if (isContains(classInfo.getPackageName(), packageList)) {
                    Class<?> aClass = classInfo.load();
                    if (aClass.isAnnotationPresent(RpcService.class)) {
                        classList.put(aClass.getName(), FastClass.create(aClass));
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
