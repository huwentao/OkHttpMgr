package org.okhttp.mgr.library.request;

import java.io.File;

/**
 * Created by huwentao on 16/10/8.
 * 文件上传对像
 */
public class UploadFile {
    private String paramKey;    //表单上传时对应的文件参数KEY值
    private String fileName;    //文件名字
    private File uploadFile;    //待上传文件
    private String MineType;    //文件类型
    private long length;        //文件大小
    private String filePath;    //文件路径

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
    }

    public String getMineType() {
        return MineType;
    }

    public void setMineType(String mineType) {
        MineType = mineType;
    }

    public long getLength() {
        if (uploadFile != null && uploadFile.exists())
            return uploadFile.length();
        else return 0;
    }

    public String getFilePath() {
        if (uploadFile != null && uploadFile.exists())
            return uploadFile.getAbsolutePath();
        else return "";
    }
}
