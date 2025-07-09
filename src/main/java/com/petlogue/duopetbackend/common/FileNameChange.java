package com.petlogue.duopetbackend.common;

import java.text.SimpleDateFormat;

public class FileNameChange {
	
	public static String change(String originalFileName, String formatStr) {
		String renameFileName = null;
		
		//바꿀 파일명에 대한 문자열 만들기
		//전달받은 포멧문자열을 이용해서 만듦 => 년월일시분초 형식의 포멧을 이용할 것이므로
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		
		//현재 시스템에서 날짜와 시간정보를 가지고와서 변경할 파일명을 만들기함
		renameFileName = sdf.format(new java.sql.Date(System.currentTimeMillis()));
		
		//원본 파일의 확장자를 추출해서, 바꿀 파일명 뒤에 추가 연결함
		renameFileName += "." + originalFileName.substring(originalFileName.indexOf(".") + 1);
		
		return renameFileName;
	}
}
