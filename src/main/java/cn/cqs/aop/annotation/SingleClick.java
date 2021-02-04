package cn.cqs.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防止View被连续点击
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleClick {
    int value() default 2000;

    int[] except() default {};

    String[] exceptIdName() default {};
}
