
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.nio.file.attribute.FileTime;

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
          System.out.println("OK: File found!");
          File remoteFile = new File(remoteFilePathString);
          if(remoteFile.exists()){
            System.out.println("OK: Destination file found!");
            Path remoteFilePath = Paths.get(remoteFilePathString);
            Path localFilePath = Paths.get(localPathString);

            if(timeCheck(localFilePath,remoteFilePath)){
              System.out.println("Working: replacing!");
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

  public static BasicFileAttributes attrFinder(Path p){
    BasicFileAttributes attr = null;
    try{
      attr = Files.readAttributes(p,BasicFileAttributes.class);
    }catch(Exception e){
      System.out.println(e);
    }

    return attr;
  }

  public static boolean timeCheck(Path local, Path remote){
    try{
      FileTime localFileTime = Files.getLastModifiedTime(local);
      FileTime remoteFileTime = Files.getLastModifiedTime(remote);

      System.out.println("Last Modified Time: "+localFileTime.toString());
      System.out.println("Last Modified Time: "+remoteFileTime.toString());

      if(localFileTime.compareTo(remoteFileTime)>0){
        System.out.println("File need to be replaced!");
        return true;
      }else{
        System.out.println("Remote file doesnt need to be replaced!");
        return false;
      }
    }catch(Exception e){
      System.out.println(e);
      return false;
    }
  }






}
