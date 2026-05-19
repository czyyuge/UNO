# UNO 联机对战游戏

基于 Java Swing + Socket 的 UNO 多人联机对战游戏，支持房间管理、实时聊天和完整的 UNO 规则。

## 功能特性

- **多人联机**：支持最多 4 人实时对战
- **房间系统**：创建房间、加入房间、准备就绪、自动开始
- **完整规则**：出牌、摸牌、喊 UNO、万能牌变色、反转、跳过、+2/+4 叠加惩罚
- **实时聊天**：房间内玩家可文字聊天
- **图形界面**：基于 Swing 的跨平台 GUI

## 技术栈

- Java 8+
- Java Swing（GUI）
- Java 原生 Socket（TCP）
- Java 序列化（Object Stream）

## 项目结构

```
src/com/uno/
├── client/           # 客户端网络模块
│   ├── Client.java
│   └── ClientListener.java
├── server/           # 服务器网络模块
│   ├── Server.java
│   ├── ClientHandler.java
│   └── RoomManager.java
├── model/            # 数据模型
│   ├── Card.java
│   ├── Player.java
│   ├── GameRoom.java
│   └── Message.java
├── game/             # 游戏核心逻辑
│   ├── GameLogic.java
│   └── CardDeck.java
├── ui/               # Swing 图形界面
│   ├── LoginFrame.java
│   ├── RoomFrame.java
│   └── GameFrame.java
└── util/             # 工具类
    ├── GameConstants.java
    ├── CardUtils.java
    └── MessageUtils.java
```

## 快速开始

### 环境要求

- JDK 8 或更高版本
- IntelliJ IDEA（推荐，项目已配置 `.iml` 模块文件）

### 编译运行

#### 方式一：命令行

```bash
# 编译
cd src
javac -d ../out com/uno/**/*.java

# 启动服务器
cd ../out
java com.uno.server.Server

# 启动客户端（新终端窗口）
java com.uno.ui.LoginFrame
```

#### 方式二：IntelliJ IDEA

1. 打开项目根目录
2. 运行 `Server.java` 启动服务器
3. 运行 `LoginFrame.java` 启动客户端

## 联机教程

### 同一局域网联机

1. **启动服务器**
   - 在充当服务器的电脑上运行 `Server.java`
   - 默认监听端口 `8888`

2. **查看服务器 IP**
   - **Windows**：命令行执行 `ipconfig`，查看 `IPv4 地址`（如 `192.168.1.5`）
   - **Mac/Linux**：终端执行 `ifconfig` 或 `ip addr`

3. **客户端连接**
   - 其他电脑运行 `LoginFrame.java`
   - 在登录界面填写：
     - **Server IP**：服务器电脑的局域网 IP（如 `192.168.1.5`）
     - **Port**：`8888`
     - **Username**：你的用户名
   - 点击 **Connect**

4. **开始游戏**
   - 一名玩家创建房间
   - 其他玩家从房间列表加入
   - 所有玩家点击 **Ready**
   - 人满且全部就绪后自动开始

### 跨网络联机（不同 Wi-Fi/城市）

程序使用直连 Socket，若不在同一局域网，需要借助内网穿透工具：

- [frp](https://github.com/fatedier/frp)
- [ngrok](https://ngrok.com/)
- 花生壳 / 向日葵

将服务器电脑的 `8888` 端口映射到公网，客户端填写映射后的公网地址即可。

## 游戏规则速查

| 卡牌 | 效果 |
|------|------|
| 数字牌（0-9）| 与上一张颜色或数字相同即可打出 |
| 跳过（Skip） | 跳过下家回合 |
| 反转（Reverse） | 改变出牌方向 |
| +2（Draw Two） | 下家摸 2 张牌，可叠加 |
| 万能牌（Wild） | 可改变当前颜色 |
| +4（Wild Draw Four） | 改变颜色，下家摸 4 张牌，可叠加 |

- 手牌剩 1 张时必须点击 **UNO**，否则被抓到罚摸 2 张
- 最先出完手牌的玩家获胜

## 注意事项

- 服务器与客户端版本需保持一致
- 确保服务器电脑的防火墙已放行 `8888` 端口，或临时关闭防火墙测试
- 游戏数据保存在内存中，服务器重启后房间和玩家数据将清空
- 默认序列化协议为 Java 原生 Object Stream，不支持与其他语言客户端互通

## 默认配置

```java
默认端口：8888
最大玩家数：4
初始手牌数：7
```

## 启动类

| 角色 | 主类 |
|------|------|
| 服务器 | `com.uno.server.Server` |
| 客户端 | `com.uno.ui.LoginFrame` |
