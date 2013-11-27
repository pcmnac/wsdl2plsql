package br.gov.serpro.wsdl2pl;

import com.predic8.wadl.Application;
import com.predic8.wadl.WADLParser;

public class WadlParserTest
{
    public static void main(String[] args)
    {

        WADLParser parser = new WADLParser();

        Application application = parser.parse("/home/04343650413/Desktop/teste.wadl");
        System.out.println("Pronto!" + application);

    }
}
