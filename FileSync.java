
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.awt.Desktop;
import java.util.ArrayList;

public class FileSync{

  public static void main(String []args){

    String sourcePath = "M:\\_working_repo\\java\\filesync\\test\\source";
    String destinationPath = "M:\\_working_repo\\java\\filesync\\test\\destination";

/*  System.out.println("====================================");
    System.out.println("Updating destination with new files!");
    System.out.println("===================================="); */

    //remoteUpdateNewFiles(sourcePath,destinationPath);

/*  System.out.println("========================");
    System.out.println("Updating source deletes!");
    System.out.println("========================"); */

    //remoteUpdateDeletes(sourcePath,destinationPath);

    ArrayList<A> jobs = new ArrayList<>();
    //ArrayList<A> toDelete = new ArrayList<>();
   
    findCopyJobs(sourcePath,destinationPath,jobs,0);
    findDeleteJobs(sourcePath,destinationPath,jobs,0);   

    for(A a: jobs){
      a.printA();
    }
  }

  public static class A{
    private File local;
    private File remote;
    private char job;
    private int indent;

    public A(File l, File r, char j, int i){
      this.local = l;
      this.remote = r;
      this.job = j;
      this.indent = i;
    }

    public File getLocal(){
      return this.local;
    }

    public File getRemote(){
      return this.remote;
    }

    public char getJob(){
      return this.job;
    }

    public int getIndent(){
      return this.indent;
    }

    public void setLocal(File f){
      this.local = f;
    }

    public void setRemote(File f){
      this.remote = f;
    }

    public void setJob(char j){
      this.job = j;
    }

    public void printA(){
      if(this.local != null && this.remote != null){
        System.out.println(this.indent+"-"+this.local.getParentFile().getName()+"->"+this.local.getName()+" <"+this.job+"> "+this.remote.getName());
      }else if(this.local != null && this.remote == null){
        System.out.println(this.indent+"-"+this.local.getParentFile().getName()+"->"+this.local.getName()+" <"+this.job+"> NULL");
      }else if(this.local == null && this.remote != null){
        System.out.println(this.indent+"-"+this.remote.getParentFile().getName()+"->"+"NULL <"+this.job+"> "+this.remote.getName());
      }     
    }
  }

  public static void findCopyJobs(String local,String remote, ArrayList<A> a,int in){
    File localDir = new File(local);
    File[] localStruct = localDir.listFiles();     
    
    for(File l : localStruct){
      String localPathString = l.getAbsolutePath();
      String subPath = localPathString.substring(local.length(),localPathString.length());
      String remoteFilePathString = remote+subPath;
      try{
        File remoteFile = new File(remoteFilePathString);
        if(l.isFile()){            
          if(remoteFile.exists()){
            if(timeCheck(localPathString,remoteFilePathString)>0 || !checkSum(localPathString,remoteFilePathString)){
              System.out.println("replace");
              a.add(new A(l,null,'r',in));
              continue;
            }
            a.add(new A(l,remoteFile,'=',in));
            continue;
          }
          a.add(new A(l,null,'+',in));
          continue;
        }else if(l.isDirectory()){          
          printTableLine(l.getName()," ",remoteFile.getName());
          findCopyJobs(l.getAbsolutePath(), remoteFilePathString,a,in+1);
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }

  }

  public static void findDeleteJobs(String local,String remote, ArrayList<A> a,int in){    
    File remoteDir = new File(remote);

    File [] remoteStruct = remoteDir.listFiles();    

    for(File r : remoteStruct){
      String remotePathString = r.getAbsolutePath();
      String subPath = remotePathString.substring(remote.length(),remotePathString.length());
      String localFilePathString = local+subPath;
      try{
        File localFile = new File(localFilePathString);
        if(r.isFile()){            
          if(!localFile.exists()){              
            a.add(new A(null,r,'-',in));
            continue;
          }
        }else if(r.isDirectory()){
          findDeleteJobs(localFilePathString,r.getAbsolutePath(),a,in+1);
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }
  }  

  public static void printTableLine(String l,String m,String r){
    String left = "| "+l;
    String right = r+" |";
    String middle = "\t| "+m+" |";
    System.out.println(left+middle+right);
    return;
  }

  public static void remoteUpdateNewFiles(String sPath, String dPath){
    File source = new File(sPath); 
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
              continue;
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

      System.out.println("Working: Time Check: Last Modified Time: "+localFileTime.toString());
      System.out.println("Working: Time Check: Last Modified Time: "+remoteFileTime.toString());

      if(localFileTime.compareTo(remoteFileTime)>0){
        System.out.println("Working: Time Check: Local file is newer!");
        return 1;
      }else if(localFileTime.compareTo(remoteFileTime)<0){
        System.out.println("Working: Time Check: Remote file is newer!");
        return -1;
      }else{
        System.out.println("Working: Time Check: Both same!");
        return 0;
      }
    }catch(Exception e){
      System.out.println("Working: Time Check: << Error >>");
      System.out.println(e);
      return -2;
    }
  }

  public static boolean checkSum(String localPath, String remotePath){
    try{
      String local = getMD5Checksum(localPath);
      String remote = getMD5Checksum(remotePath);
      if(local.equals(remote)){
        System.out.println("Wokring: Checksum: OK!");
        return true;
      }else{
        System.out.println("Wokring: Checksum: Failed!");
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
