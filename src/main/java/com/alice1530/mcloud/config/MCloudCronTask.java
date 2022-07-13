package com.alice1530.mcloud.config;


import com.alice1530.mcloud.model.CFile;
import com.alice1530.mcloud.model.FileType;
import com.alice1530.mcloud.store.MCloudClientService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MCloudCronTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCloudCronTask.class);
    @Autowired
    private MCloudClientService MCloudClientService;
    @Value("${mcloud.remotePath}")
    private String remotePath;
    @Value("${mcloud.localPath}")
    private String localPath;
    @Value("${mcloud.autoUpload}")
    private boolean autoUpload;
    @Value("${mcloud.pushPlusToken}")
    private String pushPlusToken;

    private ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<>(1);

    /**
     * 每隔5分钟请求一下接口，保证token不过期
     */
    @Scheduled(initialDelay = 30 * 1000, fixedDelay = 5 * 60 * 1000)
    public void refreshToken() {
        try {
            CFile root = MCloudClientService.getCFileByPath("/");
            MCloudClientService.getCFiles(root.getFileId());
            if (autoUpload) {
                //自动上传文件
                autoupload();
            }
        } catch (Exception e) {

            e.printStackTrace();

            Integer count = concurrentHashMap.get("refreshToken");
            if (count==null)count=0;
            count++;
            concurrentHashMap.put("refreshToken",count);
            LOGGER.error("程序第[{}]次异常超过十次，将退出程序。",count);

            //消息推送
            if (StringUtils.hasLength(pushPlusToken)) {
                try {
                    //推送通知
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(2, TimeUnit.MINUTES)
                            .readTimeout(1, TimeUnit.MINUTES)
                            .build();

                    RequestBody body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("token", pushPlusToken)
                            .addFormDataPart("title", "未知异常" + count)
                            .addFormDataPart("content", "程序第[" + count + "]次异常请检查,异常超过十次，将退出程序。\n" + new Date().toString())
                            .addFormDataPart("topic", "")
                            .build();

                    Request request = new Request.Builder()
                            .post(body)
                            .url("https://www.pushplus.plus/send")
                            .addHeader("Content-Type", "application/json")
                            .build();

                    Response response = client.newCall(request).execute();
                    LOGGER.info("push result:{}", response.body().string());

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            //错误超过十次，退出程序
            if (count > 9) {
                System.exit(e.hashCode());
            }

        }

    }

    public void autoupload() {
        try {
            String local = localPath.trim();
            String remote = remotePath.trim();
            LOGGER.info("扫描上传目录:{}......", local);
            if (!StringUtils.hasLength(local) || !StringUtils.hasLength(remote)) {
                LOGGER.error("未配置本地:{},和远程:{}文件夹，取消上传操作", localPath, remotePath);
                return;
            }
            //递归上传
            recurseUpload(local, remote);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("自动上传失败：{}", e.getMessage());
        }
    }

    /**
     * 递归上传
     *
     * @param local
     * @param remote
     * @throws Exception
     */
    private void recurseUpload(String local, String remote) throws Exception {
        File f = new File(local);
        File[] files = f.listFiles();

        for (File o : files) {
            if (o.getName().startsWith(".") || o.getName().startsWith("~") || ".DS_Store".equals(o.getName())) {
                // 临时文件不上传
                LOGGER.info("隐藏文件不上传:{}", o.getAbsolutePath());
                continue;
            }

            //上传的远程路径
            String tmpRemote = remote + "/" + o.getName();
            if (o.isDirectory()) {
                //创建远程文件夹
                createFolders(tmpRemote);
                String tmpLocal = local + File.separator + o.getName();
                //递归上传
                recurseUpload(tmpLocal, tmpRemote);
            } else {
                //上传
                long size = o.length();
                FileInputStream in = new FileInputStream(o);
                MCloudClientService.uploadPre(tmpRemote, size, in);
                in.close();
            }
            //上传完删除文件夹
            boolean delete = o.delete();
            LOGGER.info("delete={},src:{}", delete, o.getAbsolutePath());
        }
    }


    /**
     * 递归创建远程文件夹
     *
     * @param path
     * @throws Exception
     */
    private void createFolders(String path) throws Exception {

        CFile cfile = MCloudClientService.getCFileByPath(path);
        if (cfile == null) {
            String parentPath = MCloudClientService.getPathInfo(path).getParentPath();
            CFile panentFile = MCloudClientService.getCFileByPath(parentPath);
            if (panentFile == null) {
                createFolders(parentPath);
            }
            MCloudClientService.createFolder(path);
        } else {
            if (cfile.getFileType().equals(FileType.folder.name())) {
                LOGGER.info("已存在文件夹:{}", path);
            } else {
                LOGGER.info("已存在文件:{}", path);
            }
        }
    }

    //递归移动文件
    /*
    private void recurseMove(String local) {

        File lDir = new File(local);
        File[] files = lDir.listFiles();
        for (File o:files){
            if(o.getName().startsWith(".backups")){
                continue;
            }

            if (o.isDirectory()){
                recurseMove(o.getAbsolutePath());
                o.delete();
            }
                //上传完移动文件到备份文件夹
                String absolutePath = o.getAbsolutePath();
                String base = absolutePath.substring(0, localPath.length());
                String end = absolutePath.substring(localPath.length());
                String backupfile = base+File.separator+".backups"+end;
                File backupDirFile = new File(backupfile.substring(0,backupfile.lastIndexOf(File.separator)));
                if (!backupDirFile.exists()){
                    backupDirFile.mkdirs();
                }
                //移动文件
                boolean rename = o.renameTo(new File(backupfile));
                LOGGER.info("moved={},src:{},dist:{}",rename,absolutePath,backupfile);
        }
    }
    */
}
