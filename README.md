# PayDemo

## 测试应用内购买。

https://developer.android.com/google/play/billing/billing_testing.html


## 使用 billingclient 库
 
https://developer.android.com/google/play/billing/billing_library.html

## 过程

1. 引入库

```groovy
api 'com.android.billingclient:billing:1.0'
```

代替官网上：`将 AIDL 文件添加到您的项目中` 这一步。

2. 更新您的应用清单

请在 AndroidManifest.xml 文件中添加以下代码行：
```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

3. 绑定到 InAppBillingService 见官网。