package cn.cqs.aop.aspect;

import android.content.res.Resources;
import android.util.Log;
import android.view.View;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;

import cn.cqs.aop.R;
import cn.cqs.aop.annotation.SingleClick;

/**
 * 防止View被连续点击,间隔时间600ms
 */
@Aspect
public class SingleClickAspect {
    private final String TAG = "SingleClickAspect";
    private static long mLastClickTime;
    // View#setOnClickListener 针对所有点击事件
    //private static final String POINTCUT_ON_VIEW_CLICK = "execution(* android.view.View.OnClickListener.onClick(..))";
    private static final String POINTCUT_ON_ANNOTATION = "execution(@cn.cqs.aop.annotation.SingleClick * *(..))";

    @Pointcut(POINTCUT_ON_ANNOTATION)
    public void singleClickPointcut(){
    }
    @Around("singleClickPointcut()")
    public void aroundJoinPoint(final ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            //检查方法是否有注解
            boolean hasAnnotation = method != null && method.isAnnotationPresent(SingleClick.class);
            //计算点击间隔，没有自定义注解 默认2000，有注解按注解参数来
            int interval = 2000;
            if (hasAnnotation) {
                SingleClick annotation = method.getAnnotation(SingleClick.class);
                interval = annotation.value();
            }
            //获取被点击的view对象
            Object[] args = joinPoint.getArgs();
            View view = findViewInMethodArgs(args);
            if (view != null) {
                int id = view.getId();
                //注解排除某个控件不防止双击
                if (hasAnnotation) {
                    SingleClick annotation = method.getAnnotation(SingleClick.class);
                    //按id值排除不防止双击的按钮点击
                    int[] except = annotation.except();
                    for (int i : except) {
                        if (i == id) {
                            mLastClickTime = System.currentTimeMillis();
                            joinPoint.proceed();
                            return;
                        }
                    }
                    //按id名排除不防止双击的按钮点击（非app模块）
                    String[] idName = annotation.exceptIdName();
                    Resources resources = view.getResources();
                    for (String name : idName) {
                        int resId = resources.getIdentifier(name, "id", view.getContext().getPackageName());
                        if (resId == id) {
                            mLastClickTime = System.currentTimeMillis();
                            joinPoint.proceed();
                            return;
                        }
                    }
                }
                if (canClick(interval)) {
                    mLastClickTime = System.currentTimeMillis();
                    joinPoint.proceed();
                    return;
                }
            }

            //检测间隔时间是否达到预设时间并且线程空闲
            if (canClick(interval)) {
                mLastClickTime = System.currentTimeMillis();
                joinPoint.proceed();
            }
        } catch (Exception e) {
            //出现异常不拦截点击事件
            joinPoint.proceed();
        }
    }

    public View findViewInMethodArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof View) {
                View view = (View) args[i];
                if (view.getId() != View.NO_ID) {
                    return view;
                }
            }
        }
        return null;
    }

    public boolean canClick(int interval) {
        long l = System.currentTimeMillis() - mLastClickTime;
        if (l > interval) {
            mLastClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
