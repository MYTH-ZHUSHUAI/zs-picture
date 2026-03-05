package com.zhushuai.zspicturebackend.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 测试 Redis 连接是否可用
     */
    @Test
    public void testRedisConnection() {
        // 测试连接
        assertNotNull(redisTemplate);
        
        // 测试基本的增删改查
        String testKey = "test:connection";
        String testValue = "Redis连接成功-" + System.currentTimeMillis();
        
        // 1. 增
        redisTemplate.opsForValue().set(testKey, testValue, 60, TimeUnit.SECONDS);
        
        // 2. 查
        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        assertEquals(testValue, retrievedValue);
        System.out.println("✅ 连接测试通过，获取值: " + retrievedValue);
        
        // 3. 删
        Boolean deleted = redisTemplate.delete(testKey);
        assertTrue(deleted);
        
        // 4. 确认已删除
        assertNull(redisTemplate.opsForValue().get(testKey));
        System.out.println("✅ 删除测试通过");
    }

    /**
     * 测试 String 类型操作
     */
    @Test
    public void testStringOperations() {
        String key = "test:string";
        
        // 设置值并带过期时间
        redisTemplate.opsForValue().set(key, "Hello Redis", 5, TimeUnit.MINUTES);
        
        // 获取值
        String value = redisTemplate.opsForValue().get(key);
        System.out.println("String值: " + value);
        assertEquals("Hello Redis", value);
        
        // 递增操作
        String countKey = "test:count";
        redisTemplate.opsForValue().set(countKey, "0");
        Long newVal = redisTemplate.opsForValue().increment(countKey);
        System.out.println("递增后: " + newVal);
        assertEquals(1L, newVal);
        
        // 清理
        redisTemplate.delete(Arrays.asList(key, countKey));
    }

    /**
     * 测试 Hash 类型操作（存储对象）
     */
    @Test
    public void testHashOperations() {
        String key = "test:user:1";
        
        // 存储用户对象
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", "1");
        userMap.put("name", "张三");
        userMap.put("age", "25");
        userMap.put("email", "zhangsan@example.com");
        
        redisTemplate.opsForHash().putAll(key, userMap);
        
        // 获取整个对象
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        System.out.println("Hash对象: " + entries);
        assertEquals("张三", entries.get("name"));
        
        // 获取单个字段
        String name = (String) redisTemplate.opsForHash().get(key, "name");
        System.out.println("用户名: " + name);
        
        // 更新字段
        redisTemplate.opsForHash().put(key, "age", "26");
        
        // 删除字段
        redisTemplate.opsForHash().delete(key, "email");
        
        // 清理
        redisTemplate.delete(key);
    }

    /**
     * 测试 List 类型操作
     */
    @Test
    public void testListOperations() {
        String key = "test:queue";
        
        // 清空列表（如果存在）
        redisTemplate.delete(key);
        
        // 从右侧入队
        redisTemplate.opsForList().rightPush(key, "任务1");
        redisTemplate.opsForList().rightPush(key, "任务2");
        redisTemplate.opsForList().rightPush(key, "任务3");
        
        // 从左侧入队（高优先级）
        redisTemplate.opsForList().leftPush(key, "紧急任务");
        
        // 获取列表范围
        List<String> range = redisTemplate.opsForList().range(key, 0, -1);
        System.out.println("队列内容: " + range);
        assertEquals(4, range.size());
        
        // 从左侧出队（消费）
        String first = redisTemplate.opsForList().leftPop(key);
        System.out.println("消费: " + first);
        assertEquals("紧急任务", first);
        
        // 清理
        redisTemplate.delete(key);
    }

    /**
     * 测试 Set 类型操作（去重、判断存在）
     */
    @Test
    public void testSetOperations() {
        String key = "test:tags";
        
        // 添加成员
        redisTemplate.opsForSet().add(key, "java", "spring", "redis", "java"); // java重复，只会存一次
        
        // 获取所有成员
        Set<String> members = redisTemplate.opsForSet().members(key);
        System.out.println("标签集合: " + members);
        assertEquals(3, members.size()); // java只算一个
        
        // 判断是否包含
        Boolean isMember = redisTemplate.opsForSet().isMember(key, "spring");
        assertTrue(isMember);
        
        // 移除成员
        redisTemplate.opsForSet().remove(key, "redis");
        
        // 获取集合大小
        Long size = redisTemplate.opsForSet().size(key);
        System.out.println("集合大小: " + size);
        
        // 清理
        redisTemplate.delete(key);
    }

    /**
     * 测试 Sorted Set 类型操作（排行榜）
     */
    @Test
    public void testSortedSetOperations() {
        String key = "test:ranking";
        
        // 添加分数和成员
        redisTemplate.opsForZSet().add(key, "张三", 100);
        redisTemplate.opsForZSet().add(key, "李四", 85);
        redisTemplate.opsForZSet().add(key, "王五", 95);
        
        // 获取排名（按分数从高到低）
        Set<ZSetOperations.TypedTuple<String>> top2 = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, 1);
        System.out.println("前2名: ");
        top2.forEach(tuple -> 
            System.out.println("  " + tuple.getValue() + ": " + tuple.getScore())
        );
        
        // 获取指定成员的排名
        Long rank = redisTemplate.opsForZSet().reverseRank(key, "王五");
        System.out.println("王五排名: 第" + (rank + 1) + "名");
        
        // 增加分数
        redisTemplate.opsForZSet().incrementScore(key, "李四", 10);
        
        // 清理
        redisTemplate.delete(key);
    }

    /**
     * 测试过期时间
     */
    @Test
    public void testExpire() throws InterruptedException {
        String key = "test:expire";
        
        // 设置值并指定3秒后过期
        redisTemplate.opsForValue().set(key, "临时数据", 3, TimeUnit.SECONDS);
        
        // 立即查询
        assertNotNull(redisTemplate.opsForValue().get(key));
        System.out.println("3秒内存在: " + redisTemplate.opsForValue().get(key));
        
        // 等待4秒
        Thread.sleep(4000);
        
        // 确认已过期
        assertNull(redisTemplate.opsForValue().get(key));
        System.out.println("✅ 过期测试通过，key已自动删除");
    }

    /**
     * 测试批量操作（Pipeline）
     */
    @Test
    public void testBatchOperations() {
        List<String> keys = new ArrayList<>();
        
        // 批量设置
        for (int i = 0; i < 100; i++) {
            String key = "test:batch:" + i;
            keys.add(key);
            redisTemplate.opsForValue().set(key, "value-" + i, 1, TimeUnit.MINUTES);
        }
        
        // 批量查询
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        System.out.println("批量查询前5个: " + values.subList(0, 5));
        
        // 批量删除
        Long deletedCount = redisTemplate.delete(keys);
        System.out.println("批量删除数量: " + deletedCount);
        assertEquals(100L, deletedCount);
    }

    /**
     * 测试分布式锁（简单版）
     */
    @Test
    public void testDistributedLock() {
        String lockKey = "test:lock:resource";
        String lockValue = UUID.randomUUID().toString();
        
        // 尝试获取锁（10秒过期）
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
        
        assertTrue(acquired);
        System.out.println("✅ 锁获取成功");
        
        // 释放锁（使用Lua脚本保证原子性，这里简化处理）
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
            System.out.println("✅ 锁释放成功");
        }
    }
}
