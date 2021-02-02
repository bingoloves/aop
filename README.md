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
             *     2、返回值是Intent(只能是Intent),则只是构建一个跳转的Intent需要自己手动startActivity(intent)
             *     注意：第一个参数必须是Context,要实现动画跳转必须是Activity，否则不生效。fragment中可以使用Context,一般不需要@Animation
             * @param activity
             * @param name
             */
        @Navigate(TwoActivity.class)
        @Animation(enterAnim = { R.anim.slide_in_from_right,R.anim.slide_out_from_left },exitAnim = {R.anim.slide_in_from_left,R.anim.slide_out_from_right})
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
```
