package com.example.yunzhou;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 把导出文件写入系统公共「下载」目录。
 * Android 10+ 走 MediaStore.Downloads（免存储权限，文件管理可见）；
 * 更低版本回落到 getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)。
 */
@CapacitorPlugin(name = "YzExport")
public class YzExportPlugin extends Plugin {

    @PluginMethod
    public void saveToDownloads(PluginCall call) {
        String fileName = call.getString("fileName");
        String content = call.getString("content");
        if (fileName == null || fileName.trim().isEmpty()) {
            call.reject("缺少 fileName");
            return;
        }
        if (content == null) {
            content = "";
        }
        // 防止路径穿越
        fileName = new File(fileName).getName();
        if (fileName.startsWith(".")) {
            fileName = "export" + fileName;
        }

        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            String mime = guessMime(fileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri uri = writeToMediaStore(fileName, mime, bytes);
                if (uri == null) {
                    call.reject("无法写入下载目录");
                    return;
                }
                JSObject ret = new JSObject();
                ret.put("uri", uri.toString());
                ret.put("path", Environment.DIRECTORY_DOWNLOADS + "/" + fileName);
                call.resolve(ret);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists() && !dir.mkdirs()) {
                    call.reject("无法创建下载目录");
                    return;
                }
                File out = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(bytes);
                    fos.flush();
                }
                JSObject ret = new JSObject();
                ret.put("uri", Uri.fromFile(out).toString());
                ret.put("path", out.getAbsolutePath());
                call.resolve(ret);
            }
        } catch (Exception e) {
            call.reject("写入失败: " + e.getMessage(), e);
        }
    }

    private Uri writeToMediaStore(String fileName, String mime, byte[] bytes) {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        Uri uri = resolver.insert(collection, values);
        if (uri == null) {
            return null;
        }
        try (OutputStream os = resolver.openOutputStream(uri)) {
            if (os == null) {
                resolver.delete(uri, null, null);
                return null;
            }
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            try {
                resolver.delete(uri, null, null);
            } catch (Exception ignored) {
            }
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
        }
        return uri;
    }

    private static String guessMime(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".json") || lower.endsWith(".yzplan.json") || lower.endsWith(".yzplan")) {
            return "application/json";
        }
        if (lower.endsWith(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }
}
