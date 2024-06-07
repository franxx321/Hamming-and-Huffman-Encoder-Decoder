package Huffmann;
import java.util.Comparator;

public class MyComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        if (x.getFreq() != y.getFreq()) {
            return x.getFreq() - y.getFreq();
        }
        return 1;

    }
}
