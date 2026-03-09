package com.zhushuai.zspicturebackend.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhushuai.zspicturebackend.manager.websocket.disruptor.PictureEditEventProducer;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditRequestMessage;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditResponseMessage;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditActionEnum;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditMessageTypeEnum;
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

@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private ExecutorService wsSendExecutor;

    @Resource
    private UserService userService;

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    // 保留当前图片正在操作的用户：pictureId -> userId
    private final ConcurrentHashMap<Long, Long> pictureEditingUser = new ConcurrentHashMap<>();

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
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));

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

        // 发送退出编辑的动作
        handleExitEditMessage(null, session, pictureId, user);

        // 删除会话
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (!webSocketSessions.isEmpty()) {
            webSocketSessions.remove(session);
        }


        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format("%s退出编辑", user.getUserName()));
        pictureEditResponseMessage.setUser(userService.getUserVO(user));

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

        // 将请求发送给 disruptor
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage,
                session,
                user,
                pictureId);

        // websocket线程被释放
        return;

    }

    /**
     * 处理编辑动作
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param userId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                        @NonNull WebSocketSession session,
                                        Long pictureId,
                                        User user) throws Exception {

        Long editingUserId = pictureEditingUser.get(pictureId);
        String type = pictureEditRequestMessage.getType();
        String editAction = pictureEditRequestMessage.getEditAction();

        // 得到操作的枚举类型
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return;
        }

        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();

            pictureEditResponseMessage.setType(type);
            pictureEditResponseMessage.setMessage(String.format("%s执行%s", user.getUserName(), pictureEditActionEnum.getText()));
            pictureEditResponseMessage.setEditAction(pictureEditActionEnum.getValue());
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            broadcastToPicture(pictureId, pictureEditResponseMessage, session);


        }


    }

    /**
     * 处理退出编辑的请求
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param userId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                      @NonNull WebSocketSession session,
                                      Long pictureId,
                                      User user) throws Exception {
        Long editingUserId = pictureEditingUser.get(pictureId);

        // 如果正在编辑的用户和当前用户一致，则发送退出消息
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditingUser.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format("%s退出编辑", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));


            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }


    }

    /**
     * 处理进入编辑的请求
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param userId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                       @NonNull WebSocketSession session,
                                       Long pictureId,
                                       User user) throws Exception {

        // 如果当前图片没有正在编辑的
        if (!pictureEditingUser.containsKey(pictureId)) {

            // 每个图片只能有一个用户正在编辑
            pictureEditingUser.put(pictureId, user.getId());

            // 创建返回消息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format("%s进入编辑", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVO(user));

            // 发送消息
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
