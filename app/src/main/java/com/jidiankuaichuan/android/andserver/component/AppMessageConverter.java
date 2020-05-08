/*
 * Copyright 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jidiankuaichuan.android.andserver.component;

import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jidiankuaichuan.android.andserver.util.JsonUtils;
import com.yanzhenjie.andserver.annotation.Converter;
import com.yanzhenjie.andserver.framework.MessageConverter;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.util.IOUtils;
import com.yanzhenjie.andserver.util.MediaType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by Zhenjie Yan on 2018/9/11.
 */
@Converter
public class AppMessageConverter implements MessageConverter {

    private static final String TAG = "AppMessageConverter";

    @Override
    public ResponseBody convert(@NonNull Object output, @Nullable MediaType mediaType) {
        Log.e(TAG, "convert data to client");
        if (output instanceof File) {
            return new FileBody((File) output);
        } else {
            return new JsonBody(JsonUtils.successfulJson(output));
//            return (ResponseBody) output;
//            return new StringBody((String) output, MediaType.TEXT_HTML);
        }
    }

    @Nullable
    @Override
    public <T> T convert(@NonNull InputStream stream, @Nullable MediaType mediaType, Type type) throws IOException {
        Log.e(TAG, "convert data to server");
        Charset charset = mediaType == null ? null : mediaType.getCharset();
        if (charset == null) {
            return JsonUtils.parseJson(IOUtils.toString(stream), type);
        }
        return JsonUtils.parseJson(IOUtils.toString(stream, charset), type);
    }
}