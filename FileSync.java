
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.util.Map;
import java.awt.Desktop;

public class FileSync{

  public static void main(String []args){

    String sourcePath = "M:\\_working_repo\\java\\filesync\\test\\source";
    String destinationPath = "M:\\_working_repo\\java\\filesync\\test\\destination";

    System.out.println("====================================");
    System.out.println("Updating destination with new files!");
    System.out.println("====================================");

    remoteUpdateNewFiles(sourcePath,destinationPath);

    System.out.println("========================");
    System.out.println("Updating source deletes!");
    System.out.println("========================");

    remoteUpdateDeletes(sourcePath,destinationPath);



  }

  public static void remoteUpdateNewFiles(String sPath, String dPath){
    File source = new File(sPath);
    File destination = new File(dPath);

    File[] sourceStruct = source.listFiles();

    for(File f : sourceStruct){

      String localPathString = f.getAbsolutePath();
      String subPath = localPathString.substring(sPath.length(),localPathString.length());
      String remoteFilePathString = dPath+subPath;

      System.out.println("Working: File: "+localPathString);
      //System.out.println("Removed source: "+subPath);
      //System.out.println("Destination remote file: "+remoteFilePathString);

      if(f.isFile()){
        try{
          //System.out.println("OK: File found!");
          File remoteFile = new File(remoteFilePathString);
          if(remoteFile.exists()){
            if(timeCheck(localPathString,remoteFilePathString)>0 && !checkSum(localPathString,remoteFilePathString)){
                System.out.println("Working: Copying: Md5 Checksum failed! NEED to be replaced!");
                copyNewFile(localPathString,remoteFilePathString);
            }
            System.out.println("Working: Skipping: No need to be replaced!");
          }else{
            System.out.println("Working: Copying: Destination file does not exist!");
            copyNewFile(localPathString,remoteFilePathString);
          }
        }catch (Exception e){
          e.printStackTrace();
        }

        System.out.println("-----------------------------------------------------");
      }else if(f.isDirectory()){

        System.out.println("OK: Folder found: "+f.getName());
        try{
          File remoteFile = new File(remoteFilePathString);

          if(!remoteFile.exists()){
            Path newDir = Files.createDirectory(Paths.get(remoteFilePathString));
            System.out.println("Working: New Dir created: "+newDir.toString());
          }

          remoteUpdateNewFiles(f.getAbsolutePath(),remoteFilePathString);

        }catch(Exception e){
          e.printStackTrace();
        }
      }else{
        System.out.println("Working: Error: neither file nor a folder!");
        System.out.println("-----------------------------------------------------");
      }
    }

    return;
  }

  public static void remoteUpdateDeletes(String sPath, String dPath){
    File source = new File(sPath);
    File destination = new File(dPath);
    File[] destinationStruct = destination.listFiles();

    for(File f: destinationStruct){
      String localPathString = f.getAbsolutePath();
      String subPath = localPathString.substring(dPath.length(),localPathString.length());
      String remoteFilePathString = sPath+subPath;

      System.out.println("Working: File: "+localPathString);

      if(f.isFile()){
        try{
          System.out.println("Working: OK: File found!");
          File remoteFile = new File(remoteFilePathString);
          if(!remoteFile.exists()){
            System.out.println("Working: Warning: Removing: Source file not found.");
            deleteFile(f);
          }else{
            System.out.println("Working: Skipping: Source file exists.");
          }
        }catch (Exception e){
          e.printStackTrace();
        }

      }else if(f.isDirectory()){
        System.out.println("Working: OK: Folder found: "+f.getName());
        try{
          File remoteFile = new File(remoteFilePathString);

          if(!remoteFile.exists()){
            System.out.println("Working: Warning: Directory does not exist!");
            deleteFile(f);
          }else{
            remoteUpdateDeletes(remoteFilePathString,f.getAbsolutePath());
          }
        }catch(Exception e){
          e.printStackTrace();
        }


      }else{
        System.out.println("Working: << Error >>: neither file nor a folder!");

      }
      System.out.println("-----------------------------------------------------");
    }

    return;
  }

  public static void copyNewFile(String l, String r){
    try{
      Path local = Paths.get(l), remote = Paths.get(r);
      if(Files.copy(local,remote,StandardCopyOption.COPY_ATTRIBUTES,StandardCopyOption.REPLACE_EXISTING) != null){

        if(checkSum(l,r)){
          System.out.println("Working: Copy New File: Copied with success!");
        }else{
          System.out.println("Working: Copy New File: << Error >>!");
        }
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

  public static void deleteFile(File f){
    try{
      Desktop d = Desktop.getDesktop();
      if(d.isSupported(Desktop.Action.MOVE_TO_TRASH)){
        if(d.moveToTrash(f)){
          System.out.println("Working: Delete File: OK: Moved to Recycle Bin successfully!");
        }else{
          System.out.println("Working: Delete File: << Error >>: Failed to Move to Recycle Bin!");
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }

  }

  public static int timeCheck(String l,String r){
    try{
      Path local = Paths.get(l), remote = Paths.get(r);
      FileTime localFileTime = Files.getLastModifiedTime(local);
      FileTime remoteFileTime = Files.getLastModifiedTime(remote);

      //System.out.println("Time Check: Last Modified Time: "+localFileTime.toString());
      //System.out.println("Time Check: Last Modified Time: "+remoteFileTime.toString());

      if(localFileTime.compareTo(remoteFileTime)>0){
        //System.out.println("Time Check: Local file is newer!");
        return 1;
      }else if(localFileTime.compareTo(remoteFileTime)<0){
        //System.out.println("Time Check: Remote file is newer!");
        return -1;
      }else{
        //System.out.println("Time Check: Both same!");
        return 0;
      }
    }catch(Exception e){
      System.out.println("Time Check: << Error >>");
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
