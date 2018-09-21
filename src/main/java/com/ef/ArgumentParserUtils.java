package com.ef;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentParserUtils {

	private static final Logger log = LoggerFactory.getLogger(ArgumentParserUtils.class);
	
	public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
	
	public static final long HOUR = (3600 * 1000) - 1000;
	public static final long DAY = (24 * 3600 * 1000) - 1000;
	
	//
	
	@SuppressWarnings("deprecation")
	public static Map<String, Object> parseArgs(String... args) {
//    	log.info("parse args:");
    	
    	// arguments hashmap
    	Map<String, Object> arguments = new HashMap<String, Object>();
    	
    	// defaults 
    	arguments.put("accesslog", "access.log");
    	Date startDate = new Date();
    	arguments.put("startDate", startDate);
    	arguments.put("duration", "hourly");
    	arguments.put("threshold", 200);
    	
    	// create the command line parser
    	CommandLineParser parser = new DefaultParser();
    	
    	// create the Options
    	Options options = new Options();
    	options.addOption( OptionBuilder.withLongOpt( "accesslog" ).hasArg().create() );
    	options.addOption( OptionBuilder.withLongOpt( "startDate" ).hasArg().create() );
    	options.addOption( OptionBuilder.withLongOpt( "duration" ).hasArg().create() );
    	options.addOption( OptionBuilder.withLongOpt( "threshold" ).hasArg().create() );
    	
    	try {
    	    // parse the command line arguments
    		CommandLine line = parser.parse( options, args );
    		Date endDate = new Date();
    		
    		if( line.hasOption( "accesslog" ) ) {
    	    	String accesslog = line.getOptionValue("accesslog");
//    	        log.info("accesslog:"+accesslog);
    	        arguments.put("accesslog", accesslog);
    	    }
    		
    		if( line.hasOption( "startDate" ) ) {
    	    	startDate = DATE_FORMATTER.parse(line.getOptionValue("startDate"));
//    	        log.info("startDate:"+startDate);
    	        arguments.put("startDate", startDate);
    	    }
    		
    	    if( line.hasOption( "duration" ) ) {
    	    	String duration = line.getOptionValue("duration");
//    	        log.info("duration:"+duration);
    	        arguments.put("duration", duration);
    	        if (duration.equals("hourly")) {
    	        	arguments.put("threshold", 200);
    	        	endDate = new Date(startDate.getTime() + HOUR);
    	        }
    	        if (duration.equals("daily")) {
    	        	arguments.put("threshold", 500);
    	        	endDate = new Date(startDate.getTime() + DAY);
    	        }
    	    }
    	    
    	    if( line.hasOption( "threshold" ) ) {
    	    	Integer threshold = Integer.valueOf( line.getOptionValue("threshold") );
//    	        log.info("threshold:"+threshold);
    	        arguments.put("threshold", threshold);
    	    }
    	    
    	    arguments.put("endDate", endDate);
    	}
    	catch( ParseException | java.text.ParseException exp ) {
    	    log.warn( "Unexpected exception in parsing arguments:" + exp.getMessage() );
    	}

    	return arguments;
    }
}
