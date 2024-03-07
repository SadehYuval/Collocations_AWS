import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

public class Step2 {
	
	public static String printerHanna(Iterable<Text> values) {
		String str = "";
		for (Text val: values)
			str += val.toString() + "$";
		return str;
	}

	public static class MapperClass extends Mapper<LongWritable, Text, Text, Text> {
		int b = 0;
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException { 
																													
			try {
				b++;
				String[] tmp = value.toString().split("\t");
				String[] prevKey = tmp[0].split(",");		// w1 w2 year
				//String delimiterX = tmp[1].contains("!") ? "!" : "#";
				String cw1w2 = tmp[1].split("@")[0];
				String cw1 = tmp[1].split("@")[1];
				context.write(new Text(prevKey[1] + "," +  "*" + "," + prevKey[2]), new Text(cw1w2));
				context.write(new Text(prevKey[1] + "," + prevKey[0] + "," + prevKey[2] + "," + cw1), new Text(cw1w2));
				
				//we reverse the order ex: <w1,w2> -> <w2, w1> so now we want to count(w2) so in reduce() we get: <w2, *> before <w2, w1> <w2,w3> ...
				
				//if(b<200)
				//	System.out.println("Step2 Map --> Key: " + key + " value: " + value.toString() + " cw1: " + cw1 + " cw1w2: " + cw1w2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class Combiner extends Reducer<Text, Text, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			int count = 0;
			try {
				for (Text txt : values) {
					count += Integer.parseInt(txt.toString());
				}
				//System.out.println("Step2 Combine -->  key: " + key.toString() + " count: " + count);
				context.write(key, new Text(String.valueOf(count)));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in Step2 Combiner --> key: " + key.toString() + " values: " + printerHanna(values));
			}
		}
	}

	public static class ReducerClass extends Reducer<Text, Text, Text, Text> {
		int b;
		int cw2;

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			b = 0;
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			try {
				b++;
				if (key.toString().contains("*")) {
					cw2 = 0;
					for (Text val : values)
						cw2 = cw2 + Integer.parseInt(val.toString());
					//if(b<5000)
					//	System.out.println("step2 Reduce(): =====> key: " + key.toString() + " cw2: " + cw2);
				} else {
					for (Text val : values)
						sum = sum + Integer.parseInt(val.toString());
					String[] tmp = key.toString().split(",");
					context.write(new Text(tmp[1] + "," + tmp[0] + "," + tmp[2] + "," + tmp[3] + "," + cw2), new Text("" + sum));
					//w1,w2,year,cw1,cw2	cw1w2
					//if(b<50)
					//	System.out.println("step2 Reduce():  ---> key: " + key.toString() + " cw1: " + cw2 + " sum: " + sum);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class PartitionerClass extends Partitioner<Text, Text> {
		@Override
		public int getPartition(Text key, Text value, int numPartitions) {
			String[] parti = {"1530", "1540", "1560", "1620", "1670", "1680","1690", "1700", "1750", "1760",
					"1780", "1790", "1800", "1810", "1820", "1830", "1840", "1850", "1860", "1870", "1880",
					"1890", "1900", "1910", "1920", "1930", "1940", "1950", "1960", "1970", "1980", "1990", "2000"};
			String year = key.toString().split(",")[2];
			for(int i=0; i<parti.length; i++)
				if(parti[i].equalsIgnoreCase(year))
					return (i % numPartitions);
			//System.out.println("In Step2 Partitioner!  ---> key: " + key.toString() + "  value: " + value.toString() + " year: " + year);
			return 0;
		}
	}
}
