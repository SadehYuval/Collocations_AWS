import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

public class Step5 {
	public static int b = 0;
	public static double minimalPmi ;
	public static  double relativeMinmalPmi ;

	public static class MapperClass extends Mapper<LongWritable, Text, Text, Text> {

		protected void setup(Context context) throws IOException, InterruptedException {

			minimalPmi = Double.parseDouble(context.getConfiguration().get("minimalPmi"));
			relativeMinmalPmi = Double.parseDouble(context.getConfiguration().get("relativeMinmalPmi"));

			System.out.println("In Step5 Mapper.setup()");

		}


		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			try {
				String[] keyValueArray = value.toString().split("\t");
				String[] prevKey = keyValueArray[0].split(",");
                String preValue = keyValueArray[1];
				String w1 = prevKey[0];
				String w2 = prevKey[1];
				String year = prevKey[2];
				Double c1 = Double.valueOf(prevKey[3]);
				Double c2 = Double.valueOf(prevKey[4]);
				String NcounterName = "N_" + year;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ReducerClass extends Reducer<Text, Text, Text, Text> {
		int count = 0;
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			count++;
			System.out.println("Reduce(): => key: " + key.toString() + " count: " + count);
			if(count<=100) {
				String[] tmp = key.toString().split(" ");
				Double myVal = 1 / Double.valueOf(tmp[0]);
				String w1 = tmp[1];
				String w2 = tmp[2];
				String year = tmp[3];
				
				context.write(new Text(year + " " + w1 + " " + w2), new Text(String.valueOf(myVal)));
				//write to output: year,w1,w2	npmi
			}		
		}
	}

	public static class PartitionerClass extends Partitioner<Text, Text> {
		@Override
		public int getPartition(Text key, Text value, int numPartitions) {
			String[] parti = {"1530", "1540", "1560", "1620", "1670", "1680","1690", "1700", "1750", "1760",
					"1780", "1790", "1800", "1810", "1820", "1830", "1840", "1850", "1860", "1870", "1880",
					"1890", "1900", "1910", "1920", "1930", "1940", "1950", "1960", "1970", "1980", "1990", "2000"};
			String year = key.toString().split(" ")[3];
			for(int i=0; i<parti.length; i++)
				if(parti[i].equalsIgnoreCase(year))
					return (i % numPartitions);
			System.out.println("In Step5 Partitioner!  ---> key: " + key.toString() + "  value: " + value.toString() + " year: " + year);
			return 0;
		}
	}


}
