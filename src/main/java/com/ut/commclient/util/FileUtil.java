package com.ut.commclient.util;

import com.alibaba.fastjson.JSON;
import com.ut.commclient.model.RecModel;

import java.io.BufferedWriter;
import java.io.FileWriter;

public abstract class FileUtil {
    public static void write(String path, RecModel recModel) {
        try {
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(JSON.toJSONString(recModel));// 往已有的文件上添加字符串
            bw.write("\r\n");
            bw.close();
            fw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
