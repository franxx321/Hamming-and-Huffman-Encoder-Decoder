package Huffmann;

import java.io.*;
import java.util.*;

public class Huffman {

    //CODESC Funcion que imprime cada codigo asignado
    private void printCode(HuffmanNode root, String s, Map<Byte, String> cod) {
        if (root.getLeft() == null && root.getRight() == null
                && root.getB() != 0) {

            cod.put(root.getB(), s);
            return;
        }

        printCode(root.getLeft(), s + "1", cod);
        printCode(root.getRight(), s + "0", cod);
    }

    public void comprimir(String pathname, String nombreArchivo) {
        List<Byte> bin = new ArrayList<>();
        Map<Byte, Integer> freq = new HashMap<>();
        Map<Byte, String> cod = new HashMap<>();
        File file = new File(pathname);

        try {
            //CODESC El codigo de abajo lee el achivo y lo carga en el arraylist bin
            FileInputStream fis = new FileInputStream(file);
            int auxint = fis.read();
            while (auxint != -1) {
                bin.add((byte) auxint);

                auxint = fis.read();
            }

            int tam = bin.size();

            //CODESC Calculo frecuencias y las almaceno en un
            // mapa (clave: simbolo, elemento: freq)
            for (int i = 0; i < tam; i++) {
                if (freq.containsKey(bin.get(i))) {
                    freq.put(bin.get(i), freq.get(bin.get(i)) + 1);
                } else {
                    freq.put(bin.get(i), 1);
                }
            }

            System.out.println(freq);

            //CODESC Creamos una parva de minimos compuesta de nodos
            int n = freq.size();
            PriorityQueue<HuffmanNode> q
                    = new PriorityQueue<HuffmanNode>(
                    n, new MyComparator());


            for (Map.Entry<Byte, Integer> entry : freq.entrySet()) {
                HuffmanNode hn = new HuffmanNode();

                hn.setB(entry.getKey());
                hn.setFreq(entry.getValue());

                hn.setLeft(null);
                hn.setRight(null);

                q.add(hn);
            }

            HuffmanNode root = null;

            //CODESC Comenzamos con el algoritmo: tomando los dos valores mas chicos y asignando hijos hasta que el tamanio sea 1
            while (q.size() > 1) {
                HuffmanNode x = q.peek();
                q.poll();

                HuffmanNode y = q.peek();
                q.poll();

                HuffmanNode f = new HuffmanNode();

                f.setFreq(x.getFreq() + y.getFreq());
                f.setB(Byte.parseByte("0", 10));

                f.setLeft(x);

                f.setRight(y);

                root = f;

                q.add(f);
            }

            System.out.println("INICIO CODIFICACION");

            printCode(root, "", cod);

            System.out.println(cod);

            List<Byte> bout = new ArrayList<>();

            byte aux = 0;
            int count = 0;
            int size = 0;

            for (int i = 0; i < bin.size(); i++) {
                String strcod = cod.get(bin.get(i));

                for (int j = 0; j < strcod.length(); j++) {
                    char x = strcod.charAt(j);
                    byte y = (byte) Integer.parseInt(x + "");

                    aux = (byte) (aux | y);

                    if (count < 7) {
                        aux = (byte) (aux << 1);

                        count++;
                    } else {
                        count = 0;
                        bout.add(aux);
                        aux = 0;
                    }
                    size++;
                }
            }

            if (count <= 7 && count != 0) {
                for (int i = count; i < 7; i++) {
                    aux = (byte) (aux << 1);
                    count++;
                }
                bout.add(aux);
            }

            byte[] bytes = new byte[bout.size()];
            for (int i = 0; i < bout.size(); i++) {
                bytes[i] = bout.get(i);
            }

            FileOutputStream fos = new FileOutputStream(new File(nombreArchivo + ".huf"));
            fos.write(bytes);

            String mapa = "" + size + "\n";

            for (Map.Entry<Byte, String> entry : cod.entrySet()) {
                mapa += (entry.getValue() + "\n" + entry.getKey() + "\n");
            }

            FileOutputStream fos2 = new FileOutputStream(new File(nombreArchivo + "Tabla.txt"));
            fos2.write(mapa.getBytes());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void descomprimir(String pathFileCom, String pathTable, String nombreArchivo) {
        List<Byte> bin = new ArrayList<>();
        List<Byte> bout = new ArrayList<>();
        Map<String, Byte> cod = new HashMap<>();
        File fileCom = new File(pathFileCom);
        File fileTabla = new File(pathTable);

        try {
            String cadena, key = "", value = "";
            FileReader f = new FileReader(fileTabla);
            BufferedReader b = new BufferedReader(f);
            boolean flag = true;
            int size = 0;

            if ((cadena = b.readLine()) != null) {
                size = Integer.parseInt(cadena);
            }

            while ((cadena = b.readLine()) != null) {
                if (flag) {
                    key = cadena;
                } else {
                    value = cadena;
                    cod.put(key, Byte.parseByte(value));
                }
                flag = !flag;
            }

            b.close();

            FileInputStream fis = new FileInputStream(fileCom);

            byte z = (byte) fis.read();

            while (z != -1) {
                bin.add(z);
                z = (byte) (fis.read());
            }

            int tam = bin.size();
            byte aux = 0;
            String aux2 = "";
            int count = 0;

            for (int i = 0; i < tam; i++) {
                aux = bin.get(i);

                for (int j = 0; j < 8; j++) {
                    int x = 128 / (int) Math.pow(2, j);
                    aux2 += (aux & x) >> (7 - j);

                    if (cod.containsKey(aux2) && count <= size) {
                        bout.add(cod.get(aux2));
                        aux2 = "";
                    }
                    count++;
                }
            }

            byte[] bytes = new byte[bout.size()];
            for (int i = 0; i < bout.size(); i++) {
                bytes[i] = bout.get(i);
            }

            FileOutputStream fos = new FileOutputStream(new File(nombreArchivo + ".dhu"));
            fos.write(bytes);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

