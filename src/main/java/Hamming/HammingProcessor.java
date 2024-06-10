package Hamming;

import Utils.DoubleErrorException;

import java.io.*;
import java.util.Random;


//CDESC esta clase maneja todas las operaciones relacionadas con Hamming
public class HammingProcessor {





    public HammingProcessor(){
    }

    //VDESC las variables manejan la cantidad de bits total, de control, de espacio y lo en bytes
    private  int nBits;
    private int nBytes;
    private int cBits;
    private float cBytes;
    private int nHB;
    private final int sbits =32;
    private final int sbytes =sbits/8;




    //CODESC Esta funcion genera un logaritmo "con techo" en base 2
    private static int iLog2(int n){
        int r = 31 - Integer.numberOfLeadingZeros(n);
        if (n> Math.pow(2,r)){
            r++;
        }
        return r;
    }

    //CODESC setea el tamaño del bloque de hamming
    public void setBlockSize(int nBits){
        this.nBits = nBits;
        this.setInternalValues();
    }

    //CODESC setea los valores internos a su valor necesario
    private void setInternalValues(){
        this.nBytes=this.nBits/8;
        this.cBits= HammingProcessor.iLog2(this.nBits);
        this.cBytes=((float)cBits)/8;
    }

    //CODESC Lee un archivo sin incluir su tamaño ni su extension
    private byte[] outRead(String pathName) throws IOException,FileNotFoundException {
        File f1 = new File(pathName);
        FileInputStream fis = new FileInputStream(f1);
        byte[] bin = new byte[(int)f1.length()];
        fis.read(bin);
        fis.close();
        return bin;
    }

    //CODESC lee un archivo introduciendo en los primeros bytes su tamaño(en bytes), la longitud de su extension(max 255) y la extension
    private byte[] inRead(String pathName) throws  IOException , FileNotFoundException{
        File f1 = new File(pathName);
        FileInputStream fis = new FileInputStream(f1);
        String fileExtension = pathName.substring(pathName.lastIndexOf('.')+1);
        int extensionSize= fileExtension.length();
        byte[] bin = new byte[(int)f1.length()+sbytes+1+extensionSize];
        int inSize = ((int)f1.length())+extensionSize+1+sbytes;
        int i;
        for (i=0;i<sbytes;i++){
            byte auxByte = (byte)(inSize>>>((sbytes-i-1)*8));
            bin[i]=auxByte;
        }
        bin[i]=(byte)extensionSize;
        i++;
        int j=i;
        while (i<(j+extensionSize)){
            bin[i]=(byte)fileExtension.charAt(i-j);
            i++;
        }
        fis.read(bin,i,bin.length-i);
        return bin;
    }

    //CODESC escribe un arreglo de bytes en un archivo
    private void inWrite(byte[] bout, String pathName) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(new File(pathName));
        fos.write(bout);
        fos.close();
    }

    //CODESC escribe un arreglo de bytes en un archivo, remueve las primeras posiciones(que poseen tamaño y extension del archivo)
    private void outWrite(byte [] bout,String pathName) throws IOException, FileNotFoundException{
        FileOutputStream fos = new FileOutputStream(new File(pathName));
        String extension=pathName.substring(pathName.lastIndexOf('.')+1);
        fos.write(bout,sbytes+1+extension.length(),bout.length-sbytes-1-extension.length());
        fos.close();
    }

    //CODESC humminiza el archivo
    private byte[] hamminize(byte[] bin){
        float aux2 = cBytes+(1f/8f);
        int nHB = (int)Math.ceil(bin.length/(nBytes - aux2));
        byte [] binter = new byte[nHB* nBytes];
        long cin =0;
        for (int k=0; k<nHB;k++){
            int m=k* nBytes;
            for (int i = 1; i< cBits; i++){
                try{
                    int h= (int)(Math.pow(2,i+1)-2);
                    for (int j = (int)Math.pow(2,i);j<=h;j++ ){
                        binter[m+(j/8)]=(byte) (binter[m+(j/8)]| ((bin[(int)(cin/8)]&0x1<<(cin%8))>>cin%8)<<j%8);
                        cin++;
                    }
                }
                catch(IndexOutOfBoundsException e){
                    break;
                }
            }
        }

        for (int i = 0; i <nHB;i++){
            int aux3=0;
            int aux=0;
            for (int j = 0;j< nBits;j++){
                int l = i* nBytes +(j/8);
                int m = j%8;
                aux = aux ^ (((binter[l]>>>m)&0x1)*(j+1));
                aux3+=((binter[l]>>>m)&0x1);
            }
            for(int k = 0; k< cBits; k++){
                int z =((1<<k)-1);
                int l = (i* nBytes) +(z/8);
                int m = (aux>>>k)&0x1;
                int n = m<<(z%8);
                binter[l]=  (byte)(binter[l] | n);
                aux3+= ((binter[l]>>>z%8)&0x1);
            }
            aux3=aux3%2;
            binter[((i*nBytes)+((nBits-1)/8))] = (byte)(binter[(i*nBytes)+((nBits-1)/8)] ^ (aux3<<((nBits-1)%8)));
        }
        return binter;
    }

    //CODESC deshumminiza el archivo(no corrige los errores)
    private byte[] deHamminize(byte[] bin, StringBuilder extensionBuilder){
        extensionBuilder.delete(0,extensionBuilder.length());
        nHB = bin.length/nBytes;
        byte[] auxArr = new byte[sbytes+1];
        int counter= sbits+8;
        for(int k =0; k<nHB;k++){
            try {
                for(int i=1;i<cBits;i++){
                    int aux= (int)Math.pow(2,i);
                    for (int j = aux;j<=(aux*2)-2;j++){
                        int aux2= (sbits+8)-counter;
                        auxArr[aux2/8] = (byte)(auxArr[aux2/8] | ((bin[(k* nBytes)+(j/8)]>>>j%8)&0x1)<<aux2%8);
                        counter--;
                    }
                }
            }
            catch (IndexOutOfBoundsException e){
                break;
            }
        }
        int extensionSize = (auxArr[4]&0xFF);

        auxArr = new byte[sbytes+1+extensionSize];

        int aux3= sbytes+1+extensionSize*8;
        counter=aux3;
        for(int k =0; k<nHB;k++){
            try {
                for(int i=1;i<cBits;i++){
                    int aux= (int)Math.pow(2,i);
                    for (int j = aux;j<=(aux*2)-2;j++){
                        int aux2= aux3-counter;
                        auxArr[aux2/8] = (byte)(auxArr[aux2/8] | ((bin[(k* nBytes)+(j/8)]>>>j%8)&0x1)<<aux2%8);
                        counter--;
                    }
                }
            }
            catch (IndexOutOfBoundsException e){
                break;
            }
        }



        long outCounter=0;
        for (int i =0; i<sbits; i++){
            outCounter<<=1;
            outCounter+=((auxArr[i/8]&((0x1)<<(7-(i%8))))>>>(7-(i%8)));
        }

        char[] auxChar = new char[extensionSize];
        int aux4= sbits+8;
        for (int i=0;i<extensionSize*8;i++){
            int aux2=aux4+i;
            auxChar[i/8] = (char)(auxChar[i/8] | (((auxArr[aux2/8]>>>(aux2%8))&0x1)<<(aux2%8)));
        }


        for (char a :auxChar){
            extensionBuilder.append(a);
        }


        outCounter*=8;
        long inNBits=outCounter;
        byte[] bout = new byte[(int)(inNBits/8)];
        for(int k=0;k<nHB;k++){
            for (int i = 1; i< cBits; i++){
                try{
                    int aux= (int)Math.pow(2,i);
                    for (int j = aux;j<=(aux*2)-2;j++){
                        long aux2= inNBits-outCounter;
                        bout[(int)aux2/8] = (byte)(bout[(int)(aux2)/8] | ((bin[(k* nBytes)+(j/8)]>>>j%8)&0x1)<<aux2%8);
                        outCounter--;
                    }
                }
                catch (IndexOutOfBoundsException e){
                    break;
                }
            }
        }
     return bout;
    }
    
   //CODESC introduce un error por paquete a un arreglo humminizado 
    private byte[] introduceErrors(byte[] bin,int probability){
        Random rand = new Random(System.currentTimeMillis());
        nHB= bin.length/nBytes;
        for(int i=0; i<nHB;i++){
            if((int)(rand.nextDouble()*probability)==0){
                int r = (int)((rand.nextDouble())*nBits);
                bin[(i* nBytes)+(r/8)] = (byte)(bin[(i* nBytes)+(r/8)] ^ (0x1<<(r%8)));
            }
        }
        return bin;
    }

    //CODESC introduce dos errores a un arreglo humminizado
    private byte[] introduceTwoErrors(byte[] bin,int probability){
        Random rand = new Random(System.currentTimeMillis());
        nHB= bin.length/nBytes;
        for(int i=0; i<nHB;i++){
            if((int)(rand.nextDouble()*probability)==0){
                if((int)(rand.nextDouble()*probability)==0){
                    int r = (int)((rand.nextDouble())*nBits);
                    bin[(i* nBytes)+(r/8)] = (byte)(bin[(i* nBytes)+(r/8)] ^ (0x1<<(r%8)));
                    r = (int)((rand.nextDouble())*nBits);
                    bin[(i* nBytes)+(r/8)] = (byte)(bin[(i* nBytes)+(r/8)] ^ (0x1<<(r%8)));
                }
                int r = (int)((rand.nextDouble())*nBits);
                bin[(i* nBytes)+(r/8)] = (byte)(bin[(i* nBytes)+(r/8)] ^ (0x1<<(r%8)));
            }
        }
        return bin;
    }

    //CODESC corrige los errores de un archivo humminizado
    private byte[] correctErrors(byte[] bin) {
        nHB= bin.length/nBytes;
        for(int i =0 ; i<nHB;i++){
            int aux2 =0;
            int aux=0;
            for (int j = 0;j< nBits-1;j++){
                aux = aux^ ((((bin[(i* nBytes)+(j/8)])>>>j%8)&0x1)*(j+1));
                aux2+=(((bin[(i* nBytes)+(j/8)])>>>j%8)&0x1);
            }
            aux2+=(((bin[(i* nBytes)+((nBits-1)/8)])>>>(nBits-1)%8)&0x1);
            if(aux>0){
                if (aux2%2==1){
                    aux--;
                    bin[(i* nBytes) + (aux/8)]= (byte)(bin[(i* nBytes) + aux/8] ^ (0x1)<<aux%8);
                }
                else{
                    throw new DoubleErrorException();
                }
            }
        }
            return bin;
    }

    //CODESC Read Humminize and Save: lee, humminiza y guarda un archivo
    public void RHaS(String pathname) throws IOException,FileNotFoundException {

        byte[] bin = this.inRead(pathname);
        byte[] bout = this.hamminize(bin);
        String fileType;
        if(nBits == 8){
            fileType=".ha1";
        } else if (nBits==4096) {
            fileType=".ha2";
        } else if (nBits==65536) {
            fileType=".ha3";
        }
        else {
            fileType =(".ha"+nBits);
        }
        bout= this.correctErrors(bout);
        this.inWrite(bout, pathname.substring(0,pathname.indexOf('.'))+fileType);
    }

    //CODESC Read Humminize Introduce Errors and Save: idem RHaS pero introduce errores
    public void RHIEaS(String pathname,int probability) throws IOException, FileNotFoundException {
        String fileType;
        if(nBits == 8){
            fileType=".he1";
        } else if (nBits==4096) {
            fileType=".he2";
        } else if (nBits==65536) {
            fileType=".he3";
        }
        else {
            fileType =(".he"+nBits);
        }

        byte[] bin = this.inRead(pathname);
        byte[] bout = this.hamminize(bin);
        bout =this.introduceErrors( bout,probability);
        this.inWrite(bout, pathname.substring(0,pathname.lastIndexOf('.'))+fileType);
    }
    
    //CODESC Read humminize introduce 2 errors and save
    public void RHI2EaS(String pathname,int probability) throws IOException, FileNotFoundException {
        String fileType;
        if(nBits == 8){
            fileType=".he1";
        } else if (nBits==4096) {
            fileType=".he2";
        } else if (nBits==65536) {
            fileType=".he3";
        }
        else {
            fileType =(".he"+nBits);
        }

        byte[] bin = this.inRead(pathname);
        byte[] bout = this.hamminize(bin);
        bout =this.introduceTwoErrors( bout,probability);
        this.inWrite(bout, pathname.substring(0,pathname.lastIndexOf('.'))+fileType);
    }

    //CODESC Read Correct Dehuminize and Save: lee corrige errores, deshuminiza y guarda un archivo
    public void RCDaS(String pathname) throws IOException,FileNotFoundException {
        byte[] bin = this.outRead(pathname);
        bin = this.correctErrors(bin);
        StringBuilder extensionBuilder = new StringBuilder();
        byte[] bout = this.deHamminize(bin,extensionBuilder);
        this.outWrite(bout,pathname.substring(0,pathname.indexOf('.'))+"SE"+"."+extensionBuilder);
    }

    //CODESC Read Dehuminize and save: idem RCDaS pero sin corregir errores
    public void RDaS (String pathname) throws IOException,FileNotFoundException {
        byte[] bin = this.outRead(pathname);
        StringBuilder extensionBuilder = new StringBuilder();
        byte[] bout = this.deHamminize(bin,extensionBuilder);
        this.outWrite(bout,pathname.substring(0,pathname.indexOf('.'))+"CE"+"."+extensionBuilder);
    }
}
