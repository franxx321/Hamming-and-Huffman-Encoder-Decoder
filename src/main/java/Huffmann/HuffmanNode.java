package Huffmann;

public class HuffmanNode {
    int freq;
    byte b;

    HuffmanNode left;
    HuffmanNode right;

    @Override
    public String toString() {
        return freq + ":" + b;
    }
}
