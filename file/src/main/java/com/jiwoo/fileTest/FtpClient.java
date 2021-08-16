package com.jiwoo.fileTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpClient {
	private String 	serverIp;
	private int 	serverPort;
	private String 	user;
	private String 	password;
	
	public FtpClient(String serverIp, int serverPort, String user, String password) {
		this.serverIp 	= serverIp;
		this.serverPort = serverPort;
		this.user		= user;
		this.password   = password;
	}
	
	/**
	 * 파일 업로드
	 */
	public boolean upload(File fileObj, String fileDir) throws SocketException, IOException, Exception{
		FileInputStream fis = null;
		FTPClient ftpClient = new FTPClient();
		
		try {
			/* *************************************** */
			/* ftp 연결 및 로그인						   */
			/* *************************************** */
			ftpClient.connect(serverIp, serverPort);	// ftp 연결
			ftpClient.setControlEncoding("utf-8");		// ftp 인코딩 설정
			int reply = ftpClient.getReplyCode();		// 응답코드 수신
			
			if(!FTPReply.isPositiveCompletion(reply)) {
				// 응답이 false라면 연결 해제하고 exception 발생
				ftpClient.disconnect();
				throw new Exception(serverIp + "FTP 서버 연결 실패");
			}
			
			ftpClient.setSoTimeout(1000 * 10);					// timeout 설정
			ftpClient.login(user, password);					// ftp 로그인
			ftpClient.changeWorkingDirectory(fileDir);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);		// 파일 타입 설정	
			ftpClient.enterLocalActiveMode();					// active 모드 설정

			/* *************************************** */
			/* ftp 연결 및 로그인						   */
			/* *************************************** */
			fis = new FileInputStream(fileObj);
			return ftpClient.storeFile(fileDir, fis);
		} finally {
			if(ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
			if(fis != null) {
				fis.close();
			}
		}
	}
	
	/**
	 * 지난 일자 파일 삭제
	 */
	public boolean deletePastFile(String dir, String date) throws IOException , Exception{
		boolean result 		= false;
		FTPClient ftpClient = new FTPClient();
		
		try {
			/* *************************************** */
			/* ftp 연결 및 로그인						   */
			/* *************************************** */
			ftpClient.connect(serverIp, serverPort);	// ftp 연결
			ftpClient.setControlEncoding("utf-8");		// ftp 인코딩 설정
			int reply = ftpClient.getReplyCode();		// 응답코드 수신
			
			if(!FTPReply.isPositiveCompletion(reply)) {
				// 응답이 false라면 연결 해제하고 exception 발생
				ftpClient.disconnect();
				throw new Exception(serverIp + "FTP 서버 연결 실패");
			}
			
			ftpClient.setSoTimeout(1000 * 10);					// timeout 설정
			ftpClient.login(user, password);					// ftp 로그인
			ftpClient.changeWorkingDirectory(dir);				// 디렉토리 이동
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);		// 파일 타입 설정	
			ftpClient.enterLocalActiveMode();					// active 모드 설정
			
			/* *************************************** */
			/* 디렉토리의 삭제 대상 파일 목록 가져오기			   */
			/* *************************************** */
			List<String> files = new ArrayList<>();
			List<String> directories = new ArrayList<>();
			getDeleteFileList(ftpClient, dir, date, files, directories);
			
			for(int i=0; i < files.size(); i++) {
				result = ftpClient.deleteFile(files.get(i));
			}
			
			// 향상된 for 문 사용 ver.
			// String file : files -> 리스트 files를 file 변수에 담는다
//			for(String file : files) {
//				// 테스트용 (123 포함된 파일 삭제)
//				if(file.contains("123")) {
//					result = ftpClient.deleteFile(file);
//				}
//			}
		} finally {
			if(ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
		}
		
		return result;
	}
	
	/**
	 * 삭제 대상인 FTP의 파일 리스트와 디렉토리 정보를 취득하는 함수
	 */
	private static boolean getDeleteFileList(FTPClient client, String cw, String date, List<String> files, List<String> directories) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// FTP의 디렉토리 커서를 이동한다.
		if (client.changeWorkingDirectory(cw)) {
			// 해당 디렉토리의 파일 리스트를 취득한다.
			for (FTPFile file : client.listFiles()) {
				if (file.isFile()) {
					Date fileDate = file.getTimestamp().getTime();				// 파일 업로드 일자
					Date inputDate = sdf.parse(date);							// 입력 일자 Date 타입 변환
					// 날짜 비교
					int diffDate;
					try {
						diffDate = diffDate(fileDate, inputDate, -5);
						System.out.println("■■■■■ 삭제 대상입니다"+ diffDate );
						if( diffDate == 0 || diffDate < 0 ) {
							// 삭제 대상 add
							System.out.println("■■■■■ 삭제 대상입니다"+ (cw + file.getName()));
							files.add(cw + file.getName());
						}else if( diffDate > 0 ) {
							System.out.println("■■■■■ 삭제 대상이 아닙니다"+ (cw + file.getName()));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					// 디렉토리인 경우 하위 탐색을 시작한다.
					if (!getDeleteFileList(client, cw + file.getName() + File.separator, date, files, directories)) {
						return false;
					} else {
						// directories 리스트에 디렉토리 경로를 추가한다.
						directories.add(cw + file.getName() + File.separator);
					}
				}
			}
			// * 참고 FTP의 디렉토리 커서를 상위로 이동하는 함수
			// client.changeToParentDirectory();
			// FTP의 디렉토리 커서를 이동한다.
			return client.changeWorkingDirectory(File.separator);
		}
		// 커서 이동에 실패하면 false를 리턴한다.
		return false;
	}
	
	/**
	 * n년전 일자와 입력 날짜 비교 함수
	 * 파일 업로드 일자 <= (현재날짜 - 5년)
	 * param : fileUpDate - 파일업로드 일자, inputDate - 입력 일자, n - n년전
	 */
	private static int diffDate(Date fileUpDate, Date inputDate, int n) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, n);			// n년전 일자(Calendar)
		
		Date pyear = cal.getTime();			// n년전 일자(Date)
		Date today = new Date();			// 오늘 일자 (Date)
//		String today1 = "20210817";			// 테스트용 데이터
//		Date today = sdf.parse(today1);		// 테스트용 데이터
		
//		System.out.println("■■■■■ n년전 일자 " + pyear);
//		System.out.println("■■■■■ 오늘 일자 " + today);
		System.out.println("■■■■■ 입력 일자 " + inputDate); 
		System.out.println("■■■■■ 업로드 일자 " + fileUpDate);
		
//		A.compareTo(B) 
//		A = B  => 0  날짜 동일
//		A < B  => -1 날짜 지남
//		A > B  => 1  날짜 안지남
		int compare = sdf.format(fileUpDate).compareTo(sdf.format(inputDate));
		// fileUpDate(업로드일자)와 pyear(n년전일자) 비교
//		int compare = sdf.format(fileUpDate).compareTo(sdf.format(pyear)); 
		return compare;
	}
}
