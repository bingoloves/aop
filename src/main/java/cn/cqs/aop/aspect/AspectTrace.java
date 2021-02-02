package cn.cqs.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import cn.cqs.aop.annotation.AspectAnalyze;

/**
 * Created by bingo on 2021/2/2.
 *
 * @Author: bingo
 * @Email: 657952166@qq.com
 * @Description: 类作用描述
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/2
 */

@Aspect
public class AspectTrace {

    private static AspectTraceListener aspectTraceListener;

    /**
     * 针对带有AspectAnalyze注解的方法
     */
    @Pointcut("execution(@cn.cqs.aop.annotation.AspectAnalyze * *(..))")
    public void aspectAnalyzeAnnotation() {}

    /**
     * 针对前面 aspectAnalyzeAnnotation() 的配置
     */
    @Around("aspectAnalyzeAnnotation()")
    public void aroundJoinAspectAnalyze(final ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AspectAnalyze aspectAnalyze = methodSignature.getMethod().getAnnotation(AspectAnalyze.class);
        long startTimeMillis = System.currentTimeMillis();
        joinPoint.proceed();
        if (aspectTraceListener != null) {
            aspectTraceListener.onAspectAnalyze(joinPoint, aspectAnalyze, methodSignature, System.currentTimeMillis() - startTimeMillis);
        }
    }


    public static void setAspectTraceListener(AspectTraceListener aspectTraceListener) {
        AspectTrace.aspectTraceListener = aspectTraceListener;
    }

    public interface AspectTraceListener {
        void onAspectAnalyze(ProceedingJoinPoint joinPoint, AspectAnalyze aspectAnalyze, MethodSignature methodSignature, long duration);
    }
}
