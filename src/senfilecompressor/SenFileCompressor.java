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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Path src = Paths.get("mon-fichier.txt");
        Path encoded = Paths.get("encoded.temp");
        // Create the set of options for appending to the file.
        Set<OpenOption> options = new HashSet<OpenOption>();
        options.add(APPEND);
        options.add(CREATE);
            
        try(
            SeekableByteChannel sbc = Files.newByteChannel(src); 
            SeekableByteChannel sbc2 = Files.newByteChannel(encoded, options);
        ){
            ByteBuffer buf = ByteBuffer.allocate(10);
            long size = Files.size(src);
            String fileHeader = src.toFile().getName() + "," + Long.valueOf(size).toString()+"\n";
            byte[] temp = fileHeader.getBytes();
            ByteBuffer tempBuf = ByteBuffer.wrap(temp);
            sbc2.write(tempBuf);
            while(sbc.read(buf)>0){
                byte[] bytes = buf.array();
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                sbc2.write(bb);
                /*for(byte b: bytes){
                    System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                }
                System.out.println();*/
                buf.flip();
            }
            
        }catch(IOException e){
            System.out.println("Une exception: "+e);
        }
    }
    
}
