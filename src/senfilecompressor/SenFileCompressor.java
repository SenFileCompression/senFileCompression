/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senfilecompressor;

import java.io.IOException;
import java.io.File;
import org.apache.commons.io.FileUtils; //il faut ajouter le jar correspondant au projet
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dieylany
 */
public class SenFileCompressor {

    public static void writeFileMetaData(Path file, SeekableByteChannel sbc) throws IOException{
        long size = Files.size(file);
        String fileHeader = file.toFile().getName() + "," + Long.toString(size)+"\n";
        byte[] bytes = fileHeader.getBytes();
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        sbc.write(buf);
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
    public static void addFileToArchive(Path src, SeekableByteChannel sbcDst) throws IOException{
                    SeekableByteChannel sbcSrc = Files.newByteChannel(src);
                    writeFileMetaData(src, sbcDst);
                    writeFileData(sbcSrc, sbcDst);         
    }
    
    public static void desarchiver(Path archive, int nbfichiers){
              // Path dest = Paths.get("archived.txt");
        
        try(
                SeekableByteChannel src = Files.newByteChannel(archive);
           )
        {
            
            int nb = 0;
            int x = 0;
            String line0 = Files.readAllLines(archive).get(0);
            String[] parts = line0.split(",");
            int taillefichier1 = Integer.parseInt(parts[1]); // 034556
            System.out.println("Nom du fichier "+parts[0]);
            System.out.println("Taille "+taillefichier1);
            OpenOption[] options;
            options = new OpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING };
            SeekableByteChannel dest;
            dest = Files.newByteChannel(Paths.get("src/"+parts[0]),options);
            int z = taillefichier1+line0.length();/*pour copier jusqu'a la fin en tenant compte qu'on a aussi lu la ligne 
                                                       qui contient le nom et la taille*/
            ByteBuffer buf = ByteBuffer.allocate(z);
            ByteBuffer bb;
            

            while((x = src.read(buf))>0)
            {
                SeekableByteChannel dest2;
                dest2 = Files.newByteChannel(Paths.get("src/"+parts[0]),options);
                byte[] bytes = Arrays.copyOfRange(buf.array(), line0.length(), z);
                bb = ByteBuffer.wrap(bytes);
                dest.write(bb);
                if(x==z) break;
                
                buf.flip();  
            }
            
            /*for(int i=1;i<nbfichiers;i++){
                byte[] bytes = Arrays.copyOfRange(buf.array(), z, z);
                bb = ByteBuffer.wrap(bytes);
                dest.write(bb);
                if(x==z) break;
                
                buf.flip();  
            }*/
            
             
        }catch(IOException e){
            System.out.println("Une exception: "+e);
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("mon-fichier.txt");
        Path src2 = Paths.get("001-al-fatihah.mp3");
        Set<Path> files = new HashSet<Path>();
        files.add(src);
        files.add(src2);
        
        Path archived = Paths.get("archived.temp");
        // Create the set of options for appending to the file.
        OpenOption[] options;
        options = new OpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING };
            
        try(SeekableByteChannel sbcDst = Files.newByteChannel(archived, options)){
            for(Path file: files)
                addFileToArchive(file, sbcDst);
            File file = new File("archived.temp");
            String content = FileUtils.readFileToString(file, "ISO8859_1");
            FileUtils.writeStringToFile(file, content, "UTF-8");//on change l'encodage du fichier afin d'être en mesure de lire dessus
                
        }catch(IOException e){
            System.out.println("Une exception: "+e);
        }
        
        System.out.println(Files.size(Paths.get("monfichier2.txt"))+" La taille de monfichier2.txt: ");
        System.out.println(Files.size(Paths.get("mon-fichier.txt"))+" La taille de mon-fichier.txt: ");
        System.out.println("Le nombre de fichiers à archiver: " + files.size());
        desarchiver(Paths.get("archived.temp"),files.size());
    }
    
}
