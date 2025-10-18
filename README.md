# FastCache 分布式多级缓存框架

## 简介

在典型的 C 端业务场景中，为提升用户体验与响应速度，我们通常会引入 Redis 作为缓存，甚至结合 Caffeine 作为本地二级缓存实现多级缓存设计。

这也带来了一系列问题，围绕数据的一致性问题、缓存穿透、缓存雪崩等问题，常常需要写重复的代码，即繁琐又容易将代码写成“屎山”。

随着用户规模增长与服务横向扩展到分布式集群，随之而来的是更多的问题。

尽管市面上有不少比较成熟的框架，但我们认为在实际体验上还远远不够，于是 FastCache 应运而生。

FastCache 是一个基于 Java 实现的分布式多级缓存框架，它吸收了业界优秀的实践，正如它的名字所表示的，我们希望能够打造一个快捷、高效、易用的缓存框架。

## 特性

- 支持多种缓存类型，包括本地缓存、分布式缓存和多级缓存，方便根据不同业务场景选择最合适的缓存方案
- 支持多种缓存框架，比如本地缓存支持 Caffeine、Guava 等，分布式缓存支持 Redisson、Spring Data Redis 等，开发者可以直接在现有项目中快速集成
- 提供丰富的缓存配置，支持缓存过期时间、缓存大小限制、缓存刷新策略、键名转换、值序列化方式等等
- 保证缓存的数据一致性，通过广播通知的同步策略，降低分布式场景下的数据不一致风险
- 支持缓存自动刷新机制，缓存数据在指定时间内没有被访问，则会自动刷新，防止缓存失效时造成的缓存雪崩
- 支持 Spring Boot Starter 自动配置，一键集成，开箱即用
- ……

## 快速开始

### 一、引入依赖

FastCache 提供了 `fast-cache-bom` 模块进行依赖管理，这是我们比较推荐的方式，只需在 `<dependencyManagement>` 标签下配置即可。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.floseek</groupId>
            <artifactId>fast-cache-bom</artifactId>
            <version>1.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

之后，在使用时就可以不指定 `<version>` 标签，直接引入 FastCache 的依赖了。

```xml
<dependencies>
    <dependency>
        <groupId>cn.floseek</groupId>
        <artifactId>fast-cache-redisson-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

此外，FastCache 目前支持引入的依赖说明如下：

- fast-cache-core：缓存核心模块
- fast-cache-autoconfigure：缓存自动配置模块
- fast-cache-redisson：Redisson 缓存模块
- fast-cache-redisson-spring-boot-starter：Redisson 缓存自动配置模块

### 二、定义缓存键枚举

很多开发者在使用缓存时，都会定义一个枚举类，目的是方便对缓存键进行管理。

FastCache 基于这种方式的基础上进行扩展，以支持更多的能力，当然您也可以选择不使用枚举，具体的请参考官方文档。

```java
@Getter
@RequiredArgsConstructor
public enum CacheKeyEnum implements BaseCacheKeyEnum {

    /**
     * 用户信息
     */
    USER_INFO("user_info", Duration.ofMillis(7 * 24 * 60 * 60 * 1000), Duration.ofMillis(5 * 60 * 1000L));

    /**
     * 名称
     */
    private final String name;

    /**
     * 过期时间
     */
    private final Duration expireTime;

    /**
     * 本地缓存过期时间
     */
    private final Duration localExpireTime;

}
```

### 三、定义缓存服务

FastCache 提供了多种使用方式，这里介绍一种比较简单的方式。

为什么要定义缓存服务呢？主要是为了让分层更加清晰，避免与业务逻辑混在一起，这也是比较符合 DDD（领域驱动设计）的一种设计。

1）定义缓存服务接口

首先需要定义一个缓存服务接口，并继承 `CacheService` 接口，指定泛型为主键类型和缓存值类型。

```java
public interface UserCacheService extends CacheService<Long, User> {

}
```

2）定义缓存服务实现

接着写具体的实现类，继承 `AbstractCacheService` 类，指定泛型为主键类型和缓存值类型，同时实现 `UserCacheService` 接口。

这里主要是重写 `cacheKeyEnum()` 和 `cacheType()` 方法，用于指定缓存键枚举和缓存类型。

您还可以重写其它方法，比如可以重写 `query()` 和 `queryAll()` 方法，实现从数据库加载数据；重写 `syncMode()`方法，指定缓存同步模式，等等。

```java
@Service
public class UserCacheServiceImpl extends AbstractCacheService<Long, User> implements UserCacheService {

    @Resource
    private UserMapper userMapper;

    protected UserCacheServiceImpl(CacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected BaseCacheKeyEnum cacheKeyEnum() {
        return CacheKeyEnum.USER_INFO;
    }

    @Override
    protected CacheType cacheType() {
        return CacheType.MULTI_LEVEL;
    }

    @Override
    protected Function<Long, User> query() {
        return userId -> userMapper.selectOneById(userId);
    }

    @Override
    protected Function<Collection<Long>, Map<Long, User>> queryAll() {
        return userIds -> {
            List<User> userList = userMapper.selectListByIds(userIds);
            if (CollUtil.isEmpty(userList)) {
                return Collections.emptyMap();
            }

            return userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        };
    }

}
```

### 使用缓存服务

最后，您就可以在业务代码中调用缓存服务进行相关的操作了。

```java
@Repository
public class UserRepository {

    @Resource
    private UserCacheService userCacheService;

    public User queryUserById(Long userId) {
        return userCacheService.get(userId);
    }
}
```
