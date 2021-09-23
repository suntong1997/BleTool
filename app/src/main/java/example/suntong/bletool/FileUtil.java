package example.suntong.bletool;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtil {

    static FileUtil fileUtil;

    public static FileUtil getInstance() {
        if (fileUtil == null) {
            synchronized (FileUtil.class) {
                if (fileUtil == null) {
                    fileUtil = new FileUtil();
                    return fileUtil;
                }
            }
        }
        return fileUtil;
    }

    //将获取的数据写入文件中
    public boolean writeToFile(String data, String filePath,String fileName) {
        FileWriter fw;
        BufferedWriter bw = null;
        try {
            //创建文件夹
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            //创建文件
            File file = new File(dir, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

}
