/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package senfilecompressor;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
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

public class SenFileCompressor {

    public static void doc(){ //cette fonction sera appelée lorsque l'utilisateur saisira l'option -h
                System.out.println("Ceci est un programme qui permet l'archivage et la compression de fichiers.");
                System.out.println("1. Saisir java SenFileCompressor –c <liste fichiers à compresser> 2 va fournir en sortie un fichier d’extension « .sfc » qui regroupe, sous forme compressée, les différents fichiers fournis en paramètre.)");
                System.out.println("2. Saisir java SenFileCompressor –d fichierADecompresser.sfc va fournir en sortie l’intégralité des fichiers contenus dans l’archive donné en paramètre.");
                System.out.println("3. Saisir java SenFileCompressor –h pour obtenir l'aide sur l'utilisation du programme.");
    }
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
            int taillefichier1 = Integer.parseInt(parts[1]);
            OpenOption[] options;
            options = new OpenOption[] { WRITE, CREATE, TRUNCATE_EXISTING };
            SeekableByteChannel dest;
            dest = Files.newByteChannel(Paths.get("src/"+parts[0]),options);
            int z = taillefichier1+line0.length()+3;/*pour copier jusqu'a la fin en tenant compte qu'on a aussi lu la ligne 
                                                       qui contient le nom et la taille*/
            ByteBuffer buf = ByteBuffer.allocate(z);
            ByteBuffer bb;
            
            

            while((x = src.read(buf))>0)
            {
                nb+=x;
                byte[] bytes = Arrays.copyOfRange(buf.array(), line0.length()+1, z);
                bb = ByteBuffer.wrap(bytes);
                dest.write(bb);
                System.out.println("Nombre d'octets lus----------: "+nb);
                if(x==z) break;
                
                buf.flip();  
            }
            FileReader input = new FileReader("src/"+parts[0]);
            LineNumberReader count = new LineNumberReader(input);
            
               while (count.skip(Long.MAX_VALUE) > 0)
                {
                   // Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
                }

                int nblignes = count.getLineNumber()+1;//+1 car les index commencent par 0
                System.out.println("Le nombre de lignes de src/mon-fichier.txt: "+nblignes);
           /* for(int i=1;i<nbfichiers;i++){
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
        
        if(args.length==1){
            System.out.println("Veuillez donner en paramètre au moins deux fichiers");
            return;
        }
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
