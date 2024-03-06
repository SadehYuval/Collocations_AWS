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
    	loadCredentials(System.getProperty("user.home") + File.separator + ".aws" + File.separator + "credentials");
    	
        AWSCredentials credentials = new BasicSessionCredentials(accessKey, secretKey, sessionToken);
        
        String choice = "";
        if(args[1].equalsIgnoreCase("heb")) {
        	choice = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/2gram/data";
            System.out.println("You chose: Hebrew");
        }
        else {
        	choice = "s3://datasets.elasticmapreduce/ngrams/books/20090715/eng-all/2gram/data";
            System.out.println("You chose: English");
        }
        final AmazonElasticMapReduce emr = AmazonElasticMapReduceClient.builder()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        //LocalDateTime now = LocalDateTime.now();
        HadoopJarStepConfig hadoopJarStep = new HadoopJarStepConfig()
                .withJar("s3n://caspitheplayer7/steps-1.0-SNAPSHOT.jar") // This should be a full map reduce application.
                .withMainClass("StepsRunner")
                .withArgs(choice, LocalDateTime.now().toString().replace(':', '-'), args[1]);
        StepConfig stepConfig = new StepConfig()
                .withName("Steps")
                .withHadoopJarStep(hadoopJarStep)
                .withActionOnFailure("TERMINATE_JOB_FLOW");
        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(9)
                .withMasterInstanceType(InstanceType.M4Xlarge.toString())
                .withSlaveInstanceType(InstanceType.M4Xlarge.toString())
                .withHadoopVersion("2.7.2")
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1a"));
        RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                .withName("Google Bigrams collocation extract")
                .withInstances(instances)
                .withSteps(stepConfig)
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withLogUri("s3n://caspitheplayer7/logs/")
                .withReleaseLabel("emr-5.0.0");


        RunJobFlowResult runJobFlowResult = emr.runJobFlow(runFlowRequest);
        String jobFlowId = runJobFlowResult.getJobFlowId();
        System.out.println("EMR Job ID: " + jobFlowId);
    }
}






