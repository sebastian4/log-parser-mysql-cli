package com.ef;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParserApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ParserApp.class);

    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static final int LOG_ROW_SIZE = 5;
    
    //

    @Autowired
    private LogEntryRepository logRepository;
    
    @Autowired
    private IpBlockedRepository ipRepository;
    
    private Map<String, Object> cliArguments = null;
    
    private Map<String, Integer> ipCalls = null;
    
    private List<IpBlocked> ipsBlocked = null;

    //
    
    public static void main(String args[]) {
        SpringApplication.run(ParserApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
    	log.info("============================================================");
    	
    	cliArguments = ArgumentParserUtils.parseArgs(strings);
    	
    	ipCalls = new HashMap<String, Integer>();
    	ipsBlocked = new ArrayList<IpBlocked>();
    	
    	log.debug("cli arguments:");
    	for (String key : cliArguments.keySet()) {
    		log.debug(key+"="+cliArguments.get(key));
    	}
    		
        log.info("=============================== starting app");
        
        processFile((String) cliArguments.get("accesslog"));
        
        log.info("=====================");
        
        processLogResults();
        
        log.info("=============================== ending app");
    }
    
    //
    
    public void processFile(String strFile) {
    	log.debug(strFile);
    	int counter = 0;
    	
        try {

			FileReader fileReader = new FileReader(strFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				
				parseLine(line);
				
				if (counter > 0 && counter%5000 == 0) {
					log.debug("count = "+counter);
				}
				counter++;
			}
			log.debug("count = "+counter);
			fileReader.close();
			
        } catch (IOException ioex) {
            log.warn("there was no file");
        } 
    }
    
    public void parseLine(String line) {

    	StringTokenizer tokenizer = new StringTokenizer(line, "|");
    	
    	if (tokenizer.countTokens() != LOG_ROW_SIZE) {
    		log.warn("row has the wrong size: "+line);
    		return;
    	}
    	
    	LogEntry logEntry = new LogEntry();
    	
    	try {
			final Date date = (Date) DATE_FORMATTER.parse(tokenizer.nextToken());
			logEntry.setDate(date);
		} catch (java.text.ParseException e) {
			log.warn("date could not be parsed: "+line);
		}

    	logEntry.setIp(tokenizer.nextToken());
    	logEntry.setRequest(tokenizer.nextToken());
    	logEntry.setStatus(tokenizer.nextToken());
    	logEntry.setUserAgent(tokenizer.nextToken());
    	
//    	log.debug(logEntry.toString());
    	
    	logRepository.save(logEntry);
    	
    	processLogEntry(logEntry);
    }
    
    public void processLogEntry(LogEntry logEntry) {
    	
    	if ( logEntry.getDate().after((Date) cliArguments.get("startDate")) 
    			&& logEntry.getDate().before((Date) cliArguments.get("endDate")) ) {
    		
//    		log.debug(logEntry.toString());
    		
    		if (ipCalls.containsKey(logEntry.getIp())) {
        		int currentValue = ipCalls.get(logEntry.getIp());
        		ipCalls.put(logEntry.getIp(), currentValue+1);
        	}
        	else {
        		ipCalls.put(logEntry.getIp(), 1);
        	}
    		
    	}
    }
    
    public void processLogResults() {
    	log.debug("processResults");
    	
    	for (String ip : ipCalls.keySet()) {
    	    if ( ipCalls.get(ip) > ((Integer)cliArguments.get("threshold") ) ) {
    	    	
    	    	IpBlocked ipBlocked = new IpBlocked();
    	    	ipBlocked.setIp(ip);
    	    	ipBlocked.setStartDate((Date) cliArguments.get("startDate"));
    	    	ipBlocked.setDuration((String) cliArguments.get("duration"));
    	    	ipBlocked.setThreshold((Integer) cliArguments.get("threshold"));
    	    	ipBlocked.setRequests(ipCalls.get(ip));
    	    	String comment = "ip "+ip+" exceeded threshold of "+ipBlocked.getThreshold()+" requests";
    	    	ipBlocked.setComment(comment);
    	    	ipsBlocked.add(ipBlocked);
    	    	ipRepository.save(ipBlocked);
    	    }
    	}

//    	log.debug(ipCalls.toString());
//    	log.debug(ipsBlocked.toString());
    	
    	log.info("Ips Blocked:");
    	for (IpBlocked ipBlocked : ipsBlocked) {
    		log.info(ipBlocked.toString());
    	}
    	
    }

}