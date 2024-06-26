import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

public class Step3 {
	public static int b = 0;

	//Using this function will cause Iterable<Text>values to be NULL (it's an iterator so it's a single pass on the values)
	public static String printerHanna(Iterable<Text> values) {
		String str = "";
		for (Text val : values)
			str += val.toString() + " H3 ";
		return str;
	}

	public static class MapperClass extends Mapper<LongWritable, Text, Text, Text> {

		public static double calculateNPMI(double c1, double c2, double c12, double N) {
			double pw1w2 = c12 / N;
			double pw1 = c1 / N;
			double pw2 = c2 / N;
			double pmi = Math.log(pw1w2/pw1*pw2);
			double npmi = pmi / ((-1) * (Math.log(pw1w2)));
			return npmi;
		}

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			try {
				String[] tmp = value.toString().split("\t");
				String[] prevKey = tmp[0].split(",");
				String w1 = prevKey[0];
				String w2 = prevKey[1];
				String year = prevKey[2];
				Double c1 = Double.valueOf(prevKey[3]);
				Double c2 = Double.valueOf(prevKey[4]);
				Double c12 = Double.valueOf(tmp[1]);
				String NcounterName = "N_" + year;
				//npmi,w1,w2,year	cw1w2
				Double currentCounter = Double.valueOf(context.getConfiguration().get(NcounterName));
				Double npmi = calculateNPMI(c1, c2, c12, currentCounter);
				context.write(new Text(year + "," + "*"), new Text("" + npmi)); // year,*	npmi
				context.write(new Text(year + "," + w1 + "," + w2 + "," + npmi), new Text("")); // year,w1,w2,npmi	placeholder
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class ReducerClass extends Reducer<Text, Text, Text, Text> {
		double npmiSum;
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
		}

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			if(key.toString().contains("*")) { // year,*	npmi
				npmiSum = 0;
				for (Text val : values) {
					npmiSum += Double.parseDouble(val.toString());
				}
			}
			else {	// year,w1,w2,npmi	placeholder
				String[] tmp = key.toString().split(",");
				String year = tmp[0];
				String w1 = tmp[1];
				String w2 = tmp[2];
				String npmi = tmp[3];
				context.write(new Text(year + "," + w1 + "," + w2 + "," + npmi), new Text(String.valueOf(npmiSum)));
				//write to output: year,w1,w2,npmi	npmiSum	
			}
		}
	}

	public static class PartitionerClass extends Partitioner<Text, Text> {
		@Override
		public int getPartition(Text key, Text value, int numPartitions) {
			String[] parti = {"1530", "1540", "1560", "1620", "1670", "1680","1690", "1700", "1750", "1760",
					"1780", "1790", "1800", "1810", "1820", "1830", "1840", "1850", "1860", "1870", "1880",
					"1890", "1900", "1910", "1920", "1930", "1940", "1950", "1960", "1970", "1980", "1990", "2000"};
			String year = key.toString().split(",")[0];
			for(int i=0; i<parti.length; i++)
				if(parti[i].equalsIgnoreCase(year))
					return (i % numPartitions);
			System.out.println("In Step3 Partitioner!  ---> key: " + key.toString() + "  value: " + value.toString() + " year: " + year);
			return 0;
		}
	}

	public static class Combiner extends Reducer<Text, Text, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			// try and catch maybe
			try {
				if(key.toString().contains("*")) { // year,*	npmi
					int npmiSum = 0;
					for (Text val : values) {
						npmiSum += Double.parseDouble(val.toString());
					}
					context.write(key, new Text(String.valueOf(npmiSum)));
				}
				else {
					context.write(key, new Text(""));
				}
			}
			catch (Exception e) {
					e.printStackTrace();
					context.write(key, new Text("Stam key to continue"));
				}
			}
		}
	}


