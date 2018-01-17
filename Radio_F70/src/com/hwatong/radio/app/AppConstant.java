package com.hwatong.radio.app;

/**
 * 应用常量类
 * @author Ljl
 *
 */
public class AppConstant {
	public class PlayerMsg {
		public static final int PLAY_MSG = 1;	  //播放某一频道
		public static final int STOP_MSG = 2;	  //停止音频线程
		public static final int STAR_MSG = 3;	  //开启音频线程
		public static final int UPDATE_MSG = 4;	  //搜索收音机强频
		public static final int PRIVIOUS_MSG = 5; //切换到前一个强频道
		public static final int NEXT_MSG = 6;	  //切换到下一个强频道
		public static final int RESUME_MSG = 7;	  //回到收音机界面时申请焦点恢复播放
		public static final int LUANCHER_STEP_MSG = 8;	  //用于提供给主界面的步进操作
		public static final int FISTUPDATE_MSG = 9;	  //搜索收音机强频
		public static final int CANCEL_MSG = 10;	  //取消搜索
		public static final int INITDATA = 11;	  //更换列表显示
		public static final int CHANGECOLLECTION = 12;//改变收藏列表
		public static final int MEMORYSTAR_MSG = 13;//提供给系统记忆开启收音机直接播放
	}
}
