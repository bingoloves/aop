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

/**
 * 功能：拦截Activity finish方法(这里用于执行销毁动画)
 */
@Aspect
public class ActivityAspect {
    private static final String TAG = "ActivityAspect";
    private static Stack<Activity> activityStack = new Stack<Activity>();
    private Application application;
    /**
     * 设置全局Activity需要执行的跳转动画
     */
    private static int[] activityEnterAnimation;
    private static int[] activityExitAnimation;
    /**
     * 设置默认的页面跳转动画
     * @param enterAnim
     * @param exitAnim
     */
    public static void setDefaultAnimation(int[] enterAnim, int[] exitAnim){
        activityEnterAnimation = enterAnim;
        activityExitAnimation = exitAnim;
    }
    private static int[] getDefaultEnterAnimation(){
        return activityEnterAnimation;
    }
    private static int[] getDefaultExitAnimation(){
        return activityExitAnimation;
    }
    /**
     * 当前页面的需要改写的动画
     */
    private static int[] enterAnimation = null;
    private static int[] exitAnimation = null;
    public static void setEnterAnimation(int[] enterAnimation) {
        ActivityAspect.enterAnimation = enterAnimation;
    }
    public static void setExitAnimation(int[] exitAnimation) {
        ActivityAspect.exitAnimation = exitAnimation;
    }

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
    @Pointcut("execution(* android.app.Activity.startActivityForResult(..))")
    public void startActivityPointcut() {}
    @Pointcut("execution(* android.app.Activity.onBackPressed(..))")
    public void onBackPressedPointcut() {}
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
        enterAnimation = exitAnimation = null;
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
    }

    /**
     * startActivity
     * @param joinPoint
     * @throws Throwable
     */
    @Around("startActivityPointcut()")
    public void aroundJoinStartActivity(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
        Activity activity = (Activity) joinPoint.getTarget();
        if (enterAnimation != null && enterAnimation.length>1){
            activity.overridePendingTransition(enterAnimation[0],enterAnimation[1]);
        } else {
            int[] enterAnim = getDefaultEnterAnimation();
            if (enterAnim != null && enterAnim.length>1) {
                activity.overridePendingTransition(enterAnim[0],enterAnim[1]);
            }
        }
    }
    /**
     * onBackPressed
     * @param joinPoint
     * @throws Throwable
     */
    @Around("onBackPressedPointcut()")
    public void aroundJoinOnBackPressed(final ProceedingJoinPoint joinPoint) throws Throwable {
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        log(joinPoint,startTimeMillis);
        Activity activity = (Activity) joinPoint.getTarget();
        if (activity != null && getActivityStackSize() > 1){
            if (exitAnimation != null && exitAnimation.length > 1){
                activity.overridePendingTransition(exitAnimation[0],exitAnimation[1]);
            } else {
                int[] exitAnim = getDefaultExitAnimation();
                if (exitAnim != null && exitAnim.length>1) {
                    activity.overridePendingTransition(exitAnim[0],exitAnim[1]);
                }
            }
        }
    }
    /**
     * Finish
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
            if (exitAnimation != null && exitAnimation.length > 1){
                activity.overridePendingTransition(exitAnimation[0],exitAnimation[1]);
            } else {
                int[] exitAnim = getDefaultExitAnimation();
                if (exitAnim != null && exitAnim.length>1) {
                    activity.overridePendingTransition(exitAnim[0],exitAnim[1]);
                }
            }
        }
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

