package com.yahoo.ycsb;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.yahoo.ycsb.client.Client;
import com.yahoo.ycsb.client.MasterClient;
import com.yahoo.ycsb.client.SlaveClient;
import com.yahoo.ycsb.rmi.PropertyPackage;

public class LoadGenerator {

	public static void main(String[] args) {
		Properties props = new Properties();
		Properties fileprops = new Properties();
		boolean dotransactions = true;
		int threadcount = 1;
		int target = 0;
		boolean status = false;
		boolean slave = false;
		String label = "";
		int argindex = 0;

		if (args.length == 0) {
			usageMessage();
			System.exit(0);
		}

		while (args[argindex].startsWith("-")) {
			if (args[argindex].compareTo("-threads") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				int tcount = Integer.parseInt(args[argindex]);
				props.setProperty("threadcount", tcount + "");
				argindex++;
			} else if (args[argindex].compareTo("-target") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				int ttarget = Integer.parseInt(args[argindex]);
				props.setProperty("target", ttarget + "");
				argindex++;
			} else if (args[argindex].compareTo("-load") == 0) {
				dotransactions = false;
				argindex++;
			} else if (args[argindex].compareTo("-t") == 0) {
				dotransactions = true;
				argindex++;
			} else if (args[argindex].compareTo("-s") == 0) {
				status = true;
				argindex++;
			} else if (args[argindex].compareTo("-slave") == 0) {
				slave = true;
				argindex++;
			} else if (args[argindex].compareTo("-db") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				props.setProperty("db", args[argindex]);
				argindex++;
			} else if (args[argindex].compareTo("-l") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				label = args[argindex];
				argindex++;
			} else if (args[argindex].compareTo("-P") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				String propfile = args[argindex];
				argindex++;

				Properties myfileprops = new Properties();
				try {
					myfileprops.load(new FileInputStream(propfile));
				} catch (IOException e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}

				// Issue #5 - remove call to stringPropertyNames to make
				// compilable under Java 1.5
				for (Enumeration e = myfileprops.propertyNames(); e.hasMoreElements();) {
					String prop = (String) e.nextElement();
					fileprops.setProperty(prop, myfileprops.getProperty(prop));
				}
			} else if (args[argindex].compareTo("-p") == 0) {
				argindex++;
				checkMoreArgs(argindex, args.length);
				int eq = args[argindex].indexOf('=');
				if (eq < 0) {
					usageMessage();
					System.exit(0);
				}

				String name = args[argindex].substring(0, eq);
				String value = args[argindex].substring(eq + 1);
				props.put(name, value);
				argindex++;
			} else {
				System.out.println("Unknown option " + args[argindex]);
				usageMessage();
				System.exit(0);
			}

			if (argindex >= args.length) {
				break;
			}
		}
		if (argindex != args.length) {
			usageMessage();
			System.exit(0);
		}
		
		// overwrite file properties with properties from the command line

		// Issue #5 - remove call to stringPropertyNames to make compilable
		// under Java 1.5
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			String prop = (String) e.nextElement();

			fileprops.setProperty(prop, props.getProperty(prop));
		}

		props = fileprops;
		
		MasterClient client = new MasterClient(new PropertyPackage(props, dotransactions, threadcount, target, slave, label));
		client.execute();
	}
	
	public static void checkMoreArgs(int argindex, int argslength) {
		if (argindex >= argslength) {
			usageMessage();
			System.exit(0);
		}
	}
	
	public static void usageMessage() {
		System.out.println("Usage: java com.yahoo.ycsb.Client [options]");
		System.out.println("Options:");
		System.out.println("  -threads n: execute using n threads (default: 1) - can also be specified as the \n"
						 + "              \"threadcount\" property using -p");
		System.out.println("  -target n: attempt to do n operations per second (default: unlimited) - can also\n"
						 + "             be specified as the \"target\" property using -p");
		System.out.println("  -load:  run the loading phase of the workload");
		System.out.println("  -t:  run the transactions phase of the workload (default)");
		System.out.println("  -db dbname: specify the name of the DB to use (default: com.yahoo.ycsb.BasicDB) - \n" 
				         + "              can also be specified as the \"db\" property using -p");
		System.out.println("  -P propertyfile: load properties from the given file. Multiple files can");
		System.out.println("                   be specified, and will be processed in the order specified");
		System.out.println("  -p name=value:  specify a property to be passed to the DB and workloads;");
		System.out.println("                  multiple properties can be specified, and override any");
		System.out.println("                  values in the propertyfile");
		System.out.println("  -s:  show status during run (default: no status)");
		System.out.println("  -l label:  use label for status (e.g. to label one experiment out of a whole batch)");
		System.out.println("");
		System.out.println("Required properties:");
		System.out.println("  " + Client.WORKLOAD_PROPERTY + ": the name of the workload class to use (e.g. com.yahoo.ycsb.workloads.CoreWorkload)");
		System.out.println("");
		System.out.println("To run the transaction phase from multiple servers, start a separate client on each.");
		System.out.println("To run the load phase from multiple servers, start a separate client on each; additionally,");
		System.out.println("use the \"insertcount\" and \"insertstart\" properties to divide up the records to be inserted");
	}
}
