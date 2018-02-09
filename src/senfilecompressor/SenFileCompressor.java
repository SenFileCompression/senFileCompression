/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senfilecompressor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dieylany
 */
public class SenFileCompressor {

    public static void writeFileMetaData(Path file, SeekableByteChannel sbcDst) throws IOException{
        long size = Files.size(file);
        String fileHeader = file.toFile().getName() + "," + Long.toString(size)+"\n";
        byte[] bytes = fileHeader.getBytes();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        sbcDst.write(buf);
    }
    public static int writeFileData(SeekableByteChannel src, SeekableByteChannel dst) throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(10);
        int nb = 0;
        int x = 0;
        ByteBuffer bb;
        while((x = src.read(buf))>0){
             nb += x;
             byte[] bytes = buf.array();
             bb = ByteBuffer.wrap(bytes);
             dst.write(bb);
             /*for(byte b: bytes){
                 System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
             }
             System.out.println();*/
             buf.flip();
        }
        return nb;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Path src = Paths.get("mon-fichier.txt");
        Path src2 = Paths.get("001-al-fatihah.mp3");
        Path encoded = Paths.get("encoded.temp");
        // Create the set of options for appending to the file.
        Set<OpenOption> options = new HashSet<OpenOption>();
        options.add(APPEND);
        options.add(CREATE);
            
        try(
            SeekableByteChannel sbcSrc = Files.newByteChannel(src);
            SeekableByteChannel sbcSrc2 = Files.newByteChannel(src2);
            SeekableByteChannel sbcDst = Files.newByteChannel(encoded, options);
        ){
          
            writeFileMetaData(src, sbcDst);
            writeFileData(sbcSrc, sbcDst);
            
            writeFileMetaData(src2, sbcDst);
            writeFileData(sbcSrc2, sbcDst);
        
        }catch(IOException e){
            System.out.println("Une exception: "+e);
        }
    }
    
}
