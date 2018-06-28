package com.eisenvault.webscript;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class GetAzureRepoSize extends DeclarativeWebScript {
	
	private static Logger logger = Logger.getLogger(GetAzureRepoSize.class);
	
	private String accessKey;
	private String secretKey;
	private String bucketName;
	private String regionName;
	private long storeFreeSpace;

	public void setAccessKey(String accessKey) {
	    this.accessKey = accessKey;
	  }

	  public void setSecretKey(String secretKey) {
	    this.secretKey = secretKey;
	  }

	  public void setBucketName(String bucketName) {
	    this.bucketName = bucketName;
	  }

	  public void setRegionName(String regionName) {
	    this.regionName = regionName;
	  }

	public void setStoreFreeSpace(long storeFreeSpace) {
		this.storeFreeSpace = storeFreeSpace;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		
		Map<String, Object>	model = new HashMap<String, Object>();
		try{
			
			AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
			
			AmazonS3 s3client = AmazonS3ClientBuilder
					  .standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(regionName).build();			
			logger.debug("\n\n---------------------- CONNECTION ESTABLISHED ----------------\n\n");
			long storageSpaceConsumed = 0;
		    int totalItems = 0;
		    ObjectListing objects = s3client.listObjects(bucketName);
		    
		    do {
		        for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
		        	storageSpaceConsumed += objectSummary.getSize();
		            totalItems++;
		        }
		        objects = s3client.listNextBatchOfObjects(objects);
		    } while (objects.isTruncated());
		    logger.debug("\n\n -------------- storage Space Consumed : " + storageSpaceConsumed);
			model.put("storeFreeSpace", storeFreeSpace);
			model.put("storageSpaceConsumed", storageSpaceConsumed);
		
		} catch (Exception e) {
            e.printStackTrace();
        } 
		
		return model;
	}
}
