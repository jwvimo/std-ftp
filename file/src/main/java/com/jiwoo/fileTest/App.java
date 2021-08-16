package com.jiwoo.fileTest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception{
        System.out.println( "Hello World!" );
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
//        String date = sdf.format(cal.getTime());
        String date = "20210801";
        
        /* ******************************************** */
        /* ftpClient 호출									*/
        /* ******************************************** */
        FtpClient ftpIvr = new FtpClient("localhost", 21, "user01", "user01");
        
        // 업로드 할 파일 경로
		// File file 			= new File("F:/test/uploaddddddddd.txt"); 
        // String uploadDir 	= "/testDir"; 
        // String uploadFilenm 	= "/test_"+date+".txt";
		 
        /* ******************************************** */
		/* ftp uplaod (업로드할파일, 업로드할 경로+업로드할 이름)  */
		/* ******************************************** */
        //boolean result = ftpIvr.upload(file, uploadDir + uploadFilenm);
        //System.out.println( "Ftp Result : " + result);

        /* ******************************************** */
		/* ftp delete (삭제할 경로)							*/
		/* ******************************************** */
        boolean deResult = ftpIvr.deletePastFile("/testDir/", date);
        System.out.println( "Ftp Result : " + deResult);
    }
}
