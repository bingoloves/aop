# aop
Android Aop面向切面编程示例，结合Navigator实现页面跳转和传递参数

#### 使用示例
```gradle
   //项目级别 build.gradle
     dependencies {
           //添加 aspectj(Aop)
           classpath 'com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.4'
       }
   //app moudle build.gradle
   apply plugin: 'android-aspectjx'
   //aspectjx {//另外如果使用淘宝等其他的 sdk ,需要进行排除
   //    exclude 'androidx','com.google','com.squareup','com.alipay','org.apache','com.taobao'
   //    enabled true
   //}
```
```java
    //1、先定义NavigationServer
    public interface NavigationServer {
         /**
             * 跳转示例
             * <p>
             *     1、无返回值：跳转模式
             *     2、返回值是Intent(只能是Intent),则只是构建一个跳转的Intent需要自己手动{@link Activity#startActivity(Intent)
             * @param activity
             * @param name
             */
        @Navigate(TwoActivity.class)
        void moveTwo(Activity activity, @Extra("name") String name);
    
        @Navigate(TwoActivity.class)
        Intent moveTwo(Activity activity);
    }
    //2、页面跳转
    Navigator.create(NavigationServer.class).moveTwo(this, "xuebing");
    //3、目标页面自动接收数据对象
     @Autowired
     String name;
     ...
      @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_two);
            Injector.inject(this);
            LogUtils.e(name);
        }
     //页面跳转动画的实现
     //配置全局跳转动画
     ActivityAspect.setDefaultAnimation(new int[]{R.anim.slide_in_from_right,R.anim.slide_out_from_left },new int[]{R.anim.slide_in_from_left,R.anim.slide_out_from_right});
     //修改局部动画
     ActivityAspect.setEnterAnimation(new int[]{R.anim.fade_in,R.anim.fade_out });
     ActivityAspect.setExitAnimation(new int[]{R.anim.fade_in,R.anim.fade_out });
```
