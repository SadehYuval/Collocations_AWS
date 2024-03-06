import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class StepsRunner {
    public static void main(String[] args) throws Exception {
        String output = "s3n://caspitheplayer7/output/";
        String input = args[1];
        String time = args[2];
        String output1 = output + "Step1Output" + time + "/";
        Configuration conf1 = new Configuration();
        conf1.set("lang", args[3]);
        System.out.println("Configuring Step 1");
        
        Job job = Job.getInstance(conf1, "Step1");
        MultipleInputs.addInputPath(job, new Path(input), SequenceFileInputFormat.class,
                Step1.MapperClass.class);
        job.setJarByClass(Step1.class);
        job.setMapperClass(Step1.MapperClass.class);
        job.setPartitionerClass(Step1.PartitionerClass.class);
        //job.setCombinerClass(Step1.Combiner.class);
        job.setReducerClass(Step1.ReducerClass.class);
        job.setNumReduceTasks(33);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileOutputFormat.setOutputPath(job, new Path(output1));
        System.out.println("Launching Step 1");
        if (job.waitForCompletion(true)) {
            System.out.println("Step 1 finished");
        } else {
            System.out.println("Step 1 failed ");
        }

        System.out.println();
        String output2 = output + "Step2Output" + time + "/";
        System.out.println("output2 = " + output2);
        Configuration conf2 = new Configuration();
        CounterGroup jobCounters;
        jobCounters = job.getCounters().getGroup("NCounter");
            for (Counter counter : jobCounters){
            		System.out.println("Passing " + counter.getName() + " with value " + counter.getValue() + " to step 2");
                    conf2.set(counter.getName(), "" + counter.getValue());
                }
            
        System.out.println("Configuring Step 2");
        Job job2 = Job.getInstance(conf2, "Step2");
        job2.setJarByClass(Step2.class);
        job2.setMapperClass(Step2.MapperClass.class);
        job2.setPartitionerClass(Step2.PartitionerClass.class);
        job2.setReducerClass(Step2.ReducerClass.class);
        
        //job2.setCombinerClass(Step2.Combiner.class);
        
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setNumReduceTasks(33);
        FileInputFormat.setInputPaths(job2, new Path(output1));
        FileOutputFormat.setOutputPath(job2, new Path(output2));
        System.out.println("Launching Step 2");
        if (job2.waitForCompletion(true)) {
            System.out.println("Step 2 finished");
        } else {
            System.out.println("Step 2 failed ");
        }
        
        System.out.println();
        String output3 = output + "Step3Output" + time + "/";
        System.out.println("Configuring Step 3");
        Configuration conf3 = new Configuration();
        jobCounters = job.getCounters().getGroup("NCounter");
            for (Counter counter : jobCounters){
            		System.out.println("Passing " + counter.getName() + " with value " + counter.getValue() + " to step 3");
                    conf3.set(counter.getName(), "" + counter.getValue());
                }
        Job job3 = Job.getInstance(conf3, "Step3");
        job3.setJarByClass(Step3.class);
        job3.setMapperClass(Step3.MapperClass.class);
        job3.setReducerClass(Step3.ReducerClass.class);
        job3.setPartitionerClass(Step3.PartitionerClass.class);
        
        //no need for combiner in 3rd step (every key is a unique bigram with year and logLambda value)
        //job3.setCombinerClass(Step3.Combiner.class);
        
        job3.setMapOutputKeyClass(Text.class);
        job3.setNumReduceTasks(33);
        job3.setMapOutputValueClass(Text.class);
        //job3.setSortComparatorClass(Step3.HannaDecadeComparator.class);
        job3.setOutputKeyClass(Text.class);
        job3.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job3, new Path(output2));
        FileOutputFormat.setOutputPath(job3, new Path(output3));
        System.out.println("Launching Step 3");
        job3.waitForCompletion(true);
        System.out.println("All steps are done");
    }
}