package com.openxu.choosepic.util;

import android.os.Environment;

/**
 * User: chenxinming<br>
 * Date: 13-7-10<br>
 * Time: 下午16:08<br>
 * Email: terry.chen@itotemdeveloper.com<br>
 * 法制日报常量类
 */
public final class Constant {
    // 法制日报在sd上的根目录
    public static final String LEGALD_AILY_SD = Environment
            .getExternalStorageDirectory().getPath() + "/tuisonghao";

    public static final int CHOOSE_MAX_PIC = 9;
    public static final String PICS_DIR = LEGALD_AILY_SD + "/pics";     //将选中的图片缓存到此文件夹
    public static final String UPLOAD_DIR = LEGALD_AILY_SD + "/uploadpics";  //上传时，压缩上面目录的图片，然后将压缩后的图片缓存到此目录



}
