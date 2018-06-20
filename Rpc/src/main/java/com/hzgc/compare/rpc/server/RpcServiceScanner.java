package com.hzgc.compare.rpc.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RpcServiceScanner {
    private Logger logger = LoggerFactory.getLogger(RpcServiceScanner.class);

    public List<Class<?>> scanner() {

        return scanner(Lists.newArrayList("com.hzgc"));
    }

    public List<Class<?>> scanner(List<String> packageList) {
        List<Class<?>> classList = new ArrayList<>();
        try {
            ClassPath classPath = ClassPath.from(RpcServiceScanner.class.getClassLoader());
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses()) {
                if (isContains(classInfo.getPackageName(), packageList)) {
                    Class<?> aClass = classInfo.load();
                    if (aClass.isAnnotationPresent(RpcService.class)) {
                        classList.add(aClass);
                    }
                }
            }
            return classList;
        } catch (IOException e) {
            logger.info(e.getLocalizedMessage());
            return Lists.newArrayList();
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
