package Huffmann;
import java.util.Comparator;

public class MyComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        if (x.freq != y.freq) {
            return x.freq - y.freq;
        }
        return 1;

    }
}
