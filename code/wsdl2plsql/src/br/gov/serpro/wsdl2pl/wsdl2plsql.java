package br.gov.serpro.wsdl2pl;

import java.io.File;
import java.io.FileWriter;

import br.gov.serpro.wsdl2pl.emitter.impl.DefaultKeywordEmitter;
import br.gov.serpro.wsdl2pl.emitter.impl.DefaultSymbolNameEmitter;
import br.gov.serpro.wsdl2pl.parser.OperationsParser;
import br.gov.serpro.wsdl2pl.parser.TypesParser;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.writer.FunctionBodyWriter;
import br.gov.serpro.wsdl2pl.writer.SpecWriter;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;

public class wsdl2plsql implements Runnable
{
    private String wsdlUrl;
    private String packageName;
    private String destDir;

    public wsdl2plsql(String wsdlUrl, String packageName, String destDir)
    {
        this.wsdlUrl = wsdlUrl;
        this.packageName = packageName;
        this.destDir = destDir;
    }

    @Override
    public void run()
    {
        WSDLParser parser = new WSDLParser();

        new File(destDir).mkdirs();
        File specFileName = new File(destDir + File.separator + packageName.toUpperCase() + "_spc.sql");
        File bodyFileName = new File(destDir + File.separator + packageName.toUpperCase() + ".sql");

        Definitions defs = parser.parse(wsdlUrl);

        Context context = new Context(defs);

        context.setPackageName(packageName);
        context.resolveProtocol(K.Protocol.SOAP_1_2);

        context.setKeywordEmitter(new DefaultKeywordEmitter());
        context.setSymbolNameEmitter(new DefaultSymbolNameEmitter());

        FileWriter specFileWriter = null;
        FileWriter bodyWriter = null;

        try
        {

            TypesParser typesParser = new TypesParser(context);
            typesParser.parse();

            specFileWriter = new FileWriter(specFileName);
            bodyWriter = new FileWriter(bodyFileName);

            OperationsParser operationsParser = new OperationsParser(context);
            operationsParser.parse();

            SpecWriter specWriter = new SpecWriter(context);
            specFileWriter.write(specWriter.writeSpec());

            FunctionBodyWriter functionBodyWriter = new FunctionBodyWriter(context);
            bodyWriter.write(functionBodyWriter.writeFunctionsBody());

            System.out.println(String.format("\nWSDL parsed successfully!\n\nFiles generated:\n  - %s\n  - %s",
                    specFileName.getCanonicalPath(), bodyFileName.getCanonicalPath()));

        }
        catch (Exception e)
        {
            throw new RuntimeException("\nError generating PL/SQL code", e);
        }
        finally
        {
            try
            {
                specFileWriter.close();
            }
            catch (Exception e)
            {
            }

            try
            {
                bodyWriter.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public static void main(String[] args)
    {
        if (args.length >= 2)
        {
            String wsdl = args[0];
            String packageName = args[1];
            String dest = ".";

            if (args.length > 2)
            {
                dest = args[2];
            }

            wsdl2plsql wsdl2plsql = new wsdl2plsql(wsdl, packageName, dest);

            wsdl2plsql.run();
        }
        else
        {
            System.out
                    .println("Usage: java -jar wsdl2plsql.jar <WSDL location> <PL/SQL Package Name> [<Dest Dir>]\n"
                            + "Ex.: java -jar wsdl2plsql.jar http://ws.correios.com.br/calculador/CalcPrecoPrazo.asmx?WSDL pk_ws_correios\n"
                            + "Ex.: java -jar wsdl2plsql.jar ~/docs/wsdl/correios.wsdl pk_ws_correios");
        }
    }
}
