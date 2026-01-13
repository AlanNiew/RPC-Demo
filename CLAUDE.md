# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

这是一个 RPC 框架的渐进式学习项目，通过多个版本展示从基础到生产级 RPC 框架的演进过程。每个版本都是独立完整的，可以在前一个版本基础上理解新特性。

**技术栈：**
- Java 17
- Netty 4.1.128.Final（NIO 网络通信）
- Lombok 1.18.20
- Maven 构建工具

## Build and Run Commands

```bash
# 编译项目
mvn compile

# 运行指定版本的服务端/客户端
mvn exec:java -Dexec.mainClass="com.alan.rpc.v{version}.demo.ServerMain"
mvn exec:java -Dexec.mainClass="com.alan.rpc.v{version}.demo.ClientMain"

# v3 版本需要先启动注册中心
mvn exec:java -Dexec.mainClass="com.alan.rpc.v3.demo.RegistryMain"
```

## Architecture Evolution

### Version Structure

每个版本都有独立的包结构 `com.alan.rpc.v{version}`，包含：
- `common/` - 通用组件（RpcRequest、RpcResponse、Serializer）
- `provider/` - 服务提供者
- `consumer/` - 服务消费者
- `demo/` - 演示服务接口和实现
- `v3/registry/` - 服务注册中心（仅 v3）

### Key Evolution Points

**v1 → v2：动态代理**
- v1: `rpcClient.invoke(interfaceName, methodName, parameterTypes, parameters)` - 手动指定调用参数
- v2: `userService.getUserName(1001)` - 通过 JDK 动态代理实现像本地方法一样的调用体验
- 核心：`Proxy.newProxyInstance()` + `InvocationHandler`

**v2 → v3：服务注册中心**
- v2: 服务地址硬编码在客户端
- v3: 引入注册中心（RegistryServer 端口 9000），支持服务自动注册、发现、心跳检测
- 架构变化：Provider 启动时注册到注册中心，Consumer 从注册中心发现服务

## Netty Communication Patterns

所有版本的 RPC 通信都基于 Netty，核心模式：

1. **ByteBuf 处理**：Netty 传递的是 ByteBuf，需要与 byte[] 相互转换
   ```java
   // 读取
   io.netty.buffer.ByteBuf buf = (io.netty.buffer.ByteBuf) msg;
   byte[] data = new byte[buf.readableBytes()];
   buf.readBytes(data);
   buf.release(); // 记得释放
   ```

2. **序列化**：使用 Java 原生序列化
   - `Serializer.serialize(obj)` - 对象转 byte[]
   - `Serializer.deserialize(bytes)` - byte[] 转对象

3. **同步调用实现**：使用 CountDownLatch 等待响应
   ```java
   CountDownLatch latch = new CountDownLatch(1);
   // ... 发送请求
   latch.await(); // 阻塞等待响应
   ```

## Registry Center Protocol (v3)

注册中心使用 Socket + ObjectInputStream/ObjectOutputStream 通信：

**协议格式：**
1. 客户端发送请求类型字符串：`"REGISTER" | "DISCOVER" | "DEREGISTER" | "HEARTBEAT"`
2. 后续参数按 `writeObject()` 顺序发送
3. 服务端按相同顺序 `readObject()` 读取

**注意：** 所有传输对象必须实现 `Serializable` 接口（如 `ServiceInstance`）

## Version-Specific Notes

### v1 - 基础版
- 直接调用，需要手动指定接口名、方法名、参数类型
- 适合理解 RPC 基本原理

### v2 - 动态代理版
- 使用 JDK 动态代理简化调用
- `RpcInvocationHandler` 拦截方法调用并转换为 RPC 请求

### v3 - 注册中心版
- **三个组件协同工作**：注册中心(9000)、服务提供者(8080)、服务消费者
- Provider 启动时自动注册，每 10 秒发送心跳
- Consumer 每次调用前从注册中心发现服务实例（30秒过期）
- `ServiceInstance` 必须实现 `Serializable`

## Port Usage

| 组件 | 端口 | 说明 |
|------|------|------|
| RegistryServer | 9000 | 注册中心（仅 v3） |
| RpcServer | 8080 | RPC 服务提供者（所有版本） |

## Testing a Version

1. 编译：`mvn compile`
2. 启动服务端（v3 需要先启动注册中心）
3. 启动客户端查看调用结果
4. 查看控制台输出的交互日志

## Future Versions (Planned)

- **v4**: 多序列化支持（JSON、Hessian、Kryo）
- **v5**: 负载均衡策略
- **v6**: 高可用特性（重试、熔断、限流）
- **v7**: 生产级完整特性
