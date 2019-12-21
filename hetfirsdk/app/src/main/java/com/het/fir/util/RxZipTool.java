package com.het.fir.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * @author vondear
 * @date 2016/1/24
 * 压缩相关工具类
 */
public class RxZipTool {


    /**
     * 判断字符串是否为空 为空即true
     *
     * @param str 字符串
     * @return
     */
    public static boolean isNullString(String str) {
        return str == null || str.length() == 0 || "null".equals(str);
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static File getFileByPath(String filePath) {
        return isNullString(filePath) ? null : new File(filePath);
    }

    /**
     * 批量解压文件
     *
     * @param zipFiles    压缩文件集合
     * @param destDirPath 目标目录路径
     * @return {@code true}: 解压成功<br>{@code false}: 解压失败
     * @throws IOException IO错误时抛出
     */
    public static boolean unzipFiles(Collection<File> zipFiles, String destDirPath) {
        return unzipFiles(zipFiles, getFileByPath(destDirPath));
    }

    /**
     * 批量解压文件
     *
     * @param zipFiles 压缩文件集合
     * @param destDir  目标目录
     * @return {@code true}: 解压成功<br>{@code false}: 解压失败
     * @throws IOException IO错误时抛出
     */
    public static boolean unzipFiles(Collection<File> zipFiles, File destDir) {
        if (zipFiles == null || destDir == null) {
            return false;
        }
        for (File zipFile : zipFiles) {
            if (!unzipFile(zipFile, destDir)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解压文件
     *
     * @param zipFilePath 待解压文件路径
     * @param destDirPath 目标目录路径
     * @return {@code true}: 解压成功<br>{@code false}: 解压失败
     * @throws IOException IO错误时抛出
     */
    public static boolean unzipFile(String zipFilePath, String destDirPath) {
        return unzipFile(getFileByPath(zipFilePath), getFileByPath(destDirPath));
    }

    public static List<File> unzip(String zipFilePath, String destDirPath) {
        return unzip(getFileByPath(zipFilePath), getFileByPath(destDirPath));
    }

    public static List<File> unZipCurrDir(String zipFilePath) {
        File target = getFileByPath(zipFilePath);
        return unzipdir(target, target.getParentFile());
    }

    public static File unzip(File target) {
        String filename = target.getName();
        String caselsh = filename.substring(0,filename.lastIndexOf("."));
        caselsh += File.separator+"het"+File.separator+"clife";
        File dst = new File(target.getParent() + File.separator + caselsh);
        return unzipFileByDir(target, dst,null);
    }

    public static File unzip(String zipFilePath) {
        File target = getFileByPath(zipFilePath);
        return unzipFileByDir(target, target.getParentFile(),null);
    }

    public static List<File> unZipCurrDir(String zipFilePath, String destDir) {
        File target = getFileByPath(zipFilePath);
        File dest = destDir == null ? target.getParentFile() : new File(target.getParentFile().getAbsolutePath() + File.separator + destDir);
        return unzipdir(target, dest);
    }

    public static List<File> unZipFileCurrDir(String zipFilePath) {
        File target = getFileByPath(zipFilePath);
        return unzip(target, target.getParentFile());
    }

    public static List<File> unZipFileCurrDir(String zipFilePath, String destDir) {
        File target = getFileByPath(zipFilePath);
        File dest = destDir == null ? target.getParentFile() : new File(target.getParentFile().getAbsolutePath() + File.separator + destDir);
        return unzip(target, dest);
    }

    /**
     * 解压文件
     *
     * @param zipFile 待解压文件
     * @param destDir 目标目录
     * @return {@code true}: 解压成功<br>{@code false}: 解压失败
     * @throws IOException IO错误时抛出
     */
    public static boolean unzipFile(File zipFile, File destDir) {
        return unzipFileByKeyword(zipFile, destDir, null) != null;
    }

    public static List<File> unzip(File zipFile, File destDir) {
        return unzipFileByKeyword(zipFile, destDir, null);
    }

    public static List<File> unzipdir(File zipFile, File destDir) {
        return unzipFileByKeywordDir(zipFile, destDir, null);
    }


    /**
     * 解压带有关键字的文件
     *
     * @param zipFilePath 待解压文件路径
     * @param destDirPath 目标目录路径
     * @param keyword     关键字
     * @return 返回带有关键字的文件链表
     * @throws IOException IO错误时抛出
     */
    public static List<File> unzipFileByKeyword(String zipFilePath, String destDirPath, String keyword) {
        return unzipFileByKeyword(getFileByPath(zipFilePath),
                getFileByPath(destDirPath), keyword);
    }


    @SuppressWarnings("unchecked")
    public static List<File> unzipFileByKeywordDir(File zipFile, File destDir, String passwd) {
        try {
            //1.判断指定目录是否存在
            if (zipFile == null) {
                throw new ZipException("压缩文件不存在.");
            }
            if (destDir == null) {
                throw new ZipException("解压缩路径不存在.");
            }

            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }

            //2.初始化zip工具
            net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile);
            zFile.setFileNameCharset("UTF-8");
            if (!zFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法,可能被损坏.");
            }
            //3.判断是否已加密
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }
            //4.解压所有文件
            zFile.extractAll(destDir.getAbsolutePath());
            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (fileHeader.isDirectory()) {
                    String fName = fileHeader.getFileName();
                    if (fName != null) {
                        if (fName.contains("/")) {
                            String[] fs = fName.split("/");
                            if (fs.length == 1) {
                                extractedFileList.add(new File(destDir + File.separator + fName));
                            }
                        } else {
                            extractedFileList.add(new File(destDir + File.separator + fName));
                        }
                    }

                }
            }
            return extractedFileList;
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static File unzipFileByDir(File zipFile, File destDir, String passwd) {
        try {
            //1.判断指定目录是否存在
            if (zipFile == null) {
                throw new ZipException("压缩文件不存在.");
            }
            if (destDir == null) {
                throw new ZipException("解压缩路径不存在.");
            }

            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }

            //2.初始化zip工具
            net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile);
            zFile.setFileNameCharset("UTF-8");
            if (!zFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法,可能被损坏.");
            }
            //3.判断是否已加密
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }
            //4.解压所有文件
            zFile.extractAll(destDir.getAbsolutePath());
            return destDir;
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据所给密码解压zip压缩包到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     *
     * @param zipFile zip压缩包绝对路径
     * @param destDir 指定解压文件夹位置
     * @param passwd  密码(可为空)
     * @return 解压后的文件数组
     * @throws ZipException
     */
    @SuppressWarnings("unchecked")
    public static List<File> unzipFileByKeyword(File zipFile, File destDir, String passwd) {
        try {
            //1.判断指定目录是否存在
            if (zipFile == null) {
                throw new ZipException("压缩文件不存在.");
            }
            if (destDir == null) {
                throw new ZipException("解压缩路径不存在.");
            }

            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }

            //2.初始化zip工具
            net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile);
            zFile.setFileNameCharset("UTF-8");
            if (!zFile.isValidZipFile()) {
                throw new ZipException("压缩文件不合法,可能被损坏.");
            }
            //3.判断是否已加密
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }
            //4.解压所有文件
            zFile.extractAll(destDir.getAbsolutePath());
            List<FileHeader> headerList = zFile.getFileHeaders();
            List<File> extractedFileList = new ArrayList<File>();
            for (FileHeader fileHeader : headerList) {
                if (!fileHeader.isDirectory()) {
                    extractedFileList.add(new File(destDir, fileHeader.getFileName()));
                }
            }
            return extractedFileList;
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取压缩文件中的文件路径链表
     *
     * @param zipFilePath 压缩文件路径
     * @return 压缩文件中的文件路径链表
     * @throws IOException IO错误时抛出
     */
    public static List<String> getFilesPath(String zipFilePath)
            throws IOException {
        return getFilesPath(getFileByPath(zipFilePath));
    }

    /**
     * 获取压缩文件中的文件路径链表
     *
     * @param zipFile 压缩文件
     * @return 压缩文件中的文件路径链表
     * @throws IOException IO错误时抛出
     */
    public static List<String> getFilesPath(File zipFile)
            throws IOException {
        if (zipFile == null) {
            return null;
        }
        List<String> paths = new ArrayList<>();
        Enumeration<?> entries = getEntries(zipFile);
        while (entries.hasMoreElements()) {
            paths.add(((ZipEntry) entries.nextElement()).getName());
        }
        return paths;
    }

    /**
     * 获取压缩文件中的注释链表
     *
     * @param zipFilePath 压缩文件路径
     * @return 压缩文件中的注释链表
     * @throws IOException IO错误时抛出
     */
    public static List<String> getComments(String zipFilePath)
            throws IOException {
        return getComments(getFileByPath(zipFilePath));
    }


    /**
     * 获取压缩文件中的注释链表
     *
     * @param zipFile 压缩文件
     * @return 压缩文件中的注释链表
     * @throws IOException IO错误时抛出
     */
    public static List<String> getComments(File zipFile)
            throws IOException {
        if (zipFile == null) {
            return null;
        }
        List<String> comments = new ArrayList<>();
        Enumeration<?> entries = getEntries(zipFile);
        while (entries.hasMoreElements()) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            comments.add(entry.getComment());
        }
        return comments;
    }

    /**
     * 获取压缩文件中的文件对象
     *
     * @param zipFilePath 压缩文件路径
     * @return 压缩文件中的文件对象
     * @throws IOException IO错误时抛出
     */
    public static Enumeration<?> getEntries(String zipFilePath)
            throws IOException {
        return getEntries(getFileByPath(zipFilePath));
    }

    /**
     * 获取压缩文件中的文件对象
     *
     * @param zipFile 压缩文件
     * @return 压缩文件中的文件对象
     * @throws IOException IO错误时抛出
     */
    public static Enumeration<?> getEntries(File zipFile)
            throws IOException {
        if (zipFile == null) {
            return null;
        }
        return new ZipFile(zipFile).entries();
    }

    //----------------------------------------加密压缩------------------------------------------------

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     *
     * @param sourceFilePath :待压缩的文件路径
     * @param zipFilePath    :压缩后存放路径
     * @param fileName       :压缩后文件的名称
     * @return
     */
    public static boolean fileToZip(String sourceFilePath, String zipFilePath, String fileName) {
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        if (sourceFile.exists()) {
            try {
                File zipFile = new File(zipFilePath + "/" + fileName + ".zip");
                if (zipFile.exists()) {
                    System.out.println(zipFilePath + "目录下存在名字为:" + fileName + ".zip" + "打包文件.");
                } else {
                    File[] sourceFiles = sourceFile.listFiles();
                    if (null == sourceFiles || sourceFiles.length < 1) {
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                    } else {
                        fos = new FileOutputStream(zipFile);
                        zos = new ZipOutputStream(new BufferedOutputStream(fos));
                        byte[] bufs = new byte[1024 * 10];
                        for (int i = 0; i < sourceFiles.length; i++) {
                            //创建ZIP实体，并添加进压缩包
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            //zos.putNextEntry(zipEntry);
                            //读取待压缩的文件并写进压缩包里
                            fis = new FileInputStream(sourceFiles[i]);
                            bis = new BufferedInputStream(fis, 1024 * 10);
                            int read = 0;
                            while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                                zos.write(bufs, 0, read);
                            }
                        }
                        flag = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                //关闭流
                try {
                    if (null != bis) {
                        bis.close();
                    }
                    if (null != zos) {
                        zos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        } else {
            System.out.println("待压缩的文件目录：" + sourceFilePath + "不存在.");
        }
        return flag;
    }

    public static String zipEncrypt(String src, String dest, boolean isCreateDir, String passwd) {
        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (!isNullString(passwd)) {
            parameters.setEncryptFiles(true);
            // 加密方式
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(passwd.toCharArray());
        }
        try {
            net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(dest);
            if (srcFile.isDirectory()) {
                // 如果不创建目录的话,将直接把给定目录下的文件压缩到压缩文件,即没有目录结构
                if (!isCreateDir) {
                    File[] subFiles = srcFile.listFiles();
                    ArrayList<File> temp = new ArrayList<File>();
                    Collections.addAll(temp, subFiles);
                    zipFile.addFiles(temp, parameters);
                    return dest;
                }
                zipFile.addFolder(srcFile, parameters);
            } else {
                zipFile.addFile(srcFile, parameters);
            }
            return dest;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构建压缩文件存放路径,如果不存在将会创建
     * 传入的可能是文件名或者目录,也可能不传,此方法用以转换最终压缩文件的存放路径
     *
     * @param srcFile   源文件
     * @param destParam 压缩目标路径
     * @return 正确的压缩文件存放路径
     */
    private static String buildDestinationZipFilePath(File srcFile, String destParam) {
        if (isNullString(destParam)) {
            if (srcFile.isDirectory()) {
                destParam = srcFile.getParent() + File.separator + srcFile.getName() + ".zip";
            } else {
                String fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                destParam = srcFile.getParent() + File.separator + fileName + ".zip";
            }
        } else {
            // 在指定路径不存在的情况下将其创建出来
            createDestDirectoryIfNecessary(destParam);
            if (destParam.endsWith(File.separator)) {
                String fileName = "";
                if (srcFile.isDirectory()) {
                    fileName = srcFile.getName();
                } else {
                    fileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                }
                destParam += fileName + ".zip";
            }
        }
        return destParam;
    }

    /**
     * 在必要的情况下创建压缩文件存放目录,比如指定的存放路径并没有被创建
     *
     * @param destParam 指定的存放路径,有可能该路径并没有被创建
     */
    private static void createDestDirectoryIfNecessary(String destParam) {
        File destDir = null;
        if (destParam.endsWith(File.separator)) {
            destDir = new File(destParam);
        } else {
            destDir = new File(destParam.substring(0, destParam.lastIndexOf(File.separator)));
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    public static String zipEncryptRargo(String src, String dest, boolean isCreateDir, String passwd, int unit) {
        File srcFile = new File(src);
        dest = buildDestinationZipFilePath(srcFile, dest);
        ZipParameters parameters = new ZipParameters();
        // 默认COMP_DEFLATE
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        if (!isNullString(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(0);
            parameters.setPassword(passwd.toCharArray());
        }

        try {
            net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(dest);
            if (srcFile.isDirectory()) {
                if (!isCreateDir) {
                    File[] subFiles = srcFile.listFiles();
                    ArrayList<File> temp = new ArrayList();
                    Collections.addAll(temp, subFiles);
//                    zipFile.addFiles(temp, parameters);
                    zipFile.createZipFile(temp, parameters, true, unit * 1000);
                    return dest;
                }
                zipFile.createZipFileFromFolder(srcFile, parameters, true, unit * 1000);
                //粗略的算一下分成多少份，获取的大小比实际的大点（一般是准确的）
                int partsize = (int) zipInfo(dest) / (unit); //65536byte=64kb
                System.out.println("分割成功！总共分割成了" + (partsize + 1) + "个文件！");
            } else {
                zipFile.createZipFile(srcFile, parameters, true, unit * 1000);
            }

            return dest;
        } catch (ZipException var9) {
            var9.printStackTrace();
            return null;
        }
    }

    // 预览压缩文件信息
    public static double zipInfo(String zipFile) throws ZipException {
        net.lingala.zip4j.core.ZipFile zip = new net.lingala.zip4j.core.ZipFile(zipFile);
        zip.setFileNameCharset("GBK");
        List<FileHeader> list = zip.getFileHeaders();
        long zipCompressedSize = 0;
        for (FileHeader head : list) {
            zipCompressedSize += head.getCompressedSize();
            //      System.out.println(zipFile+"文件相关信息如下：");
            //      System.out.println("Name: "+head.getFileName());
            //      System.out.println("Compressed Size:"+(head.getCompressedSize()/1.0/1024)+"kb");
            //      System.out.println("Uncompressed Size:"+(head.getUncompressedSize()/1.0/1024)+"kb");
            //      System.out.println("CRC32:"+head.getCrc32());
            //      System.out.println("*************************************");
        }
        double size = zipCompressedSize / 1.0 / 1024;//转换为kb
        return size;
    }

    /**
     * 删除ZIP文件内的文件夹
     *
     * @param file
     * @param removeDir
     */
    public static boolean removeDirFromZipArchive(String file, String removeDir) {
        try {
            // 创建ZipFile并设置编码
            net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(file);
            zipFile.setFileNameCharset("GBK");

            // 给要删除的目录加上路径分隔符
            if (!removeDir.endsWith(File.separator)) {
                removeDir += File.separator;
            }

            // 如果目录不存在, 直接返回
            FileHeader dirHeader = zipFile.getFileHeader(removeDir);

            if (null == dirHeader) {
                return false;
            }

            // 遍历压缩文件中所有的FileHeader, 将指定删除目录下的子文件名保存起来
            List headersList = zipFile.getFileHeaders();
            List<String> removeHeaderNames = new ArrayList<String>();
            for (int i = 0, len = headersList.size(); i < len; i++) {
                FileHeader subHeader = (FileHeader) headersList.get(i);
                if (subHeader.getFileName().startsWith(dirHeader.getFileName())
                        && !subHeader.getFileName().equals(dirHeader.getFileName())) {
                    removeHeaderNames.add(subHeader.getFileName());
                }
            }
            // 遍历删除指定目录下的所有子文件, 最后删除指定目录(此时已为空目录)
            for (String headerNameString : removeHeaderNames) {
                zipFile.removeFile(headerNameString);
            }
            zipFile.removeFile(dirHeader);
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void Unzip(final File zipFile, String dest, String passwd,
                             String charset, final Handler handler, final boolean isDeleteZipFile) {
        try {
            net.lingala.zip4j.core.ZipFile zFile = new net.lingala.zip4j.core.ZipFile(zipFile);
            if (TextUtils.isEmpty(charset)) {
                charset = "UTF-8";
            }
            zFile.setFileNameCharset(charset);
            if (!zFile.isValidZipFile()) {
                throw new ZipException(
                        "Compressed files are not illegal, may be damaged.");
            }
            File destDir = new File(dest); // Unzip directory
            if (destDir.isDirectory() && !destDir.exists()) {
                destDir.mkdir();
            }
            if (zFile.isEncrypted()) {
                zFile.setPassword(passwd.toCharArray());
            }

            final ProgressMonitor progressMonitor = zFile.getProgressMonitor();

            Thread progressThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Bundle bundle = null;
                    Message msg = null;
                    try {
                        int percentDone = 0;
                        // long workCompleted=0;
                        // handler.sendEmptyMessage(ProgressMonitor.RESULT_SUCCESS)
                        if (handler == null) {
                            return;
                        }
                        handler.sendEmptyMessage(CompressStatus.START);
                        while (true) {
                            Thread.sleep(1000);

                            percentDone = progressMonitor.getPercentDone();
                            bundle = new Bundle();
                            bundle.putInt(CompressKeys.PERCENT, percentDone);
                            msg = new Message();
                            msg.what = CompressStatus.HANDLING;
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if (percentDone >= 100) {
                                break;
                            }
                        }
                        handler.sendEmptyMessage(CompressStatus.COMPLETED);
                    } catch (InterruptedException e) {
                        bundle = new Bundle();
                        bundle.putString(CompressKeys.ERROR, e.getMessage());
                        msg = new Message();
                        msg.what = CompressStatus.ERROR;
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    } finally {
                        if (isDeleteZipFile) {
                            zipFile.deleteOnExit();//zipFile.delete();
                        }
                    }
                }
            });

            progressThread.start();
            zFile.setRunInThread(true);
            zFile.extractAll(dest);
        } catch (ZipException e) {
            e.printStackTrace();
        }

    }

    public class CompressStatus {
        public final static int START = 0;
        public final static int HANDLING = 1;
        public final static int COMPLETED = 2;
        public final static int ERROR = 3;
    }

    public class CompressKeys {
        public final static String PERCENT = "PERCENT";
        public final static String ERROR = "ERROR";
    }
}
