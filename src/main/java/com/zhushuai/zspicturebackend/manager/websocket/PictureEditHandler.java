package com.zhushuai.zspicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditRequestMessage;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditResponseMessage;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditActionEnum;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditMessageTypeEnum;
import com.zhushuai.zspicturebackend.service.PictureEditService;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author zhushuai
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private ExecutorService wsSendExecutor;

    @Resource
    private UserService userService;

    @Resource
    private PictureEditService pictureEditService;

    // pictureId -> sessions
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();


    // 对新加入的连接进行操作
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);

        // 构造响应
        PictureEditResponseMessage pictureEditResponseMessage = pictureEditService.buildEnterEditResponse(user);

        // 广播给同一张图片的所有用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);
    }


    /**
     * 处理用户断开连接
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus status) throws Exception {

        // 得到属性
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");

        // 从编辑状态中移除
        pictureEditService.removeEditor(pictureId);

        // 删除会话
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (!webSocketSessions.isEmpty()) {
            webSocketSessions.remove(session);
        }

        PictureEditResponseMessage pictureEditResponseMessage = pictureEditService.buildExitEditResponse(user);

        // 发送消息
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 处理发送过来的动作
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session,
                                  @NonNull TextMessage message) throws Exception {

        // 将请求转为 PictureEditRequestMessage 对象
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);

        // 得到请求类型，获得编辑的枚举类
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum editEnum = PictureEditMessageTypeEnum.getEnumByValue(type);

        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long pictureId = (Long) attributes.get("pictureId");

        // 直接在当前线程处理，不通过 disruptor
        handleTextMessageInternal(session, pictureEditRequestMessage, user, pictureId);
    }

    /**
     * 内部处理消息方法
     */
    private void handleTextMessageInternal(WebSocketSession session,
                                           PictureEditRequestMessage pictureEditRequestMessage,
                                           User user,
                                           Long pictureId) throws Exception {
        // 得到请求类型，获得编辑的枚举类
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum editEnum = PictureEditMessageTypeEnum.getEnumByValue(type);

        Map<String, Object> attributes = session.getAttributes();
        Long editingUserId = pictureEditService.getCurrentEditor(pictureId);

        // 根据类型处理
        switch (type) {
            case "ENTER_EDIT":
                handleEnterEditMessageInternal(pictureEditRequestMessage, session, pictureId, user);
                break;
            case "EXIT_EDIT":
                handleExitEditMessageInternal(pictureEditRequestMessage, session, pictureId, user);
                break;
            case "EDIT_ACTION":
                handleEditActionMessageInternal(pictureEditRequestMessage, session, pictureId, user);
                break;
        }
    }

    /**
     * 处理编辑动作（内部实现）
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     */
    private void handleEditActionMessageInternal(PictureEditRequestMessage pictureEditRequestMessage,
                                        @NonNull WebSocketSession session,
                                        Long pictureId,
                                        User user) throws Exception {

        Long editingUserId = pictureEditService.getCurrentEditor(pictureId);
        String type = pictureEditRequestMessage.getType();
        String editAction = pictureEditRequestMessage.getEditAction();

        // 得到操作的枚举类型
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return;
        }

        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = pictureEditService.buildEditActionResponse(type, editAction, user);
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 处理退出编辑的请求（内部实现）
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     */
    private void handleExitEditMessageInternal(PictureEditRequestMessage pictureEditRequestMessage,
                                      @NonNull WebSocketSession session,
                                      Long pictureId,
                                      User user) throws Exception {
        Long editingUserId = pictureEditService.getCurrentEditor(pictureId);

        // 如果正在编辑的用户和当前用户一致，则发送退出消息
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditService.removeEditor(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = pictureEditService.buildExitEditResponse(user);
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 处理进入编辑的请求（内部实现）
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     */
    private void handleEnterEditMessageInternal(PictureEditRequestMessage pictureEditRequestMessage,
                                       @NonNull WebSocketSession session,
                                       Long pictureId,
                                       User user) throws Exception {

        // 尝试获取编辑锁
        boolean acquiredLock = pictureEditService.tryAcquireEditLock(pictureId, user.getId());
        if (acquiredLock) {
            PictureEditResponseMessage pictureEditResponseMessage = pictureEditService.buildEnterEditResponse(user);
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 向排除掉的session所有的session发送消息
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession
     * @throws Exception
     */
    private void broadcastToPicture(Long pictureId,
                                    PictureEditResponseMessage pictureEditResponseMessage,
                                    WebSocketSession excludeSession) throws Exception {

        // 拿到picture对应的所有的session
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);

        if (CollUtil.isNotEmpty(sessionSet)) {
            // 创建 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);

            // 序列化为 JSON 字符串
            String message = objectMapper.writeValueAsString(pictureEditResponseMessage);

            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : sessionSet) {
                // 排除掉的 session 不发送
                if (excludeSession != null && excludeSession.equals(session)) {
                    continue;
                }

                // 异步发送消息
                wsSendExecutor.execute(() -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(textMessage);
                        }
                    } catch (Exception e) {
                        log.error("发送消息失败", e);
                    }
                });
            }
        }
    }


    // 全部广播
    private void broadcastToPicture(Long pictureId,
                                    PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }


}
