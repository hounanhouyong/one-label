package com.hn.onelabel.server.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;

import java.util.HashMap;
import java.util.Map;

public class GroovyScriptUtil {

    public static Map<String, GroovyObject> passedClassMap = new HashMap<>();

    public static GroovyClassLoader groovyClassLoader;

    static {
        ClassLoader parent = AutowiredAnnotationBeanPostProcessor.class.getClassLoader();
        groovyClassLoader = new GroovyClassLoader(parent);
    }

    public static GroovyObject loadScript(String script) {
        GroovyObject groovyObject = passedClassMap.get(CryptUtils.md5(script));
        if (groovyObject == null) {
            Class groovyClass = groovyClassLoader.parseClass(script);
            try {
                groovyObject = (GroovyObject) groovyClass.newInstance();
                passedClassMap.put(CryptUtils.md5(script), groovyObject);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return groovyObject;
    }

    public static Object invokeMethod(GroovyObject object, String method, Object[] args) {
        return object.invokeMethod(method, args);
    }

    public static Object invokeMethod(String script, String method, Object[] args) {
        GroovyObject groovy = loadScript(script);
        if (groovy != null) {
            return invokeMethod(groovy, method, args);
        } else {
            return null;
        }
    }

    public static void removeInactiveScript(String  script){
        passedClassMap.remove(CryptUtils.md5(script));
    }

}
