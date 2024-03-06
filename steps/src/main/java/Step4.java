import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Step4 {
    public static class MapperClass extends Mapper<LongWritable, Text, Text, Text> {

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // this mapper will upload ea
            try {
                String[] tmp = value.toString().split("\t");
                String[] prevKey = tmp[0].split(",");
                String npmi = prevKey[0];
                String w1 = prevKey[1];
                String w2 = prevKey[2];
                String year = prevKey[3];
                String c12 = tmp[1];
                context.write(new Text(year), new Text(npmi + "," + w1 + "," + w2 + "," + c12));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }// npmi, mpmi, sum npmi decade ,relminpmi

    public static class ReducerClass extends Reducer<Text, Text, Text, Text> {

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            try {
                for (Text val : values){
                    String[] ValsArray = val.toString().split(",");
                    String npmi = ValsArray[0];
                    String w1 = ValsArray[1];
                    String w2 = ValsArray[2];
                    String c12 = ValsArray[3];
                    sum = sum + Integer.parseInt(npmi);
                }
            context.write(new Text(w1 + "," + w2 + "," + tmp[2] + "," + tmp[3] + "," + cw2), new Text("" + sum));

            }
			catch (Exception e) {
				e.printStackTrace();
			}

    }

        public static class PartitionerClass extends Partitioner<Text, Text> {
            @Override
            public int getPartition(Text key, Text value, int numPartitions) {
                return 0;
            }
        }

}