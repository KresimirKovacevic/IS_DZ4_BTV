import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;

public class FormValidatorMain {
	
	private final static Logger LOGGER = Logger.getLogger(FormValidatorMain.class.getName());

	public static void main(String[] args) {
		
		System.out.println("Staring FormValidatorMain");
		
		ExternalTaskClient client = ExternalTaskClient.create()				
		        .baseUrl("http://localhost:8080/engine-rest")	        
		        .build();
		client.subscribe("assignment-validation")
        .lockDuration(10000) // the default lock duration is 20 seconds, but you can override this
        .handler((externalTask, externalTaskService) -> {
          // Put your business logic here

          // Get a process variable
          String dueDate = externalTask.<String>getVariable("dueDate");
          String desc = externalTask.<String>getVariable("desc");
          
          LOGGER.info("Recived task '"+desc+"' with due date of "+dueDate+".");
          
          try {
        	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
  			  LocalDateTime dateTime = LocalDateTime.parse(dueDate, formatter);
  			  if(dateTime.isBefore(LocalDateTime.now())) {
  				  externalTaskService.complete(externalTask, Collections.singletonMap("isValid", "False"));	        	  
  			  }else {
  				  externalTaskService.complete(externalTask, Collections.singletonMap("isValid", "True"));
  			  }
          } 
          catch (DateTimeParseException e) {
        	  LOGGER.info(desc +" is invalid.");
              externalTaskService.complete(externalTask, Collections.singletonMap("isValid", "False"));	        	  
          }	          		         
        })
        .open();
	}

}
