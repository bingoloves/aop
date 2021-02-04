package cn.cqs.aop.utils;

import android.app.Application;

/**
 * Created by bingo on 2021/2/3.
 *
 * @Author: bingo
 * @Email: 657952166@qq.com
 * @Description: Application代理
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/3
 */
public interface IApplicationProxy {
    void onCreate(Application application);
}
