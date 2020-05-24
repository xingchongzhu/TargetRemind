/**
 * Copyright (C) 2015~2050 by foolstudio. All rights reserved.
 * 
 * ��Դ�ļ��д��벻����������������ҵ��;�����߱�������Ȩ��
 * 
 * ���ߣ�������
 * 
 * �������䣺foolstudio@qq.com
 * 
*/

package com.wtach.stationremind.utils;

import android.os.Environment;

import java.io.File;

public interface IDef {
	
	public static final String App_Tag = "VehicleNumDemo";
	
	public static final int Req_Code_Camera = 8670;
	
	public static final String Extra_File_Path = "_file_path";
	public static final String Extra_Recog_Result = "_recog_result";
	public static final String Extra_Recog_License = "_recog_license";
	public static final String Extra_Recog_Color = "_recog_color";

	public static final String AUTO_MANAGER_FLAG = "auto_manager_flag";
	
	public static final int Default_Width = 640;
	public static final int Default_Height = 480;		
	
	

	public static final String App_Dir = Environment.getExternalStorageDirectory()+ File.separator+IDef.App_Tag;
	public static String MISSPATH = App_Dir + "/source/";
	public static String MISSPATHNAME = "miss.txt";
	
	public static final int Msg_Type_Unknown = -1;	
	public static final int Msg_Recognized_Finished = 137;	
	public static final int Msg_Net_Ready = 101;
	public static final int Msg_Data_Upd = 103;
	public static final int Msg_Empty = 107;
	public static final int Msg_Tick = 123;
	public static final int Msg_Auto_Tick = 124;

	public static final String DEFAULTCITY = "深圳";
	public static final String RECENT_TARGET_SELECT_LIST_KEY = "recent_target_select_list_key";

	public static final String TARGET_LIST_SPLIT = "#";
	public static final int  MAX_HISTORY_SIZE = 10;
}
