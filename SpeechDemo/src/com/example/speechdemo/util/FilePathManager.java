//===========================================================================
// Summary:
//		文件路径工具类。
// Usage:
//		Null
// Remarks:
//		Null
// Date:
//		2014-08-14
// Author:
//		Liu Xin (liuxin@wafa.com.cn)
//===========================================================================
package com.example.speechdemo.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FilePathManager {
	private static final boolean D = false;
	private static final String TAG = "FilePathManager";
	
	private static FilePathManager instance;

	public static FilePathManager getFilePathManager(Context _context) {
		if (instance == null) {
			instance = new FilePathManager();
			instance.init(_context);
		}
		return instance;
	}
	
	private FilePathManager() {		
	}
	

	private final static String App_Dir_Name = "speechdemo";
	private final static String UiCache_Dir_Name = "ui_cahce_agreement";
	private final static String Doc_Dir_Name = "doc";
	private final static String Json_Dir_Name = "json";
	private final static String DataBase_Dir_Name = "database";
	private final static String Setting_File_Name = "setting.data";
	private final static String Download_Dir_Name = "download";
	private final static String Update_Dir_Name = "update";
	
	private final static String Default_SdCard_Path = "/mnt/sdcard";
	
	private final static String Documnets_File_Name = "document.zip";
	private final static String Documnets_Dir_Name = "document";
	
	private String strSdPath;
	
	/**7
	 * 初始化操作
	 * @param context
	 */
	public void init(Context context) {
		initDirs(context);
	}

	/**
	 * 初始化目录
	 * @return
	 */
	private boolean initDirs(Context context) {
		try {
			boolean ret = initSdPath(context);
			
			if (!ret) {
				return false;
			}

			ensureDirs();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void ensureDirs() {
		mkdir(getAppDirPath());
//		mkdir(getUiCahceDirPath());
//		mkdir(getDocDirPath());
//		mkdir(getJsonDirPath());
//		mkdir(getDataBaseDirPath());
	}
	
	/**
	 * 创建目录
	 * @param strDirPath
	 * @return
	 */
	private boolean mkdir(String strDirPath) {
		try {
			if (isInvalidStr(strDirPath)) {
				return false;
			}
			
			File dir = new File(strDirPath);
			if (dir.exists() && dir.isDirectory()) {
				return true;
			}
			
			dir.mkdirs();
			
			if (dir.exists() && dir.isDirectory()) {
				return true;
			}
			
			//	尝试2次创建目录
			return dir.mkdirs();
		} catch (Exception  e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 是否是无效的字符
	 * @return
	 */
	public boolean isInvalidStr(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	/**
	 * 初始化sd卡目录
	 * @return
	 */
	public boolean initSdPath(Context context) {
		if (D) {
			new Exception("initSdPath").printStackTrace();
		}
		try {
			File sd = Environment.getExternalStorageDirectory();
			if (sd != null && sd.canWrite()) {
				strSdPath = sd.getAbsolutePath();
				return true;
			}
			
			sd = context.getFilesDir();
			if (sd != null && sd.canWrite()) {
				strSdPath = sd.getAbsolutePath();
				return true;
			}
			
			sd = new File(Default_SdCard_Path);
			if (sd.canWrite()) {
				strSdPath = Default_SdCard_Path;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 获取sd卡目录路径
	 * @return
	 */
	public String getSdDirPath() {
		if (D) {
			Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>5 strSdPath=" + strSdPath);
		}
		return strSdPath;
	}
	
	/**
	 * 获取应用目录路径
	 * @return
	 */
	public String getAppDirPath() {
		if (isInvalidStr(getSdDirPath())) {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>4");
			}
			return null;
		} else {
			return getSdDirPath() + File.separator + App_Dir_Name;
		}
	}
	
	/**
	 * 获取ui缓存数据目录路径
	 * @return
	 */
	public String getUiCahceDirPath() {
		if (isInvalidStr(getAppDirPath())) {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>3");
			}
			return null;
		} else {
			return getAppDirPath() + File.separator + UiCache_Dir_Name;
		}
	}
	
	/**
	 * 获得文档目录路径
	 * @return
	 */
	public String getDocDirPath() {
		if (isInvalidStr(getUiCahceDirPath())) {
			return null;
		} else {
			return getUiCahceDirPath() + File.separator + Doc_Dir_Name;
		}
	}

	public String getDownloadPath() {
		if (isInvalidStr(getUiCahceDirPath())) {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>1");
			}
			return null;
		} else {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>2");
			}
			return getAppDirPath() + File.separator + Download_Dir_Name;
		}
	}
	
	public String getUpdatePath() {
		if (isInvalidStr(getUiCahceDirPath())) {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>1");
			}
			return null;
		} else {
			if (D) {
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>2");
			}
			return getAppDirPath() + File.separator + Update_Dir_Name;
		}		
	}
	
	/**
	 * 获得json数据路径
	 * @return
	 */
	public String getJsonDirPath() {
		if (isInvalidStr(getUiCahceDirPath())) {
			return null;
		} else {
			return getUiCahceDirPath() + File.separator + Json_Dir_Name;
		}
	}
	
	/**
	 * 获得json数据路径
	 * @return
	 */
	public String getSettingPath() {
		if (isInvalidStr(getAppDirPath())) {
			return null;
		} else {
			return getAppDirPath() + File.separator + Setting_File_Name;
		}
	}
	
	/**
	 * 获得数据库路径
	 * @return
	 */
	public String getDataBaseDirPath() {
		if (isInvalidStr(getUiCahceDirPath())) {
			return null;
		} else {
			return getUiCahceDirPath() + File.separator + DataBase_Dir_Name;
		}
	}
	
	public String getSourceDocumentsFile() {
		return Documnets_File_Name;
	}
	
	public String getDocumentsDirPath() {
		if (isInvalidStr(getAppDirPath())) {
			return null;
		} else {
			return getAppDirPath() + File.separator + Documnets_Dir_Name;
		}
	}
	
	public String getLocalDocumentFile() {
		if (isInvalidStr(getAppDirPath())) {
			return null;
		} else {
			return getAppDirPath() + File.separator + Documnets_File_Name;
		}
	}
	
	/** 
	 * 删除目录（文件夹）以及目录下的文件 
	 * @param   sPath 被删除目录的文件路径 
	 * @return  目录删除成功返回true，否则返回false 
	 */  
	public static boolean deleteDirectory(String sPath) {  
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) {  
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	    if (!flag) return false;  
	    //删除当前目录 
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	}
	
	/** 
	 * 删除单个文件 
	 * @param   sPath    被删除文件的文件名 
	 * @return 单个文件删除成功返回true，否则返回false 
	 */  
	public static boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag = true;  
	    }  
	    return flag;  
	}  
	
	/** 
	 *  根据路径删除指定的目录或文件，无论存在与否 
	 *@param sPath  要删除的目录或文件 
	 *@return 删除成功返回 true，否则返回 false。 
	 */  
	public static boolean deleteFolder(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 判断目录或文件是否存在  
	    if (!file.exists()) {  // 不存在返回 false  
	        return flag;  
	    } else {  
	        // 判断是否为文件  
	        if (file.isFile()) {  // 为文件时调用删除文件方法  
	            return deleteFile(sPath);  
	        } else {  // 为目录时调用删除目录方法  
	            return deleteDirectory(sPath);  
	        }  
	    }  
	}

}