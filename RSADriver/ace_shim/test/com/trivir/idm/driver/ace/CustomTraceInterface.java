package com.trivir.idm.driver.ace;

import java.io.IOException;
import java.io.PrintWriter;

import com.novell.nds.dirxml.driver.Trace;
import com.novell.nds.dirxml.driver.TraceInterface;
import com.novell.nds.dirxml.driver.XmlDocument;

public class CustomTraceInterface implements TraceInterface {
	public int getLevel() {
		return 5;
	}

	private void printTraceLevel(int level) {
		switch (level) {
		case Trace.DEFAULT_TRACE:
			System.out.print("Default: ");
			break;
		case Trace.XML_TRACE:
			System.out.print("XML: ");
			break;
		default:
			System.out.print("unknown: ");
			break;
		}
	}

	public void trace(int level, String arg1) {
		printTraceLevel(level);
		System.out.println(arg1);
	}

	public void trace(int level, XmlDocument arg1) {
		printTraceLevel(level);
		try {
			arg1.setIndent(true);
			arg1.writeDocument(new PrintWriter(System.out));
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
