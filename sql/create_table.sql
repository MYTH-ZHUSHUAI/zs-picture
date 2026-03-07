-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE IF NOT EXISTS picture
(
    id            bigint auto_increment comment 'id' primary key,
    url           varchar(512)                       not null comment '图片 url',
    name          varchar(128)                       not null comment '图片名称',
    introduction  varchar(512)                       null comment '简介',
    category      varchar(64)                        null comment '分类',
    tags          varchar(512)                       null comment '标签（JSON 数组）',
    picSize       bigint                             null comment '图片体积',
    picWidth      int                                null comment '图片宽度',
    picHeight     int                                null comment '图片高度',
    picScale      double                             null comment '图片宽高比例',
    picFormat     varchar(32)                        null comment '图片格式',
    userId        bigint                             not null comment '创建用户 id',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime      datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    reviewStatus  int      default 0                 not null comment '审核状态：0-待审核; 1-通过; 2-拒绝',
    reviewMessage varchar(512)                       null comment '审核信息',
    reviewerId    bigint                             null comment '审核人 ID',
    reviewTime    datetime                           null comment '审核时间',
    thumbnailUrl  varchar(512)                       null comment '缩略图 url',
    md5           varchar(64)                        not null comment '图片 MD5 值',
    spaceId       bigint   default 0                 not null comment '空间 id（为 0 表示公共空间）',
    mainColor     varchar(32)                        null comment '图片主色调（RGB 十六进制格式）',
    watermarkedUrl  varchar(512)                     null comment '带水印的原图 URL',
    watermarkedThumbnailUrl varchar(512)             null comment '带水印的缩略图 URL',
    originalUrl   varchar(512)                       null comment '原图 URL（原始格式）',
    
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId),
    INDEX idx_reviewStatus (reviewStatus),
    INDEX idx_spaceId (spaceId)
) comment '图片' collate = utf8mb4_unicode_ci;


-- 用户私有图片表
CREATE TABLE IF NOT EXISTS user_picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除（0默认不公开）',
    isOpen       tinyint  default 0                 not null comment '是否公开',
    thumbnailUrl varchar(512)                       null comment '缩略图 url',
    md5          varchar(64)                        not null comment '图片 MD5 值',
    spaceId      bigint   default 0                 not null comment '空间 id（为 0 表示公共空间）',
    mainColor    varchar(32)                        null comment '图片主色调（RGB 十六进制格式）',
    watermarkedUrl  varchar(512)                     null comment '带水印的原图 URL',
    watermarkedThumbnailUrl varchar(512)             null comment '带水印的缩略图 URL',
    originalUrl   varchar(512)                       null comment '原图 URL（原始格式）',
    
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId),
    INDEX idx_spaceId (spaceId)
) comment '图片' collate = utf8mb4_unicode_ci;


-- 为 picture 表添加新字段
ALTER TABLE picture
ADD COLUMN mainColor varchar(32) DEFAULT NULL COMMENT '图片主色调 (RGB 十六进制格式)',
ADD COLUMN watermarkedUrl varchar(512) DEFAULT NULL COMMENT '带水印的原图 URL',
ADD COLUMN watermarkedThumbnailUrl varchar(512) DEFAULT NULL COMMENT '带水印的缩略图 URL',
ADD COLUMN originalUrl varchar(512) DEFAULT NULL COMMENT '原图 URL（原始格式）';

-- 为 user_picture 表添加新字段
ALTER TABLE user_picture
ADD COLUMN mainColor varchar(32) DEFAULT NULL COMMENT '图片主色调 (RGB 十六进制格式)',
ADD COLUMN watermarkedUrl varchar(512) DEFAULT NULL COMMENT '带水印的原图 URL',
ADD COLUMN watermarkedThumbnailUrl varchar(512) DEFAULT NULL COMMENT '带水印的缩略图 URL',
ADD COLUMN originalUrl varchar(512) DEFAULT NULL COMMENT '原图 URL（原始格式）';
