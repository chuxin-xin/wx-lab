# wx-lab

个人 Xposed 机制折腾：微信 8.0.76 锚点流水线 POC（纯观测 · 零网络 · 零收集）

> **先读 [DISCLAIMER.md](DISCLAIMER.md) 再玩**：自行组装着玩，风险自担，强烈建议用测试小号。

## 它是什么
LSPosed 模块，验证"版本表 → 类名解析 → hook 安装 → 事件日志"这条锚点流水线在微信 8.0.76 上是否全通。
v0.1 只做**观测日志**：不修改任何数据、不拦截任何行为、不发任何网络请求。

| slot | 8.0.76 类 | 方法 | 观测 |
|---|---|---|---|
| 系统消息解析 | com.tencent.mm.sdk.platformtools.aa | d(String,String,String) | 系统消息解析；含"撤回"打 ★RECALL★ |
| 朋友圈 DB | ka5.b0, ka5.z | A / H | 朋友圈 DB 操作日志 |

## 安装测试
1. root + LSPosed 的手机，安装 `wxpoc.apk`
2. LSPosed → 启用本模块 → scope 勾微信 → 重启微信
3. 看日志：`cat /data/data/com.chuxin.wxpoc/files/poc.log`
   期望出现 `HOST wechat versionName=8.0.76` 和 `HOOK ...` / `REPORT hooks=N`
4. 玩：发一条朋友圈再删；让朋友发条消息再撤回（各 30 秒）

## 上报（贴 issue，每号一行）
```
wxpoc=0.1.0 | wechat=8.0.76 | 号龄=xx天 | 号型=主/小 | 观察=14天 | 结果=正常/限流/封号 | hooks=N
```
只填数字，**不发截图/昵称/openid 等任何个人信息**。

## 构建
`sh build.sh`（宿主需 JDK17 + Google build-tools r36，详见脚本头注释；产物自签 v1+v2）

## 许可
MIT，见 [LICENSE](LICENSE)。**使用后果与作者无关，详见 DISCLAIMER。**
