package com.smartpc.chiyun.utils;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @ClassName: FastDfsUtil
 * @Description: FastDfs文件管理简单工具类
 * @version V1.0
 */
public class FastDfsUtil {

    private static TrackerClient trackerClient = null;
    private static TrackerServer trackerServer = null;
    private static StorageServer storageServer = null;
    private static StorageClient storageClient = null;

    static {
        try {
            ClientGlobal.init("fdfs_client.conf");
            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getTrackerServer();
            storageClient = new StorageClient(trackerServer, storageServer);
        } catch (IOException | MyException e) {
            throw new RuntimeException("FastDfs工具类初始化失败!");
        }
    }

    /**
     *
     * @Title: fdfsUpload
     * @Description: 通过文件流上传文件
     * @param @param inputStream 文件流
     * @param @param filename 文件名称
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return String 返回文件在FastDfs的存储路径
     * @throwss
     */
    public static String fdfsUpload(InputStream inputStream, String filename) throws IOException, MyException{
        String suffix = ""; //后缀名
        try{
            suffix = filename.substring(filename.lastIndexOf(".")+1);
        }catch (Exception e) {
            throw new RuntimeException("参数filename不正确!格式例如：a.png");
        }
        String savepath = ""; //FastDfs的存储路径
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            swapStream.write(buff, 0, len);
        }
        byte[] in2b = swapStream.toByteArray();
        String[] strings = storageClient.upload_file(in2b, suffix, null); //上传文件
        for (String str : strings) {
            savepath += "/" + str; //拼接路径
        }
        return savepath;
    }

    /**
     *
     * @Title: fdfsUpload
     * @Description: 本地文件上传
     * @param @param filepath 本地文件路径
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return String 返回文件在FastDfs的存储路径
     * @throws
     */
    public static String fdfsUpload(String filepath) throws IOException, MyException{
        String suffix = ""; //后缀名
        try{
            suffix = filepath.substring(filepath.lastIndexOf(".")+1);
        }catch (Exception e) {
            throw new RuntimeException("上传的不是文件!");
        }
        String savepath = ""; //FastDfs的存储路径
        //String[] strings = storageClient.upload_file(filepath.getBytes(), suffix, null);
        String[] strings = storageClient.upload_file(filepath, suffix, null); //上传文件
        for (String str : strings) {
            savepath += "/" + str; //拼接路径
        }
        return savepath;
    }

    /**
     *
     * @Title: fdfsDownload
     * @Description: 下载文件到目录
     * @param @param savepath 文件存储路径
     * @param @param localPath 下载目录
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return boolean 返回是否下载成功
     * @throws
     */
    public static boolean fdfsDownload(String savepath, String localPath) throws IOException, MyException{
        String group = ""; //存储组
        String path = ""; //存储路径
        try{
            int secondindex = savepath.indexOf("/", 2); //第二个"/"索引位置
            group = savepath.substring(1, secondindex); //类似：group1
            path = savepath.substring(secondindex + 1); //类似：M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png
        }catch (Exception e) {
            throw new RuntimeException("传入文件存储路径不正确!格式例如：/group1/M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png");
        }
        int result = storageClient.download_file(group, path, localPath);
        if(result != 0){
            throw new RuntimeException("下载文件失败：文件路径不对或者文件已删除!");
        }
        return true;
    }

    /**
     *
     * @Title: fdfsDownload
     * @Description: 返回文件字符数组
     * @param @param savepath 文件存储路径
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return byte[] 字符数组
     * @throws
     */
    public static byte[] fdfsDownload(String savepath) throws IOException, MyException{
        byte[] bs = null;
        String group = ""; //存储组
        String path = ""; //存储路径
        try{
            int secondindex = savepath.indexOf("/", 2); //第二个"/"索引位置
            group = savepath.substring(1, secondindex); //类似：group1
            path = savepath.substring(secondindex + 1); //类似：M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png
        }catch (Exception e) {
            throw new RuntimeException("传入文件存储路径不正确!格式例如：/group1/M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png");
        }
        bs = storageClient.download_file(group, path); //返回byte数组
        return bs;
    }

    /**
     *
     * @Title: fdfsDeleteFile
     * @Description: 删除文件
     * @param @param savepath 文件存储路径
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return boolean 返回true表示删除成功
     * @throws
     */
    public static boolean fdfsDeleteFile(String savepath) throws IOException, MyException{
        String group = ""; //存储组
        String path = ""; //存储路径
        try{
            int secondindex = savepath.indexOf("/", 2); //第二个"/"索引位置
            group = savepath.substring(1, secondindex); //类似：group1
            path = savepath.substring(secondindex + 1); //类似：M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png
        }catch (Exception e) {
            throw new RuntimeException("传入文件存储路径不正确!格式例如：/group1/M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png");
        }
        int result = storageClient.delete_file(group, path); //删除文件，0表示删除成功
        if(result != 0){
            throw new RuntimeException("删除文件失败：文件路径不对或者文件已删除!");
        }
        return true;
    }

    /**
     *
     * @Title: fdfdFileInfo
     * @Description: 返回文件信息
     * @param @param savepath 文件存储路径
     * @param @return
     * @param @throws IOException
     * @param @throws MyException
     * @return FileInfo 文件信息
     * @throws
     */
    public static FileInfo fdfdFileInfo(String savepath) throws IOException, MyException{
        String group = ""; //存储组
        String path = ""; //存储路径
        try{
            int secondindex = savepath.indexOf("/", 2); //第二个"/"索引位置
            group = savepath.substring(1, secondindex); //类似：group1
            path = savepath.substring(secondindex + 1); //类似：M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png
        }catch (Exception e) {
            throw new RuntimeException("传入文件存储路径不正确!格式例如：/group1/M00/00/00/wKgBaFv9Ad-Abep_AAUtbU7xcws013.png");
        }
        FileInfo fileInfo = storageClient.get_file_info(group, path);
        return fileInfo;
    }


/*    public static void main(String[] args) {
        FastDfsUtil fast = new FastDfsUtil();
        try {
            String str="shenningwuid";
            InputStream stream= new ByteArrayInputStream(str.getBytes());
            String txt = fast.fdfsUpload(stream, "txt");
            System.out.println(txt);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }*/


    /**
     * 该方法也可上传
     * @param file
     */
    public void upload1(@RequestParam MultipartFile file) {
        try {
            if(file.isEmpty()){
                System.out.println("空上传" );
            }else{
                String conf_filename = this.getClass().getClassLoader().getResource("fdfs_client.conf").getPath().replaceAll("%20"," ");
                System.out.println(conf_filename);
                String tempFileName = file.getOriginalFilename();
                //fastDFS方式
                ClientGlobal.init(conf_filename);

                byte[] fileBuff = file.getBytes();
                String fileId= "";
                String fileExtName = tempFileName.substring(tempFileName.lastIndexOf("."));

                //建立连接
                TrackerClient tracker = new TrackerClient();
                TrackerServer trackerServer = tracker.getTrackerServer();
                StorageClient1 client = new StorageClient1(trackerServer, null);

                //设置元信息
                NameValuePair[] metaList = new NameValuePair[3];
                metaList[0] = new NameValuePair("fileName", tempFileName);
                metaList[1] = new NameValuePair("fileExtName", fileExtName);
                metaList[2] = new NameValuePair("fileLength", String.valueOf(file.getSize()));

                //上传文件，获得fileId
                fileId = client.upload_file1(fileBuff, fileExtName, metaList);
                //TODO 这里可以追加一些业务代码，例如上传成功后保存到upload_file表，统一进行上传文件管理之类
                System.out.println("fileId---------------------"+fileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



//
//    @RequestMapping(value = "/webjars/readProductExcels", method = RequestMethod.POST)
//    public ResultVO upload(@RequestParam MultipartFile file) {
//        String path = "";
//        try{
//            FastDfsUtil fastDfsUtil = new FastDfsUtil();
//            path = fastDfsUtil.fdfsUpload(file.getInputStream(),file.getOriginalFilename());
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return ResultVOUtils.success(path);
//    }

}
