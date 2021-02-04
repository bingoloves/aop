package cn.cqs.aop.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import cn.cqs.aop.annotation.AppLifecycle;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by bingo on 2021/2/3.
 *
 * @Author: bingo
 * @Email: 657952166@qq.com
 * @Description: 生命周期处理器
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/3
 */
public class AppLifecycleProcessor {
    private static final String TAG = "AppLifecycleProcessor";
    /**
     * 初始化各需要的模快Application感知
     * @param application 當前Application
     * @param packageName 需要加載的模快包名
     */
    public static void init(Application application,String[] packageName){
        List<Class<?>> appLifecycleList = getPackageClasses(application,packageName);
//        Log.e(TAG,"AppLifecycle Size = " + appLifecycleList.size());
        initModuleApplication(application,appLifecycleList);
    }

    /**
     * 初始化模块需要的必要操作
     * @param application
     * @param appLifecycleList
     */
    private static void initModuleApplication(Application application, List<Class<?>> appLifecycleList){
        if (!appLifecycleList.isEmpty()){
            //根据优先级排序
            Collections.sort(appLifecycleList, new Comparator<Class<?>>() {
                @Override
                public int compare(Class<?> o1, Class<?> o2) {
                    AppLifecycle appLifecycle1 = o1.getAnnotation(AppLifecycle.class);
                    AppLifecycle appLifecycle2 = o2.getAnnotation(AppLifecycle.class);
                    int priority1 = appLifecycle1.priority();
                    int priority2 = appLifecycle2.priority();
                    return priority1 - priority2;
                }
            });
            /**
             * 执行{@link IApplicationProxy#onCreate}
             */
            for (Class<?> clazz : appLifecycleList) {
                try {
                    IApplicationProxy proxy = (IApplicationProxy) clazz.newInstance();
                    proxy.onCreate(application);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 扫描需要感知的Application生命周期的类
     * @param ctx
     * @param modulePackages 模块包名，如:cn.cqs.common
     * @return
     */
    private static List<Class<?>> getPackageClasses(Context ctx, String[] modulePackages) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        try {
            PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();
            DexFile dex = new DexFile(ctx.getPackageResourcePath());
            Enumeration<String> entries = dex.entries();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (modulePackages != null){
                    for (String entityPackage : modulePackages) {
                        if (entryName.contains(entityPackage)) {
                            Class<?> entryClass = Class.forName(entryName, true,classLoader);
                            AppLifecycle annotation = entryClass.getAnnotation(AppLifecycle.class);
                            if (annotation != null) {
                                classes.add(entryClass);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
}
