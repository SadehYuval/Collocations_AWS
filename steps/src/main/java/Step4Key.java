import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Step4Key implements WritableComparable<Step4Key> {
    Text word1;
    Text word2;
    IntWritable decade;
    DoubleWritable npmi;

    Step4Key() {
        this.word1 = new Text("");
        this.word2 = new Text("");
        this.decade = new IntWritable(-1);
        this.npmi = new DoubleWritable(-1);
    }

    public Step4Key(String word1, String word2, int decade, double npmi) {
        this.word1 = new Text(word1);
        this.word2 = new Text(word2);
        this.npmi = new DoubleWritable(npmi);
        this.decade = new IntWritable(decade);
    }

    @Override
    public int compareTo(Step4Key other) {
        if(other == null)
            return 1;
        int ret = getDecade() - other.getDecade();
        if(ret != 0)
            return ret;
        if(getnpmi() == other.getnpmi())
            return 0;
        if(getnpmi() > other.getnpmi())
            return -1;
        return 1;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        word1.write(out);
        word2.write(out);
        npmi.write(out);
        decade.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        word1.readFields(in);
        word2.readFields(in);
        npmi.readFields(in);
        decade.readFields(in);
    }

    public String toString(){
        return String.format("%s\t%s\t%d", this.word1, this.word2, this.decade);
    }

    public String getFirstWord() {
        return word1.toString();
    }

    public String getSecondWord() {
        return word2.toString();
    }

    public int getDecade() {
        return decade.get();
    }

    public double getnpmi() { 
        return npmi.get(); 
    }
}