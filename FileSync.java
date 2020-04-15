
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
// import java.nio.file.Paths;
// import java.nio.file.Path;
// import java.nio.file.attribute.BasicFileAttributes;
// import java.nio.file.attribute.FileTime;
// import java.util.ArrayList;
// import java.security.DigestInputStream;

public class FileSync{

  public static void main(String []args){

    String sourcePath = "M:\\_working_repo\\java\\filesync\\test\\source";
    String destinationPath = "M:\\_working_repo\\java\\filesync\\test\\destination";

/*    if(!source.exists()){
      System.out.println("Error: Source folder not found!");
      return;
    }

    if(!destination.exists()){
      System.out.println("Error: Destination folder not found!");
      return;
    }
*/
    fileFinder(sourcePath,destinationPath);

    //String sourcePath = "./test/source";
    //Path source = Paths.get(sourcePath);


  }

  public static void fileFinder(String sPath, String dPath){
    File source = new File(sPath);
    File destination = new File(dPath);

    File[] sourceStruct = source.listFiles();
    //File[] destinationStruct = destination.listFiles();

    for(File f : sourceStruct){
      String localPathString = f.getAbsolutePath();
      String subPath = localPathString.substring(sPath.length(),localPathString.length());

      System.out.println("Removed source: "+subPath);

      String remoteFilePathString = dPath+subPath;
      System.out.println("Destination remote file: "+remoteFilePathString);

      if(f.isFile()){
        try{
          long fileSize = f.length();
          System.out.println("OK: File found! size: "+fileSize);
          File remoteFile = new File(remoteFilePathString);
          if(remoteFile.exists()){
            System.out.println("OK: Destination file found!");

            // Path remoteFilePath = Paths.get(remoteFilePathString);
            // Path localFilePath = Paths.get(localPathString);

            if(timeCheck(localPathString,remoteFilePathString)>0){
              // BasicFileAttributes remoteFileAttr = attrFinder(remoteFilePath);
              // BasicFileAttributes localFileAttr = attrFinder(localFilePath);

              System.out.println("Working: Time Check!");

              if(checkSum(localPathString,remoteFilePathString)){
                System.out.println("Working: Md5 Checksum success! No need to replace!");
                continue;
              }else{
                System.out.println("Working: Md5 Checksum failed! NEED to be replaced!");
                work(localPathString,remoteFilePathString);
              }

            }

          }else{
            System.out.println("Error: Destination file doesnt Exists");
          }
        }catch (Exception e){
          System.out.println(e);
        }
        System.out.println("-----------------------------------------------------");
      }else if(f.isDirectory()){
        System.out.println("OK: Folder found! - "+f.getName());
        fileFinder(f.getAbsolutePath(),remoteFilePathString);

      }else{
        System.out.println("Error: neither file nor a folder!");
        System.out.println("-----------------------------------------------------");
      }
    }


    return;
  }

  public static void work(String l, String r){
    try{
      Path local = Paths.get(l), remote = Paths.get(r);
      if(Files.copy(local,remote,StandardCopyOption.COPY_ATTRIBUTES,StandardCopyOption.REPLACE_EXISTING) != null){
        System.out.println("Copied with success!");
      }
    }catch(Exception e){
      e.printStackTrace();
    }


  }


  public static BasicFileAttributes attrFinder(String s){
    BasicFileAttributes attr = null;
    try{
      Path p = Paths.get(s);
      attr = Files.readAttributes(p,BasicFileAttributes.class);
    }catch(Exception e){
      System.out.println(e);
    }
    return attr;
  }

  public static int timeCheck(String l,String r){
    try{
      Path local = Paths.get(l), remote = Paths.get(r);
      FileTime localFileTime = Files.getLastModifiedTime(local);
      FileTime remoteFileTime = Files.getLastModifiedTime(remote);

      System.out.println("Last Modified Time: "+localFileTime.toString());
      System.out.println("Last Modified Time: "+remoteFileTime.toString());

      if(localFileTime.compareTo(remoteFileTime)>0){
        System.out.println("Local file is newer!");
        return 1;
      }else if(localFileTime.compareTo(remoteFileTime)<0){
        System.out.println("Remote file is newer!");
        return -1;
      }else{
        System.out.println("Both same!");
        return 0;
      }
    }catch(Exception e){
      System.out.println("Error: Both same!");
      System.out.println(e);
      return -2;
    }
  }

  public static boolean checkSum(String localPath, String remotePath){
    try{
      String local = getMD5Checksum(localPath);
      String remote = getMD5Checksum(remotePath);
      if(local.equals(remote)){
        return true;
      }else{
        return false;
      }
    }catch(Exception e){
      e.printStackTrace();
      return false;
    }
  }

  public static byte[] createChecksum(String filename) throws Exception {
      InputStream fis =  new FileInputStream(filename);

      byte[] buffer = new byte[1024];
      MessageDigest complete = MessageDigest.getInstance("MD5");
      int numRead;

      do {
          numRead = fis.read(buffer);
          if (numRead > 0) {
              complete.update(buffer, 0, numRead);
          }
      } while (numRead != -1);

      fis.close();
      return complete.digest();
  }

  public static String getMD5Checksum(String filename) throws Exception {
    byte[] b = createChecksum(filename);
    String result = "";
    for (int i=0; i < b.length; i++) {
        result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
    }
    return result;
  }



}
