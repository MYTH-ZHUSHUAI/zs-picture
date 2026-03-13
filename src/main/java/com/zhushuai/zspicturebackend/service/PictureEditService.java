package com.zhushuai.zspicturebackend.service;

import cn.hutool.json.JSONUtil;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditActionEnum;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditMessageTypeEnum;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditRequestMessage;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditResponseMessage;
import com.zhushuai.zspicturebackend.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhushuai
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PictureEditService {

    private final UserService userService;

    // 保留当前图片正在操作的用户：pictureId -> userId
    private final ConcurrentHashMap<Long, Long> pictureEditingUser = new ConcurrentHashMap<>();

    /**
     * 处理进入编辑的请求
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     * @return true if successfully entered edit mode, false otherwise
     */
    public boolean handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                          WebSocketSession session,
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

            return true;
        }
        return false;
    }

    /**
     * 处理退出编辑的请求
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                      WebSocketSession session,
                                      Long pictureId,
                                      User user) throws Exception {
        Long editingUserId = pictureEditingUser.get(pictureId);

        // 如果正在编辑的用户和当前用户一致，则发送退出消息
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditingUser.remove(pictureId);
        }
    }

    /**
     * 处理编辑动作
     *
     * @param pictureEditRequestMessage
     * @param session
     * @param pictureId
     * @param user
     * @return true if the action was performed, false otherwise
     */
    public boolean handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMessage,
                                           WebSocketSession session,
                                           Long pictureId,
                                           User user) throws Exception {

        Long editingUserId = pictureEditingUser.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();

        // 得到操作的枚举类型
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (pictureEditActionEnum == null) {
            return false;
        }

        if (editingUserId != null && editingUserId.equals(user.getId())) {
            return true;
        }
        
        return false;
    }

    /**
     * 构建进入编辑的响应消息
     */
    public PictureEditResponseMessage buildEnterEditResponse(User user) {
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        message.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
        message.setMessage(String.format("%s进入编辑", user.getUserName()));
        message.setUser(userService.getUserVO(user));
        return message;
    }

    /**
     * 构建退出编辑的响应消息
     */
    public PictureEditResponseMessage buildExitEditResponse(User user) {
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        message.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
        message.setMessage(String.format("%s退出编辑", user.getUserName()));
        message.setUser(userService.getUserVO(user));
        return message;
    }

    /**
     * 构建编辑动作响应消息
     */
    public PictureEditResponseMessage buildEditActionResponse(String type, String editAction, User user) {
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        PictureEditResponseMessage message = new PictureEditResponseMessage();
        message.setType(type);
        message.setMessage(String.format("%s执行%s", user.getUserName(), pictureEditActionEnum.getText()));
        message.setEditAction(editAction);
        message.setUser(userService.getUserVO(user));
        return message;
    }

    /**
     * 尝试获取图片的编辑锁
     * @return true if successfully acquired the lock, false otherwise
     */
    public boolean tryAcquireEditLock(Long pictureId, Long userId) {
        return pictureEditingUser.putIfAbsent(pictureId, userId) == null;
    }

    /**
     * 检查用户是否是图片的当前编辑者
     */
    public boolean isCurrentEditor(Long pictureId, Long userId) {
        Long editingUserId = pictureEditingUser.get(pictureId);
        return editingUserId != null && editingUserId.equals(userId);
    }

    /**
     * 获取图片的当前编辑者 ID
     */
    public Long getCurrentEditor(Long pictureId) {
        return pictureEditingUser.get(pictureId);
    }

    /**
     * 移除图片的编辑者
     */
    public void removeEditor(Long pictureId) {
        pictureEditingUser.remove(pictureId);
    }
}
