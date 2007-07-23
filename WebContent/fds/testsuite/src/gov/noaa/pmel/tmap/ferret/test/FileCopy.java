package gov.noaa.pmel.tmap.ferret.test;

import java.io.*;

public class FileCopy {

   public static void copy(String source_name, String dest_name)
       throws IOException  {
       File source_file = new File(source_name);
       File destination_file = new File(dest_name);
       FileInputStream source = null;
       FileOutputStream destination = null;
       byte[] buffer;
       int bytes_read;
                                         
       try {
           // First make sure the specified source file
           // exists, is a file, and is readable.
           if (!source_file.exists() || !source_file.isFile())
                throw new FileCopyException("FileCopy: no such source file: "
                                            + source_name);
           if (!source_file.canRead())
                throw new FileCopyException("FileCopy: source file " 
                                             + "is unreadable: " + source_name);
                                       
           // If the destination exists, make sure it is a writeable file
           // and ask before overwriting it.  If the destination doesn't
           // exist, make sure the directory exists and is writeable.

            if (destination_file.exists()) {
                if (destination_file.isFile()) {
                     DataInputStream in = new DataInputStream(System.in);
                     String response;
                                                         
                     if (!destination_file.canWrite())
                             throw new FileCopyException("FileCopy: destination "
                                                          + "file is unwriteable: " + dest_name);
                }
                else
                     throw new FileCopyException("FileCopy: destination "
                                                  + "is not a file: " +  dest_name);
            }
            else {
                File parentdir = parent(destination_file);
                if (!parentdir.exists()){
                    parentdir.mkdirs();
                    if (!parentdir.exists()){
                        throw new FileCopyException("FileCopy: destination parent "
                                                  + "directory can not be created for: " + dest_name);
                    }
                }

                if (!parentdir.canWrite())
                     throw new FileCopyException("FileCopy: destination parent "
                                                  + "directory is unwriteable: " + dest_name);
            }
                                                 
            // If we've gotten this far, then everything is okay; we can
            // copy the file.
            source = new FileInputStream(source_file);
            destination = new FileOutputStream(destination_file);
            buffer = new byte[1024];
            while(true) {
                bytes_read = source.read(buffer);
                if (bytes_read == -1) break;
                   destination.write(buffer, 0, bytes_read);
            }
        }
        finally {
              // No matter what happens, always close any streams we've opened.
            if (source != null)
                try { source.close(); } catch (IOException e) {}
            if (destination != null)
               try { destination.close(); } catch (IOException e) {}
        }
    }
                                         
    // File.getParent() can return null when the file is specified
    //  without a directory or is in the root directory.  
    // This method handles those cases.
    private static File parent(File f) {
        String dirname = f.getParent();
        if (dirname == null) {
            if (f.isAbsolute()) 
                return new File(File.separator);
            else 
                return new File(System.getProperty("user.dir"));
        }
        return new File(dirname);
    }
                                       
    public static void main(String[] args) {
         if (args.length != 2)
             System.err.println("Usage: java FileCopy " 
                                + "<source file> <destination file>");
         else {
             try { 
                 copy(args[0], args[1]); 
             }
             catch (IOException e) {
                 System.err.println(e.getMessage()); }
             }
         }
    }

    class FileCopyException extends IOException {
         public FileCopyException(String msg) { super(msg); }
}

