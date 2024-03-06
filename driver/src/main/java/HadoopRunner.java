import com.amazonaws.auth.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.*;
//import org.apache.log4j.BasicConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;

public class HadoopRunner {
	private static String accessKey;
	private static String secretKey;
	private static String sessionToken;

	public static void loadCredentials(String path){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            line = reader.readLine();
            accessKey = line.split("=", 2)[1];
            line = reader.readLine();
            secretKey = line.split("=", 2)[1];
            line = reader.readLine();
            if (line!=null) 
            	sessionToken = line.split("=", 2)[1];
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

    }
    public static void main(String[] args) {
        String bucketName = "caspiThePlayer369";
        String language = "heb";
        if (args.length < 3 ){
            System.out.println("[ERROR]: not enought arguments given");
            System.exit(1);
        }
        String minimalPmi = args[1];
        String relativeMinmalPmi = args[2];
        // TO DO: change the credentials to the one given in hadoop in the moodle.
    	loadCredentials(System.getProperty("user.home") + File.separator + ".aws" + File.separator + "credentials");
    	
        AWSCredentials credentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);

        String input = "";
        
        if(language.equals("heb")) {
        	input = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/2gram/data";
            System.out.println("About to run extract collations in Hebrew");
        }
        else {
        	input = "s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-all/2gram/data";
            System.out.println("About to run extract collations in English");
        }
        // 1. Create an instance of AmazonElasticMapReduce
        final AmazonElasticMapReduce emr = AmazonElasticMapReduceClient.builder()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        // 2. Configure the Hadoop jar step for the EMR job.
        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar("s3://"+bucketName+"/steps-1.0-SNAPSHOT.jar") 
                .withMainClass("StepsRunner")
                .withArgs(input, LocalDateTime.now().toString().replace(':', '-'),language,minimalPmi,relativeMinmalPmi);
        // 3. Configure the main step for the EMR job, including the Hadoop Jar Step.
        StepConfig stepConfig = new StepConfig()
                .withName("Steps")
                .withHadoopJarStep(hadoopJarStep)
                .withActionOnFailure("TERMINATE_JOB_FLOW");
        // 4. Configure the instances for the EMR job.
        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(9)
                .withMasterInstanceType(InstanceType.M4Xlarge.toString())
                .withSlaveInstanceType(InstanceType.M4Xlarge.toString())
                .withHadoopVersion("2.9.2")
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1a"));
        // 5. Create a request to run the EMR job flow.
        RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                .withName("Google Bigrams collocation extract")
                .withInstances(instances)
                .withSteps(stepConfig)
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withLogUri("s3://"+bucketName+"/logs/")
                .withReleaseLabel("emr-5.11.0");


        RunJobFlowResult runJobFlowResult = emr.runJobFlow(runFlowRequest);
        String jobFlowId = runJobFlowResult.getJobFlowId();
        System.out.println("Ran job flow with id: " + jobFlowId);
    }
}






