package br.gov.serpro.wsdl2pl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.serpro.wsdl2pl.emitter.impl.DefaultKeywordEmitter;
import br.gov.serpro.wsdl2pl.emitter.impl.DefaultSymbolNameEmitter;
import br.gov.serpro.wsdl2pl.parser.OperationsParser;
import br.gov.serpro.wsdl2pl.parser.TypesParser;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.util.K;
import br.gov.serpro.wsdl2pl.writer.FunctionBodyWriter;
import br.gov.serpro.wsdl2pl.writer.SpecWriter;
import br.gov.serpro.wsdl2pl.writer.TestsWriter;

import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;

public class wsdl2plsql implements Runnable
{
    private String wsdlUrl;
    private String packageName;
    private String destDir;
    private int operationsPerPackage;
    private String[] operations;
    private String[] services;

    private static final Logger L = LoggerFactory.getLogger(wsdl2plsql.class);

    public wsdl2plsql(String wsdlUrl, String packageName, String destDir, String[] services, String[] operations,
            int operationsPerPackage)
    {
        this.wsdlUrl = wsdlUrl;
        this.packageName = packageName;
        this.destDir = destDir;
        this.operations = operations;
        this.services = services;
        this.operationsPerPackage = operationsPerPackage;
    }

    @Override
    public void run()
    {
        WSDLParser parser = new WSDLParser();

        new File(destDir).mkdirs();

        L.info("Parsing WSDL from " + wsdlUrl);
        Definitions defs = parser.parse(wsdlUrl);

        Context context = new Context(defs);

        context.setServices(services);
        context.setPackageName(packageName);
        context.resolveProtocol(K.Protocol.SOAP_1_2);

        context.setKeywordEmitter(new DefaultKeywordEmitter());
        context.setSymbolNameEmitter(new DefaultSymbolNameEmitter());

        try
        {
            FileWriter specFileWriter = null;
            FileWriter bodyWriter = null;
            FileWriter testsWriter = null;

            TypesParser typesParser = new TypesParser(context);
            typesParser.parse();

            OperationsParser operationsParser = new OperationsParser(context);
            operationsParser.parse();

            int parts = 1;

            List<Function> allFunctions = context.getPlFunctions();

            if (operations == null || operations.length == 0)
            {

                if (operationsPerPackage > 0)
                {
                    parts = (int) Math.ceil((double) allFunctions.size() / operationsPerPackage);
                }

                if (parts == 1)
                {

                    operations = new String[allFunctions.size()];
                    for (int i = 0; i < allFunctions.size(); i++)
                    {
                        operations[i] = allFunctions.get(i).getElement().getName();
                    }
                }
            }

            int places = (int) Math.log10(parts + 1) + 1;
            String pattern = "";
            for (int j = 0; j < places; j++)
            {
                pattern += "0";
            }
            NumberFormat numberFormat = new DecimalFormat(pattern);

            for (int i = 0; i < parts; i++)
            {
                try
                {
                    if (parts > 1)
                    {
                        operations = new String[operationsPerPackage];

                        for (int j = i * operationsPerPackage; j < Math.min(i * operationsPerPackage
                                + operationsPerPackage, allFunctions.size()); j++)
                        {
                            operations[j - i * operationsPerPackage] = allFunctions.get(j).getElement().getName();
                        }

                        context.setPackageName(packageName + "_part" + numberFormat.format(i + 1));
                    }
                    else
                    {
                        context.setPackageName(packageName);
                    }

                    for (String functionName : operations)
                    {
                        if (functionName != null)
                        {
                            context.makeElegible(functionName.trim());
                        }
                    }

                    File specFileName = new File(destDir + File.separator + context.getPackageName().toUpperCase()
                            + "_spc.sql");
                    File bodyFileName = new File(destDir + File.separator + context.getPackageName().toUpperCase()
                            + ".sql");
                    File testsFileName = new File(destDir + File.separator + context.getPackageName().toUpperCase()
                            + "_test.sql");

                    specFileWriter = new FileWriter(specFileName);
                    bodyWriter = new FileWriter(bodyFileName);
                    testsWriter = new FileWriter(testsFileName);

                    FunctionBodyWriter functionBodyWriter = new FunctionBodyWriter(context);
                    bodyWriter.write(functionBodyWriter.write());

                    SpecWriter specWriter = new SpecWriter(context);
                    specFileWriter.write(specWriter.write());

                    TestsWriter testWriter = new TestsWriter(context);
                    testsWriter.write(testWriter.write());

                    L.info(String.format("WSDL parsed successfully!\n\nFiles generated:\n  - %s\n  - %s\n  - %s",
                            specFileName.getCanonicalPath(), bodyFileName.getCanonicalPath(),
                            testsFileName.getCanonicalPath()));

                    context.clear();
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

                    try
                    {
                        testsWriter.close();
                    }
                    catch (Exception e)
                    {
                    }
                }

            }

        }
        catch (Exception e)
        {
            throw new RuntimeException("\nError generating PL/SQL code", e);
        }
    }

    @SuppressWarnings("static-access")
    public static void main(String[] args)
    {
        Options options = new Options();

        Option inputOption = OptionBuilder//
                .withArgName("src")//
                .hasArg()//
                .withLongOpt("wsdl-input")//
                .withDescription("The location of the WSDL file (it can be a local path or an URL).")//
                .create("i");
        options.addOption(inputOption);

        Option nameOption = OptionBuilder//
                .withArgName("name")//
                .hasArg()//
                .withLongOpt("package-name")//
                .withDescription("PL/SQL Package Name.")//
                .create("n");
        options.addOption(nameOption);
        options.addOption(OptionBuilder//
                .withArgName("path")//
                .hasArg()//
                .withLongOpt("output-dir")//
                .withDescription("Output directory.")//
                .create("o"));

        options.addOption(OptionBuilder//
                .withLongOpt("help")//
                .withDescription("Show this help.")//
                .create("h"));

        options.addOption(OptionBuilder//
                .withArgName("n")//
                .hasArg()//
                .withLongOpt("operations-per-package")//
                .withDescription(
                        "Number of operations per package. Use this for WSDLs containing many operations to avoid PL/SQL errors related to program size.")//
                .create("p"));

        options.addOption(OptionBuilder//
                .withArgName("o1,o2...")//
                .hasArg()//
                .withLongOpt("wsdl-operations")//
                .withDescription("Comma-separated list of operations to be included in the generation task.")//
                .create("wo"));

        options.addOption(OptionBuilder//
                .withArgName("path")//
                .hasArg()//
                .withLongOpt("wsdl-operations-list-path")//
                .withDescription(
                        "Path to a *.properties file with the list of operations (one per line) to be included in the generation task.")//
                .create("wop"));

        options.addOption(OptionBuilder//
                .withArgName("s1,s2...")//
                .hasArg()//
                .withLongOpt("wsdl-services")//
                .withDescription("Comma-separated list of services to be included in the generation task.")//
                .create("ws"));

        options.addOption(OptionBuilder//
                .withArgName("path")//
                .hasArg()//
                .withLongOpt("wsdl-services-list-path")//
                .withDescription(
                        "Path to a *.properties file with the list of services (one per line) to be included in the generation task.")//
                .create("wsp"));

        CommandLineParser parser = new BasicParser();
        try
        {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption('h'))
            {
                // automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();

                formatter.printHelp(120, "java -jar wsdl2plsql.jar", "", options, "", true);
                return;
            }

            List<Option> missingOptions = new ArrayList<Option>();

            if (!line.hasOption('i'))
            {
                missingOptions.add(inputOption);
            }

            if (!line.hasOption('n'))
            {
                missingOptions.add(nameOption);

            }

            if (!missingOptions.isEmpty())
            {
                throw new MissingOptionException(missingOptions);
            }

            String wsdl = line.getOptionValue("i");
            String packageName = line.getOptionValue('n');
            String dest = line.getOptionValue('o', ".");

            int operationsPerPackage = 0;
            List<String> operations = new ArrayList<String>();

            if (line.hasOption("wo"))
            {
                operations.addAll(Arrays.asList(line.getOptionValue("wo").split(",")));
            }

            if (line.hasOption("wop"))
            {
                Properties properties = new Properties();

                properties.load(new FileInputStream(line.getOptionValue("wop")));

                operations.addAll(properties.stringPropertyNames());
            }

            if (line.hasOption('p'))
            {
                operationsPerPackage = Integer.parseInt(line.getOptionValue('p'));
            }

            String[] ops = new String[operations.size()];

            List<String> services = new ArrayList<String>();

            if (line.hasOption("ws"))
            {
                services.addAll(Arrays.asList(line.getOptionValue("ws").split(",")));
            }

            if (line.hasOption("wsp"))
            {
                Properties properties = new Properties();

                properties.load(new FileInputStream(line.getOptionValue("wsp")));

                services.addAll(properties.stringPropertyNames());
            }

            String[] srvs = new String[services.size()];

            wsdl2plsql wsdl2plsql = new wsdl2plsql(wsdl, packageName, dest, services.toArray(srvs),
                    operations.toArray(ops), operationsPerPackage);

            try
            {
                wsdl2plsql.run();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception exp)
        {
            // oops, something went wrong
            L.error("Parsing failed.  Reason: " + exp.getMessage() + "\n");

            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp(120, "java -jar wsdl2plsql.jar", "", options, "", true);

        }
    }
}
