package cn.cqs.aop.navigation;

import android.app.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bingo on 2021/2/1.
 *
 * @Author: bingo
 * @Email: 657952166@qq.com
 * @Description: 类作用描述
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/1
 */

public class AnimationUtils {
    private static Map<Class<? extends Activity>,int[]> animationCache = new HashMap();

    public static void put(Class<? extends Activity> target,int[] anim) {
        animationCache.put(target,anim);
    }
    public static boolean contains(Class<? extends Activity> target) {
        return animationCache.containsKey(target);
    }
    public static int[] getAnimation(Class<? extends Activity> target) {
        if (animationCache.containsKey(target)){
            return animationCache.get(target);
        }
        return null;
    }
}
