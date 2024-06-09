import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;

public class DueDateCheckerMain {

	private final static Logger LOGGER = Logger.getLogger(DueDateCheckerMain.class.getName());

	public static void main(String[] args) {
		
		System.out.println("Staring DueDateCheckerMain");
		
		ExternalTaskClient client = ExternalTaskClient.create()				
		        .baseUrl("http://localhost:8080/engine-rest")		        
		        .build();
		client.subscribe("due-date-check")
        .lockDuration(10000) // the default lock duration is 20 seconds, but you can override this
        .handler((externalTask, externalTaskService) -> {
          // Put your business logic here

          // Get a process variable
          String dueDate = externalTask.<String>getVariable("dueDate");
          String desc = externalTask.<String>getVariable("desc");
          
          LOGGER.info("Checking due date " + dueDate +" of task '"+desc+"'.");
          
          try {
        	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
  			  LocalDateTime dateTime = LocalDateTime.parse(dueDate, formatter);
  			  if(dateTime.isBefore(LocalDateTime.now())) {
  	        	  LOGGER.info(desc +" has expired.");
  				  externalTaskService.complete(externalTask, Collections.singletonMap("expired", "True"));
  	          }else {
  				  externalTaskService.complete(externalTask, Collections.singletonMap("expired", "False"));
          	  }
          } 
          catch (DateTimeParseException e) {
        	  LOGGER.info("An error occured for " + desc + " with due date of " + dueDate);
              externalTaskService.complete(externalTask, Collections.singletonMap("expired", "True"));	        	  
          }	          		         
        })
        .open();
	}

}
