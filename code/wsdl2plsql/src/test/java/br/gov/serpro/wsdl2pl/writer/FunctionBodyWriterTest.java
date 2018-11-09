package br.gov.serpro.wsdl2pl.writer;

import static org.junit.Assert.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.IOException;

import org.junit.Test;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.impl.DefaultKeywordEmitter;
import br.gov.serpro.wsdl2pl.emitter.impl.DefaultSymbolNameEmitter;
import br.gov.serpro.wsdl2pl.parser.OperationsParser;
import br.gov.serpro.wsdl2pl.parser.TypesParser;
import br.gov.serpro.wsdl2pl.util.K;

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
	
	@Test
	public void shouldProvideSoapFaultMessageInSqlErrm() throws IOException {
		Context context = makeContext();
		
		final String output = new FunctionBodyWriter(context).write();
		
		final String expected = "raise_application_error(-20001, ex_soap_fault.m_faultstring);";
		
		assertThat(output, containsString(expected));
	}

	private Context makeContext() {
        WSDLParser parser = new WSDLParser();

        Definitions defs = parser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-service.wsdl"));

        Context context = new Context(defs);

        context.setServices(new String[] {"Calculator"});
        context.setPackageName("pk_sample");
        context.resolveProtocol(K.Protocol.SOAP_1_2);
        context.setDebuggingMode(false);

        context.setKeywordEmitter(new DefaultKeywordEmitter());
        context.setSymbolNameEmitter(new DefaultSymbolNameEmitter());

        TypesParser typesParser = new TypesParser(context);
        typesParser.parse();

        OperationsParser operationsParser = new OperationsParser(context);
        operationsParser.parse();
        
        context.makeElegible("ws_addRequestType");
		
        return context;
	}
}
