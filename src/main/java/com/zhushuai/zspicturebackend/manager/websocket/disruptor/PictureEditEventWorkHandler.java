package com.zhushuai.zspicturebackend.manager.websocket.disruptor;


import cn.hutool.json.JSONUtil;
import com.lmax.disruptor.WorkHandler;
import com.zhushuai.zspicturebackend.manager.websocket.PictureEditHandler;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditActionEnum;
import com.zhushuai.zspicturebackend.manager.websocket.enums.PictureEditMessageTypeEnum;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditRequestMessage;
import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditResponseMessage;
import com.zhushuai.zspicturebackend.model.entity.User;
import com.zhushuai.zspicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * 事件消费者（处理消息）
 *
 * @author zhushuai
 */
@Component
@Slf4j
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {

        // 获取事件参数
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        WebSocketSession session = pictureEditEvent.getSession();
        User user = pictureEditEvent.getUser();
        Long pictureId = pictureEditEvent.getPictureId();


        // 得到消息类别
        String type = pictureEditRequestMessage.getType();
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum pictureEditActionEnum = PictureEditActionEnum.getEnumByValue(editAction);


        // 调用对应消息的处理方法
        switch (type) {
            case "ENTER_EDIT":
                pictureEditHandler.handleEnterEditMessage(pictureEditRequestMessage,
                        session,
                        pictureId,
                        user);
                break;
            case "EXIT_EDIT":
                pictureEditHandler.handleExitEditMessage(pictureEditRequestMessage,
                        session,
                        pictureId,
                        user);
                break;
            case "EDIT_ACTION":
                pictureEditHandler.handleEditActionMessage(pictureEditRequestMessage,
                        session,
                        pictureId,
                        user);
                break;
            default:
                // 发送错误的消息类型
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();

                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setMessage("消息类型错误");
                pictureEditResponseMessage.setUser(userService.getUserVO(user));

                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
        }
    }
}
