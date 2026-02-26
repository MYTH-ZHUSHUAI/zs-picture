package com.zhushuai.zspicturebackend.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -7569543679935591090L;
    private String userAccount;
    private String userPassword;

}
