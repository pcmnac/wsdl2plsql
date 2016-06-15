package br.gov.serpro.wsdl2pl;

import com.predic8.wadl.Application;
import com.predic8.wadl.WADLParser;

public class WadlParserTest
{
    public static void main(String[] args)
    {
        String url = "";
        url = "/home/04343650413/Desktop/teste.wadl";
        url = "http://www.crummy.com/software/wadl.rb/YahooSearch.wadl";
        url = "/home/04343650413/Desktop/application.wadl";

        WADLParser parser = new WADLParser();

        Application application = parser.parse(url);
        System.out.println("Pronto!" + application);

    }
}
