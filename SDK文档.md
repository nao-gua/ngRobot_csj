[TOC]

---



# 入门指南Getting started



## 1.1 文档概况Document Overview

本文档主要提供穿山甲实体机器人的SDK接口使用方法供第三方厂家使用机器人完成自定义的需求。

## 1.2 网络拓扑及通信方式Network topology and communication mode

### 1.2.1 机器人网络拓扑Robot Network topology

机器人内置一个路由器，由内置路由器进行联网。The robot has a built-in router, which is connected to the Internet.

![](<F:\Typora1.8.10\pictureInsert\4.1 SDK文档-FRj2bL9puo88EyxG4KkcNeZOn2d.png>)

内网的IP网段是192.168.99.x，一般来说机器人安卓屏幕的IP是192.168.99.101，这个IP是可变的。

机器人的路由器通过桥接模式，一端连接到客户的无线AP上（在机器人路由器上此口为wan口），同时可以自己做为AP，分享热点，供扫图、调试等电脑连接。

如果机器人能够上网，此时连接到机器人路由器上的电脑也可以上网，此时机器人的安卓系统、用户的开发电脑以及机器人内部网络设备都在一个局域网内。

Intranet the IP address CIDR block is 192.168.99.x. Generally, the IP address of the robot Android screen is 192.168.99.101.

The router of the robot is connected to the customer's wireless through bridge mode.

On the AP (this port is a wan port on the robot router), you can also use it as an AP to share hot spots for computer connections such as scanning images and debugging.

If the robot can access the Internet, the computer connected to the router of the robot can also access the internet. At this time, the Android system of the robot, the development Computer of the user and the internal network equipment of the robot are all in the same LAN.

### 1.2.2 软件整体功能架构Overall software functional architecture

![](<F:\Typora1.8.10\pictureInsert\4.1 SDK文档-软件架构.png>)

穿山甲SDK服务组合了语音识别、语音播报、语义理解能力、运动控制能力，通过AIDL和客户应用通信，本质上【穿山甲SDK服务】是一个基于 AIDL 的本地服务，因此可按照 AIDL 接口协议，通过应用间进程通信机制实现交互。

The Pangolin SDK service combines speech recognition, voice broadcast、semantic understanding, and motion control capabilities, and communicates with client applications through AIDL. In essence, the Pangolin SDK service is a local service based on AIDL, so it can interact with applications through the inter-application process communication mechanism in accordance with the AIDL interface protocol.

本文主要针对性说明机器人底层系统和上层应用之间的交互方式，值得注意的是，如果上位机是android，请使用android端demo内提供的tts语音方式，本文中提供的tts方式无效。

针对国外开发客户：本文中的语音服务仅针对中国，若想自己开发非中文的语音识别引擎，有以下两种方式：

&#x20;       1.联系销售，使用安卓端支持的国际语音引擎

&#x20;       2.参考安卓demo，自己寻找语音识别引擎做语音服务。

This article mainly explains the interaction mode between the underlying system of the robot and the upper application. It is worth noting that if the upper computer is a android, use the tts voice mode provided in the android demo. The tts mode provided in this article is invalid.

For foreign development customers: the voice service in this article is only for China. If you want to develop a non-Chinese voice recognition engine, you can use the following two methods:

1.Contact Sales and use the international voice engine supported by Android

1. Refer to the Android demo to find a voice recognition engine for voice service.





# SDK集成指南SDK Integration Guide

## 1.1 简介Overview

机器人胸口的屏幕是7.1.2的安卓系统，所以推荐集成安卓SDK来进行开发。

The screen of the robot's chest is 7.1.2 Android system, so recommend integrate Android SDK for development.

SDK封装了所有接口，可以直接调用

The SDK encapsulates all interfaces and can be called directly.

## 1.2 如何集成How to integrate?

### 1.2.1 兼容性Compatibility

| 类别                            | 兼容范围                                                     |
| ------------------------------- | ------------------------------------------------------------ |
| 系统System                      | 机器人系统为Android 7.1.2The robot system is Android 7.1.2   |
| 网络Network                     | 支持移动网络（包括2G、3G、4G等）、WIFI等网络环境Supports mobile networks (including2G,3G, 4G),WIFI and other network environments |
| 开发环境Development Environment | 建议使用最新版本Android Studio 进行开发We recommend that you use the latest version.Develop Android Studio |

附：sdk demo

[csjbot SDK demo](https://pan.baidu.com/s/1VtXgvVaMYjOiDHVf3-pvqQ?pwd=ree1)

### 1.2.2 AndroidStudio集成 AndroidStudio Integration

* 导入aar文件，将newSceneSDK-release.aar文件添加到libs文件中 
  Import the aar file and add the newSceneSDK-release.aar file to the libs file.
* build.gradle添加

代码块

```gradle
implementation(name: 'newSceneSDK-release', ext: 'aar')
```

因SDK使用了Retrofit,需在应用级build.gradle里添加

代码块

```gradle
repositories {
    google()
    jcenter()
    mavenCentral()
    maven { url "https://jitpack.io" }
    maven { url 'https://repo1.maven.org/maven2/' }
}
```

- 在app的build.gradle文件的android{}结构下添加如下代码Add the following code under the android{} structure of the build.gradle file of the app

代码块

```gradle
repositories {
    flatDir {
        dirs 'libs'
    }
}
```

- 在AndroidManifest文件中添加如下权限

代码块

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

在Application里判断是否获取悬浮窗权限

代码块

```java
if (Settings.canDrawOverlays(this)) {
    //Todo 此处写第一次检查权限且已经拥有权限后的业务

} else {
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            , Uri.parse("package:" + getPackageName()));
    startActivityForResult(intent, REQUEST_SYSTEM_ALERT_PERMISSION);
    finish();
}
```

在application节点下添加Add a application node

代码块

```xml
<meta-data
    android:name="CSJ_SDK_REMOTE_APP_NAME"
    android:value="Your applicationId" />
```

* 在Application中初始化SDK Initialize the SDK in the Application

请保证设备端有网络的情况下进行代码授权Make sure that the device has a network.

在[穿山甲开发者平台上注册账号](http://openpro.csjbot.com)

填入信息后申请 API Key和 User Key

Enter the information and apply API Key and User Key

在右侧菜单中找到API管理，API Key 对应 key ，User Key 对应 secret

在初始化的时候需要给予应用权限，需要在application的onCreat方法中添加以下代码

代码块

```java
CsjRobot.authentication(this, "appKey", "appSecret", new OnAuthenticationListener() {
    @Override
    public void success() {
        Log.d("TAG", "授权成功!");
    }

    @Override
    public void error() {
        Log.d("TAG", "授权失败!");
    }
});
```

因SDK使用了Retrofit,还需要在application的onCreat方法中添加以下代码

代码块

```java
RetrofitFactory.initClient();
```

下面所有方法都是采用单例调用方式,前缀不在重复添加\[ 特殊情况会完整写明调用方式 ]

示例：

代码块

```java
CsjRobot.getInstance().功能实例类名().方法名();
```

## 1.3 语音识别

功能实例类名：getSpeech()

### 1.3.1 开启语音识别服务

#### 1 接口定义

| 方法                 | 功能描述         | 支持版本 | 备注 |
| -------------------- | ---------------- | -------- | ---- |
| startSpeechService() | 开启语音识别服务 | -        | -    |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

代码块（Java）

```
CsjRobot.getInstance().getSpeech().startSpeechService();
```

### 1.3.2 启动语音识别

### 1.3.2 启动语音识别

#### &#x20;1 接口定义

| 方法       | 功能描述     | 支持版本 | 备注 |
| ---------- | ------------ | -------- | ---- |
| startIsr() | 开启语音识别 | -        | -    |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

代码块（Java）

```java
CsjRobot.getInstance().getSpeech().startIsr(); 
```

### 1.3.3 注册语音识别结果监听

#### 1 接口定义

| **方法**                                                | **功能描述** | **支持版本** | **备注** |
| :------------------------------------------------------ | :----------- | :----------- | :------- |
| registerSpeechListener(OnSpeechListener speechListener) | 开启语音识别 | 所有版本     | -        |

#### 2 参数说明

| **字段名**     | **类型**         | **默认值** | **释义**                             | **是否必选** | **最低支持版本** | **备注** |
| :------------- | :--------------- | :--------- | :----------------------------------- | :----------- | :--------------- | :------- |
| speechListener | OnSpeechListener | -          | 用于接收识别到的语音结果和机器人回答 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```
CsjRobot.getInstance().registerSpeechListener(speechListener);
private OnSpeechListener speechListener = new OnSpeechListener() {
    @Override
    public void speechInfo(final String s, final int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 简单解析示例
                if (Speech.SPEECH_RECOGNITION_RESULT == i) {
                    // 识别到的语音信息
                    try {
                        String text = new JSONObject(s).getString("text");
                        asr_result.setText(text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (Speech.SPEECH_RECOGNITION_AND_ANSWER_RESULT == i) {
                    // 识别到的语音信息与回答
                    try {
                        String say = new JSONObject(s).getJSONObject("result").getJSONObject("
                        nlp_result.setText(say);
                        speechSpeak.startSpeaking(say, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
};
```

语音识别结果示例

```json
{
  "msg_id": "SPEECH_ISR_ONLY_RESULT_NTF",
  "text": "你可以回家了",
  "isLast": false
}
```

| 字段名 | 类型   | 释义               | 是否必选 | 最低支持版本 | 备注 |
| ------ | ------ | ------------------ | -------- | ------------ | ---- |
| msg_id | String | 标识语音识别消息   |          | 所有版本     | -    |
| text   | String | 识别的文本         |          | 所有版本     | -    |
| isLast | bool   | 表当前识别是否结束 |          | 所有版本     | -    |

nlp 文本类型结果示例

```json
{
  "msg_id": "SPEECH_ISR_LAST_RESULT_NTF",
  "result": {
    "data": {
      "actionList": [],
      "answer": "这个已经超出我的学识范围了，换一个问题吧。",
      "say": "这个已经超出我的学识范围了，换一个问题吧。",
      "serviceId": "other",
      "type": "chat"
    },
    "error_code": 0,
    "text": "你可以回家了"
  }
}
```

| **字段名** | **类型** | **释义**             | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------- | :------------------- | :----------- | :--------------- | :------- |
| msg_id     | String   | 标识nlp结果消息      |              | 所有版本         | -        |
| result     | Object   | 结果集               |              | 所有版本         | -        |
| data       | Object   | 数据集               |              | 所有版本         | -        |
| actionList | Array    | 动作列表，无实际意义 |              | 所有版本         | -        |
| answer     | String   | nlp结果              |              | 所有版本         | -        |
| say        | String   | nlp播报内容          |              | 所有版本         | -        |
| serviceId  | String   | nlp结果id            |              | 所有版本         | -        |
| type       | String   | nlp结果类型          |              | 所有版本         | -        |
| text       | String   | 识别内容             |              | 所有版本         | -        |

可通过type来判断是否是知识库答案，type是satisfy时，表示是后台配置的知识库答案。

**重要：SDK NLP结果只返回小蜜蜂后台配置的知识库问答，如果没配置将会返回兜底话术。二次开发可自行根据ASR结果去请求第三方闲聊库或者大模型。**

**国内用户可参考的大模型示例：**

**[讯飞星火大模型](https://www.xfyun.cn/doc/spark/AndroidSDK.html#_1-sdk%E4%BB%8B%E7%BB%8D)     [百度文心一言](https://developer.baidu.com/article/detail.html?id=1089328)   [通义千问](https://help.aliyun.com/zh/dashscope/developer-reference/quick-start?spm=a2c4g.11186623.0.0.317964d1nXODWU)**

当后台知识库答案包含非文本格式时，结果如下：

```json
{
  "msg_id": "SPEECH_ISR_LAST_RESULT_NTF",
  "result": {
    "data": {
      "actionList": [],
      "answer": "看图片，华看啥看和",
      "graphic": "{\"type\":\"2\",\"answer\":\"看图片，华看啥看和\",\"imgFile\":[{\"url\":\"http...",
      "say": "看图片，华看啥看和",
      "type": "satisfy"
    },
    "error_code": 0,
    "text": "人类是怎么来的？"
  }
}
```
```json
{
  "msg_id": "SPEECH_ISR_LAST_RESULT_NTF",
  "result": {
    "data": {
      "actionList": [],
      "answer": "请观看以下视频",
      "graphic": "{\"type\":\"4\",\"answer\":\"请观看以下视频\",\"videoFile\":[{\"url\":\"https...",
      "say": "请观看以下视频",
      "type": "satisfy"
    },
    "error_code": 0,
    "text": "视频。"
  }
}
```
```json
{
  "msg_id": "SPEECH_ISR_LAST_RESULT_NTF",
  "result": {
    "data": {
      "actionList": [],
      "answer": "请欣赏以下视频",
      "graphic": "{\"type\":\"10\",\"answer\":\"请欣赏以下视频\",\"link\":\"https://v.qq.com/x/",
      "say": "请欣赏以下视频",
      "type": "satisfy"
    },
    "error_code": 0,
    "text": "超链接。"
  }
}
```

| 字段名  | 类型   | 释义                                                         | 是否必选 | 最低支持版本 | 备注 |
| ------- | ------ | ------------------------------------------------------------ | -------- | ------------ | ---- |
| graphic | String | 是一个json字符串,可根据具体内容解析                          |          | 所有版本     | -    |
| type    | String | 类型，枚举值 1:文本类型 2:图片类型 3:音频类型 4:视频类型 10:超链接 |          | 所有版本     | -    |

### 1.3.4 注册语音唤醒监听

### 1.3.4 注册语音唤醒监听

#### 1 接口定义

| **方法**                                                | **功能描述**     | **支持版本** | **备注** |
| :------------------------------------------------------ | :--------------- | :----------- | :------- |
| registerWakeupListener(OnWakeupListener wakeupListener) | 注册语音唤醒监听 | 所有版本     | -        |

#### 2 参数说明

| **字段名**     | **类型**         | **默认值** | **释义**                 | **是否必选** | **最低支持版本** | **备注**                                       |
| :------------- | :--------------- | :--------- | :----------------------- | :----------- | :--------------- | :--------------------------------------------- |
| wakeupListener | OnWakeupListener | -          | 用于接收语音功能唤醒通知 | yes          | 所有版本         | 当语音功能被唤醒时(语音唤醒)，会自行执行该回调 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().registerWakeupListener(new OnWakeupListener() {
    @Override
    public void response(int i) {
        // i 唤醒角度，开发无需关系i的值
    }
});
```



针对六麦机器，角度数值参考：

![](<F:\Typora1.8.10\pictureInsert\4.1 SDK文档-JkZ0bYLlUot2cVxSVMRc63benzg.png>)



### 1.3.5 关闭语音识别

#### 1 接口定义

| **方法**  | **功能描述** | **支持版本** | **备注** |
| :-------- | :----------- | :----------- | :------- |
| stopIsr() | 关闭语音识别 | -            | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getSpeech().stopIsr();
```

### 1.3.6 关闭语音识别服务

#### 1 接口定义

| **方法**             | **功能描述**     | **支持版本** | **备注** |
| :------------------- | :--------------- | :----------- | :------- |
| closeSpeechService() | 关闭语音识别服务 | -            | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getSpeech().closeSpeechService();
```

## 1.4 语音播报

功能实例类名：getTts()

### 1.4.1 开始说话

初始化

```java
private void initSpeech() {
    SpeakProxy.getInstance().initSpeak(this, com.csjbot.coshandler.tts.SpeechFactory.SpeechType.GOOGLE);
    //启动语音识别服务
    mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
            ServerFactory.getSpeechInstance().startSpeechService();
            String s = MMKVUtils.getString(ShareKeyConstants.LANKEY, "日本語");
            if ("日本語".equals(s)) {
                SpeakProxy.getInstance().setLanguage(Locale.JAPANESE);
            } else if ("English".equals(s)) {
                SpeakProxy.getInstance().setLanguage(new Locale("en-US"));
                SpeakProxy.getInstance().setSpeakerName("catherine");
            } else if ("繁體中文".equals(s)) {
                SpeakProxy.getInstance().setLanguage(Locale.TRADITIONAL_CHINESE);
            } else {
                SpeakProxy.getInstance().setLanguage(Locale.SIMPLIFIED_CHINESE);
            }
            ServerFactory.getSpeechInstance().startIsr();
        }
    }, 5000);
}
```

如使用google TTS，支持的语言可参考官方文档：\[可能需要科学上网]

[speech-to-text-supported-languages](https://cloud.google.com/speech-to-text/docs/speech-to-text-supported-languages?hl=zh-cn)

#### 1 接口定义

| **方法**                                             | **功能描述** | **支持版本** | **备注** |
| :--------------------------------------------------- | :----------- | :----------- | :------- |
| startSpeaking(String text, OnSpeakListener listener) | 开始说话     | -            | -        |

#### 2 参数说明

| **字段名**     | **类型**         | **默认值** | **释义**                             | **是否必选** | **最低支持版本** | **备注** |
| :------------- | :--------------- | :--------- | :----------------------------------- | :----------- | :--------------- | :------- |
| text           | String           | -          | 要说的话术                           | no           | 所有版本         | -        |
| speechListener | OnSpeechListener | -          | 说话监听，用于监听说话开始，说话结束 | no           | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getTts().startSpeaking("什么是机器人", new OnReceptionSpeakListener() {
    @Override
    public void onSpeakBegin() {
        // 说话之前
    }

    @Override
    public void onCompleted() {
        // 说话完成
    }
});
```

### 1.4.2 停止说话

#### 1 接口定义

| **方法**       | **功能描述** | **支持版本** | **备注** |
| :------------- | :----------- | :----------- | :------- |
| stopSpeaking() | 停止说话     | 所有版本     | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getTts().stopSpeaking();
```

### 1.4.3 暂停说话

#### 1 接口定义

| **方法**        | **功能描述** | **支持版本** | **备注** |
| :-------------- | :----------- | :----------- | :------- |
| pauseSpeaking() | 暂停说话     | 所有版本     | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getTts().pauseSpeaking();
```

### 1.4.4 继续说话

#### 1 接口定义

| **方法**                                 | **功能描述** | **支持版本** | **备注** |
| :--------------------------------------- | :----------- | :----------- | :------- |
| resumeSpeaking(OnSpeakListener listener) | 继续说话     | 所有版本     | -        |

#### 2 参数说明

| **字段名**     | **类型**         | **默认值** | **释义**                             | **是否必选** | **最低支持版本** | **备注** |
| :------------- | :--------------- | :--------- | :----------------------------------- | :----------- | :--------------- | :------- |
| speechListener | OnSpeechListener | -          | 说话监听，用于监听说话开始，说话结束 | no           | 所有版本         |          |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getTts().resumeSpeaking(new OnReceptionSpeakListener() {
    @Override
    public void onSpeakBegin() {
        // 说话之前
    }

    @Override
    public void onCompleted() {
        // 说话完成
    }
});
```

### 1.4.5 是否在说话

#### 1 接口定义

| **方法**     | **功能描述** | **支持版本** | **备注** |
| :----------- | :----------- | :----------- | :------- |
| isSpeaking() | 是否正在说话 | 所有版本     | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getTts().isSpeaking();
```

## 1.5 底盘及导航控制

功能实例类名：getAction()

### 1.5.1 获取坐标

#### 1 接口定义

| **方法**                                      | **功能描述** | **支持版本** | **备注** |
| :-------------------------------------------- | :----------- | :----------- | :------- |
| void getPosition(OnPositionListener listener) | 获取坐标     | -            | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().getPosition(new OnPositionListener() {
    /**
     * Position info.
     *
     * @param json the json
     *        {
     *             "error_code": 0,
     *             "msg_id": "NAVI_GET_CURPOS_RSP",
     *             "rotation": 0,
     *             "x": 0,
     *             "y": 0,
     *             "z": 0
     *        }
     */
    @Override
    public void positionInfo(String json) {
        Log.d("TAG", "OnPositionListener:s:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            String rotation = jsonObject.getString("rotation");
            String x = jsonObject.getString("x");
            String y = jsonObject.getString("y");
            String z = jsonObject.getString("z");
            RobotPose.PosBean posBean = new RobotPose.PosBean();
            posBean.setRotation(Float.valueOf(rotation));
            posBean.setX(Float.valueOf(x));
            posBean.setY(Float.valueOf(y));
            posBean.setZ(Float.valueOf(z));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
});
```

### 1.5.2 控制前后左右

方式一

#### 1 接口定义

| **方法**                 | **功能描述** | **支持版本** | **备注** |
| :----------------------- | :----------- | :----------- | :------- |
| void move(int direction) | 控制前后左右 | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义** | **是否必选** | **最低支持版本** | **备注**            |
| :--------- | :------- | :--------- | :------- | :----------- | :--------------- | :------------------ |
| direction  | int      | -          | 方向     | yes          | 所有版本         | 0 前;1 后;2 左;3 右 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().move(0);
```

### 1.5.3 控制移动

方式一：

#### 1 接口定义

| **方法**                         | **功能描述** | **支持版本** | **备注**                                                     |
| :------------------------------- | :----------- | :----------- | :----------------------------------------------------------- |
| void moveBySerial(int direction) | 控制移动     | -            | direction 枚举: <br />0x01: 表示前进 10cm; <br />0x02: 表示后退 10cm; <br />0x03: 表示左转 45 度; <br />0x04: 表示右转45 度; <br />0x05: 表示立即停止; |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().moveBySerial(0x01);
```



方式二：

#### 1 接口定义

| **方法**                                 | **功能描述** | **支持版本** | **备注**                                                     |
| :--------------------------------------- | :----------- | :----------- | :----------------------------------------------------------- |
| void moveSerial(int linear, int angular) | 控制移动     | -            | Android 直接下发线速度和角速度指令，为了安全考虑，需要维持线速度和角速度，Android端需要≤50ms 下发一次该指令，主控每 50ms刷新一次速度指令，如果未收到新的指令，主控会将线速度和角速度置为零。 |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义**   | **是否必选** | **最低支持版本** | **备注**                                      |
| :--------- | :------- | :--------- | :--------- | :----------- | :--------------- | :-------------------------------------------- |
| linear     | int      | -          | 目标线速度 | no           | 所有版本         | 数据正表示前进，数据负表示后退，单位 mm/s。   |
| angular    | int      | -          | 目标角速度 | no           | 所有版本         | 数据正表示左转，数据负表示右转，单位 mrad/s。 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().moveSerial(1,1);
```

### 1.5.4 使机器人移动到具体坐标

#### 1 接口定义

| **方法**                                        | **功能描述**   | **支持版本** | **备注** |
| :---------------------------------------------- | :------------- | :----------- | :------- |
| void navi(String json, OnNaviListener listener) | 移动到具体坐标 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**       | **默认值** | **释义**           | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------- | :--------- | :----------------- | :----------- | :--------------- | :------- |
| json       | String         | -          | 目标点位json字符串 | yes          | 所有版本         | -        |
| listener   | OnNaviListener | -          | 移动监听           | no           | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().navi(new Gson().toJson(pose.getPos()), new OnNaviListener() {
    @Override
    public void moveResult(String s) {
        // 已移动到目标位置
    }

    @Override
    public void messageSendResult(String s) {
        // 移动到某点的消息是否下发给导航成功
    }

    @Override
    public void cancelResult(String s) {
    }

    @Override
    public void goHome() {

    }
});
```

json参考下列实体类

```java
public class RosPosition {
    private float x;
    private float y;
    private float z;
    private float rotation;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public String toString() {
        return "RosPosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", rotation=" + rotation +
                '}';
    }
}
```

### 1.5.5 使机器人移动到具体坐标

#### 1 接口定义

| **方法**               | **功能描述**   | **支持版本** | **备注** |
| :--------------------- | :------------- | :----------- | :------- |
| void navi(String json) | 移动到具体坐标 | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义**           | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------- | :--------- | :----------------- | :----------- | :--------------- | :------- |
| json       | String   | -          | 目标点位json字符串 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

导航正确流程：

将机器人移动到目标点位，调用[1.5.1 获取坐标](https://www.yuque.com/cll520/public/vg0f243qz7oso695#pMgsM)，将获取的坐标保存，使用navi（String json）方法传入获取的坐标值即可。

### 1.5.6 取消当前导航任务

#### 1 接口定义

| **方法**                                 | **功能描述**     | **支持版本** | **备注** |
| :--------------------------------------- | :--------------- | :----------- | :------- |
| void cancelNavi(OnNaviListener listener) | 取消当前导航任务 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**       | **默认值** | **释义**             | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------- | :--------- | :------------------- | :----------- | :--------------- | :------- |
| listener   | OnNaviListener | -          | 取消当前导航任务监听 | no           | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().cancelNavi(new OnNaviListener() {
    @Override
    public void moveResult(String s) {
    }

    @Override
    public void messageSendResult(String s) {
    }

    @Override
    public void cancelResult(String s) {
        // 取消当前导航任务结果，其他回调无需处理
    }

    @Override
    public void goHome() {
    }
});
```

### 1.5.7 以原点为参考，旋转到特定角度

#### 1 接口定义

| **方法**                   | **功能描述**                 | **支持版本** | **备注** |
| :------------------------- | :--------------------------- | :----------- | :------- |
| void goAngle(int rotation) | 以原点为参考，旋转到特定角度 | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义**   | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------- | :--------- | :--------- | :----------- | :--------------- | :------- |
| rotation   | int      | -          | 旋转的角度 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().goAngle(60);
```

### 1.5.8 以当前位置为参考，旋转指定的角度

#### 1 接口定义

| **方法**                                                    | **功能描述**                     | **支持版本** | **备注** |
| :---------------------------------------------------------- | :------------------------------- | :----------- | :------- |
| void moveAngle(int rotation, OnGoRotationListener listener) | 以当前位置为参考，旋转到特定角度 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**             | **默认值** | **释义**   | **是否必选** | **最低支持版本** | **备注**                                          |
| :--------- | :------------------- | :--------- | :--------- | :----------- | :--------------- | :------------------------------------------------ |
| rotation   | int                  | -          | 旋转的角度 | yes          | 所有版本         | rotation>0: 向左旋转; <br />rotation<0: 向右旋转; |
| listener   | OnGoRotationListener | -          | 旋转监听   | no           | -                | -                                                 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().moveAngle(60, new OnGoRotationListener() {
    @Override
    public void response(int i) {

    }
});
```

### 1.5.9 回桩充电

#### 1 接口定义

| **方法**                             | **功能描述** | **支持版本** | **备注** |
| :----------------------------------- | :----------- | :----------- | :------- |
| void goHome(OnNaviListener listener) | 回充电桩     | -            | -        |

#### 2 参数说明

| **字段名** | **类型**       | **默认值** | **释义** | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------- | :--------- | :------- | :----------- | :--------------- | :------- |
| listener   | OnNaviListener | -          | 移动监听 | no           | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().goHome(new OnNaviListener() {
    @Override
    public void moveResult(String s) {
    }

    @Override
    public void messageSendResult(String s) {
    }

    @Override
    public void cancelResult(String s) {
    }

    @Override
    public void goHome() {
    }
});
```

### 1.5.10.1 保存地图

#### 1 接口定义

| **方法**       | **功能描述** | **支持版本** | **备注** |
| :------------- | :----------- | :----------- | :------- |
| void saveMap() | 保存地图     | -            | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().saveMap();
```

### 1.5.10.2 保存地图

#### 1 接口定义

| **方法**                             | **功能描述** | **支持版本** | **备注** |
| :----------------------------------- | :----------- | :----------- | :------- |
| void saveMap(OnMapListener listener) | 保存地图     | -            | -        |

#### 2 参数说明

| **字段名** | **类型**      | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------ | :--------- | :----------- | :----------- | :--------------- | :------- |
| listener   | OnMapListener | -          | 保存地图监听 | no           | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().saveMap(new OnMapListener() {
    @Override
    public void saveMap(int state) {
        // 保存地图回调
    }

    @Override
    public void loadMap(int state) {
    }
});
```

### 1.5.10.3 对地图命名并保存

#### 1 接口定义

| **方法**                  | **功能描述**     | **支持版本** | **备注** |
| :------------------------ | :--------------- | :----------- | :------- |
| void saveMap(String name) | 对地图命名并保存 | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义** | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------- | :--------- | :------- | :----------- | :--------------- | :------- |
| name       | String   | -          | 地图名称 | no           | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().saveMap("map1");
```

### 1.5.10.4  对地图命名并保存，附带监听

#### 1 接口定义

| **方法**                                          | **功能描述**               | **支持版本** | **备注** |
| :------------------------------------------------ | :------------------------- | :----------- | :------- |
| void saveMap(String name, OnMapListener listener) | 对地图命名并保存，附带监听 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**      | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------ | :--------- | :----------- | :----------- | :--------------- | :------- |
| name       | String        | -          | 地图名称     | no           | -                | -        |
| listener   | OnMapListener | -          | 保存地图监听 | no           | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().saveMap("map1", new OnMapListener() {
    @Override
    public void saveMap(int state) {
        // 保存地图回调
    }

    @Override
    public void loadMap(int state) {
    }
});
```

### 1.5.11 加载地图

#### 1 接口定义

| **方法**                                                     | **功能描述** | **支持版本** | **备注** |
| :----------------------------------------------------------- | :----------- | :----------- | :------- |
| void loadMap(String name, float x, float y, float rotation, OnMapListener listener) | 加载地图     | -            | -        |

#### 2 参数说明

| **字段名** | **类型**      | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注**                   |
| :--------- | :------------ | :--------- | :----------- | :----------- | :--------------- | :------------------------- |
| name       | String        | -          | 地图名称     | no           | -                | name为空时，则加载默认地图 |
| x          | float         | -          | 坐标x        | no           | -                | -                          |
| y          | float         | -          | 坐标y        | no           | -                | -                          |
| rotation   | float         | -          | 旋转         | no           | -                | -                          |
| listener   | OnMapListener | -          | 保存地图监听 | no           | -                | -                          |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().loadMap(new OnMapListener() {
    @Override
    public void saveMap(int state) {
    }

    @Override
    public void loadMap(int state) {
        // 加载地图回调
    }
});

// 其他调用示例
/**
 * Load map. Let the robot out of a specific point on the map
 * 不带回调
 * @param name      the name
 * @param x         the x
 * @param y         the y
 * @param rotation  the rotation
 */
void loadMap(String name, float x, float y, float rotation);
/**
 * Load map.
 * 带回调
 * @param name      the name
 * @param x         the x
 * @param y         the y
 * @param rotation  the rotation
 * @param onMapListener the on map listener
 */
void loadMap(String name, float x, float y, float rotation, OnMapListener onMapListener);
```

### 1.5.12 设置速度

#### 1 接口定义

| **方法**                   | **功能描述** | **支持版本** | **备注** |
| :------------------------- | :----------- | :----------- | :------- |
| void setSpeed(float speed) | 设置速度     | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义** | **是否必选** | **最低支持版本** | **备注**                                               |
| :--------- | :------- | :--------- | :------- | :----------- | :--------------- | :----------------------------------------------------- |
| speed      | float    | -          | 速度     | yes          | 所有版本         | 速度（0.3 ~ 1.2）不同的机器人有不同的限制，单位：米/秒 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().setSpeed(0.7f);
```

### 1.5.13 获取当前速度

#### 1 接口定义

| **方法**                                   | **功能描述** | **支持版本** | **备注** |
| :----------------------------------------- | :----------- | :----------- | :------- |
| void getSpeed(OnSpeedGetListener listener) | 获取当前速度 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**           | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :----------------- | :--------- | :----------- | :----------- | :--------------- | :------- |
| listener   | OnSpeedGetListener | -          | 获取速度监听 | yes          | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().getSpeed(new OnSpeedGetListener() {
    @Override
    public void getNaviSpeed(double v) {
        // 获取当前行走速度
    }
});
```

### 1.5.14 获取地图列表

#### 1 接口定义

| **方法**                                        | **功能描述** | **支持版本** | **备注** |
| :---------------------------------------------- | :----------- | :----------- | :------- |
| void getMapList(OnMapListListener listListener) | 获取地图列表 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**          | **默认值** | **释义**         | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :---------------- | :--------- | :--------------- | :----------- | :--------------- | :------- |
| listener   | OnMapListListener | -          | 获取地图列表监听 | yes          | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().getMapList(new OnMapListListener() {
    @Override
    public void response(String s) {
        BaseLogger.info("getMapList == >" + s);
    }
});
```

### 1.5.15 获取当前地图状态

#### 1 接口定义

| **方法**                                      | **功能描述**     | **支持版本** | **备注** |
| :-------------------------------------------- | :--------------- | :----------- | :------- |
| void getMapState(OnMapStateListener listener) | 获取当前地图状态 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**           | **默认值** | **释义**             | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :----------------- | :--------- | :------------------- | :----------- | :--------------- | :------- |
| listener   | OnMapStateListener | -          | 获取当前地图状态监听 | yes          | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().getMapState(new OnMapStateListener() {
    @Override
    public void mapState(String s) {
        JSONObject root = null;
        try {
            root = new JSONObject(s);
            boolean state = root.getBoolean("state");
            // state: true, 地图已经恢复
            // state: fase, 地图没有恢复
            String result = state ? getString(R.string.map_restored) : getString(R.string.map_
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
});
```

### 1.5.16 获取充电桩状态

#### 1 接口定义

| **方法**                                               | **功能描述**   | **支持版本** | **备注**   |
| :----------------------------------------------------- | :------------- | :----------- | :--------- |
| void getDockerState(OnRobotDockStateListener listener) | 获取充电桩状态 | -            | 几乎不使用 |

#### 2 参数说明

| **字段名** | **类型**                 | **默认值** | **释义**           | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :----------------------- | :--------- | :----------------- | :----------- | :--------------- | :------- |
| listener   | OnRobotDockStateListener | -          | 获取充电桩状态监听 | no           | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().getDockerState(new OnRobotDockStateListener() {
    @Override
    public void response(int state) {
        // 充电桩状态, state = 1 on dock; other not
    }
});
```

### 1.5.17 设置导航模式

#### 1 接口定义

| **方法**                   | **功能描述** | **支持版本** | **备注** |
| :------------------------- | :----------- | :----------- | :------- |
| void setNaviMode(int mode) | 设置导航模式 | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义** | **是否必选** | **最低支持版本** | **备注**                                                     |
| :--------- | :------- | :--------- | :------- | :----------- | :--------------- | :----------------------------------------------------------- |
| mode       | int      | -          | 导航模式 | yes          | 所有版本         | 0: 自由模式 <br />1: 轨道模式 <br />2: 轨道优先  <br />自由模式：表示自由规划路径，默认该模式； <br />轨道模式：表示需要画轨道，没有画轨道走不了，遇到障碍的时候不会绕行。在扫图软件中加载地图并绘制轨道； <br />轨道优先模式：表示在轨道模式的基础上，遇到障碍的时候会绕行； |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().setNaviMode(0);
```

### 1.5.18 查询目标点是否可以到达

#### 1 接口定义

| **方法**                                                     | **功能描述**           | **支持版本** | **备注** |
| :----------------------------------------------------------- | :--------------------- | :----------- | :------- |
| void destReachable(float x, float y, float rotation, OnDestReachableListener listener) | 查询目标点是否可以到达 | -            | -        |

#### 2 参数说明

| **字段名** | **类型**                | **默认值** | **释义**             | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :---------------------- | :--------- | :------------------- | :----------- | :--------------- | :------- |
| x          | float                   | -          | 坐标x                | yes          | 所有版本         | -        |
| y          | float                   | -          | 坐标y                | yes          | 所有版本         | -        |
| rotation   | int                     | -          | 旋转的角度           | yes          | 所有版本         | -        |
| listener   | OnDestReachableListener | -          | 目标点位是否到达监听 | yes          | -                | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getAction().destReachable(0, 0, 0, new OnDestReachableListener() {
    @Override
    public void destReachable(boolean reachable) {

    }
});
```

### 1.5.19 肢体动作控制

Timo

TimoActionHeadLeftRightCtrl(int angle)&#x20;

表示头部左右运动的角度比例：0-100（%），左边为 0，右边为 100；

TimoActionHeadUpDownCtrl(int angle)

表示头部上下运动的角度比例：0-100（%），下部为 0，上部为 100；

TimoActionLeftHandCtrl(int angle)

表示左臂上下运动的角度比例：0-100（%），下部为 0，上部为 100；

TimoActionRightHandCtrl(int angle)

表示右臂上下运动的角度比例：0-100（%），下部为 0，上部为 100；

TimoActionCustomerCtrl(int headLeft, int headUp, int lefthand, int righthand)

自定义各部动作

TimoActionReset()

重置动作



Alice动作

参照Timo动作，将前缀Timo换成AliceNew即可

## 1.6 机器人状态

功能实例类名：getState()

### 1.6.1 重启机器人

#### 1 接口定义

| **方法** | **功能描述** | **支持版本** | **备注** |
| :------- | :----------- | :----------- | :------- |
| reboot() | 重启机器人   | 所有版本     | -        |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().reboot();
```

### 1.6.2 获取电量

#### 1 接口定义

| **方法**                                  | **功能描述** | **支持版本** | **备注** |
| :---------------------------------------- | :----------- | :----------- | :------- |
| getBattery(OnRobotStateListener listener) | 获取电量     | 所有版本     | -        |

#### 2 参数说明

| **字段名** | **类型**             | **默认值** | **释义**                               | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------------- | :--------- | :------------------------------------- | :----------- | :--------------- | :------- |
| listener   | OnRobotStateListener | -          | 机器人状态监听，用于获取电量和充电状态 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().getBattery(new OnRobotStateListener() {
    @Override
    public void getBattery(int i) {
        // Power value returned
    }

    @Override
    public void getCharge(int i) {

    }
});
```

### 1.6.3 获取充电状态

getCharge(OnRobotStateListener listener)

#### 1 接口定义

| **方法**                                 | **功能描述** | **支持版本** | **备注** |
| :--------------------------------------- | :----------- | :----------- | :------- |
| getCharge(OnRobotStateListener listener) | 获取充电状态 | 所有版本     | -        |

#### 2 参数说明

| **字段名** | **类型**             | **默认值** | **释义**                               | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------------- | :--------- | :------------------------------------- | :----------- | :--------------- | :------- |
| listener   | OnRobotStateListener | -          | 机器人状态监听，用于获取电量和充电状态 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().getCharge(new OnRobotStateListener() {
    @Override
    public void getBattery(int i) {

    }

    @Override
    public void getCharge(int i) {
        // State of charge returned
    }
});
```

### 1.6.4 获取机器人主控板信息

#### 1 接口定义

| **方法**                                       | **功能描述**         | **支持版本** | **备注** |
| :--------------------------------------------- | :------------------- | :----------- | :------- |
| checkSelf(OnWarningCheckSelfListener listener) | 获取机器人主控板信息 | 所有版本     | -        |

#### 2 参数说明

| **字段名** | **类型**                   | **默认值** | **释义**             | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :------------------------- | :--------- | :------------------- | :----------- | :--------------- | :------- |
| listener   | OnWarningCheckSelfListener | -          | 获取机器人主控板信息 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().checkSelf(new OnWarningCheckSelfListener() {
    @Override
    public void response(String s) {
        // Return inspection information of robot
    }
});
```

数据示例：【随着sdk的更新，该数据可能会新增内容，请以实际为准】

```java
{
    "firmwareversion": "V2.0.0.5",
    "model": "GD32F470",
    "serialnumber": "17C8F9F9",
    "state": "OK",
    "type": "X_Main_Board"
}
```



### 1.6.5 获取导航版本

#### 1 接口定义

| **方法**                                               | **功能描述**                   | **支持版本** | **备注** |
| :----------------------------------------------------- | :----------------------------- | :----------- | :------- |
| setSlamVersionListener(OnSlamVersionListener listener) | 设置导航版本监听，获取导航版本 | 所有版本     | -        |

#### 2 参数说明

| **字段名** | **类型**              | **默认值** | **释义**         | **是否必选** | **最低支持版本** | **备注** |
| :--------- | :-------------------- | :--------- | :--------------- | :----------- | :--------------- | :------- |
| listener   | OnSlamVersionListener | -          | 获取导航版本监听 | yes          | 所有版本         | -        |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().setSlamVersionListener(new OnSlamVersionListener() {
    @Override
    public void response(String json) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            firmwareversion = jsonObject.getString("firmwareversion");
            sdkVersionCode = jsonObject.getInt("sdkVersionCode");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
});
```

### 1.6.6 获取急停状态监听

#### 1 接口定义

| **方法**                                               | **功能描述** | **支持版本** | **备注** |
| :----------------------------------------------------- | :----------- | :----------- | :------- |
| getEmergencyStatus(OnEmergencyStatusListener listener) | 获取急停状态 | 所有版本     | -        |

#### 2 参数说明

| **字段名** | **类型**                  | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注**                      |
| :--------- | :------------------------ | :--------- | :----------- | :----------- | :--------------- | :---------------------------- |
| listener   | OnEmergencyStatusListener | -          | 急停状态监听 | yes          | 所有版本         | 0: 按下急停 <br />1: 解除急停 |

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().getEmergencyStatus(new OnEmergencyStatusListener(){
    @Override
    public void response(int status){

    }
});
```

### 1.6.7 自恢复急停按钮释放急停

#### 1 接口定义

| **方法**                | **功能描述** | **支持版本** | **备注**                                                     |
| :---------------------- | :----------- | :----------- | :----------------------------------------------------------- |
| void releaseEmergency() | 释放急停     | -            | 用于自恢复急停按钮触发之后，整机处于急停状态无法恢复，需要从 Android 端下发指令才可以使得机器人恢复正常运行。 |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

```java
CsjRobot.getInstance().getState().releaseEmergency();
```

### 1.6.8.1 关于飞毛腿机器人开关门控制 - 打开所有舱门

本机器人用不到

### 1.6.8.2 关于飞毛腿机器人开关门控制 - 关闭所有舱门

本机器人用不到

### 1.6.8.3 关于飞毛腿机器人开关门控制 - 打开上舱门

本机器人用不到

### 1.6.8.4 关于飞毛腿机器人开关门控制 - 关闭上舱门

本机器人用不到

### 1.6.8.5 关于飞毛腿机器人开关门控制 - 打开下舱门

本机器人用不到

### 1.6.8.6 关于飞毛腿机器人开关门控制 - 关闭下舱门

本机器人用不到

### 1.6.9 多模态检测是否有人接口(Multimodal detection for presence detection interface)

#### 1 接口定义

| **方法**                                           | **功能描述**     | **支持版本** | **备注**                                                     |
| :------------------------------------------------- | :--------------- | :----------- | :----------------------------------------------------------- |
| void registerFaceListener(OnFaceListener listener) | 注册是否有人接口 | -            | 多模态检测是否有人 <br />Multimodal detection for identifying whether there people |

#### 2 参数说明

无

#### 3 返回值

无

#### 4 调用示例

`````
// 1. 开启多模态，激光的覆盖区域为180度，小鱼机器人胸前的超声波检测比较准确，一般只设置超声波
// 1. Enable multimodal mode. The laser coverage area is 180 degrees. The ultrasonic detection on the front of the small fish robot is relatively accurate. Generally, only ultrasonic detection is set.
CsjRobot.getInstance().setPersonCheckType(false, false, true);

// 2. 设置检测距离，默认2米
// 2. Set the detection distance to the default value of 2 meters.
CsjRobot.getInstance().setPersonCheckDistance(2.0);

// 3. 设置是否有人的回调
// 3. Set whether to have a callback for someone's action
CsjRobot.getInstance().registerFaceListener(new OnFaceListener() {
    @Override
    public void personInfo(String json) {
        //废弃
    }

    @Override
    public void personNear(boolean person) {
        BaseLogger.info("personNear, 是否有人: " + person);
    }
});
`````



### 1.6.10 定时开关机

#### 1 接口定义

| **方法**                                                     | **功能描述** | **支持版本** | **备注** |
| :----------------------------------------------------------- | :----------- | :----------- | :------- |
| void timingPowerOffForXRobot(boolean isOpen, String year, int month, int day, int hour, int minute, int second) | 定时开关机   | -            | -        |

#### 2 参数说明

| **字段名** | **类型** | **默认值** | **释义**     | **是否必选** | **最低支持版本** | **备注**                                                     |
| :--------- | :------- | :--------- | :----------- | :----------- | :--------------- | :----------------------------------------------------------- |
| isOpen     | boolean  | -          | 是否定时开机 | yes          | 所有版本         | true: 定时开机 <br />false: 表示机器人断电关机，其他值可以随便设置 |
| year       | String   | -          | 年           | yes          | 所有版本         | -                                                            |
| month      | int      | -          | 月           | yes          | 所有版本         | -                                                            |
| day        | int      | -          | 日           | yes          | 所有版本         | -                                                            |
| hour       | int      | -          | 时           | yes          | 所有版本         | -                                                            |
| minute     | int      | -          | 分           | yes          | 所有版本         | -                                                            |
| second     | int      | -          | 秒           | yes          | 所有版本         | -                                                            |

#### 3 返回值

无

#### 4 调用示例

```java
// 开机
Calendar cal = Calendar.getInstance();
String year = String.valueOf(cal.get(Calendar.YEAR));
String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
int day = cal.get(Calendar.DAY_OF_MONTH);
BaseLogger.info("定时开机: 今日开机时间: " + year + "年" + month + "月" + day + "日" + 8 + "时");
ReceptionRobot.getInstance().timingPowerOffForXRobot(true, year, Integer.parseInt(month), day, 
// 关机
CsjRobot.getInstance().getState().timingPowerOffForXRobot(false, "00", 1, 2, 3, 4, 0);
```

### 定时开关机最佳实践

第一步：软件启动校准时间

机器人时间需要准确，24H制

setXRobotTime(String year, String month, String day, String hour, String minute, String second, String week)

第二步：根据自定义的定时关机时间设置系统闹钟

第三步：设置的闹钟时间到接收到广播后，调用下列方式实现下次自动开机

timingPowerOffForXRobot(boolean isOpen, String year, int month, int day, int hour, int minute, int second)

| **参数** | **说明**                                                     |
| :------- | :----------------------------------------------------------- |
| isOpen   | true时，下面的参数需要正确设置，否则影响下次自动开机，为false时,下列参数随意设置，但不可为空，表示机器人断电关机 |
| year     | 下次自动开机年份                                             |
| month    | 下次自动开机月份                                             |
| day      | 下次自动开机日                                               |
| hour     | 下次自动开机时                                               |
| minute   | 下次自动开机分                                               |
| second   | 下次自动开机秒                                               |

自动开机误差在30s内

# 常见问题

1.初始化后报如下错误

```text
java.lang.NullPointerException:
Attempt to invoke virtual method 'java.lang.String android.os.BaseBundle.getString(java.lang.S
at com.csjbot.coshandler.service.HandlerMsgService.getRemoteAppName(HandlerMsgService.java:204
at com.csjbot.coshandler.service.HandlerMsgService.access$300(HandlerMsgService.java:42)
at com.csjbot.coshandler.service.HandlerMsgService$4.onServiceConnected(HandlerMsgService.java
```

参考：[配置AndroidManifest](https://www.yuque.com/cll520/public/vg0f243qz7oso695#LGxDe)

