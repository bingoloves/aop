package cn.cqs.aop.aspect;

import android.app.Activity;
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
    private static Stack<Activity> activityStack;
    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }
    /**
     * 移除指定的Activity
     */
    public void removeActivity(Activity activity) {
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
     * 针对所有继承 Activity 类的 onCreate 方法
     */
    @Pointcut("execution(* android.app.Activity+.onCreate(..))")
    public void activityOnCreatePointcut() {}

    @Pointcut("execution(* android.app.Activity+.finish(..))")
    public void activityFinishPointcut() {
    }
    @Pointcut("execution(* android.app.Activity+.onDestroy(..))")
    public void activityOnDestroyPointcut() {
    }
    /**
     * activity OnCreate
     */
    @Around("activityOnCreatePointcut()")
    public void aroundJoinActivityOnCreate(final ProceedingJoinPoint joinPoint) throws Throwable {
        Activity activity = (Activity) joinPoint.getTarget();
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null && !activity.equals(currentActivity)){
            addActivity(activity);
            Log.d(TAG,activity.getClass().getName());
        }
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTimeMillis;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        SourceLocation location = joinPoint.getSourceLocation();
        String message = String.format("%s(%s:%s) [%sms]", methodSignature.getMethod().getName(), location.getFileName(), location.getLine(), duration);
        Log.d(TAG,message);
    }
    /**
     * activity onDestroy
     */
    @Around("activityOnDestroyPointcut()")
    public void aroundJoinActivityOnDestroy(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getThis().getClass().getSimpleName();
        Log.d(TAG, "class:" + className+" method:" + methodSignature.getName());
        Activity activity = (Activity) joinPoint.getTarget();
        if (activity != null){
            removeActivity(activity);
        }
        joinPoint.proceed();
    }
    /**
     * 在MainActivity的所有生命周期的方法中打印log
     * @param joinPoint
     * @throws Throwable
     */
    @Around("activityFinishPointcut()")
    public void aroundJoinActivityFinish(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getThis().getClass().getSimpleName();
        Log.d(TAG, "class:" + className+" method:" + methodSignature.getName());
        joinPoint.proceed();
        //在Finish后插入退出动画,当前栈若只有一个Activity默认去掉之前的动画直接退出应用
        Activity activity = (Activity) joinPoint.getTarget();
        if (activity != null && getActivityStackSize() > 1){
            int[] animation = AnimationUtils.getAnimation(activity.getClass());
            if (animation != null){
                activity.overridePendingTransition(animation[0],animation[1]);
            }
        }
    }
}

