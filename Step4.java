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
                context.write(new Text(year + "," + "*" + "," + "*" + "*"), new Text(npmi));
                context.write(new Text(year + "," + w1 + "," + w2 + "," + npmi), new Text());
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
                if (key.toString().contains("*")){
                    for (Text val : values){
                        String[] valsArray = val.toString().split(",");
                        String npmi = valsArray[0];
                        sum += Integer.parseInt(npmi);
                    }
                }
                else{
                    String[] keysArray = key.toString().split(",");
                    String year = keysArray[0];
                    String w1 = keysArray[1];
                    String w2 = keysArray[2];
                    String npmi = keysArray[3];
                    context.write(new Text(year + "," + w1 + "," + w2 + "," + npmi), new Text(sum + ""));
                }
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