package Hamming;

import java.io.*;
import java.util.Random;

public class HammingProcessor {

    private static int rLog2(int n){
        int r = 31 - Integer.numberOfLeadingZeros(n);
        if (n> Math.pow(2,r)){
            r++;
        }
        return r;
    }

    public HammingProcessor(int nBits){
        this.nBits =nBits;
        nBytes=nBits/8;
        cBits= HammingProcessor.rLog2(nBits);
        cBytes=((float)cBits)/8;
    }

    private  int nBits ,nBytes,cBits ,nHB,sbits =32, sbytes =sbits/8;


    private float cBytes;


    private byte[] outRead(String pathName) throws IOException,FileNotFoundException {
        File f1 = new File(pathName);
        FileInputStream fis = new FileInputStream(f1);
        byte[] bin = new byte[(int)f1.length()];
        fis.read(bin);
        fis.close();
        return bin;
    }

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

    private void inWrite(byte[] bout, String pathName) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(new File(pathName));
        fos.write(bout);
        fos.close();
    }

    private void outWrite(byte [] bout,String pathName) throws IOException, FileNotFoundException{
        FileOutputStream fos = new FileOutputStream(new File(pathName));
        String extension=pathName.substring(pathName.lastIndexOf('.')+1);
        fos.write(bout,sbytes+1+extension.length(),bout.length-sbytes-1-extension.length());
        fos.close();
    }

    private byte[] humminize(byte[] bin){
        float aux2 = cBytes+(1f/8f);
        int nHB = (int)Math.ceil(bin.length/(nBytes - aux2));
        byte [] binter = new byte[nHB* nBytes];
        int cin =0;
        for (int k=0; k<nHB;k++){
            int m=k* nBytes;
            for (int i = 1; i< cBits; i++){
                try{
                    int h= (int)(Math.pow(2,i+1)-2);
                    for (int j = (int)Math.pow(2,i);j<=h;j++ ){
                        binter[m+(j/8)]=(byte) (binter[m+(j/8)]| ((bin[cin/8]&0x1<<(cin%8))>>cin%8)<<j%8);
                        cin++;
                    }
                }
                catch(IndexOutOfBoundsException e){
                    break;
                }
            }
        }

        for (int i = 0; i <nHB;i++){
            int aux=0;
            for (int j = 0;j< nBits;j++){
                int l = i* nBytes +(j/8);
                int m = j%8;
                aux = aux ^ (((binter[l]>>>m)&0x1)*(j+1));
            }
            for(int k = 0; k< cBits; k++){
                int z =((1<<k)-1);
                int l = (i* nBytes) +(z/8);
                int m = (aux>>>k)&0x1;
                int n = m<<(z%8);
                binter[l]=  (byte)(binter[l] | n);
            }
        }
        return binter;
    }
    private byte[] deHumminize(byte[] bin, StringBuilder extensionBuilder){
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

    private byte[] correctErrors(byte[] bin) {
        nHB= bin.length/nBytes;
        for(int i =0 ; i<nHB;i++){
            int aux=0;
            for (int j = 0;j< nBits;j++){
                aux = aux^ (((bin[(i* nBytes)+(j/8)])>>>j%8)&0x1)*(j+1);
            }
            if(aux>0){
                aux--;
                bin[(i* nBytes) + (aux/8)]= (byte)(bin[(i* nBytes) + aux/8] ^ (0x1)<<aux%8);
            }
        }
            return bin;
    }

    public void RHaS(String pathname) throws IOException,FileNotFoundException {

        byte[] bin = this.inRead(pathname);
        byte[] bout = this.humminize(bin);
        String fileType="";
        if(nBits == 8){
            fileType=".ha1";
        } else if (nBits==4096) {
            fileType=".ha2";
        } else if (nBits==65536) {
            fileType=".ha3";
        }
        else {
            fileType =(".ha"+Integer.toString(nBits));
        }
        this.inWrite(bout, pathname.substring(0,pathname.indexOf('.'))+fileType);
    }

    public void RHIEaS(String pathname,int probability) throws IOException, FileNotFoundException {

        String fileType="";
        if(nBits == 8){
            fileType=".he1";
        } else if (nBits==4096) {
            fileType=".he2";
        } else if (nBits==65536) {
            fileType=".he3";
        }
        else {
            fileType =(".he"+Integer.toString(nBits));
        }

        byte[] bin = this.inRead(pathname);
        byte[] bout = this.humminize(bin);
        bout =this.introduceErrors( bout,probability);
        this.inWrite(bout, pathname.substring(0,pathname.indexOf('.'))+fileType);
    }
    public void RCDaS(String pathname) throws IOException,FileNotFoundException {
        byte[] bin = this.outRead(pathname);
        int aux = bin.length/nBytes;
        bin = this.correctErrors(bin);
        StringBuilder extensionBuilder = new StringBuilder();
        byte[] bout = this.deHumminize(bin,extensionBuilder);
        this.outWrite(bout,pathname.substring(0,pathname.indexOf('.'))+"SE"+"."+extensionBuilder.toString());
    }
    public void RDaS (String pathname) throws IOException,FileNotFoundException {
        byte[] bin = this.outRead(pathname);
        int aux = bin.length/nBytes;
        StringBuilder extensionBuilder = new StringBuilder();
        byte[] bout = this.deHumminize(bin,extensionBuilder);
        this.outWrite(bout,pathname.substring(0,pathname.indexOf('.'))+"CE"+"."+extensionBuilder.toString());
    }
}
