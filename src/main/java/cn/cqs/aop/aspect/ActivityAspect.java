package cn.cqs.aop.aspect;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;

import java.util.Stack;

import cn.cqs.aop.navigation.AnimationUtils;

/**
 * 功能：拦截Activity finish方法(这里用于执行销毁动画)
 */
@Aspect
public class ActivityAspect {
    private String TAG = this.getClass().getSimpleName();
    private static Stack<Activity> activityStack = new Stack<Activity>();
    private Application application;
    /**
     * 添加Activity到堆栈
     */
    private void addActivity(Activity activity) {
        activityStack.add(activity);
    }
    /**
     * 移除指定的Activity
     */
    private void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    /**
     * 获取当前Activity栈数量
     * @return
     */
    public static int getActivityStackSize(){
        try {
            return activityStack.size();
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static Stack<Activity> getActivityStack() {
        return activityStack;
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public static Activity getCurrentActivity() {
        Activity activity = null;
        try {
            activity = activityStack.lastElement();
        } catch (Exception e){
            e.printStackTrace();
        }
        return activity;
    }
    /**
     * 针对Application 类的 onCreate 方法
     */
    @Pointcut("execution(* android.app.Application.onCreate(..))")
    public void applicationOnCreatePointcut() {}
    /**
     * 针对所有继承 Activity 类的 onCreate 方法
     */
//    @Pointcut("execution(* android.app.Activity+.onCreate(..))")
//    public void activityOnCreatePointcut() {}
    @Pointcut("execution(* android.app.Activity.onCreate(..))")
    public void activityOnCreatePointcut() {}
    @Pointcut("execution(* android.app.Activity.finish(..))")
    public void activityFinishPointcut() {}
    @Pointcut("execution(* android.app.Activity.onDestroy(..))")
    public void activityOnDestroyPointcut() {}
    /**
     * Application OnCreate
     */
    @Around("applicationOnCreatePointcut()")
    public void aroundJoinApplicationOnCreate(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
        if (application == null){
            application = (Application) joinPoint.getTarget();
            activityStack.clear();
            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    addActivity(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    removeActivity(activity);
                }
            });
        }
    }
    /**
     * activity OnCreate
     */
    @Around("activityOnCreatePointcut()")
    public void aroundJoinActivityOnCreate(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
    }
    /**
     * activity onDestroy
     */
    @Around("activityOnDestroyPointcut()")
    public void aroundJoinActivityOnDestroy(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
    }
    /**
     * 在MainActivity的所有生命周期的方法中打印log
     * @param joinPoint
     * @throws Throwable
     */
    @Around("activityFinishPointcut()")
    public void aroundJoinActivityFinish(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
        //在Finish后插入退出动画,当前栈若只有一个Activity默认去掉之前的动画直接退出应用
        Activity activity = (Activity) joinPoint.getTarget();
        if (activity != null && getActivityStackSize() > 1){
            int[] animation = AnimationUtils.getAnimation(activity.getClass());
            if (animation != null){
                activity.overridePendingTransition(animation[0],animation[1]);
            }
        }
    }

    /**
     * 日志输出
     * @param joinPoint
     */
    private void log(ProceedingJoinPoint joinPoint,long startTimeMillis){
        long duration = System.currentTimeMillis() - startTimeMillis;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        SourceLocation location = joinPoint.getSourceLocation();
        String message = String.format("%s(%s:%s) [%sms]", methodSignature.getMethod().getName(), location.getFileName(), location.getLine(), duration);
        Log.d(TAG,message);
    }
}

