package com.jidiankuaichuan.android.andserver.controller;

import android.os.Build;
import android.text.Html;
import android.text.SpannableString;

import androidx.annotation.RequiresApi;

import com.jidiankuaichuan.android.utils.kuaichuan_utils.OtherFileUtils;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;

@RestController
//@RequestMapping(path = "/user")
public class DownLoadController {

    public static String filePath;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @GetMapping(path = "/download", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    FileBody downLoad(HttpRequest request, HttpResponse response) {
        if (filePath != null && !"".equals(filePath)) {
            String fileName = OtherFileUtils.getFileName(filePath);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            return new FileBody(new File(filePath));
        }
        return null;
    }
}
