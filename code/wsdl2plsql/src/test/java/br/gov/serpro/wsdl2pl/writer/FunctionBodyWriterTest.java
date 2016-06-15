package br.gov.serpro.wsdl2pl.writer;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.impl.DefaultKeywordEmitter;

public class FunctionBodyWriterTest {

	@Test
	public void debuggingOutputCanBeTurnedOn() throws IOException {
		Context c = makeContext();
		c.setDebuggingMode(true);

		String output = new FunctionBodyWriter(c).write();

		assertTrue("Output should contain debugging (dbms_output) calls", output.contains("dbms_output"));
	}

	@Test
	public void debuggingOutputCanBeTurnedOff() throws IOException {
		Context c = makeContext();
		c.setDebuggingMode(false);

		String output = new FunctionBodyWriter(c).write();

		assertFalse("Output should not contain debugging (dbms_output) calls", output.contains("dbms_output"));
	}

	private Context makeContext() {
		Definitions defs = new WSDLParser().parse(getClass().getClassLoader().getResourceAsStream("sample-service.wsdl"));
		Context c = new Context(defs);
        c.setKeywordEmitter(new DefaultKeywordEmitter());
		return c;
	}
}
