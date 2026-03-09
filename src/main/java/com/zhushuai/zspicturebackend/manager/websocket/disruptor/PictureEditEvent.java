package com.zhushuai.zspicturebackend.manager.websocket.disruptor;

import com.zhushuai.zspicturebackend.manager.websocket.message.PictureEditRequestMessage;
import com.zhushuai.zspicturebackend.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;


/**
 * 图片处理事件，包含
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    private PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前用户的 session
     */
    private WebSocketSession session;

    /**
     * 当前用户
     */
    private User user;

    /**
     * 图片 id
     */
    private Long pictureId;

}
