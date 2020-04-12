
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

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
      String path = f.getAbsolutePath();
      String subPath = path.substring(sPath.length(),path.length());

      System.out.println("Removed source: "+subPath);

      String remoteFilePath = dPath+subPath;
      System.out.println("Destination remote file: "+remoteFilePath);

      if(f.isFile()){
        try{
          System.out.println("OK: File found!");
          File remoteFile = new File(remoteFilePath);
          if(remoteFile.exists()){
            System.out.println("OK: Destination file found!");
            Path remoteTempFile = Paths.get(remoteFilePath);
            Path tempFile = Paths.get(path);
            BasicFileAttributes remoteAttr = Files.readAttributes(remoteTempFile,BasicFileAttributes.class);
            BasicFileAttributes tempAttr = Files.readAttributes(tempFile,BasicFileAttributes.class);

          }else{
            System.out.println("Error: Destination file doesnt Exists");
          }
        }catch (Exception e){
          System.out.println(e);
        }
      }else if(f.isDirectory()){
        System.out.println("OK: Folder found! - "+f.getName());
        fileFinder(f.getAbsolutePath(),remoteFilePath);
      }else{
        System.out.println("Error: neither file nor a folder!");

      }
    }

    return;
  }
}
